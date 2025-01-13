package com.denisshulika.road_radar.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStream

fun readValueFromJsonFile(currLanguage : String, context: Context): Map<String, String>? {
    return try {
        var pathFile = ""
        if (currLanguage == "en") {
            pathFile = "localizationEN.json"
        } else if (currLanguage == "uk") {
            pathFile = "localizationUK.json"
        }
        val inputStream: InputStream = context.assets.open(pathFile)
        val json = inputStream.bufferedReader().use { it.readText() }

        Gson().fromJson(json, object : TypeToken<Map<String, String>>() {}.type)
    } catch (e: Exception) {
        null
    }
}