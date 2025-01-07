package com.denisshulika.road_radar.model

enum class IncidentType {
    CAR_ACCIDENT,
    ROADBLOCK,
    WEATHER_CONDITIONS,
    TRAFFIC_JAM,
    OTHER
}

class IncidentInfo(
    val type: String,
    val date: String,
    val address: String,
    val description: String,
    val createdBy: String,
    val photos: List<String>
)