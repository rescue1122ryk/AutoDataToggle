package com.example.autodatatoggle

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val statusText = findViewById<TextView>(R.id.statusText)
        val btnEnableAccessibility = findViewById<Button>(R.id.btnEnableAccessibility)
        val btnStartService = findViewById<Button>(R.id.btnStartService)
        val btnStopService = findViewById<Button>(R.id.btnStopService)

        btnEnableAccessibility.setOnClickListener {
            // Opens system Accessibility settings so the user can manually enable
            // "Auto Data Toggle" in the list. This is a normal permission grant,
            // no ADB or root needed.
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        btnStartService.setOnClickListener {
            val serviceIntent = Intent(this, WifiMonitorService::class.java)
            startForegroundService(serviceIntent)
            statusText.text = "Status: Monitoring WiFi changes"
        }

        btnStopService.setOnClickListener {
            stopService(Intent(this, WifiMonitorService::class.java))
            statusText.text = "Status: Stopped"
        }
    }
}
