package com.github.adrijanrogan.etiketa.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.adrijanrogan.etiketa.R
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        settings_toolbar.setNavigationOnClickListener { finish() }
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
    }
}