package com.example.util

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.DepartureSignal
import com.example.data.model.ParkingSearchFilter
import com.example.data.model.RadarSubBlockDensity
import com.example.data.model.ZtlZone
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object ParkingNotificationService {
    private const val TAG = "ParkingNotifService"

    const val CHANNEL_ZTL = "ztl_proximity_channel"
    const val CHANNEL_CONGESTION = "congestion_proximity_channel"
    const val CHANNEL_SPOT_AVAILABLE = "nearby_spot_channel"

    const val NOTIFICATION_ID_ZTL = 41890
    const val NOTIFICATION_ID_CONGESTION = 41891
    const val NOTIFICATION_ID_SPOT = 41892

    private var lastCongestionAlertEpochMs: Long = 0
    private var lastSpotAlertEpochMs: Long = 0

    fun createAllNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // 1. ZTL Channel
            val ztlChannel = NotificationChannel(
                CHANNEL_ZTL,
                "Avvisi ZTL Roma",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avviso quando ti avvicini a varchi ZTL durante gli orari di divieto"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
            }
            notificationManager.createNotificationChannel(ztlChannel)

            // 2. High Congestion Alert Channel
            val congestionChannel = NotificationChannel(
                CHANNEL_CONGESTION,
                "Allerta Alta Congestione",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifica in tempo reale quando ti avvicini a un'area con parcheggio critico o saturo"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200, 150, 200)
            }
            notificationManager.createNotificationChannel(congestionChannel)

            // 3. Spot Available Channel
            val spotChannel = NotificationChannel(
                CHANNEL_SPOT_AVAILABLE,
                "Posti Liberi & Preferiti",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avviso immediato quando un posto si libera nelle tue vie salvate o vicine"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 100, 100, 100, 200)
            }
            notificationManager.createNotificationChannel(spotChannel)

            // 4. Emergency Traffic & Closure Channel (WorkManager)
            val emergencyChannel = NotificationChannel(
                TerminiTrafficAlertWorker.CHANNEL_EMERGENCY_TRAFFIC,
                "Allerte Traffico & Chiusure Termini",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avvisi real-time WorkManager su chiusure stradali di emergenza e traffico critico Termini-Esquilino"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
            }
            notificationManager.createNotificationChannel(emergencyChannel)
        }
    }

    private fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Send Real-Time Alert when approaching a high congestion sub-block in Termini-Esquilino
     */
    @SuppressLint("MissingPermission")
    fun sendHighCongestionAlert(
        context: Context,
        subBlock: RadarSubBlockDensity,
        distanceMeters: Int,
        alternativeRecommendation: String
    ) {
        if (!hasNotificationPermission(context)) {
            Log.w(TAG, "Notification permission not granted for congestion alert")
            return
        }
        createAllNotificationChannels(context)

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAV_VIEW", "RADAR")
            putExtra("EXTRA_SUBBLOCK_ID", subBlock.id)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_CONGESTION,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "⚠️ Zona Saturo: ${subBlock.name}"
        val contentText = "Congestione al ${subBlock.congestionPercent}% (~${subBlock.estimatedSearchTimeMinutes} min attesa). Distanza: ${distanceMeters}m"
        val bigText = "Ti stai avvicinando a una zona ad elevatissima congestione (${subBlock.name}).\n" +
                "• Livello traffico/sosta: ${subBlock.congestionPercent}%\n" +
                "• Tempo medio di ricerca posto: ~${subBlock.estimatedSearchTimeMinutes} min\n" +
                "💡 Alternativa consigliata: $alternativeRecommendation"

        val builder = NotificationCompat.Builder(context, CHANNEL_CONGESTION)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_NAVIGATION)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 200, 150, 200))

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_CONGESTION, builder.build())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send congestion notification: ${e.message}")
        }
    }

    /**
     * Send Real-Time Alert when a nearby spot matching saved preferences becomes available
     */
    @SuppressLint("MissingPermission")
    fun sendNearbySpotAvailableAlert(
        context: Context,
        signal: DepartureSignal,
        matchReason: String
    ) {
        if (!hasNotificationPermission(context)) {
            Log.w(TAG, "Notification permission not granted for spot alert")
            return
        }
        createAllNotificationChannels(context)

        // Main Tap: Open App to MAP focused on this spot
        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAV_VIEW", "MAP")
            putExtra("EXTRA_MICROAREA_ID", signal.microAreaId)
        }
        val appPendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_SPOT,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action Button: Open in Google Maps / Waze External Navigation
        val navIntent = NavigationHelper.createExternalMapsIntent(
            signal.latitude,
            signal.longitude,
            signal.streetName
        )
        val navPendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_SPOT + 1,
            navIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val remainingSecs = signal.remainingSeconds(System.currentTimeMillis())
        val remainingMins = (remainingSecs / 60).coerceAtLeast(1)

        val title = "🎉 Posto Libero: ${signal.streetName}"
        val contentText = "$matchReason • Rimangono ~$remainingMins min prima che si occupi!"
        val bigText = "Un guidatore sta liberando un posto auto in ${signal.streetName} (${signal.microAreaName}).\n" +
                "• Criterio preferenza: $matchReason\n" +
                "• Affidabilità segnalatore: ${signal.userReliabilityPercent}%\n" +
                "• Finestra stimata di disponibilità: ~$remainingMins minuti.\n" +
                "Tocca 'Naviga Ora' per guidare direttamente sul posto con Google Maps o Waze."

        val builder = NotificationCompat.Builder(context, CHANNEL_SPOT_AVAILABLE)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setContentIntent(appPendingIntent)
            .addAction(
                R.mipmap.ic_launcher,
                "Naviga Ora con Maps",
                navPendingIntent
            )
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 100, 100, 100, 200))

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_SPOT, builder.build())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send spot notification: ${e.message}")
        }
    }

    /**
     * Proximity calculator between two coordinate points in meters (Haversine formula)
     */
    fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
        val earthRadius = 6371000.0 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (earthRadius * c).toInt()
    }

    /**
     * Evaluates current user location against high-congestion sub-blocks in Termini-Esquilino
     */
    fun checkCongestionProximity(
        userLat: Double,
        userLng: Double,
        subBlocks: List<RadarSubBlockDensity>,
        context: Context,
        throttleMs: Long = 60_000L
    ): RadarSubBlockDensity? {
        val now = System.currentTimeMillis()
        if (now - lastCongestionAlertEpochMs < throttleMs) return null

        // Find high congestion subblocks (> 65%) within 350 meters
        val approachingBlock = subBlocks
            .filter { it.congestionPercent >= 65 }
            .map { block ->
                val dist = calculateDistanceMeters(userLat, userLng, block.latitude, block.longitude)
                block to dist
            }
            .filter { it.second <= 350 }
            .minByOrNull { it.second }

        approachingBlock?.let { (block, dist) ->
            lastCongestionAlertEpochMs = now
            sendHighCongestionAlert(
                context = context,
                subBlock = block,
                distanceMeters = dist,
                alternativeRecommendation = block.recommendation
            )
            return block
        }
        return null
    }

    /**
     * Evaluates a new departure signal against user saved favorite streets and preferences
     */
    fun checkSpotAvailableForPreferences(
        signal: DepartureSignal,
        favoriteAreaIds: Set<String>,
        userLat: Double?,
        userLng: Double?,
        context: Context,
        throttleMs: Long = 30_000L
    ): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastSpotAlertEpochMs < throttleMs) return false

        val isFavoriteArea = favoriteAreaIds.contains(signal.microAreaId)
        val distance = if (userLat != null && userLng != null) {
            calculateDistanceMeters(userLat, userLng, signal.latitude, signal.longitude)
        } else null

        val isNearby = distance != null && distance <= 500

        if (isFavoriteArea || isNearby) {
            lastSpotAlertEpochMs = now
            val reason = if (isFavoriteArea && isNearby) {
                "Nella tua via salvata (${signal.streetName}) a soli ${distance}m da te"
            } else if (isFavoriteArea) {
                "Nella tua via salvata tra i preferiti (${signal.microAreaName})"
            } else {
                "A soli ${distance}m dalla tua posizione attuale"
            }
            sendNearbySpotAvailableAlert(context, signal, reason)
            return true
        }
        return false
    }
}
