package com.example.smartattend.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppSettingsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences(
        "smart_attend_settings",
        Context.MODE_PRIVATE
    )

    private val _isDarkMode = MutableStateFlow(
        prefs.getBoolean(KEY_DARK_MODE, false) // default light
    )

    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        prefs.edit()
            .putBoolean(KEY_DARK_MODE, enabled)
            .apply()

        _isDarkMode.value = enabled
    }

    companion object {
        private const val KEY_DARK_MODE = "dark_mode"
    }
}