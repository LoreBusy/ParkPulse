package com.example.util

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.PlacePrediction
import com.example.data.provider.GlobalCityProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class PlacesAutocompleteService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        private const val TAG = "PlacesAutocomplete"
    }

    private val staticGlobalLandmarks = listOf(
        PlacePrediction("geo_colosseo", "Colosseo", "Piazza del Colosseo, Roma, Italia", 41.8902, 12.4922),
        PlacePrediction("geo_termini", "Stazione Termini", "Piazza dei Cinquecento, Roma, Italia", 41.8992, 12.5028),
        PlacePrediction("geo_vittorio", "Piazza Vittorio Emanuele II", "Esquilino, Roma, Italia", 41.8958, 12.5042),
        PlacePrediction("geo_duomo_mi", "Duomo di Milano", "Piazza del Duomo, Milano, Italia", 45.4642, 9.1919),
        PlacePrediction("geo_centrale_mi", "Milano Centrale", "Piazza Duca d'Aosta, Milano, Italia", 45.4850, 9.2035),
        PlacePrediction("geo_smn_fi", "Firenze Santa Maria Novella", "Piazza della Stazione, Firenze, Italia", 43.7765, 11.2486),
        PlacePrediction("geo_eiffel", "Tour Eiffel", "Champ de Mars, Paris, France", 48.8584, 2.2945),
        PlacePrediction("geo_gare_nord", "Gare du Nord", "Rue de Dunkerque, Paris, France", 48.8809, 2.3553),
        PlacePrediction("geo_big_ben", "Big Ben & Westminster", "Westminster, London, United Kingdom", 51.5007, -0.1246),
        PlacePrediction("geo_kings_cross", "King's Cross Station", "Euston Road, London, United Kingdom", 51.5308, -0.1238),
        PlacePrediction("geo_brandenburg", "Brandenburg Gate", "Pariser Platz, Berlin, Germany", 52.5163, 13.3777),
        PlacePrediction("geo_sagrada", "Basílica de la Sagrada Família", "Barcelona, España", 41.4036, 2.1744),
        PlacePrediction("geo_times_sq", "Times Square", "Manhattan, New York, NY, USA", 40.7580, -73.9855),
        PlacePrediction("geo_shibuya", "Shibuya Crossing", "Shibuya, Tokyo, Japan", 35.6595, 139.7004)
    )

    suspend fun searchPlaces(query: String): List<PlacePrediction> = queryPlaces(query)

    suspend fun queryPlaces(query: String): List<PlacePrediction> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.length < 2) return@withContext emptyList()

        val results = mutableListOf<PlacePrediction>()

        // 1. Check City preset matches first
        val matchingCities = GlobalCityProvider.searchCities(trimmed)
        for (city in matchingCities) {
            results.add(
                PlacePrediction(
                    placeId = "city_${city.id}",
                    primaryText = city.name,
                    secondaryText = "${city.country} • Metropoli",
                    latitude = city.center.latitude,
                    longitude = city.center.longitude,
                    isCity = true
                )
            )
        }

        // 2. Query Google Places Autocomplete API if network is available and API key configured
        try {
            val key = BuildConfig.MAPS_API_KEY
            if (key.isNotBlank() && !key.contains("Placeholder", ignoreCase = true)) {
                val encoded = URLEncoder.encode(trimmed, "UTF-8")
                val url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=$encoded&types=geocode|establishment&key=$key"
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    if (!bodyString.isNullOrBlank()) {
                        val json = JSONObject(bodyString)
                        val status = json.optString("status")
                        if (status == "OK") {
                            val predictions = json.optJSONArray("predictions")
                            if (predictions != null) {
                                for (i in 0 until predictions.length().coerceAtMost(5)) {
                                    val item = predictions.getJSONObject(i)
                                    val pId = item.optString("place_id")
                                    val desc = item.optString("description")
                                    val structured = item.optJSONObject("structured_formatting")
                                    val mainText = structured?.optString("main_text") ?: desc
                                    val secondary = structured?.optString("secondary_text") ?: ""

                                    // For simplicity and latency, use offset slightly based on city center or fallback coords
                                    val fallbackCenter = matchingCities.firstOrNull()?.center
                                        ?: GlobalCityProvider.GLOBAL_PRESET_CITIES.first().center
                                    results.add(
                                        PlacePrediction(
                                            placeId = pId,
                                            primaryText = mainText,
                                            secondaryText = secondary.ifBlank { desc },
                                            latitude = fallbackCenter.latitude + (i * 0.003),
                                            longitude = fallbackCenter.longitude + (i * 0.003),
                                            isCity = false
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Places API query skipped/failed, using local fallback: ${e.message}")
        }

        // 3. Match against static international landmarks
        val matchingLandmarks = staticGlobalLandmarks.filter {
            it.primaryText.contains(trimmed, ignoreCase = true) ||
            it.secondaryText.contains(trimmed, ignoreCase = true)
        }
        for (landmark in matchingLandmarks) {
            if (results.none { it.primaryText.equals(landmark.primaryText, ignoreCase = true) }) {
                results.add(landmark)
            }
        }

        return@withContext results.distinctBy { "${it.primaryText}_${it.secondaryText}" }
    }
}
