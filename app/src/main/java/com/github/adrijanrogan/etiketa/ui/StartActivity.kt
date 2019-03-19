package com.github.adrijanrogan.etiketa.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.adrijanrogan.etiketa.R
import com.github.adrijanrogan.etiketa.ui.browse.BrowseActivity

class StartActivity : AppCompatActivity() {

    // Nimamo dovoljenja -> zahtevamo
    // sicer -> zazenemo BrowseActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            proceed()
        } else {
            requestPermission()
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                proceed()
            } else {
                Toast.makeText(this,
                        "Prosimo sprejmite zahtevo za dostop do pomnilnika",
                        Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun proceed() {
        val intent = Intent(this, BrowseActivity::class.java)
        startActivity(intent)
    }

    companion object {
        private const val PERMISSION_REQUEST = 9858
    }
}