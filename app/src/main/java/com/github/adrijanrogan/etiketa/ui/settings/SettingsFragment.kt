package com.github.adrijanrogan.etiketa.ui.settings

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.github.adrijanrogan.etiketa.R

class SettingsFragment : PreferenceFragmentCompat() {

    companion object {
        const val PREFERENCE_DISPLAY_THEME_LIGHT = "1"
        const val PREFERENCE_DISPLAY_THEME_DARK = "2"
        const val PREFERENCE_DISPLAY_THEME_BATTERY = "3"
        const val PREFERENCE_DISPLAY_THEME_SYSTEM = "4"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
        findPreference<ListPreference>("settings_display_theme")?.
                setOnPreferenceChangeListener { _, newValue -> updateDisplayTheme(newValue) }
    }

    private fun updateDisplayTheme(value: Any): Boolean {
        Log.d("SettingsFragment", "updateDisplayTheme($value)")
        return if (value is String) {
            when (value) {
                PREFERENCE_DISPLAY_THEME_LIGHT ->
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                PREFERENCE_DISPLAY_THEME_DARK ->
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                PREFERENCE_DISPLAY_THEME_BATTERY ->
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                PREFERENCE_DISPLAY_THEME_SYSTEM ->
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
            true
        } else {
            false
        }
    }
}