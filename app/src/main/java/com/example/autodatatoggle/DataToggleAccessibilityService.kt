package com.example.autodatatoggle

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Opens the phone's native "Mobile Network" / "SIM & network" settings screen
 * and taps the Mobile Data switch off, using normal Accessibility actions
 * (no root, no ADB, no WRITE_SECURE_SETTINGS needed).
 *
 * IMPORTANT: Every phone brand (Samsung/OneUI, Xiaomi/MIUI, stock Android,
 * Oppo/ColorOS, etc.) labels and arranges this screen slightly differently.
 * The TARGET_LABELS list below covers the most common wording. If it doesn't
 * find the switch on your phone, open Settings > Network manually once, note
 * the exact switch label text, and add it to TARGET_LABELS.
 */
class DataToggleAccessibilityService : AccessibilityService() {

    private var pendingToggleOff = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!pendingToggleOff) return
        // Give the settings screen a moment to fully render, then try to find
        // and tap the mobile data switch.
        handler.postDelayed({ tryTapMobileDataSwitch() }, 400)
    }

    override fun onInterrupt() {}

    private fun tryTapMobileDataSwitch() {
        val root = rootInActiveWindow ?: return

        for (label in TARGET_LABELS) {
            val nodes = root.findAccessibilityNodeInfosByText(label)
            for (node in nodes) {
                val switchNode = findClickableSwitchNear(node)
                if (switchNode != null) {
                    switchNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    pendingToggleOff = false
                    // Return to home screen so the phone doesn't stay parked in Settings.
                    handler.postDelayed({ performGlobalAction(GLOBAL_ACTION_HOME) }, 500)
                    return
                }
            }
        }
    }

    /** Walks up/around a text node to find the nearest clickable Switch widget. */
    private fun findClickableSwitchNear(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var current: AccessibilityNodeInfo? = node
        var depth = 0
        while (current != null && depth < 5) {
            if (current.className?.contains("Switch") == true && current.isClickable) {
                return current
            }
            // check siblings under the same parent
            val parent = current.parent
            if (parent != null) {
                for (i in 0 until parent.childCount) {
                    val sibling = parent.getChild(i) ?: continue
                    if (sibling.className?.contains("Switch") == true && sibling.isClickable) {
                        return sibling
                    }
                }
            }
            current = parent
            depth++
        }
        return null
    }

    companion object {
        private var instance: DataToggleAccessibilityService? = null

        private val TARGET_LABELS = listOf(
            "Mobile data", "Mobile Data", "Data connection",
            "SIM 1", "SIM 2", "Cellular data"
        )

        /**
         * Called from WifiMonitorService when WiFi connects. Opens the mobile
         * network settings screen; the running accessibility service then
         * detects the screen and taps the switch (see onAccessibilityEvent).
         */
        fun requestTurnOffMobileData(context: Context) {
            val svc = instance ?: return // accessibility service not enabled yet
            svc.pendingToggleOff = true

            val intent = Intent(Settings.ACTION_DATA_ROAMING_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback to the general network settings screen on phones
                // that don't support ACTION_DATA_ROAMING_SETTINGS directly.
                val fallback = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallback)
            }
        }
    }
}
