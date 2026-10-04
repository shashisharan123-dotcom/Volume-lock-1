package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.DarkNavySurface
import com.example.ui.theme.DarkSlateCard
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.NeonCyan

@Composable
fun PermissionCheckCard(
    isAccessibilityGranted: Boolean,
    isBatteryOptimizationIgnored: Boolean,
    isNotificationGranted: Boolean,
    isOverlayGranted: Boolean,
    onRequestAccessibility: () -> Unit,
    onRequestBatteryOptimization: () -> Unit,
    onRequestNotification: () -> Unit,
    onRequestOverlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(!isAccessibilityGranted) }

    val allEssentialGranted = isAccessibilityGranted && isBatteryOptimizationIgnored

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("permission_check_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (allEssentialGranted) EmeraldGlow.copy(alpha = 0.2f)
                                else AmberWarning.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (allEssentialGranted) Icons.Default.Check else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (allEssentialGranted) EmeraldGlow else AmberWarning,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (allEssentialGranted) "Permissions Configured" else "Setup Action Required",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (allEssentialGranted) "All background wake features active" else "Tap to configure accessibility & battery",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Toggle",
                    tint = Color.Gray
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    // Item 1: Accessibility Service (Mandatory)
                    PermissionItemRow(
                        title = "Accessibility Service",
                        subtitle = "Required to detect volume keys when screen is off and provide lock screen",
                        icon = Icons.Default.Layers,
                        isGranted = isAccessibilityGranted,
                        isRequired = true,
                        testTag = "btn_grant_accessibility",
                        onAction = onRequestAccessibility
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Item 2: Battery Optimization (Crucial)
                    PermissionItemRow(
                        title = "Battery Unrestricted",
                        subtitle = "Prevents Android from killing the volume listener during sleep",
                        icon = Icons.Default.BatteryAlert,
                        isGranted = isBatteryOptimizationIgnored,
                        isRequired = true,
                        testTag = "btn_grant_battery_opt",
                        onAction = onRequestBatteryOptimization
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Item 3: Ongoing Notification (Android 13+)
                    PermissionItemRow(
                        title = "Notification Controls",
                        subtitle = "Shows persistent status & instant Lock Screen / Power Menu in status bar",
                        icon = Icons.Default.Notifications,
                        isGranted = isNotificationGranted,
                        isRequired = false,
                        testTag = "btn_grant_notification",
                        onAction = onRequestNotification
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Item 4: Floating Overlay (Optional)
                    PermissionItemRow(
                        title = "Floating Power Button",
                        subtitle = "Optional on-screen assistive button to lock/power off at any time",
                        icon = Icons.Default.Layers,
                        isGranted = isOverlayGranted,
                        isRequired = false,
                        testTag = "btn_grant_overlay",
                        onAction = onRequestOverlay
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionItemRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isGranted: Boolean,
    isRequired: Boolean,
    testTag: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkNavySurface)
            .border(
                1.dp,
                if (isGranted) EmeraldGlow.copy(alpha = 0.3f) else AmberWarning.copy(alpha = 0.3f),
                RoundedCornerShape(14.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isGranted) EmeraldGlow else AmberWarning,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (isRequired) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MANDATORY",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberWarning,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (isGranted) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(EmeraldGlow.copy(alpha = 0.2f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Granted",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldGlow,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Button(
                onClick = onAction,
                modifier = Modifier.testTag(testTag),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRequired) AmberWarning else NeonCyan,
                    contentColor = Color.Black
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isRequired) "Setup" else "Enable",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
