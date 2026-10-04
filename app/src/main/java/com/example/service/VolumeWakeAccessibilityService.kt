package com.example.service

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.example.data.ActivationMode
import com.example.data.TriggerKey
import com.example.data.VolumeWakePreferences
import com.example.ui.WakeScreenActivity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

data class KeyLogEntry(
    val keyCode: Int,
    val keyName: String,
    val action: String,
    val timestamp: Long,
    val screenWasOn: Boolean,
    val wakeTriggered: Boolean
)

class VolumeWakeAccessibilityService : AccessibilityService(), SensorEventListener {

    private val handler = Handler(Looper.getMainLooper())
    private var powerManager: PowerManager? = null
    private var sensorManager: SensorManager? = null
    private var proximitySensor: Sensor? = null
    private var isProximityNear = false

    private var lastVolumeDownPressTime = 0L
    private var lastVolumeUpPressTime = 0L
    private var lastEitherVolumePressTime = 0L

    private var longPressRunnable: Runnable? = null
    private var isLongPressTriggered = false

    // Double-click while screen is ON to lock device
    private var lastScreenOnVolumePressTime = 0L

    companion object {
        private const val TAG = "VolumeWakeService"
        var instanceRef: WeakReference<VolumeWakeAccessibilityService>? = null

        private val _isServiceConnected = MutableStateFlow(false)
        val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

        private val _keyEventFlow = MutableSharedFlow<KeyLogEntry>(extraBufferCapacity = 20)
        val keyEventFlow: SharedFlow<KeyLogEntry> = _keyEventFlow.asSharedFlow()

        private val _proximityNearState = MutableStateFlow(false)
        val proximityNearState: StateFlow<Boolean> = _proximityNearState.asStateFlow()

        fun isRunning(): Boolean = instanceRef?.get() != null

        fun lockDevice(): Boolean {
            val service = instanceRef?.get() ?: return false
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                service.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
            } else {
                false
            }
        }

        fun showPowerMenu(): Boolean {
            val service = instanceRef?.get() ?: return false
            return service.performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)
        }

        fun wakeScreenExplicitly(context: Context) {
            instanceRef?.get()?.wakeScreen(isTest = true) ?: run {
                // Fallback direct wake if service not bound yet
                val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                @Suppress("DEPRECATION")
                val wakeLock = pm?.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.ON_AFTER_RELEASE,
                    "VolumeWake::ExplicitWake"
                )
                wakeLock?.acquire(3000)
                val intent = Intent(context, WakeScreenActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instanceRef = WeakReference(this)
        _isServiceConnected.value = true
        powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        proximitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        registerProximityListener()
        Log.i(TAG, "VolumeWakeAccessibilityService connected successfully")
    }

    private fun registerProximityListener() {
        proximitySensor?.let { sensor ->
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_PROXIMITY) {
            val distance = event.values[0]
            val maxRange = proximitySensor?.maximumRange ?: 5f
            // Proximity is considered "near" (in pocket/face down) if distance < 5cm or < maxRange
            isProximityNear = distance < 4.0f && distance < maxRange
            _proximityNearState.value = isProximityNear
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // We only care about key filtering, no content retrieval needed
    }

    override fun onInterrupt() {
        Log.w(TAG, "VolumeWakeAccessibilityService interrupted")
    }

    override fun onDestroy() {
        instanceRef = null
        _isServiceConnected.value = false
        sensorManager?.unregisterListener(this)
        longPressRunnable?.let { handler.removeCallbacks(it) }
        super.onDestroy()
    }

    @SuppressLint("WakelockTimeout")
    override fun onKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        if (keyCode != KeyEvent.KEYCODE_VOLUME_UP && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN) {
            return super.onKeyEvent(event)
        }

        val isServiceEnabled = VolumeWakePreferences.isServiceEnabled.value
        val configuredKey = VolumeWakePreferences.triggerKey.value
        val activationMode = VolumeWakePreferences.activationMode.value
        val isScreenOn = powerManager?.isInteractive ?: true
        val now = System.currentTimeMillis()

        val keyName = if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) "Volume Up" else "Volume Down"
        val actionName = if (event.action == KeyEvent.ACTION_DOWN) "Down" else "Up"

        // Check if this key matches user selection
        val isMatchingKey = when (configuredKey) {
            TriggerKey.BOTH -> true
            TriggerKey.VOLUME_UP -> keyCode == KeyEvent.KEYCODE_VOLUME_UP
            TriggerKey.VOLUME_DOWN -> keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
        }

        // Screen is currently ON
        if (isScreenOn) {
            // Check double-click to lock feature if enabled
            if (VolumeWakePreferences.doubleClickToLock.value && event.action == KeyEvent.ACTION_DOWN && isMatchingKey) {
                if (now - lastScreenOnVolumePressTime < 450L) {
                    lastScreenOnVolumePressTime = 0L
                    lockDevice()
                    _keyEventFlow.tryEmit(
                        KeyLogEntry(keyCode, keyName, actionName, now, screenWasOn = true, wakeTriggered = false)
                    )
                    return true
                }
                lastScreenOnVolumePressTime = now
            }

            // Normal volume adjustment pass-through
            _keyEventFlow.tryEmit(
                KeyLogEntry(keyCode, keyName, actionName, now, screenWasOn = true, wakeTriggered = false)
            )
            return super.onKeyEvent(event)
        }

        // Screen is OFF: WAKE LOGIC
        if (!isServiceEnabled || !isMatchingKey) {
            return super.onKeyEvent(event)
        }

        var shouldConsume = VolumeWakePreferences.muteVolumeOnWake.value

        when (activationMode) {
            ActivationMode.SINGLE_PRESS -> {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    processWakeAttempt(keyCode, keyName, actionName, now)
                }
            }
            ActivationMode.DOUBLE_CLICK -> {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    val lastPressTime = when (configuredKey) {
                        TriggerKey.VOLUME_UP -> lastVolumeUpPressTime
                        TriggerKey.VOLUME_DOWN -> lastVolumeDownPressTime
                        TriggerKey.BOTH -> lastEitherVolumePressTime
                    }
                    val doubleClickTimeout = VolumeWakePreferences.doubleClickTimeout.value

                    if (now - lastPressTime in 50..doubleClickTimeout) {
                        // Second click confirmed!
                        processWakeAttempt(keyCode, keyName, actionName, now)
                        when (configuredKey) {
                            TriggerKey.VOLUME_UP -> lastVolumeUpPressTime = 0L
                            TriggerKey.VOLUME_DOWN -> lastVolumeDownPressTime = 0L
                            TriggerKey.BOTH -> lastEitherVolumePressTime = 0L
                        }
                    } else {
                        // First click recorded
                        when (configuredKey) {
                            TriggerKey.VOLUME_UP -> lastVolumeUpPressTime = now
                            TriggerKey.VOLUME_DOWN -> lastVolumeDownPressTime = now
                            TriggerKey.BOTH -> lastEitherVolumePressTime = now
                        }
                        _keyEventFlow.tryEmit(
                            KeyLogEntry(keyCode, keyName, "1st Click", now, screenWasOn = false, wakeTriggered = false)
                        )
                    }
                }
            }
            ActivationMode.LONG_PRESS -> {
                if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                    isLongPressTriggered = false
                    longPressRunnable?.let { handler.removeCallbacks(it) }
                    val r = Runnable {
                        isLongPressTriggered = true
                        processWakeAttempt(keyCode, keyName, "Long Press Held", System.currentTimeMillis())
                    }
                    longPressRunnable = r
                    handler.postDelayed(r, 500)
                } else if (event.action == KeyEvent.ACTION_UP) {
                    longPressRunnable?.let { handler.removeCallbacks(it) }
                    if (isLongPressTriggered) {
                        shouldConsume = true
                    }
                }
            }
        }

        return shouldConsume
    }

    private fun processWakeAttempt(keyCode: Int, keyName: String, actionName: String, timestamp: Long) {
        val pocketModeEnabled = VolumeWakePreferences.pocketProtectionEnabled.value

        // Check if device is in pocket (proximity sensor covered)
        if (pocketModeEnabled && isProximityNear) {
            VolumeWakePreferences.recordPocketBlock()
            triggerPocketBlockedHaptic()
            _keyEventFlow.tryEmit(
                KeyLogEntry(keyCode, keyName, "$actionName (Pocket Blocked)", timestamp, screenWasOn = false, wakeTriggered = false)
            )
            Log.d(TAG, "Screen wake prevented: proximity sensor is covered (pocket protection)")
            return
        }

        // Wake screen!
        wakeScreen(isTest = false)
        VolumeWakePreferences.recordWake()
        _keyEventFlow.tryEmit(
            KeyLogEntry(keyCode, keyName, "$actionName (Wake Triggered)", timestamp, screenWasOn = false, wakeTriggered = true)
        )
    }

    @SuppressLint("WakelockTimeout")
    fun wakeScreen(isTest: Boolean = false) {
        try {
            @Suppress("DEPRECATION")
            val wakeLock = powerManager?.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                PowerManager.ACQUIRE_CAUSES_WAKEUP or
                PowerManager.ON_AFTER_RELEASE,
                "VolumeWake::ScreenWake"
            )
            wakeLock?.acquire(3500)

            val wakeIntent = Intent(this, WakeScreenActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NO_USER_ACTION)
            }
            startActivity(wakeIntent)

            if (VolumeWakePreferences.vibrateOnWake.value) {
                triggerWakeHaptic()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to wake screen: ${e.message}", e)
        }
    }

    private fun triggerWakeHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(50)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Haptic vibration failed: ${e.message}")
        }
    }

    private fun triggerPocketBlockedHaptic() {
        try {
            @Suppress("DEPRECATION")
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 30, 60, 30), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 30, 60, 30), -1)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Pocket haptic vibration failed: ${e.message}")
        }
    }
}
