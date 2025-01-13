package com.denisshulika.road_radar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.denisshulika.road_radar.model.LanguageState
import com.denisshulika.road_radar.model.ThemeState

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val _theme = MutableLiveData<ThemeState>()
    val theme: LiveData<ThemeState> get() = _theme

    private val _language = MutableLiveData<LanguageState>()
    val language: LiveData<LanguageState> get() = _language

    private val _localization = MutableLiveData<Map<String, String>>(emptyMap())
    val localization: LiveData<Map<String, String>> get() = _localization

    fun setTheme(state : ThemeState) {
        _theme.value = state
    }

    fun setLanguage(state : LanguageState) {
        _language.value = state
    }

    fun setLocalisation(map : Map<String, String>) {
        _localization.value = map
    }

    fun getTheme(): ThemeState {
        return _theme.value!!
    }

    fun getLanguage(): LanguageState {
        return _language.value!!
    }

    fun getLocalisation(): Map<String, String> {
        return _localization.value!!
    }
}