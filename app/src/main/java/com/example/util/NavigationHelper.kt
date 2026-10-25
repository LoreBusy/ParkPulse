package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri

object NavigationHelper {

    /**
     * Creates an Intent explicitly targeting Google Maps with navigation mode or coordinate query.
     */
    fun createGoogleMapsIntent(
        latitude: Double,
        longitude: Double,
        destinationName: String = ""
    ): Intent {
        val navUri = "google.navigation:q=$latitude,$longitude".toUri()
        return Intent(Intent.ACTION_VIEW, navUri).apply {
            setPackage("com.google.android.apps.maps")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    /**
     * Launches Google Maps app with the coordinates of the parking area, with automatic fallback.
     */
    fun launchGoogleMaps(
        context: Context,
        latitude: Double,
        longitude: Double,
        destinationName: String = ""
    ) {
        try {
            val intent = createGoogleMapsIntent(latitude, longitude, destinationName)
            context.startActivity(intent)
        } catch (e: Exception) {
            // If Google Maps package is not installed or fails, fallback to general maps intent
            openInExternalMaps(context, latitude, longitude, destinationName)
        }
    }

    /**
     * Creates an Intent to open external navigation apps (Google Maps, Waze, etc.)
     */
    fun createExternalMapsIntent(
        latitude: Double,
        longitude: Double,
        destinationName: String
    ): Intent {
        val encodedLabel = Uri.encode(destinationName)
        val geoUri = "geo:$latitude,$longitude?q=$latitude,$longitude($encodedLabel)".toUri()
        return Intent(Intent.ACTION_VIEW, geoUri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    /**
     * Opens external navigation apps (Google Maps, Waze, etc.) to guide the driver to the specified coordinates.
     */
    fun openInExternalMaps(
        context: Context,
        latitude: Double,
        longitude: Double,
        destinationName: String
    ) {
        try {
            val intent = createExternalMapsIntent(latitude, longitude, destinationName)
            val chooser = Intent.createChooser(intent, "Scegli navigatore per $destinationName")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            // Fallback to web Google Maps
            try {
                val webUrl = "https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude"
                val webIntent = Intent(Intent.ACTION_VIEW, webUrl.toUri())
                webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(webIntent)
            } catch (err: Exception) {
                Toast.makeText(context, "Nessuna applicazione di mappe trovata", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Opens specifically in Waze if installed, otherwise opens standard map.
     */
    fun openInWaze(
        context: Context,
        latitude: Double,
        longitude: Double
    ) {
        try {
            val wazeUri = "https://waze.com/ul?ll=$latitude,$longitude&navigate=yes".toUri()
            val intent = Intent(Intent.ACTION_VIEW, wazeUri)
            intent.setPackage("com.waze")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Waze not installed, fallback to general maps
            openInExternalMaps(context, latitude, longitude, "Destinazione Parcheggio")
        }
    }
}
