package com.example.data.model

enum class EmergencyIncidentType(val label: String) {
    STREET_CLOSURE("Chiusura Stradale Emergenza"),
    CRITICAL_CONGESTION("Traffico Saturo / Blocco"),
    ROADWORKS_TRAM("Cantiere e Lavori in Corso"),
    ACCIDENT("Incidente Stradale / Deviazione")
}

enum class IncidentSeverity {
    CRITICAL,
    HIGH,
    MODERATE
}

data class EmergencyTrafficIncident(
    val id: String,
    val title: String,
    val streetAffected: String,
    val incidentType: EmergencyIncidentType,
    val severity: IncidentSeverity,
    val delayMinutes: Int,
    val description: String,
    val suggestedAlternativeRoute: String,
    val reportTimestampEpochMs: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
