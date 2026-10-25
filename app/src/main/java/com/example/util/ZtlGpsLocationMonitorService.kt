package com.example.util

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.model.VehiclePermitType
import com.example.data.model.ZtlWarningAlert
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng

/**
 * Foreground Service that continuously tracks user GPS location and evaluates it against
 * known Rome ZTL boundaries (Centro Storico, Trastevere, San Lorenzo, Testaccio, etc.).
 * When the user approaches within 250m of an active restricted camera gate or enters an active zone,
 * it triggers a high-priority heads-up notification and in-app breach alert.
 */
class ZtlGpsLocationMonitorService : Service() {

    private val binder = LocalBinder()
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var isMonitoring = false

    inner class LocalBinder : Binder() {
        fun getService(): ZtlGpsLocationMonitorService = this@ZtlGpsLocationMonitorService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createForegroundNotificationChannel()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_MONITORING -> {
                stopGpsTracking()
                stopSelf()
            }
            ACTION_START_MONITORING, null -> {
                startForegroundTracking()
            }
        }
        return START_STICKY
    }

    private fun createForegroundNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                FOREGROUND_CHANNEL_ID,
                "Monitoraggio GPS ZTL Attivo",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitoraggio in background dei varchi ZTL e della sosta a Roma Termini"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, FOREGROUND_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Monitoraggio ZTL Roma Attivo")
            .setContentText("Rilevamento GPS varchi e zone a traffico limitato in corso")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    @SuppressLint("MissingPermission")
    private fun startForegroundTracking() {
        if (isMonitoring) return

        val hasFine = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            Log.w(TAG, "Location permission not granted for ZtlGpsLocationMonitorService")
            return
        }

        try {
            startForeground(FOREGROUND_NOTIFICATION_ID, buildForegroundNotification())
        } catch (e: Exception) {
            Log.e(TAG, "Could not start foreground service: ${e.message}")
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL_MS)
            .setMinUpdateIntervalMillis(FASTEST_INTERVAL_MS)
            .setMinUpdateDistanceMeters(MIN_DISTANCE_METERS)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                val currentLatLng = LatLng(loc.latitude, loc.longitude)
                evaluateCurrentLocation(currentLatLng)
            }
        }

        try {
            fusedLocationClient?.requestLocationUpdates(
                request,
                locationCallback as LocationCallback,
                Looper.getMainLooper()
            )
            isMonitoring = true
            Log.i(TAG, "ZTL GPS Live Monitoring started successfully")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while requesting location updates: ${e.message}")
        }
    }

    private fun stopGpsTracking() {
        locationCallback?.let { callback ->
            fusedLocationClient?.removeLocationUpdates(callback)
        }
        locationCallback = null
        isMonitoring = false
        Log.i(TAG, "ZTL GPS Live Monitoring stopped")
    }

    /**
     * Evaluates the GPS location against known Rome ZTL boundaries and gates.
     */
    fun evaluateCurrentLocation(
        latLng: LatLng,
        vehiclePermit: VehiclePermitType = currentVehiclePermit,
        isPermitActive: Boolean = currentPermitActive
    ): ZtlWarningAlert? {
        val alert = ZtlLiveMonitorService.evaluateZtlBreach(
            userLocation = latLng,
            vehiclePermit = vehiclePermit,
            isPermitActive = isPermitActive,
            context = applicationContext
        )

        // Notify registered in-app listener if any
        locationUpdateListener?.invoke(latLng, alert)
        return alert
    }

    override fun onDestroy() {
        stopGpsTracking()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "ZtlGpsLocationMonitor"
        const val ACTION_START_MONITORING = "com.example.action.START_ZTL_MONITOR"
        const val ACTION_STOP_MONITORING = "com.example.action.STOP_ZTL_MONITOR"

        private const val FOREGROUND_CHANNEL_ID = "ztl_monitor_service_channel"
        private const val FOREGROUND_NOTIFICATION_ID = 41891

        private const val UPDATE_INTERVAL_MS = 6000L
        private const val FASTEST_INTERVAL_MS = 3000L
        private const val MIN_DISTANCE_METERS = 8f

        // Cached vehicle state for background evaluation
        var currentVehiclePermit: VehiclePermitType = VehiclePermitType.NONE
        var currentPermitActive: Boolean = true

        // Callback for in-app UI updates
        var locationUpdateListener: ((LatLng, ZtlWarningAlert?) -> Unit)? = null

        /**
         * Starts the background GPS ZTL monitoring service.
         */
        fun startMonitoring(context: Context) {
            val intent = Intent(context, ZtlGpsLocationMonitorService::class.java).apply {
                action = ACTION_START_MONITORING
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start ZtlGpsLocationMonitorService: ${e.message}")
            }
        }

        /**
         * Stops the GPS ZTL monitoring service.
         */
        fun stopMonitoring(context: Context) {
            val intent = Intent(context, ZtlGpsLocationMonitorService::class.java).apply {
                action = ACTION_STOP_MONITORING
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop ZtlGpsLocationMonitorService: ${e.message}")
            }
        }

        /**
         * Direct on-demand check for a given coordinate.
         */
        fun checkGpsLocation(
            context: Context,
            location: LatLng,
            permit: VehiclePermitType = currentVehiclePermit,
            isPermitActive: Boolean = currentPermitActive
        ): ZtlWarningAlert? {
            return ZtlLiveMonitorService.evaluateZtlBreach(
                userLocation = location,
                vehiclePermit = permit,
                isPermitActive = isPermitActive,
                context = context
            )
        }
    }
}
