package com.denisshulika.road_radar.model

import java.util.Date

data class Incident(
    val id: String,
    val description: String,
    val creationTime: Date,
    val createdBy: String,
    val photos: List<String>,
    val gps: GPS,
    val area: String,
    val region: String,
    val incidentType: IncidentType
)

data class GPS(
    val longitude: Double,
    val latitude: Double
)

enum class IncidentType {
    CAR_ACCIDENT,
    ROADBLOCK,
    WEATHER_CONDITIONS,
    TRAFFIC_JAM,
    OTHER
}