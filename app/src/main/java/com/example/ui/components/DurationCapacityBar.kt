package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GaugeDanger
import com.example.ui.theme.GaugeSafe
import com.example.ui.theme.GaugeWarning
import com.example.ui.theme.QcBlueDark
import com.example.ui.theme.QcBlueLight
import com.example.ui.theme.QcBluePrimary
import com.example.ui.theme.QcTextPrimary
import com.example.ui.theme.QcTextSecondary
import com.example.ui.theme.SeverityHighBg
import com.example.ui.theme.SeverityHighText

@Composable
fun DurationCapacityBar(
    totalDurationMinutes: Int,
    maxCapacityMinutes: Int = 840,
    modifier: Modifier = Modifier,
    findingCount: Int? = null
) {
    val fraction = (totalDurationMinutes.toFloat() / maxCapacityMinutes.toFloat()).coerceIn(0f, 1f)
    val percentage = (fraction * 100).toInt()

    val animatedProgress by animateFloatAsState(targetValue = fraction, label = "progress")

    val gaugeColor by animateColorAsState(
        targetValue = when {
            totalDurationMinutes >= maxCapacityMinutes -> GaugeDanger
            totalDurationMinutes >= 600 -> GaugeWarning
            else -> GaugeSafe
        },
        label = "gaugeColor"
    )

    val isFull = totalDurationMinutes >= maxCapacityMinutes
    val remainingMinutes = (maxCapacityMinutes - totalDurationMinutes).coerceAtLeast(0)
    val totalHours = totalDurationMinutes / 60
    val totalMins = totalDurationMinutes % 60

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("duration_capacity_section")
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Header Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Capacity",
                            tint = gaugeColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Repair Duration Capacity",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = QcBlueDark
                            )
                        )
                    }

                    Surface(
                        color = gaugeColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "$percentage%",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = gaugeColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = gaugeColor,
                    trackColor = Color(0xFFE2E8F0)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Metrics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$totalDurationMinutes / $maxCapacityMinutes min",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = QcBlueDark
                            )
                        )
                        Text(
                            text = "($totalHours hrs $totalMins mins)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = QcTextSecondary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        )
                    }

                    if (findingCount != null) {
                        Surface(
                            color = QcBlueLight,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "$findingCount Findings",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = QcBluePrimary
                                )
                            )
                        }
                    } else {
                        Text(
                            text = if (isFull) "Capacity Full" else "Remaining: $remainingMinutes min",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isFull) GaugeDanger else QcTextSecondary,
                                fontWeight = if (isFull) FontWeight.Bold else FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }

        // Notification if Capacity Reached (>= 840 mins / 14 hours)
        AnimatedVisibility(visible = isFull) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .testTag("capacity_limit_warning"),
                shape = RoundedCornerShape(10.dp),
                color = SeverityHighBg,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Alert",
                        tint = SeverityHighText,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Work capacity has reached the maximum 840 minutes (14 hours) threshold. New findings cannot be added.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SeverityHighText,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 16.sp
                        )
                    )
                }
            }
        }
    }
}

