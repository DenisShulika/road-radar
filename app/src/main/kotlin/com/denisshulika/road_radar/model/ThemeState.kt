package com.denisshulika.road_radar.model

enum class ThemeState(val value: String) {
    LIGHT("light"),
    DARK("dark"),
    SYSTEM("system");

    companion object {
        fun fromValue(value: String): ThemeState {
            return entries.find { it.value == value } ?: SYSTEM
        }
    }
}