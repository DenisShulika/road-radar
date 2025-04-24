package com.denisshulika.road_radar

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.denisshulika.road_radar.model.LanguageState
import com.denisshulika.road_radar.model.ThemeState

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val _radius = MutableLiveData<Float>()
    val radius: LiveData<Float> get() = _radius

    private val _theme = MutableLiveData<ThemeState>()
    val theme: LiveData<ThemeState> get() = _theme

    private val _language = MutableLiveData<LanguageState>()
    val language: LiveData<LanguageState> get() = _language

    private val _localization = MutableLiveData<Map<String, String>>(emptyMap())
    val localization: LiveData<Map<String, String>> get() = _localization

    private val _themeColors = MutableLiveData<Map<String, Color>>(emptyMap())
    val themeColors: LiveData<Map<String, Color>> get() = _themeColors

    fun setRadius(radius: Float) {
        _radius.value = radius
    }

    fun setTheme(state : ThemeState) {
        _theme.value = state
    }

    fun setLanguage(state : LanguageState) {
        _language.value = state
    }

    fun setThemeColors(map : Map<String, Color>) {
        _themeColors.value = map
    }

    fun setLocalisation(map : Map<String, String>) {
        _localization.value = map
    }

    fun getRadius(): Float {
        return _radius.value!!
    }

    fun getTheme(): ThemeState {
        return _theme.value!!
    }

    fun getLanguage(): LanguageState {
        return _language.value!!
    }
}