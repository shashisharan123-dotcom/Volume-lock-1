package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.DarkNavySurface
import com.example.ui.theme.DarkSlateCard
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.NeonCyan
import com.example.viewmodel.MainViewModel

@Composable
fun DiagnosticsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val keyLogs by viewModel.keyLogs.collectAsState()
    val isProximityNear by viewModel.isProximityNear.collectAsState()
    val isAccessibilityGranted by viewModel.isAccessibilityPermissionGranted.collectAsState()

    val lastKey = keyLogs.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Live Hardware Key Tester
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hardware_key_tester_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "HARDWARE BUTTON TESTER",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                letterSpacing = 1.sp
                            )
                        }

                        Text(
                            text = if (isAccessibilityGranted) "Active" else "Disabled",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isAccessibilityGranted) EmeraldGlow else AmberWarning,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Press your physical Volume Up or Volume Down buttons on your phone. If your power button still has partial life, you can also test it here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Virtual Phone Buttons Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val isVolUpActive = lastKey != null && lastKey.keyName.contains("Up") &&
                                (System.currentTimeMillis() - lastKey.timestamp < 1500)
                        val isVolDownActive = lastKey != null && lastKey.keyName.contains("Down") &&
                                (System.currentTimeMillis() - lastKey.timestamp < 1500)

                        KeyTestButton(
                            title = "Volume Up",
                            subtitle = if (isVolUpActive) "PRESSED" else "Waiting...",
                            isActive = isVolUpActive,
                            icon = Icons.Default.VolumeUp,
                            accentColor = NeonCyan,
                            modifier = Modifier.weight(1f)
                        )

                        KeyTestButton(
                            title = "Volume Down",
                            subtitle = if (isVolDownActive) "PRESSED" else "Waiting...",
                            isActive = isVolDownActive,
                            icon = Icons.Default.VolumeDown,
                            accentColor = ElectricIndigo,
                            modifier = Modifier.weight(1f)
                        )

                        KeyTestButton(
                            title = "Power Key",
                            subtitle = "Broken?",
                            isActive = false,
                            icon = Icons.Default.PowerSettingsNew,
                            accentColor = CrimsonError,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Proximity Sensor Real-time Visualizer
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("proximity_sensor_tester_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Radar,
                            contentDescription = null,
                            tint = EmeraldGlow,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PROXIMITY SENSOR RADAR",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGlow,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Wave your palm over the top ear speaker to verify pocket protection. When covered, the app blocks screen wakes so it never turns on in your pocket.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Radar visualization
                    val radarColor by animateColorAsState(
                        targetValue = if (isProximityNear) AmberWarning else EmeraldGlow,
                        label = "radar_color"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkNavySurface)
                            .border(1.5.dp, radarColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(radarColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sensors,
                                    contentDescription = null,
                                    tint = radarColor,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (isProximityNear) "POCKET MODE ENGAGED (SENSOR BLOCKED)" else "SENSOR CLEAR (READY TO WAKE)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = radarColor,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (isProximityNear) "Object detected within 4cm (wake disabled)" else "No obstacle in front of top sensor",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Broken Power Button Survival Guide
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("survival_guide_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = AmberWarning,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BROKEN POWER BUTTON SURVIVAL GUIDE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AmberWarning,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    GuideStepItem(
                        number = "1",
                        title = "Never let your battery reach 0%",
                        description = "If your phone shuts off completely with a dead power button, boot it up by plugging into a charger while holding Volume Down to enter Fastboot, then select Restart."
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    GuideStepItem(
                        number = "2",
                        title = "Lock Volume Wake in App Switcher",
                        description = "Open your recent apps overview, long press on Volume Wake and tap the Lock icon so your phone's memory cleaner doesn't stop it."
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    GuideStepItem(
                        number = "3",
                        title = "Enable 'Double Tap to Wake' in Settings",
                        description = "Most Android phones (Samsung, Xiaomi, Pixel) have a native 'Double tap to wake / sleep' in Display or Advanced Settings that pairs perfectly with this app."
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    GuideStepItem(
                        number = "4",
                        title = "Use the Floating Button or Notification",
                        description = "Lock your screen and access the power menu (restart dialog) using our built-in Floating Assistive Touch button or notification controls."
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun KeyTestButton(
    title: String,
    subtitle: String,
    isActive: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isActive) accentColor.copy(alpha = 0.35f) else DarkNavySurface,
        label = "key_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isActive) accentColor else Color.Transparent,
        label = "key_border"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isActive) accentColor else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 11.sp
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = if (isActive) accentColor else Color.DarkGray,
                fontSize = 9.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun GuideStepItem(number: String, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkNavySurface)
            .padding(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(AmberWarning.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = AmberWarning
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray,
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
        }
    }
}
