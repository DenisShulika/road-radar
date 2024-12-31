package com.denisshulika.road_radar.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.denisshulika.road_radar.model.LanguageState
import com.denisshulika.road_radar.model.ThemeState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings_prefs")

class SettingsLocalStorage(private val context: Context) {
    companion object {
        private val THEME_KEY = stringPreferencesKey("theme")
        private val LANGUAGE_KEY = stringPreferencesKey("language")
    }

    suspend fun saveTheme(theme: ThemeState) {
        context.settingsDataStore.edit { prefs ->
            prefs[THEME_KEY] = theme.value
        }
    }

    suspend fun getTheme(): ThemeState {
        val themeValue = context.settingsDataStore.data.map { prefs ->
            prefs[THEME_KEY]
        }.firstOrNull()
        return ThemeState.fromValue(themeValue ?: ThemeState.SYSTEM.value)
    }

    suspend fun saveLanguage(language: LanguageState) {
        context.settingsDataStore.edit { prefs ->
            prefs[LANGUAGE_KEY] = language.value
        }
    }

    suspend fun getLanguage(): LanguageState {
        val languageValue = context.settingsDataStore.data.map { prefs ->
            prefs[LANGUAGE_KEY]
        }.firstOrNull()
        return LanguageState.fromValue(languageValue ?: LanguageState.UKRAINIAN.value)
    }
}
