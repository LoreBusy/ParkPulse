package com.example.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.R
import com.example.data.model.UserProfile
import com.example.data.model.VehiclePermitType
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthService(
    authInstance: FirebaseAuth? = null
) {
    companion object {
        private const val TAG = "FirebaseAuthService"
    }

    private val auth: FirebaseAuth? = authInstance ?: try {
        FirebaseAuth.getInstance()
    } catch (e: Exception) {
        Log.w(TAG, "FirebaseAuth not initialized: ${e.message}")
        null
    }

    val currentUser: FirebaseUser?
        get() = auth?.currentUser

    val isUserAuthenticated: Boolean
        get() = auth?.currentUser != null

    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val safeAuth = auth
        if (safeAuth == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        safeAuth.addAuthStateListener(listener)
        awaitClose { safeAuth.removeAuthStateListener(listener) }
    }

    /**
     * Executes Google Sign-In using Jetpack CredentialManager and Firebase Auth.
     */
    suspend fun signInWithGoogle(context: Context): Result<FirebaseUser> {
        return try {
            val credentialManager = CredentialManager.create(context)

            // Resolve Google Web Client ID from auto-generated string resources
            val serverClientId = try {
                context.getString(R.string.default_web_client_id)
            } catch (e: Exception) {
                // Fallback to client_type 3 clientId from google-services.json
                "789701106089-gr5g67irvmu7odr0f1a4qh77vht98uao.apps.googleusercontent.com"
            }

            val googleIdOption = GetSignInWithGoogleOption.Builder(serverClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result: GetCredentialResponse = credentialManager.getCredential(
                context = context,
                request = request
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val safeAuth = auth ?: error("Firebase Auth non inizializzato")
                val authResult = safeAuth.signInWithCredential(authCredential).await()
                val user = authResult.user ?: error("FirebaseUser is null after Google sign-in")

                // Synchronize profile to Firestore
                syncUserProfileToFirestore(context, user)

                Result.success(user)
            } else {
                Result.failure(IllegalStateException("Unsupported credential type: ${credential.type}"))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "User cancelled Google Sign-In.")
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Signs in with Email and Password.
     */
    suspend fun signInWithEmail(email: String, pass: String): Result<FirebaseUser> {
        val safeAuth = auth ?: return Result.failure(IllegalStateException("Firebase Auth non inizializzato"))
        return try {
            val result = safeAuth.signInWithEmailAndPassword(email.trim(), pass).await()
            val user = result.user ?: error("User is null")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Email sign-in error: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Creates a new account with Email and Password, automatically initiating email verification.
     */
    suspend fun createAccountWithEmail(email: String, pass: String, displayName: String = ""): Result<FirebaseUser> {
        val safeAuth = auth ?: return Result.failure(IllegalStateException("Firebase Auth non inizializzato"))
        return try {
            val result = safeAuth.createUserWithEmailAndPassword(email.trim(), pass).await()
            val user = result.user ?: error("User is null")
            try {
                user.sendEmailVerification().await()
                Log.d(TAG, "Email verification successfully dispatched to: ${user.email}")
            } catch (evErr: Exception) {
                Log.w(TAG, "Notice: could not dispatch verification email immediately: ${evErr.message}")
            }
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Email account creation error: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Sends an email verification link to the authenticated user.
     */
    suspend fun sendEmailVerification(user: FirebaseUser? = auth?.currentUser): Result<Unit> {
        return try {
            val target = user ?: auth?.currentUser ?: error("Nessun utente autenticato")
            target.sendEmailVerification().await()
            Log.d(TAG, "Verification email resent to: ${target.email}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending verification email: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Sends a password reset email using Firebase Auth.
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        val trimmed = email.trim()
        if (trimmed.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) {
            return Result.failure(IllegalArgumentException("Inserisci un indirizzo email valido."))
        }
        val safeAuth = auth ?: return Result.failure(IllegalStateException("Firebase Auth non inizializzato"))
        return try {
            safeAuth.sendPasswordResetEmail(trimmed).await()
            Log.d(TAG, "Password reset email sent to: $trimmed")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Password reset error: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Reloads the current user profile from Firebase to check updated email verification status.
     */
    suspend fun reloadUser(): Result<FirebaseUser?> {
        val safeAuth = auth ?: return Result.failure(IllegalStateException("Firebase Auth non inizializzato"))
        return try {
            val user = safeAuth.currentUser
            user?.reload()?.await()
            Result.success(safeAuth.currentUser)
        } catch (e: Exception) {
            Log.e(TAG, "User reload error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun signOut(context: Context) {
        try {
            auth?.signOut()
            val credentialManager = CredentialManager.create(context)
            credentialManager.clearCredentialState(androidx.credentials.ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e(TAG, "Sign out error: ${e.message}")
        }
    }

    /**
     * Saves / updates user profile in Cloud Firestore at /users/{uid}.
     */
    private suspend fun syncUserProfileToFirestore(context: Context, user: FirebaseUser) {
        try {
            val databaseId = try {
                context.getString(R.string.firestore_database_id)
            } catch (e: Exception) {
                ""
            }
            val firestore = if (databaseId.isNotEmpty() && databaseId != "(default)") {
                FirebaseFirestore.getInstance(databaseId)
            } else {
                FirebaseFirestore.getInstance()
            }

            val userDoc = mapOf(
                "userId" to user.uid,
                "displayName" to (user.displayName ?: "Global Driver"),
                "email" to (user.email ?: ""),
                "photoUrl" to (user.photoUrl?.toString() ?: ""),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            firestore.collection("users").document(user.uid)
                .set(userDoc, SetOptions.merge())
                .await()
            Log.d(TAG, "User profile successfully synced to Cloud Firestore: ${user.uid}")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync profile to Cloud Firestore (offline or rule check): ${e.message}")
        }
    }
}
