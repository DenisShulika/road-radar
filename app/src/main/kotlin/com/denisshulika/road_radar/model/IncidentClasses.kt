package com.denisshulika.road_radar.model

enum class IncidentType(val value: String) {
    CAR_ACCIDENT("CAR_ACCIDENT"),
    ROADBLOCK("ROADBLOCK"),
    WEATHER_CONDITIONS("WEATHER_CONDITIONS"),
    TRAFFIC_JAM("TRAFFIC_JAM"),
    OTHER("OTHER");

    companion object {
        fun fromValue(value: String?): IncidentType =
            entries.find { it.value == value } ?: OTHER
    }
}