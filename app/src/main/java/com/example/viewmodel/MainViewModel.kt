package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ActivationMode
import com.example.data.TriggerKey
import com.example.data.VolumeWakePreferences
import com.example.service.FloatingPowerButtonService
import com.example.service.KeyLogEntry
import com.example.service.VolumeWakeAccessibilityService
import com.example.service.VolumeWakeForegroundService
import com.example.util.PermissionHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val isServiceEnabled = VolumeWakePreferences.isServiceEnabled
    val triggerKey = VolumeWakePreferences.triggerKey
    val activationMode = VolumeWakePreferences.activationMode
    val doubleClickTimeout = VolumeWakePreferences.doubleClickTimeout
    val pocketProtectionEnabled = VolumeWakePreferences.pocketProtectionEnabled
    val vibrateOnWake = VolumeWakePreferences.vibrateOnWake
    val muteVolumeOnWake = VolumeWakePreferences.muteVolumeOnWake
    val floatingButtonEnabled = VolumeWakePreferences.floatingButtonEnabled
    val floatingButtonSize = VolumeWakePreferences.floatingButtonSize
    val floatingButtonAlpha = VolumeWakePreferences.floatingButtonAlpha
    val doubleClickToLock = VolumeWakePreferences.doubleClickToLock
    val wakeOnShake = VolumeWakePreferences.wakeOnShake
    val keepAliveNotif = VolumeWakePreferences.keepAliveNotif
    val totalWakes = VolumeWakePreferences.totalWakes
    val pocketBlocks = VolumeWakePreferences.pocketBlocks
    val lastWakeTimestamp = VolumeWakePreferences.lastWakeTimestamp

    val isAccessibilityConnected = VolumeWakeAccessibilityService.isServiceConnected
    val isProximityNear = VolumeWakeAccessibilityService.proximityNearState

    private val _isAccessibilityPermissionGranted = MutableStateFlow(false)
    val isAccessibilityPermissionGranted: StateFlow<Boolean> = _isAccessibilityPermissionGranted.asStateFlow()

    private val _isBatteryOptimizationIgnored = MutableStateFlow(false)
    val isBatteryOptimizationIgnored: StateFlow<Boolean> = _isBatteryOptimizationIgnored.asStateFlow()

    private val _isNotificationGranted = MutableStateFlow(false)
    val isNotificationGranted: StateFlow<Boolean> = _isNotificationGranted.asStateFlow()

    private val _isOverlayGranted = MutableStateFlow(false)
    val isOverlayGranted: StateFlow<Boolean> = _isOverlayGranted.asStateFlow()

    private val _keyLogs = MutableStateFlow<List<KeyLogEntry>>(emptyList())
    val keyLogs: StateFlow<List<KeyLogEntry>> = _keyLogs.asStateFlow()

    private val _testWakeCountdown = MutableStateFlow<Int?>(null)
    val testWakeCountdown: StateFlow<Int?> = _testWakeCountdown.asStateFlow()

    private val _hardwareKeyTestResult = MutableStateFlow<String?>(null)
    val hardwareKeyTestResult: StateFlow<String?> = _hardwareKeyTestResult.asStateFlow()

    init {
        refreshPermissions()

        // Collect key events from accessibility service
        viewModelScope.launch {
            VolumeWakeAccessibilityService.keyEventFlow.collect { entry ->
                val current = _keyLogs.value.toMutableList()
                current.add(0, entry)
                if (current.size > 25) current.removeAt(current.size - 1)
                _keyLogs.value = current
                _hardwareKeyTestResult.value = "${entry.keyName} detected (${entry.action})"
            }
        }
    }

    fun refreshPermissions() {
        val context = getApplication<Application>()
        _isAccessibilityPermissionGranted.value = PermissionHelper.isAccessibilityEnabled(context)
        _isBatteryOptimizationIgnored.value = PermissionHelper.isBatteryOptimizationIgnored(context)
        _isNotificationGranted.value = PermissionHelper.hasNotificationPermission(context)
        _isOverlayGranted.value = PermissionHelper.canDrawOverlays(context)
    }

    fun toggleServiceEnabled() {
        val newState = !isServiceEnabled.value
        VolumeWakePreferences.setServiceEnabled(newState)
        val context = getApplication<Application>()
        if (newState && keepAliveNotif.value) {
            VolumeWakeForegroundService.start(context)
        } else if (!newState) {
            // Keep foreground notification to allow toggling or stop
            VolumeWakeForegroundService.start(context)
        }
    }

    fun setTriggerKey(key: TriggerKey) {
        VolumeWakePreferences.setTriggerKey(key)
    }

    fun setActivationMode(mode: ActivationMode) {
        VolumeWakePreferences.setActivationMode(mode)
    }

    fun setDoubleClickTimeout(timeoutMs: Long) {
        VolumeWakePreferences.setDoubleClickTimeout(timeoutMs)
    }

    fun setPocketProtectionEnabled(enabled: Boolean) {
        VolumeWakePreferences.setPocketProtectionEnabled(enabled)
    }

    fun setVibrateOnWake(enabled: Boolean) {
        VolumeWakePreferences.setVibrateOnWake(enabled)
    }

    fun setMuteVolumeOnWake(enabled: Boolean) {
        VolumeWakePreferences.setMuteVolumeOnWake(enabled)
    }

    fun setFloatingButtonEnabled(enabled: Boolean) {
        VolumeWakePreferences.setFloatingButtonEnabled(enabled)
        val context = getApplication<Application>()
        if (enabled && PermissionHelper.canDrawOverlays(context)) {
            FloatingPowerButtonService.start(context)
        } else {
            FloatingPowerButtonService.stop(context)
        }
    }

    fun setFloatingButtonSize(size: Int) {
        VolumeWakePreferences.setFloatingButtonSize(size)
        // Restart floating service if currently running to update size
        val context = getApplication<Application>()
        if (floatingButtonEnabled.value && PermissionHelper.canDrawOverlays(context)) {
            FloatingPowerButtonService.stop(context)
            FloatingPowerButtonService.start(context)
        }
    }

    fun setFloatingButtonAlpha(alpha: Float) {
        VolumeWakePreferences.setFloatingButtonAlpha(alpha)
    }

    fun setDoubleClickToLock(enabled: Boolean) {
        VolumeWakePreferences.setDoubleClickToLock(enabled)
    }

    fun setWakeOnShake(enabled: Boolean) {
        VolumeWakePreferences.setWakeOnShake(enabled)
    }

    fun setKeepAliveNotif(enabled: Boolean) {
        VolumeWakePreferences.setKeepAliveNotif(enabled)
        val context = getApplication<Application>()
        if (enabled) {
            VolumeWakeForegroundService.start(context)
        } else {
            VolumeWakeForegroundService.stop(context)
        }
    }

    fun resetStats() {
        VolumeWakePreferences.resetStats()
    }

    fun lockDevice(): Boolean {
        return VolumeWakeAccessibilityService.lockDevice()
    }

    fun showPowerMenu(): Boolean {
        return VolumeWakeAccessibilityService.showPowerMenu()
    }

    fun triggerSimulatedWakeTest() {
        viewModelScope.launch {
            for (i in 3 downTo 1) {
                _testWakeCountdown.value = i
                delay(1000)
            }
            _testWakeCountdown.value = 0
            delay(300)
            _testWakeCountdown.value = null
            VolumeWakeAccessibilityService.wakeScreenExplicitly(getApplication())
        }
    }
}
