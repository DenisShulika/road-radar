package com.denisshulika.road_radar.model

enum class LanguageState(val value: String) {
    UKRAINIAN("uk"),
    ENGLISH("en");

    companion object {
        fun fromValue(value: String): LanguageState {
            return entries.find { it.value == value } ?: UKRAINIAN
        }
    }
}