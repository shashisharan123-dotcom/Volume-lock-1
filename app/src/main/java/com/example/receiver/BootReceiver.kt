package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.VolumeWakePreferences
import com.example.service.FloatingPowerButtonService
import com.example.service.VolumeWakeForegroundService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            VolumeWakePreferences.init(context)
            if (VolumeWakePreferences.isServiceEnabled.value && VolumeWakePreferences.keepAliveNotif.value) {
                VolumeWakeForegroundService.start(context)
            }
            if (VolumeWakePreferences.floatingButtonEnabled.value) {
                FloatingPowerButtonService.start(context)
            }
        }
    }
}
