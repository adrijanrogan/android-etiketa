package com.github.adrijanrogan.etiketa.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.github.adrijanrogan.etiketa.R
import com.github.adrijanrogan.etiketa.util.FileComparator

class SettingsFragment : PreferenceFragmentCompat() {

    companion object {

        const val PREFERENCE_DISPLAY_THEME_LIGHT = "1"
        const val PREFERENCE_DISPLAY_THEME_DARK = "2"
        const val PREFERENCE_DISPLAY_THEME_BATTERY = "3"
        const val PREFERENCE_DISPLAY_THEME_SYSTEM = "4"

        const val PREFERENCE_SORTING_BY = "settings_sorting_by"
        const val PREFERENCE_SORTING_BY_NAME = "name"
        const val PREFERENCE_SORTING_BY_LAST_MODIFIED = "last_modified"

        const val PREFERENCE_SORTING_ORDER = "settings_sorting_order"
        const val PREFERENCE_SORTING_ORDER_NORMAL = "normal"
        const val PREFERENCE_SORTING_ORDER_REVERSED = "reversed"

        const val PREFERENCE_SORTING_GROUP = "settings_sorting_group"
        const val PREFERENCE_SORTING_SHOW_HIDDEN = "settings_sorting_show_hidden"

        var invalidateSort = false

        fun getSortBy(sp: SharedPreferences): Int {
            return when (sp.getString(PREFERENCE_SORTING_BY, PREFERENCE_SORTING_BY_NAME)) {
                PREFERENCE_SORTING_BY_NAME -> FileComparator.SORT_MODE_FILENAME
                PREFERENCE_SORTING_BY_LAST_MODIFIED -> FileComparator.SORT_MODE_LAST_MODIFIED
                else -> FileComparator.SORT_MODE_FILENAME
            }
        }

        fun getSortOrder(sp: SharedPreferences): Int {
            return when (sp.getString(PREFERENCE_SORTING_ORDER, PREFERENCE_SORTING_ORDER_NORMAL))  {
                PREFERENCE_SORTING_ORDER_NORMAL -> FileComparator.SORT_MODE_NORMAL
                PREFERENCE_SORTING_ORDER_REVERSED -> FileComparator.SORT_MODE_REVERSED
                else -> FileComparator.SORT_MODE_NORMAL
            }
        }

        fun getSortGroup(sp: SharedPreferences): Int {
            val group = sp.getBoolean(PREFERENCE_SORTING_GROUP, true)
            return if (group) FileComparator.SORT_MODE_GROUP
            else FileComparator.SORT_MODE_DO_NOT_GROUP
        }

        fun getSortShowHidden(sp: SharedPreferences): Boolean {
            return sp.getBoolean(PREFERENCE_SORTING_SHOW_HIDDEN, false)
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
        findPreference<ListPreference>("settings_display_theme")?.
                setOnPreferenceChangeListener { _, newValue -> updateDisplayTheme(newValue) }
        findPreference<ListPreference>(PREFERENCE_SORTING_BY)?.
                setOnPreferenceChangeListener { _, _ -> invalidateSort() }
        findPreference<ListPreference>(PREFERENCE_SORTING_ORDER)?.
                setOnPreferenceChangeListener { _, _ -> invalidateSort() }
        findPreference<CheckBoxPreference>(PREFERENCE_SORTING_GROUP)?.
                setOnPreferenceChangeListener { _, _ -> invalidateSort() }
        findPreference<CheckBoxPreference>(PREFERENCE_SORTING_SHOW_HIDDEN)?.
                setOnPreferenceChangeListener { _, _ -> invalidateSort() }
    }

    private fun invalidateSort(): Boolean {
        invalidateSort = true
        return true
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