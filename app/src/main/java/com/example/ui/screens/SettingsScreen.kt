package com.example.ui.screens

import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ActivationMode
import com.example.data.TriggerKey
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.DarkNavySurface
import com.example.ui.theme.DarkSlateCard
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.NeonCyan
import com.example.util.PermissionHelper
import com.example.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val triggerKey by viewModel.triggerKey.collectAsState()
    val activationMode by viewModel.activationMode.collectAsState()
    val doubleClickTimeout by viewModel.doubleClickTimeout.collectAsState()
    val pocketProtectionEnabled by viewModel.pocketProtectionEnabled.collectAsState()
    val isProximityNear by viewModel.isProximityNear.collectAsState()
    val vibrateOnWake by viewModel.vibrateOnWake.collectAsState()
    val muteVolumeOnWake by viewModel.muteVolumeOnWake.collectAsState()
    val floatingButtonEnabled by viewModel.floatingButtonEnabled.collectAsState()
    val floatingButtonSize by viewModel.floatingButtonSize.collectAsState()
    val floatingButtonAlpha by viewModel.floatingButtonAlpha.collectAsState()
    val doubleClickToLock by viewModel.doubleClickToLock.collectAsState()
    val wakeOnShake by viewModel.wakeOnShake.collectAsState()
    val keepAliveNotif by viewModel.keepAliveNotif.collectAsState()

    var showResetDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Section: Trigger Key
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("trigger_key_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(
                        icon = Icons.Default.VolumeUp,
                        title = "VOLUME TRIGGER KEY",
                        color = NeonCyan
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TriggerKey.values().forEach { key ->
                            SelectableItem(
                                title = key.displayName,
                                subtitle = key.description,
                                isSelected = triggerKey == key,
                                testTag = "trigger_key_${key.name.lowercase()}",
                                onClick = { viewModel.setTriggerKey(key) }
                            )
                        }
                    }
                }
            }
        }

        // Section: Activation Mode
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("activation_mode_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(
                        icon = Icons.Default.Tune,
                        title = "ACTIVATION GESTURE",
                        color = ElectricIndigo
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActivationMode.values().forEach { mode ->
                            SelectableItem(
                                title = mode.displayName,
                                subtitle = mode.description,
                                isSelected = activationMode == mode,
                                testTag = "activation_mode_${mode.name.lowercase()}",
                                onClick = { viewModel.setActivationMode(mode) }
                            )
                        }
                    }

                    // Double Click Timeout Slider (if double click is selected)
                    AnimatedVisibility(visible = activationMode == ActivationMode.DOUBLE_CLICK) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkNavySurface)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Double Click Sensitivity",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${doubleClickTimeout}ms",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Slider(
                                value = doubleClickTimeout.toFloat(),
                                onValueChange = { viewModel.setDoubleClickTimeout(it.toLong()) },
                                valueRange = 300f..800f,
                                steps = 9,
                                modifier = Modifier.testTag("double_click_timeout_slider"),
                                colors = SliderDefaults.colors(
                                    thumbColor = NeonCyan,
                                    activeTrackColor = NeonCyan,
                                    inactiveTrackColor = Color.DarkGray
                                )
                            )
                            Text(
                                text = "Lower = requires faster double click; Higher = allows relaxed double click.",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Section: Smart Pocket & Battery Protection
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("protection_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(
                        icon = Icons.Default.Shield,
                        title = "SMART POCKET & BATTERY PROTECTION",
                        color = EmeraldGlow
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Pocket Protection
                    SettingToggleRow(
                        title = "Pocket Mode (Proximity Sensor)",
                        subtitle = "Prevents turning on screen when phone is in your pocket, bag, or facedown",
                        checked = pocketProtectionEnabled,
                        testTag = "toggle_pocket_protection",
                        onCheckedChange = { viewModel.setPocketProtectionEnabled(it) }
                    )

                    // Live Proximity Readout
                    if (pocketProtectionEnabled) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkNavySurface)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isProximityNear) AmberWarning else EmeraldGlow)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isProximityNear) "Sensor Covered (Pocket Active: Wake blocked)" else "Sensor Clear (Ready to wake)",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isProximityNear) AmberWarning else EmeraldGlow,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Vibrate on Wake
                    SettingToggleRow(
                        title = "Haptic Vibration",
                        subtitle = "Tactile vibration confirmation when screen is successfully turned on",
                        checked = vibrateOnWake,
                        testTag = "toggle_vibrate_on_wake",
                        onCheckedChange = { viewModel.setVibrateOnWake(it) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mute volume sound on wake
                    SettingToggleRow(
                        title = "Mute Volume Beep on Wake",
                        subtitle = "Suppresses Android's volume beep sound when waking up the display",
                        checked = muteVolumeOnWake,
                        testTag = "toggle_mute_volume_on_wake",
                        onCheckedChange = { viewModel.setMuteVolumeOnWake(it) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Wake on Shake
                    SettingToggleRow(
                        title = "Shake to Wake (Backup Gesture)",
                        subtitle = "Firmly shake phone to turn on screen if volume key is inconvenient",
                        checked = wakeOnShake,
                        testTag = "toggle_wake_on_shake",
                        onCheckedChange = { viewModel.setWakeOnShake(it) }
                    )
                }
            }
        }

        // Section: Power Button Alternatives (Lock & Power Menu)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("power_alternatives_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(
                        icon = Icons.Default.Power,
                        title = "POWER BUTTON REPLACEMENTS",
                        color = AmberWarning
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Floating Button Toggle
                    SettingToggleRow(
                        title = "Floating Assistive Power Button",
                        subtitle = "Draggable on-screen button to Lock Screen or open Power Menu anytime",
                        checked = floatingButtonEnabled,
                        testTag = "toggle_floating_button",
                        onCheckedChange = { isChecked ->
                            if (isChecked && !PermissionHelper.canDrawOverlays(context)) {
                                PermissionHelper.openOverlaySettings(context)
                            }
                            viewModel.setFloatingButtonEnabled(isChecked)
                        }
                    )

                    // Floating Button Customization
                    AnimatedVisibility(visible = floatingButtonEnabled) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkNavySurface)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Floating Button Size: ${floatingButtonSize}dp",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Slider(
                                value = floatingButtonSize.toFloat(),
                                onValueChange = { viewModel.setFloatingButtonSize(it.toInt()) },
                                valueRange = 40f..72f,
                                steps = 7,
                                colors = SliderDefaults.colors(
                                    thumbColor = NeonCyan,
                                    activeTrackColor = NeonCyan
                                )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Opacity: ${(floatingButtonAlpha * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Slider(
                                value = floatingButtonAlpha,
                                onValueChange = { viewModel.setFloatingButtonAlpha(it) },
                                valueRange = 0.3f..1.0f,
                                steps = 7,
                                colors = SliderDefaults.colors(
                                    thumbColor = ElectricIndigo,
                                    activeTrackColor = ElectricIndigo
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Double-click volume while screen is ON to lock
                    SettingToggleRow(
                        title = "Double-Tap Volume to Lock",
                        subtitle = "When screen is ON, quickly double-press volume key to turn off screen",
                        checked = doubleClickToLock,
                        testTag = "toggle_double_click_to_lock",
                        onCheckedChange = { viewModel.setDoubleClickToLock(it) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Ongoing Notification controls
                    SettingToggleRow(
                        title = "Notification Shade Quick Controls",
                        subtitle = "Displays persistent Lock Screen and Power Menu buttons in notification shade",
                        checked = keepAliveNotif,
                        testTag = "toggle_keep_alive_notif",
                        onCheckedChange = { viewModel.setKeepAliveNotif(it) }
                    )
                }
            }
        }

        // Section: Reset & Storage
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reset_stats_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(
                        icon = Icons.Default.Delete,
                        title = "DATA & DIAGNOSTICS",
                        color = CrimsonError
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Reset all activity counters and wake statistics back to zero.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.testTag("btn_reset_stats"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CrimsonError.copy(alpha = 0.2f),
                            contentColor = CrimsonError
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Cached, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset Wake Counters", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Wake Statistics?") },
            text = { Text("This will clear your total screen wakes and pocket block counts.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetStats()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset", color = CrimsonError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun SelectableItem(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkNavySurface)
            .border(
                1.5.dp,
                if (isSelected) NeonCyan else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (isSelected) NeonCyan else Color(0xFF334155)),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = Color.White
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkNavySurface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray,
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = NeonCyan,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color(0xFF334155)
            )
        )
    }
}
