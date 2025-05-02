package com.denisshulika.road_radar.model

enum class IncidentType(val value: String) {
    CAR_ACCIDENT("CAR_ACCIDENT"),
    ROADBLOCK("ROADBLOCK"),
    WEATHER_CONDITIONS("WEATHER_CONDITIONS"),
    TRAFFIC_JAM("TRAFFIC_JAM"),
    ROAD_WORKS("ROAD_WORKS"),
    POLICE_ACTIVITY("POLICE_ACTIVITY"),
    BROKEN_DOWN_VEHICLE("BROKEN_DOWN_VEHICLE"),
    FLOODING("FLOODING"),
    FIRE_NEAR_ROAD("FIRE_NEAR_ROAD"),
    OBSTACLE_ON_ROAD("OBSTACLE_ON_ROAD"),
    OTHER("OTHER");

    companion object {
        fun fromValue(value: String?): IncidentType =
            entries.find { it.value == value } ?: OTHER
    }
}
