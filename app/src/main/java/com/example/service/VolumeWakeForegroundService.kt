package com.example.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.VolumeWakeApplication
import com.example.data.VolumeWakePreferences
import kotlin.math.sqrt

class VolumeWakeForegroundService : Service(), SensorEventListener {

    companion object {
        const val ACTION_START_SERVICE = "com.example.action.START_SERVICE"
        const val ACTION_STOP_SERVICE = "com.example.action.STOP_SERVICE"
        const val ACTION_LOCK_DEVICE = "com.example.action.LOCK_DEVICE"
        const val ACTION_POWER_MENU = "com.example.action.POWER_MENU"
        const val ACTION_TOGGLE_ENABLED = "com.example.action.TOGGLE_ENABLED"

        private const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, VolumeWakeForegroundService::class.java).apply {
                action = ACTION_START_SERVICE
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, VolumeWakeForegroundService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.startService(intent)
        }
    }

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var lastShakeTime = 0L

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    // Start shake sensor if enabled
                    if (VolumeWakePreferences.wakeOnShake.value) {
                        registerShakeSensor()
                    }
                }
                Intent.ACTION_SCREEN_ON -> {
                    unregisterShakeSensor()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(screenReceiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_SERVICE -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_LOCK_DEVICE -> {
                VolumeWakeAccessibilityService.lockDevice()
            }
            ACTION_POWER_MENU -> {
                VolumeWakeAccessibilityService.showPowerMenu()
            }
            ACTION_TOGGLE_ENABLED -> {
                val current = VolumeWakePreferences.isServiceEnabled.value
                VolumeWakePreferences.setServiceEnabled(!current)
                updateNotification()
            }
        }

        startForeground(NOTIFICATION_ID, buildNotification())
        return START_STICKY
    }

    private fun buildNotification(): Notification {
        val isEnabled = VolumeWakePreferences.isServiceEnabled.value

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val lockIntent = Intent(this, VolumeWakeForegroundService::class.java).apply {
            action = ACTION_LOCK_DEVICE
        }
        val lockPendingIntent = PendingIntent.getService(
            this, 1, lockIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val powerMenuIntent = Intent(this, VolumeWakeForegroundService::class.java).apply {
            action = ACTION_POWER_MENU
        }
        val powerMenuPendingIntent = PendingIntent.getService(
            this, 2, powerMenuIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleIntent = Intent(this, VolumeWakeForegroundService::class.java).apply {
            action = ACTION_TOGGLE_ENABLED
        }
        val togglePendingIntent = PendingIntent.getService(
            this, 3, toggleIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isEnabled) "Volume Wake: Active" else "Volume Wake: Paused"
        val triggerInfo = VolumeWakePreferences.triggerKey.value.displayName
        val text = if (isEnabled) "Ready to wake with $triggerInfo" else "Tap Resume to turn on volume wake"

        return NotificationCompat.Builder(this, VolumeWakeApplication.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_screen_wake_tile)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                R.drawable.ic_screen_wake_tile,
                getString(R.string.action_lock_screen),
                lockPendingIntent
            )
            .addAction(
                R.drawable.ic_floating_power,
                getString(R.string.action_power_menu),
                powerMenuPendingIntent
            )
            .addAction(
                R.drawable.ic_screen_wake_tile,
                if (isEnabled) getString(R.string.action_pause) else getString(R.string.action_resume),
                togglePendingIntent
            )
            .build()
    }

    private fun updateNotification() {
        val notification = buildNotification()
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun registerShakeSensor() {
        accelerometer?.let { sensor ->
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun unregisterShakeSensor() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val gForce = sqrt((x * x + y * y + z * z).toDouble()) / SensorManager.GRAVITY_EARTH

            if (gForce > 2.7) {
                val now = System.currentTimeMillis()
                if (now - lastShakeTime > 1200L) {
                    lastShakeTime = now
                    VolumeWakeAccessibilityService.wakeScreenExplicitly(this)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        try {
            unregisterReceiver(screenReceiver)
        } catch (e: Exception) {
            // Already unregistered
        }
        unregisterShakeSensor()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
