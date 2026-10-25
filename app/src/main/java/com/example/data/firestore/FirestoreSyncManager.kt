package com.example.data.firestore

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.LeaderboardEntry
import com.example.data.model.MicroArea
import com.example.data.model.UserProfile
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreSyncManager(
    private val firestore: FirebaseFirestore
) {
    constructor(context: Context) : this(create(context).firestore)
    companion object {
        private const val TAG = "FirestoreSyncManager"

        fun create(context: Context): FirestoreSyncManager {
            val databaseId = try {
                context.getString(R.string.firestore_database_id)
            } catch (e: Exception) {
                ""
            }
            val db = if (databaseId.isNotEmpty() && databaseId != "(default)") {
                FirebaseFirestore.getInstance(databaseId)
            } else {
                FirebaseFirestore.getInstance()
            }
            return FirestoreSyncManager(db)
        }
    }

    /**
     * Persists user profile, points, vehicle data and favorite areas in Cloud Firestore.
     */
    suspend fun saveUserProfile(profile: UserProfile, uid: String): Result<Unit> {
        return try {
            val data = hashMapOf(
                "userId" to uid,
                "displayName" to profile.username,
                "points" to profile.points,
                "karmaScore" to profile.karmaScore,
                "levelRank" to profile.levelRank,
                "licensePlate" to profile.licensePlate,
                "vehicleModel" to profile.vehicleModel,
                "vehiclePermit" to profile.vehiclePermit.name,
                "favoriteAreaIds" to profile.favoriteAreaIds.toList(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            firestore.collection("users").document(uid)
                .set(data, SetOptions.merge())
                .await()

            // Also update public leaderboard entry
            syncLeaderboardEntry(
                userId = uid,
                displayName = profile.username,
                points = profile.points,
                karmaScore = profile.karmaScore,
                badgeTitle = profile.badges.firstOrNull { it.isUnlocked }?.title ?: "Driver Globale"
            )

            Log.d(TAG, "Saved profile to Cloud Firestore for user: $uid")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save profile to Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Syncs a specific favorite spot to the user's /users/{userId}/favorites subcollection.
     */
    suspend fun syncFavorite(userId: String, area: MicroArea, isFavorite: Boolean): Result<Unit> {
        return try {
            val favRef = firestore.collection("users").document(userId)
                .collection("favorites").document(area.id)

            if (isFavorite) {
                val data = hashMapOf(
                    "areaId" to area.id,
                    "name" to area.name,
                    "streetName" to area.streetName,
                    "latitude" to area.latitude,
                    "longitude" to area.longitude,
                    "tariff" to area.blueLineTariff,
                    "savedAt" to FieldValue.serverTimestamp()
                )
                favRef.set(data, SetOptions.merge()).await()
            } else {
                favRef.delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync favorite to Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Observes the user's saved favorite IDs in real time from Firestore.
     */
    fun observeFavorites(userId: String): Flow<Set<String>> = callbackFlow {
        val colRef = firestore.collection("users").document(userId).collection("favorites")
        val registration = colRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Failed listening to favorites: ${error.message}")
                return@addSnapshotListener
            }
            val ids = snapshot?.documents?.mapNotNull { it.id }?.toSet() ?: emptySet()
            trySend(ids)
        }
        awaitClose { registration.remove() }
    }

    suspend fun syncLeaderboardEntry(entry: LeaderboardEntry): Result<Unit> {
        return syncLeaderboardEntry(
            userId = entry.userId,
            displayName = entry.username,
            points = entry.points,
            karmaScore = entry.karmaScore,
            badgeTitle = entry.badgeTitle
        )
    }

    /**
     * Updates or creates the user's public entry on the global leaderboard.
     */
    suspend fun syncLeaderboardEntry(
        userId: String,
        displayName: String,
        points: Int,
        karmaScore: Int,
        badgeTitle: String
    ): Result<Unit> {
        return try {
            val entry = hashMapOf(
                "userId" to userId,
                "displayName" to displayName,
                "points" to points,
                "karmaScore" to karmaScore,
                "badgeTitle" to badgeTitle,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("leaderboard").document(userId)
                .set(entry, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync leaderboard: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Observes real-time Community Leaderboard.
     */
    fun observeLeaderboard(currentUserId: String?): Flow<List<LeaderboardEntry>> = callbackFlow {
        val query = firestore.collection("leaderboard")
            .orderBy("points", Query.Direction.DESCENDING)
            .limit(20)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Leaderboard query error: ${error.message}")
                return@addSnapshotListener
            }
            val entries = mutableListOf<LeaderboardEntry>()
            var rank = 1
            snapshot?.documents?.forEach { doc ->
                val uid = doc.getString("userId") ?: doc.id
                val name = doc.getString("displayName") ?: "Driver #${rank}"
                val pts = (doc.getLong("points") ?: 0L).toInt()
                val karma = (doc.getLong("karmaScore") ?: 80L).toInt()
                val badge = doc.getString("badgeTitle") ?: "Global Scout"
                entries.add(
                    LeaderboardEntry(
                        userId = uid,
                        username = name,
                        points = pts,
                        karmaScore = karma,
                        rank = rank++,
                        badgeTitle = badge,
                        isCurrentUser = currentUserId != null && currentUserId == uid
                    )
                )
            }
            trySend(entries)
        }
        awaitClose { registration.remove() }
    }

    /**
     * Listens in real time to the user's Cloud Firestore profile document.
     */
    fun observeUserProfile(uid: String): Flow<Map<String, Any>?> = callbackFlow {
        val docRef = firestore.collection("users").document(uid)
        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Listen failed for user profile: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                trySend(snapshot.data)
            } else {
                trySend(null)
            }
        }
        awaitClose { registration.remove() }
    }

    /**
     * Records a crowd-sourced parking report in Firestore with user provenance.
     */
    suspend fun recordParkingReport(
        userId: String,
        userEmail: String,
        areaId: String,
        areaName: String,
        actionType: String = "DEPARTURE_SIGNAL",
        departureMinutes: Int = 0,
        latitude: Double = 0.0,
        longitude: Double = 0.0,
        userName: String = "Driver",
        reportId: String = java.util.UUID.randomUUID().toString()
    ): Result<Unit> {
        return try {
            val report = hashMapOf(
                "reportId" to reportId,
                "userId" to userId,
                "userEmail" to userEmail,
                "userName" to userName,
                "areaId" to areaId,
                "areaName" to areaName,
                "latitude" to latitude,
                "longitude" to longitude,
                "departureMinutes" to departureMinutes,
                "actionType" to actionType,
                "createdAt" to FieldValue.serverTimestamp(),
                "status" to "ACTIVE"
            )
            firestore.collection("parking_reports").document(reportId)
                .set(report, SetOptions.merge())
                .await()

            // Write tamper-proof audit log entry
            val auditLog = hashMapOf(
                "logId" to reportId,
                "userId" to userId,
                "userEmail" to userEmail,
                "action" to "SUBMIT_PARKING_REPORT",
                "targetAreaId" to areaId,
                "timestamp" to FieldValue.serverTimestamp()
            )
            firestore.collection("security_audit_logs").document(reportId)
                .set(auditLog)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to record parking report in Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Records immutable audit log for community spot verifications / feedback.
     */
    suspend fun recordFeedbackAudit(
        userId: String,
        userEmail: String,
        signalId: String,
        feedbackType: String,
        feedbackId: String = java.util.UUID.randomUUID().toString()
    ): Result<Unit> {
        return try {
            val auditLog = hashMapOf(
                "logId" to feedbackId,
                "userId" to userId,
                "userEmail" to userEmail,
                "action" to "VERIFY_SIGNAL_$feedbackType",
                "targetSignalId" to signalId,
                "timestamp" to FieldValue.serverTimestamp()
            )
            firestore.collection("security_audit_logs").document(feedbackId)
                .set(auditLog)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to record feedback audit in Firestore: ${e.message}")
            Result.failure(e)
        }
    }
}

