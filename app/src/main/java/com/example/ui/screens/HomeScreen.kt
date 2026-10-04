package com.example.ui.screens

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.service.KeyLogEntry
import com.example.ui.components.EmergencyPowerBar
import com.example.ui.components.PermissionCheckCard
import com.example.ui.components.ServiceStatusHero
import com.example.ui.theme.DarkNavySurface
import com.example.ui.theme.DarkSlateCard
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.NeonCyan
import com.example.util.PermissionHelper
import com.example.viewmodel.MainViewModel
import java.util.Date

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isServiceEnabled by viewModel.isServiceEnabled.collectAsState()
    val triggerKey by viewModel.triggerKey.collectAsState()
    val activationMode by viewModel.activationMode.collectAsState()

    val isAccessibilityGranted by viewModel.isAccessibilityPermissionGranted.collectAsState()
    val isBatteryOptimizationIgnored by viewModel.isBatteryOptimizationIgnored.collectAsState()
    val isNotificationGranted by viewModel.isNotificationGranted.collectAsState()
    val isOverlayGranted by viewModel.isOverlayGranted.collectAsState()

    val totalWakes by viewModel.totalWakes.collectAsState()
    val pocketBlocks by viewModel.pocketBlocks.collectAsState()
    val lastWakeTimestamp by viewModel.lastWakeTimestamp.collectAsState()
    val keyLogs by viewModel.keyLogs.collectAsState()
    val testCountdown by viewModel.testWakeCountdown.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Hero Status Banner
        item {
            ServiceStatusHero(
                isServiceEnabled = isServiceEnabled,
                isAccessibilityGranted = isAccessibilityGranted,
                triggerKey = triggerKey,
                activationMode = activationMode,
                onToggleService = { viewModel.toggleServiceEnabled() }
            )
        }

        // Emergency Power Controls Bar (Instant lock screen / power menu)
        item {
            EmergencyPowerBar(
                onLockScreen = { viewModel.lockDevice() },
                onPowerMenu = { viewModel.showPowerMenu() },
                onTestWake = { viewModel.triggerSimulatedWakeTest() }
            )
        }

        // Test Wake Countdown Modal / Card
        item {
            AnimatedVisibility(visible = testCountdown != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_wake_countdown_card"),
                    colors = CardDefaults.cardColors(containerColor = ElectricIndigo.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(NeonCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${testCountdown ?: 0}",
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontSize = 22.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Screen Wake Trigger Test",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Acquiring WakeLock & turning on display...",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
                            )
                        }
                    }
                }
            }
        }

        // Permissions Checklist Card
        item {
            PermissionCheckCard(
                isAccessibilityGranted = isAccessibilityGranted,
                isBatteryOptimizationIgnored = isBatteryOptimizationIgnored,
                isNotificationGranted = isNotificationGranted,
                isOverlayGranted = isOverlayGranted,
                onRequestAccessibility = { PermissionHelper.openAccessibilitySettings(context) },
                onRequestBatteryOptimization = { PermissionHelper.requestIgnoreBatteryOptimization(context) },
                onRequestNotification = { PermissionHelper.openNotificationSettings(context) },
                onRequestOverlay = { PermissionHelper.openOverlaySettings(context) }
            )
        }

        // Live Statistics & Protective Impact
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("statistics_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ACTIVITY & PROTECTION STATS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatBox(
                            title = "Screen Wakes",
                            value = "$totalWakes",
                            icon = Icons.Default.WbSunny,
                            color = NeonCyan,
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            title = "Pocket Blocks",
                            value = "$pocketBlocks",
                            icon = Icons.Default.Shield,
                            color = EmeraldGlow,
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            title = "Last Wake",
                            value = formatTime(lastWakeTimestamp),
                            icon = Icons.Default.History,
                            color = ElectricIndigo,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Live Hardware Key Monitor Ticker
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("key_monitor_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIVE KEYBOARD INTERCEPTOR LOG",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = if (isAccessibilityGranted) "LISTENING" else "OFFLINE",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isAccessibilityGranted) EmeraldGlow else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (keyLogs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkNavySurface)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Press Volume Up or Volume Down to see live detection events",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            keyLogs.take(4).forEach { log ->
                                KeyLogItemRow(log)
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
private fun StatBox(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DarkNavySurface)
            .padding(12.dp)
    ) {
        Column {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                fontSize = 17.sp
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun KeyLogItemRow(log: KeyLogEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkNavySurface)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (log.wakeTriggered) NeonCyan else Color.Gray)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = log.keyName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "• ${log.action}",
                style = MaterialTheme.typography.labelSmall,
                color = if (log.wakeTriggered) NeonCyan else Color.LightGray
            )
        }

        Text(
            text = DateFormat.format("HH:mm:ss", Date(log.timestamp)).toString(),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontSize = 11.sp
        )
    }
}

private fun formatTime(timestamp: Long): String {
    if (timestamp == 0L) return "Never"
    val diffSec = (System.currentTimeMillis() - timestamp) / 1000
    return when {
        diffSec < 60 -> "${diffSec}s ago"
        diffSec < 3600 -> "${diffSec / 60}m ago"
        else -> DateFormat.format("HH:mm", Date(timestamp)).toString()
    }
}
