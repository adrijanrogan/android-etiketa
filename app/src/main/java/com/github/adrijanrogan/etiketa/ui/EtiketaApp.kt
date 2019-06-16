package com.github.adrijanrogan.etiketa.ui

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.github.adrijanrogan.etiketa.ui.settings.SettingsFragment

class EtiketaApp: Application() {

    override fun onCreate() {
        super.onCreate()
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        when (sp.getString("settings_display_theme",
                SettingsFragment.PREFERENCE_DISPLAY_THEME_LIGHT)) {
            SettingsFragment.PREFERENCE_DISPLAY_THEME_LIGHT ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            SettingsFragment.PREFERENCE_DISPLAY_THEME_DARK ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            SettingsFragment.PREFERENCE_DISPLAY_THEME_BATTERY ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
            SettingsFragment.PREFERENCE_DISPLAY_THEME_SYSTEM ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}