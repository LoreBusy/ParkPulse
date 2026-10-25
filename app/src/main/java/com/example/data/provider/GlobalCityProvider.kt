package com.example.data.provider

import com.example.data.model.MicroArea
import com.google.android.gms.maps.model.LatLng
import java.util.UUID

data class CityRegion(
    val id: String,
    val name: String,
    val country: String,
    val center: LatLng,
    val defaultZoom: Float = 14.5f,
    val currencySymbol: String = "€",
    val defaultHourlyTariff: String = "€1,50/h",
    val isZtlActiveCity: Boolean = false,
    val ztlName: String = ""
)

object GlobalCityProvider {

    val GLOBAL_PRESET_CITIES = listOf(
        CityRegion(
            id = "rome",
            name = "Roma",
            country = "Italia",
            center = LatLng(41.8992, 12.5028),
            defaultHourlyTariff = "€1,50/h",
            isZtlActiveCity = true,
            ztlName = "ZTL Centro Storico & Trastevere"
        ),
        CityRegion(
            id = "milan",
            name = "Milano",
            country = "Italia",
            center = LatLng(45.4642, 9.1900),
            defaultHourlyTariff = "€2,00/h",
            isZtlActiveCity = true,
            ztlName = "Area C / ZTL Cerchia dei Bastioni"
        ),
        CityRegion(
            id = "florence",
            name = "Firenze",
            country = "Italia",
            center = LatLng(43.7696, 11.2558),
            defaultHourlyTariff = "€2,00/h",
            isZtlActiveCity = true,
            ztlName = "ZTL Firenze Centro"
        ),
        CityRegion(
            id = "paris",
            name = "Paris",
            country = "France",
            center = LatLng(48.8566, 2.3522),
            defaultHourlyTariff = "€3,00/h",
            isZtlActiveCity = true,
            ztlName = "Zone à Trafic Limité (Paris Centre)"
        ),
        CityRegion(
            id = "london",
            name = "London",
            country = "United Kingdom",
            center = LatLng(51.5074, -0.1278),
            currencySymbol = "£",
            defaultHourlyTariff = "£3.50/h",
            isZtlActiveCity = true,
            ztlName = "Ultra Low Emission Zone (ULEZ)"
        ),
        CityRegion(
            id = "berlin",
            name = "Berlin",
            country = "Germany",
            center = LatLng(52.5200, 13.4050),
            defaultHourlyTariff = "€2,50/h",
            isZtlActiveCity = true,
            ztlName = "Umweltzone Berlin"
        ),
        CityRegion(
            id = "barcelona",
            name = "Barcelona",
            country = "España",
            center = LatLng(41.3851, 2.1734),
            defaultHourlyTariff = "€2,25/h",
            isZtlActiveCity = true,
            ztlName = "Zona de Bajas Emisiones (ZBE)"
        ),
        CityRegion(
            id = "madrid",
            name = "Madrid",
            country = "España",
            center = LatLng(40.4168, -3.7038),
            defaultHourlyTariff = "€2,10/h",
            isZtlActiveCity = true,
            ztlName = "Madrid 360 (ZBE Distrito Centro)"
        ),
        CityRegion(
            id = "new_york",
            name = "New York",
            country = "United States",
            center = LatLng(40.7128, -74.0060),
            currencySymbol = "$",
            defaultHourlyTariff = "$4.50/h",
            isZtlActiveCity = false,
            ztlName = "Congestion Relief Zone"
        ),
        CityRegion(
            id = "tokyo",
            name = "Tokyo",
            country = "Japan",
            center = LatLng(35.6762, 139.6503),
            currencySymbol = "¥",
            defaultHourlyTariff = "¥400/h",
            isZtlActiveCity = false,
            ztlName = ""
        )
    )

    fun searchCities(query: String): List<CityRegion> {
        if (query.isBlank()) return GLOBAL_PRESET_CITIES
        val q = query.trim().lowercase()
        return GLOBAL_PRESET_CITIES.filter {
            it.name.lowercase().contains(q) || it.country.lowercase().contains(q)
        }
    }

    fun findNearestCity(location: LatLng): CityRegion {
        return GLOBAL_PRESET_CITIES.minByOrNull { city ->
            calculateDistanceMeters(location, city.center)
        } ?: GLOBAL_PRESET_CITIES.first()
    }

    fun generateRegionalParkingData(city: CityRegion): List<MicroArea> {
        val lat = city.center.latitude
        val lng = city.center.longitude
        val tariff = city.defaultHourlyTariff

        return when (city.id) {
            "rome" -> getRomeParkingAreas()
            "milan" -> listOf(
                createSpot("mi_centrale", "Stazione Centrale (Piazza Duca d'Aosta)", "Piazza Duca d'Aosta", lat + 0.0210, lng + 0.0100, 28, "€2,50/h", 0, true, "Fuori Area C"),
                createSpot("mi_buenos_aires", "Corso Buenos Aires Shopping", "Corso Buenos Aires", lat + 0.0180, lng + 0.0160, 42, "€2,00/h", 2, false, "Fuori Area C"),
                createSpot("mi_duomo_san_babila", "Duomo - San Babila", "Corso Vittorio Emanuele II", lat - 0.0010, lng + 0.0030, 18, "€3,00/h", 0, true, "Area C Attiva"),
                createSpot("mi_porta_garibaldi", "Porta Garibaldi - Isola", "Piazza XXV Aprile", lat + 0.0170, lng - 0.0030, 55, "€2,00/h", 4, true, "Limite Area C"),
                createSpot("mi_navigli", "Darsena & Navigli", "Viale Gorizia", lat - 0.0160, lng - 0.0120, 32, "€2,00/h", 1, false, "Fuori Area C"),
                createSpot("mi_porta_romana", "Porta Romana Terme", "Corso di Porta Romana", lat - 0.0120, lng + 0.0090, 60, "€1,50/h", 5, true, "Fuori Area C")
            )
            "florence" -> listOf(
                createSpot("fi_smn", "Firenze Santa Maria Novella", "Piazza della Stazione", lat + 0.0040, lng - 0.0070, 35, "€2,50/h", 0, true, "Fuori ZTL"),
                createSpot("fi_fortezza", "Fortezza da Basso Hub", "Viale Filippo Strozzi", lat + 0.0120, lng - 0.0060, 65, "€1,80/h", 8, true, "Fuori ZTL"),
                createSpot("fi_beccaria", "Piazza Beccaria Parcheggio", "Viale Giovanni Amendola", lat - 0.0020, lng + 0.0150, 48, "€1,60/h", 4, false, "Fuori ZTL"),
                createSpot("fi_porta_romana", "Porta Romana Oltrarno", "Piazzale di Porta Romana", lat - 0.0150, lng - 0.0120, 52, "€1,50/h", 6, false, "Fuori ZTL")
            )
            "paris" -> listOf(
                createSpot("pa_nord", "Gare du Nord Hub", "Rue de Dunkerque", lat + 0.0230, lng + 0.0060, 30, "€3,50/h", 0, true, "Paris 10e"),
                createSpot("pa_chatelet", "Châtelet - Les Halles", "Rue Berger", lat + 0.0050, lng - 0.0050, 22, "€4,00/h", 0, true, "ZTL Paris Centre"),
                createSpot("pa_bastille", "Place de la Bastille", "Boulevard Beaumarchais", lat + 0.0010, lng + 0.0210, 45, "€3,00/h", 2, true, "Paris 11e"),
                createSpot("pa_montparnasse", "Gare Montparnasse", "Avenue du Maine", lat - 0.0160, lng - 0.0310, 50, "€3,20/h", 3, true, "Paris 14e"),
                createSpot("pa_republique", "Place de la République", "Avenue de la République", lat + 0.0110, lng + 0.0160, 38, "€3,00/h", 1, false, "Paris 11e")
            )
            "london" -> listOf(
                createSpot("lo_kings_cross", "King's Cross & St Pancras", "Euston Road", lat + 0.0230, lng - 0.0020, 25, "£4.00/h", 0, true, "Congestion Charge Active"),
                createSpot("lo_euston", "Euston Square Parking", "Melton Street", lat + 0.0210, lng - 0.0090, 40, "£3.50/h", 0, true, "Congestion Charge Active"),
                createSpot("lo_liverpool_st", "Liverpool Street & Shoreditch", "Bishopsgate", lat + 0.0110, lng + 0.0480, 32, "£4.50/h", 0, true, "City of London"),
                createSpot("lo_waterloo", "Waterloo South Bank", "York Road", lat - 0.0040, lng - 0.0080, 48, "£3.50/h", 1, true, "Lambeth Hub"),
                createSpot("lo_paddington", "Paddington Station Hub", "Praed Street", lat + 0.0110, lng - 0.0520, 55, "£3.00/h", 2, true, "Westminster")
            )
            "new_york" -> listOf(
                createSpot("ny_midtown", "Midtown Manhattan Central", "8th Avenue & W 34th St", lat + 0.0380, lng - 0.0120, 15, "$6.00/h", 0, true, "Manhattan Zone"),
                createSpot("ny_grand_central", "Grand Central Terminal Area", "Lexington Ave & E 42nd St", lat + 0.0410, lng - 0.0040, 20, "$7.00/h", 0, true, "Manhattan Zone"),
                createSpot("ny_brooklyn_heights", "Brooklyn Downtown Hub", "Cadman Plaza West", lat - 0.0150, lng + 0.0150, 48, "$3.50/h", 3, true, "Downtown Brooklyn"),
                createSpot("ny_long_island_city", "LIC Queens Plaza", "Queens Plaza South", lat + 0.0400, lng + 0.0500, 62, "$2.50/h", 6, true, "Queens Hub")
            )
            else -> generateGenericCitySpots(city)
        }
    }

    private fun generateGenericCitySpots(city: CityRegion): List<MicroArea> {
        val lat = city.center.latitude
        val lng = city.center.longitude
        val t = city.defaultHourlyTariff

        return listOf(
            createSpot("${city.id}_central", "${city.name} Central Hub", "Central Station Ave", lat + 0.0030, lng + 0.0020, 35, t, 1, true, "City Center"),
            createSpot("${city.id}_north", "${city.name} North Plaza", "North Boulevard", lat + 0.0120, lng + 0.0080, 58, t, 4, true, "North District"),
            createSpot("${city.id}_east", "${city.name} East Park & Ride", "East Avenue", lat - 0.0040, lng + 0.0160, 72, t, 8, true, "Suburban Hub"),
            createSpot("${city.id}_west", "${city.name} West Promenade", "West Gate Road", lat - 0.0080, lng - 0.0110, 40, t, 2, false, "Commercial District"),
            createSpot("${city.id}_south", "${city.name} South Station", "South Main Street", lat - 0.0140, lng + 0.0040, 64, t, 5, true, "South Terminal")
        )
    }

    private fun createSpot(
        id: String,
        name: String,
        street: String,
        lat: Double,
        lng: Double,
        avail: Int,
        tariff: String,
        whiteSpots: Int,
        ev: Boolean,
        ztl: String
    ): MicroArea {
        return MicroArea(
            id = id,
            name = name,
            streetName = street,
            latitude = lat,
            longitude = lng,
            distanceMeters = 180,
            radiusMeters = 50,
            baselineAvailability = avail,
            blueLineTariff = tariff,
            whiteLineSpots = whiteSpots,
            ztlZoneName = ztl,
            isZtlBoundary = ztl.contains("ZTL") || ztl.contains("Area C"),
            cleaningSchedule = "Martedì 00:00 - 06:00",
            normX = 0.5f,
            normY = 0.5f,
            hasEvCharging = ev,
            allowsLongStay = true,
            isEveningFree = true,
            availabilityNextHour = avail - 4,
            availabilityAfter6Pm = avail + 15
        )
    }

    private fun getRomeParkingAreas(): List<MicroArea> {
        return listOf(
            createSpot("via_giolitti_stazione", "Stazione Termini (Ala Giolitti)", "Via Giovanni Giolitti", 41.8992, 12.5035, 24, "€1,50/h", 0, true, "Fuori ZTL"),
            createSpot("via_marsala_nord", "Termini Nord (Ingresso Marsala)", "Via Marsala", 41.9028, 12.5028, 35, "€1,50/h", 2, false, "Fuori ZTL"),
            createSpot("via_filippo_turati", "Via Filippo Turati (Adiacente FS)", "Via Filippo Turati", 41.8988, 12.5052, 32, "€1,50/h", 1, true, "Fuori ZTL"),
            createSpot("pza_vittorio_nord", "Piazza Vittorio Emanuele II (Nord)", "Piazza Vittorio Emanuele II", 41.8958, 12.5042, 58, "€1,50/h", 6, true, "Limite ZTL"),
            createSpot("via_principe_amedeo", "Via Principe Amedeo (Tratto Esquilino)", "Via Principe Amedeo", 41.8985, 12.5015, 22, "€1,50/h", 0, false, "Fuori ZTL"),
            createSpot("via_cavour_santa_maria", "Via Cavour (Basilica S. Maria Maggiore)", "Via Cavour", 41.8970, 12.4990, 15, "€1,50/h", 0, true, "Confine ZTL"),
            createSpot("via_manin_esquilino", "Via Daniele Manin", "Via Daniele Manin", 41.8998, 12.4998, 48, "€1,50/h", 3, false, "Fuori ZTL"),
            createSpot("via_merulana_sud", "Via Merulana (Asse San Giovanni)", "Via Merulana", 41.8912, 12.5038, 45, "€1,50/h", 5, true, "Fuori ZTL")
        )
    }

    private fun calculateDistanceMeters(p1: LatLng, p2: LatLng): Int {
        val earthRadius = 6371000.0
        val dLat = Math.toRadians(p2.latitude - p1.latitude)
        val dLng = Math.toRadians(p2.longitude - p1.longitude)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(p1.latitude)) * Math.cos(Math.toRadians(p2.latitude)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return (earthRadius * c).toInt()
    }
}
