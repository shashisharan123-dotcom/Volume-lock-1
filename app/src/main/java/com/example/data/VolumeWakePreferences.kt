package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class TriggerKey(val displayName: String, val description: String) {
    BOTH("Both Volume Keys", "Wake screen with either Volume Up or Volume Down"),
    VOLUME_UP("Volume Up Only", "Wake screen only with Volume Up"),
    VOLUME_DOWN("Volume Down Only", "Wake screen only with Volume Down")
}

enum class ActivationMode(val displayName: String, val description: String) {
    SINGLE_PRESS("Single Press", "Instant wake on one key click"),
    DOUBLE_CLICK("Double Click", "Click twice quickly to prevent accidental wake in pocket"),
    LONG_PRESS("Long Press (0.5s)", "Hold key for half a second to turn screen on")
}

object VolumeWakePreferences {
    private const val PREFS_NAME = "volume_wake_prefs"

    private const val KEY_SERVICE_ENABLED = "key_service_enabled"
    private const val KEY_TRIGGER_KEY = "key_trigger_key"
    private const val KEY_ACTIVATION_MODE = "key_activation_mode"
    private const val KEY_DOUBLE_CLICK_TIMEOUT = "key_double_click_timeout"
    private const val KEY_POCKET_PROTECTION = "key_pocket_protection"
    private const val KEY_VIBRATE_ON_WAKE = "key_vibrate_on_wake"
    private const val KEY_MUTE_VOLUME_ON_WAKE = "key_mute_volume_on_wake"
    private const val KEY_FLOATING_BUTTON = "key_floating_button"
    private const val KEY_FLOATING_SIZE = "key_floating_size"
    private const val KEY_FLOATING_ALPHA = "key_floating_alpha"
    private const val KEY_DOUBLE_CLICK_TO_LOCK = "key_double_click_to_lock"
    private const val KEY_WAKE_ON_SHAKE = "key_wake_on_shake"
    private const val KEY_KEEP_ALIVE_NOTIF = "key_keep_alive_notif"
    private const val KEY_TOTAL_WAKES = "key_total_wakes"
    private const val KEY_POCKET_BLOCKS = "key_pocket_blocks"
    private const val KEY_LAST_WAKE_TIME = "key_last_wake_time"

    private lateinit var prefs: SharedPreferences

    private val _isServiceEnabled = MutableStateFlow(true)
    val isServiceEnabled: StateFlow<Boolean> = _isServiceEnabled.asStateFlow()

    private val _triggerKey = MutableStateFlow(TriggerKey.BOTH)
    val triggerKey: StateFlow<TriggerKey> = _triggerKey.asStateFlow()

    private val _activationMode = MutableStateFlow(ActivationMode.DOUBLE_CLICK)
    val activationMode: StateFlow<ActivationMode> = _activationMode.asStateFlow()

    private val _doubleClickTimeout = MutableStateFlow(450L)
    val doubleClickTimeout: StateFlow<Long> = _doubleClickTimeout.asStateFlow()

    private val _pocketProtectionEnabled = MutableStateFlow(true)
    val pocketProtectionEnabled: StateFlow<Boolean> = _pocketProtectionEnabled.asStateFlow()

    private val _vibrateOnWake = MutableStateFlow(true)
    val vibrateOnWake: StateFlow<Boolean> = _vibrateOnWake.asStateFlow()

    private val _muteVolumeOnWake = MutableStateFlow(true)
    val muteVolumeOnWake: StateFlow<Boolean> = _muteVolumeOnWake.asStateFlow()

    private val _floatingButtonEnabled = MutableStateFlow(false)
    val floatingButtonEnabled: StateFlow<Boolean> = _floatingButtonEnabled.asStateFlow()

    private val _floatingButtonSize = MutableStateFlow(54)
    val floatingButtonSize: StateFlow<Int> = _floatingButtonSize.asStateFlow()

    private val _floatingButtonAlpha = MutableStateFlow(0.85f)
    val floatingButtonAlpha: StateFlow<Float> = _floatingButtonAlpha.asStateFlow()

    private val _doubleClickToLock = MutableStateFlow(false)
    val doubleClickToLock: StateFlow<Boolean> = _doubleClickToLock.asStateFlow()

    private val _wakeOnShake = MutableStateFlow(false)
    val wakeOnShake: StateFlow<Boolean> = _wakeOnShake.asStateFlow()

    private val _keepAliveNotif = MutableStateFlow(true)
    val keepAliveNotif: StateFlow<Boolean> = _keepAliveNotif.asStateFlow()

    private val _totalWakes = MutableStateFlow(0L)
    val totalWakes: StateFlow<Long> = _totalWakes.asStateFlow()

    private val _pocketBlocks = MutableStateFlow(0L)
    val pocketBlocks: StateFlow<Long> = _pocketBlocks.asStateFlow()

    private val _lastWakeTimestamp = MutableStateFlow(0L)
    val lastWakeTimestamp: StateFlow<Long> = _lastWakeTimestamp.asStateFlow()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        _isServiceEnabled.value = prefs.getBoolean(KEY_SERVICE_ENABLED, true)
        val triggerKeyName = prefs.getString(KEY_TRIGGER_KEY, TriggerKey.BOTH.name) ?: TriggerKey.BOTH.name
        _triggerKey.value = try { TriggerKey.valueOf(triggerKeyName) } catch (e: Exception) { TriggerKey.BOTH }

        val activationModeName = prefs.getString(KEY_ACTIVATION_MODE, ActivationMode.DOUBLE_CLICK.name) ?: ActivationMode.DOUBLE_CLICK.name
        _activationMode.value = try { ActivationMode.valueOf(activationModeName) } catch (e: Exception) { ActivationMode.DOUBLE_CLICK }

        _doubleClickTimeout.value = prefs.getLong(KEY_DOUBLE_CLICK_TIMEOUT, 450L)
        _pocketProtectionEnabled.value = prefs.getBoolean(KEY_POCKET_PROTECTION, true)
        _vibrateOnWake.value = prefs.getBoolean(KEY_VIBRATE_ON_WAKE, true)
        _muteVolumeOnWake.value = prefs.getBoolean(KEY_MUTE_VOLUME_ON_WAKE, true)
        _floatingButtonEnabled.value = prefs.getBoolean(KEY_FLOATING_BUTTON, false)
        _floatingButtonSize.value = prefs.getInt(KEY_FLOATING_SIZE, 54)
        _floatingButtonAlpha.value = prefs.getFloat(KEY_FLOATING_ALPHA, 0.85f)
        _doubleClickToLock.value = prefs.getBoolean(KEY_DOUBLE_CLICK_TO_LOCK, false)
        _wakeOnShake.value = prefs.getBoolean(KEY_WAKE_ON_SHAKE, false)
        _keepAliveNotif.value = prefs.getBoolean(KEY_KEEP_ALIVE_NOTIF, true)
        _totalWakes.value = prefs.getLong(KEY_TOTAL_WAKES, 0L)
        _pocketBlocks.value = prefs.getLong(KEY_POCKET_BLOCKS, 0L)
        _lastWakeTimestamp.value = prefs.getLong(KEY_LAST_WAKE_TIME, 0L)
    }

    fun setServiceEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SERVICE_ENABLED, enabled).apply()
        _isServiceEnabled.value = enabled
    }

    fun setTriggerKey(key: TriggerKey) {
        prefs.edit().putString(KEY_TRIGGER_KEY, key.name).apply()
        _triggerKey.value = key
    }

    fun setActivationMode(mode: ActivationMode) {
        prefs.edit().putString(KEY_ACTIVATION_MODE, mode.name).apply()
        _activationMode.value = mode
    }

    fun setDoubleClickTimeout(timeoutMs: Long) {
        prefs.edit().putLong(KEY_DOUBLE_CLICK_TIMEOUT, timeoutMs).apply()
        _doubleClickTimeout.value = timeoutMs
    }

    fun setPocketProtectionEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_POCKET_PROTECTION, enabled).apply()
        _pocketProtectionEnabled.value = enabled
    }

    fun setVibrateOnWake(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATE_ON_WAKE, enabled).apply()
        _vibrateOnWake.value = enabled
    }

    fun setMuteVolumeOnWake(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MUTE_VOLUME_ON_WAKE, enabled).apply()
        _muteVolumeOnWake.value = enabled
    }

    fun setFloatingButtonEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FLOATING_BUTTON, enabled).apply()
        _floatingButtonEnabled.value = enabled
    }

    fun setFloatingButtonSize(size: Int) {
        prefs.edit().putInt(KEY_FLOATING_SIZE, size).apply()
        _floatingButtonSize.value = size
    }

    fun setFloatingButtonAlpha(alpha: Float) {
        prefs.edit().putFloat(KEY_FLOATING_ALPHA, alpha).apply()
        _floatingButtonAlpha.value = alpha
    }

    fun setDoubleClickToLock(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DOUBLE_CLICK_TO_LOCK, enabled).apply()
        _doubleClickToLock.value = enabled
    }

    fun setWakeOnShake(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WAKE_ON_SHAKE, enabled).apply()
        _wakeOnShake.value = enabled
    }

    fun setKeepAliveNotif(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_KEEP_ALIVE_NOTIF, enabled).apply()
        _keepAliveNotif.value = enabled
    }

    @Synchronized
    fun recordWake() {
        val newCount = _totalWakes.value + 1
        val now = System.currentTimeMillis()
        prefs.edit()
            .putLong(KEY_TOTAL_WAKES, newCount)
            .putLong(KEY_LAST_WAKE_TIME, now)
            .apply()
        _totalWakes.value = newCount
        _lastWakeTimestamp.value = now
    }

    @Synchronized
    fun recordPocketBlock() {
        val newCount = _pocketBlocks.value + 1
        prefs.edit().putLong(KEY_POCKET_BLOCKS, newCount).apply()
        _pocketBlocks.value = newCount
    }

    fun resetStats() {
        prefs.edit()
            .putLong(KEY_TOTAL_WAKES, 0L)
            .putLong(KEY_POCKET_BLOCKS, 0L)
            .putLong(KEY_LAST_WAKE_TIME, 0L)
            .apply()
        _totalWakes.value = 0L
        _pocketBlocks.value = 0L
        _lastWakeTimestamp.value = 0L
    }
}
