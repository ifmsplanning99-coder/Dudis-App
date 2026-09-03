package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import com.example.ui.components.DurationCapacityBar
import com.example.ui.theme.GaugeDanger
import com.example.ui.theme.GaugeSafe
import com.example.ui.theme.GaugeWarning
import com.example.ui.theme.QcBlueDark
import com.example.ui.theme.QcBlueLight
import com.example.ui.theme.QcBluePrimary
import com.example.ui.theme.QcBlueSecondary
import com.example.ui.theme.QcOutline
import com.example.ui.theme.QcSurfaceVariant
import com.example.ui.theme.QcTextPrimary
import com.example.ui.theme.QcTextSecondary
import com.example.ui.theme.SeverityHighBg
import com.example.ui.theme.SeverityHighText
import com.example.ui.theme.SeverityLowBg
import com.example.ui.theme.SeverityLowText
import com.example.ui.theme.SeverityMediumBg
import com.example.ui.theme.SeverityMediumText
import com.example.ui.viewmodel.QcViewModel

@Composable
fun DashboardSummaryScreen(
    viewModel: QcViewModel,
    onNavigateToReview: () -> Unit,
    onNavigateToInput: () -> Unit,
    onNavigateToExport: () -> Unit,
    onNavigateToLocation: () -> Unit
) {
    val activeInspection by viewModel.activeInspection.collectAsState()
    val findings by viewModel.activeFindings.collectAsState()
    val totalDuration by viewModel.activeTotalDuration.collectAsState()
    val isCapacityReached by viewModel.isCapacityReached.collectAsState()

    val totalHours = totalDuration / 60
    val remainingMins = totalDuration % 60
    val percentage = ((totalDuration.toFloat() / 840f) * 100).toInt().coerceIn(0, 100)

    // Calculate distributions
    val areaGroups = findings.groupBy { it.damageArea }
    val severityGroups = findings.groupBy { 
        when (it.severityLevel.lowercase()) {
            "low", "ringan" -> "Low"
            "medium", "sedang" -> "Medium"
            else -> "High"
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = QcBluePrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "QC SUMMARY DASHBOARD",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )

                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = activeInspection?.status ?: "Draft",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = activeInspection?.locationName ?: "Location Not Selected",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Type: ${activeInspection?.buildingType ?: "-"}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.9f))
                        )
                        Text(
                            text = "Date: ${activeInspection?.inspectionDate ?: "-"}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.9f))
                        )
                    }

                    Text(
                        text = "QC Inspector: ${activeInspection?.inspectorName ?: "-"}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.9f))
                    )
                }
            }
        }

        // Duration Capacity Bar (0 - 840 mins)
        item {
            DurationCapacityBar(
                totalDurationMinutes = totalDuration,
                maxCapacityMinutes = 840,
                findingCount = findings.size
            )
        }

        // Summary KPI Metrics 2x2 Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Metric 1: Total Findings
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(QcBlueLight, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Construction,
                                        contentDescription = null,
                                        tint = QcBluePrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Total Findings",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = QcTextSecondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "${findings.size}",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = QcBlueDark
                                )
                            )
                            Text(
                                text = "Defect Points",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = QcTextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    // Metric 2: Total Duration
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color(0xFFFEF3C7), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = Color(0xFFB45309),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Total Duration",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = QcTextSecondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "$totalDuration min",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = QcBlueDark
                                )
                            )
                            Text(
                                text = "$totalHours hrs $remainingMins mins ($percentage%)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = QcTextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }
        }

        // Breakdown by Damage Area
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Findings Distribution by Area",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = QcBlueDark
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (findings.isEmpty()) {
                        Text(
                            text = "No findings recorded yet to calculate distribution.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = QcTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    } else {
                        areaGroups.forEach { (area, list) ->
                            val areaDuration = list.sumOf { it.durationMinutes }
                            val areaPercent = if (totalDuration > 0) (areaDuration * 100) / totalDuration else 0

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(QcBluePrimary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = area,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = QcBlueDark
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(${list.size} points)",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = QcTextSecondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }

                                Text(
                                    text = "$areaDuration min ($areaPercent%)",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = QcBluePrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Breakdown by Severity
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Defect Severity Level",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = QcBlueDark
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("Low", SeverityLowBg, SeverityLowText),
                            Triple("Medium", SeverityMediumBg, SeverityMediumText),
                            Triple("High", SeverityHighBg, SeverityHighText)
                        ).forEach { (label, bg, textCol) ->
                            val count = severityGroups[label]?.size ?: 0
                            Surface(
                                modifier = Modifier.weight(1f),
                                color = bg,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$count",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = textCol
                                        )
                                    )
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = textCol
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Navigation Buttons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onNavigateToReview,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dashboard_btn_review"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = QcBluePrimary)
                ) {
                    Icon(Icons.Default.FormatListNumbered, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open Findings Review", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onNavigateToExport,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dashboard_btn_export"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857))
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Excel Report & Share", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onNavigateToLocation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dashboard_btn_change_location"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = QcBlueDark)
                ) {
                    Icon(Icons.Default.LocationCity, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select / Change Inspection Location", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

