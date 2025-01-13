package com.denisshulika.road_radar.local

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.denisshulika.road_radar.SettingsViewModel
import com.denisshulika.road_radar.model.LanguageState
import com.denisshulika.road_radar.model.ThemeState
import com.denisshulika.road_radar.util.readValueFromJsonFile
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings_prefs")

class SettingsLocalStorage(
    private val context: Context,
    private val settingsViewModel: SettingsViewModel
) {
    companion object {
        private val THEME_KEY = stringPreferencesKey("theme")
        private val LANGUAGE_KEY = stringPreferencesKey("language")
        private val FIRST_LAUNCH_KEY = stringPreferencesKey("first_launch")
    }

    suspend fun initializeSettings(context: Context) {
        val isFirstLaunch = context.settingsDataStore.data
            .map { prefs -> prefs[FIRST_LAUNCH_KEY] }
            .firstOrNull() == null

        if (isFirstLaunch) {
            saveTheme(ThemeState.SYSTEM)
            saveLanguage(LanguageState.UKRAINIAN, context)

            context.settingsDataStore.edit { prefs ->
                prefs[FIRST_LAUNCH_KEY] = "false"
            }

            settingsViewModel.setTheme(ThemeState.SYSTEM)
            settingsViewModel.setLanguage(LanguageState.UKRAINIAN)
        }

        settingsViewModel.setTheme(getTheme())
        settingsViewModel.setLanguage(getLanguage())
    }

    suspend fun saveTheme(theme: ThemeState) {
        context.settingsDataStore.edit { prefs ->
            prefs[THEME_KEY] = theme.value
        }
        settingsViewModel.setTheme(theme)
    }

    suspend fun getTheme(): ThemeState {
        val themeValue = context.settingsDataStore.data.map { prefs ->
            prefs[THEME_KEY]
        }.firstOrNull()
        return ThemeState.fromValue(themeValue ?: ThemeState.SYSTEM.value)
    }

    suspend fun saveLanguage(language: LanguageState, context: Context) {
        context.settingsDataStore.edit { prefs ->
            prefs[LANGUAGE_KEY] = language.value
        }
        settingsViewModel.setLanguage(language)
        settingsViewModel.setLocalisation(readValueFromJsonFile(language.value, context)!!)
    }

    suspend fun getLanguage(): LanguageState {
        val languageValue = context.settingsDataStore.data.map { prefs ->
            prefs[LANGUAGE_KEY]
        }.firstOrNull()
        return LanguageState.fromValue(languageValue ?: LanguageState.UKRAINIAN.value)
    }
}
