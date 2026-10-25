package com.example.data.model

import java.util.Locale

enum class ThemeMode(val labelIt: String, val labelEn: String) {
    SYSTEM("Predefinito di Sistema", "System Default"),
    LIGHT("Tema Chiaro", "Light Theme"),
    DARK("Tema Scuro", "Dark Theme")
}

enum class DistanceUnit(val symbolMeters: String, val symbolKm: String, val labelIt: String, val labelEn: String) {
    METRIC("m", "km", "Metrico (m / km)", "Metric (m / km)"),
    IMPERIAL("ft", "mi", "Imperiale (ft / mi)", "Imperial (ft / mi)")
}

enum class CurrencyFormat(val symbol: String, val code: String, val labelIt: String, val labelEn: String) {
    EUR("€", "EUR", "Euro (€)", "Euro (€)"),
    USD("$", "USD", "Dollaro USA ($)", "US Dollar ($)"),
    GBP("£", "GBP", "Sterlina UK (£)", "British Pound (£)"),
    JPY("¥", "JPY", "Yen Giapponese (¥)", "Japanese Yen (¥)")
}

enum class ParkingTypeCategory(val labelIt: String, val labelEn: String) {
    ALL("Tutti", "All"),
    PUBLIC("Pubblico", "Public"),
    PRIVATE("Garage Privato", "Private"),
    EV_CHARGING("Ricarica EV", "EV Charging"),
    ACCESSIBLE("Stalli Disabili", "Accessible")
}

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val distanceUnit: DistanceUnit = DistanceUnit.METRIC,
    val currencyFormat: CurrencyFormat = CurrencyFormat.EUR,
    val language: String = "it",
    val hasCompletedOnboarding: Boolean = false
) {
    val languageCode: String get() = language

    fun formatDistance(meters: Int): String {
        return when (distanceUnit) {
            DistanceUnit.METRIC -> {
                if (meters < 1000) "$meters m"
                else String.format(Locale.US, "%.1f km", meters / 1000.0)
            }
            DistanceUnit.IMPERIAL -> {
                val feet = (meters * 3.28084).toInt()
                if (feet < 5280) "$feet ft"
                else String.format(Locale.US, "%.1f mi", feet / 5280.0)
            }
        }
    }

    fun formatTariff(baseTariff: String): String {
        val targetSymbol = currencyFormat.symbol
        val safeReplacement = java.util.regex.Matcher.quoteReplacement(targetSymbol)
        val regex = Regex("""[€$£¥]""")
        return if (regex.containsMatchIn(baseTariff)) {
            baseTariff.replace(regex, safeReplacement)
        } else {
            "$targetSymbol$baseTariff"
        }
    }
}

data class LeaderboardEntry(
    val userId: String,
    val username: String,
    val points: Int,
    val karmaScore: Int,
    val rank: Int = 1,
    val badgeTitle: String = "Top Driver",
    val signalsShared: Int = 0,
    val isCurrentUser: Boolean = false
) {
    companion object {
        val DEFAULT_COMMUNITY_LEADERBOARD = listOf(
            LeaderboardEntry("u1", "Marco RomaDriver", 980, 98, 1, "Senatore della Mobilità", 42),
            LeaderboardEntry("u2", "Elena TerminiFast", 840, 95, 2, "Tribuno di Roma", 35),
            LeaderboardEntry("u3", "Giulia Esquilino", 720, 92, 3, "Centurione della Sosta", 28),
            LeaderboardEntry("u4", "Alex MetroPark", 590, 88, 4, "Legionario Urbano", 19),
            LeaderboardEntry("u5", "Davide EV", 460, 85, 5, "Pioniere Ricarica", 14)
        )
    }
}

data class PlacePrediction(
    val placeId: String,
    val primaryText: String,
    val secondaryText: String,
    val latitude: Double,
    val longitude: Double,
    val isCity: Boolean = false
) {
    val mainText: String get() = primaryText
    val description: String get() = if (secondaryText.isNotBlank()) "$primaryText, $secondaryText" else primaryText
}
