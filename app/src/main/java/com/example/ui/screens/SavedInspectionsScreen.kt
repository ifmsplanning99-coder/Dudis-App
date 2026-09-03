package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import com.example.data.model.InspectionEntity
import com.example.ui.components.QcInputField
import com.example.ui.theme.GaugeDanger
import com.example.ui.theme.GaugeSafe
import com.example.ui.theme.GaugeWarning
import com.example.ui.theme.QcBlueDark
import com.example.ui.theme.QcBlueLight
import com.example.ui.theme.QcBluePrimary
import com.example.ui.theme.QcOutline
import com.example.ui.theme.QcTextPrimary
import com.example.ui.theme.QcTextSecondary
import com.example.ui.viewmodel.QcViewModel

@Composable
fun SavedInspectionsScreen(
    viewModel: QcViewModel,
    onOpenInspection: () -> Unit,
    onReviewInspection: () -> Unit,
    onStartNewInspection: () -> Unit,
    onExportInspection: () -> Unit
) {
    val context = LocalContext.current
    val allInspections by viewModel.allInspections.collectAsState()
    val activeInspection by viewModel.activeInspection.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedBuildingFilter by remember { mutableStateOf("All") }
    var selectedServiceFilter by remember { mutableStateOf("All") }
    var selectedStatusFilter by remember { mutableStateOf("All") }

    var inspectionToDelete by remember { mutableStateOf<InspectionEntity?>(null) }

    // Filter logic
    val filteredInspections = allInspections.filter { inspection ->
        val matchesQuery = searchQuery.isBlank() ||
                inspection.locationName.contains(searchQuery, ignoreCase = true) ||
                inspection.inspectorName.contains(searchQuery, ignoreCase = true) ||
                inspection.buildingType.contains(searchQuery, ignoreCase = true)

        val matchesBuilding = (selectedBuildingFilter == "All") ||
                inspection.buildingType.equals(selectedBuildingFilter, ignoreCase = true)

        val matchesService = (selectedServiceFilter == "All") ||
                inspection.serviceType.equals(selectedServiceFilter, ignoreCase = true)

        val matchesStatus = (selectedStatusFilter == "All") ||
                inspection.status.equals(selectedStatusFilter, ignoreCase = true)

        matchesQuery && matchesBuilding && matchesService && matchesStatus
    }

    val approvedCount = allInspections.count { it.status.equals("Approved", ignoreCase = true) }
    val draftCount = allInspections.count { !it.status.equals("Approved", ignoreCase = true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header Banner
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = QcBlueDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "SAVED INSPECTIONS",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 16.sp
                                    )
                                )
                                Text(
                                    text = "Riwayat Inspeksi QC Lapangan Tersimpan",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Button(
                            onClick = { onStartNewInspection() },
                            colors = ButtonDefaults.buttonColors(containerColor = QcBluePrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_new_inspection_from_history")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Metrics Summary Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatMetricCard(
                            label = "Total Records",
                            value = "${allInspections.size}",
                            modifier = Modifier.weight(1f)
                        )
                        StatMetricCard(
                            label = "Draft / Active",
                            value = "$draftCount",
                            valueColor = Color(0xFFFBBF24),
                            modifier = Modifier.weight(1f)
                        )
                        StatMetricCard(
                            label = "Approved",
                            value = "$approvedCount",
                            valueColor = Color(0xFF4ADE80),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. Search Field
        item {
            QcInputField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Search location, unit, inspector...",
                placeholder = "Type to search inspections...",
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = QcBluePrimary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = QcTextSecondary)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_search_saved_inspections"),
                singleLine = true
            )
        }

        // 3. Filter Chips: Service Type & Building Type
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Service Filter
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SERVICE:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = QcTextSecondary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("All", "1 PM", "3 PM", "6 PM", "Deep check", "CM").forEach { service ->
                            val isSel = (selectedServiceFilter == service)
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedServiceFilter = service },
                                label = { Text(service, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.SemiBold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = QcBluePrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White,
                                    labelColor = QcTextPrimary
                                )
                            )
                        }
                    }
                }

                // Building Filter
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "BUILDING:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = QcTextSecondary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("All", "House", "Camp", "Public Facility", "Office").forEach { bType ->
                            val isSel = (selectedBuildingFilter == bType)
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedBuildingFilter = bType },
                                label = { Text(bType, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.SemiBold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF0284C7),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White,
                                    labelColor = QcTextPrimary
                                )
                            )
                        }
                    }
                }
            }
        }

        // 4. Section Subtitle & Reset Filters
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Inspections List (${filteredInspections.size})",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = QcBlueDark
                    )
                )

                if (searchQuery.isNotBlank() || selectedBuildingFilter != "All" || selectedServiceFilter != "All" || selectedStatusFilter != "All") {
                    TextButton(onClick = {
                        searchQuery = ""
                        selectedBuildingFilter = "All"
                        selectedServiceFilter = "All"
                        selectedStatusFilter = "All"
                    }) {
                        Text("Reset Filters", fontSize = 11.sp, color = QcBluePrimary)
                    }
                }
            }
        }

        // 5. Inspection Items or Empty State
        if (filteredInspections.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(QcBlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = QcBluePrimary,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (allInspections.isEmpty()) "Belum ada inspeksi yang dibuat" else "Tidak ada inspeksi yang cocok dengan filter",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = QcBlueDark
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (allInspections.isEmpty())
                                "Mulai inspeksi baru dari halaman Lokasi untuk mencatat temuan QC."
                            else
                                "Coba ubah kata kunci pencarian atau reset filter di atas.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = QcTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onStartNewInspection() },
                            colors = ButtonDefaults.buttonColors(containerColor = QcBluePrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Buat Inspeksi Baru")
                        }
                    }
                }
            }
        } else {
            items(filteredInspections) { inspection ->
                val isActive = (activeInspection?.id == inspection.id)
                SavedInspectionItemCard(
                    inspection = inspection,
                    isActive = isActive,
                    onOpen = {
                        viewModel.selectInspection(inspection)
                        onOpenInspection()
                    },
                    onReview = {
                        viewModel.selectInspection(inspection)
                        onReviewInspection()
                    },
                    onExport = {
                        viewModel.selectInspection(inspection)
                        onExportInspection()
                    },
                    onDelete = {
                        inspectionToDelete = inspection
                    }
                )
            }
        }
    }

    // Delete Confirmation Dialog
    if (inspectionToDelete != null) {
        val toDel = inspectionToDelete!!
        AlertDialog(
            onDismissRequest = { inspectionToDelete = null },
            title = {
                Text(
                    text = "Delete Saved Inspection?",
                    fontWeight = FontWeight.Bold,
                    color = GaugeDanger
                )
            },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to permanently delete this inspection record and all its associated findings?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = toDel.locationName,
                                fontWeight = FontWeight.Bold,
                                color = QcBlueDark,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${toDel.buildingType} • ${toDel.serviceType} • Date: ${toDel.inspectionDate}",
                                fontSize = 11.sp,
                                color = QcTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteInspection(toDel)
                        Toast.makeText(context, "Inspection deleted", Toast.LENGTH_SHORT).show()
                        inspectionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GaugeDanger)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { inspectionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SavedInspectionItemCard(
    inspection: InspectionEntity,
    isActive: Boolean,
    onOpen: () -> Unit,
    onReview: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFFF0FDF4) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 3.dp else 1.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                color = if (isActive) Color(0xFF22C55E) else QcOutline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Badges & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Building Type Badge
                    Surface(
                        color = when (inspection.buildingType.lowercase()) {
                            "house" -> Color(0xFFE0F2FE)
                            "camp" -> Color(0xFFFEF3C7)
                            "public facility" -> Color(0xFFF3E8FF)
                            else -> Color(0xFFE2E8F0)
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = getBuildingIcon(inspection.buildingType),
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = when (inspection.buildingType.lowercase()) {
                                    "house" -> QcBluePrimary
                                    "camp" -> Color(0xFFB45309)
                                    "public facility" -> Color(0xFF7E22CE)
                                    else -> Color.DarkGray
                                }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = inspection.buildingType,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = when (inspection.buildingType.lowercase()) {
                                        "house" -> QcBluePrimary
                                        "camp" -> Color(0xFFB45309)
                                        "public facility" -> Color(0xFF7E22CE)
                                        else -> Color.DarkGray
                                    }
                                )
                            )
                        }
                    }

                    // Service Type Badge
                    Surface(
                        color = Color(0xFF0284C7).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = inspection.serviceType,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF0369A1),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }

                    // Active badge if currently selected
                    if (isActive) {
                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "CURRENT ACTIVE",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF15803D),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }
                }

                // Date & Status
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = inspection.inspectionDate,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = QcTextSecondary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Location Name (Main Title)
            Text(
                text = inspection.locationName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = QcBlueDark,
                    fontSize = 15.sp
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Inspector & Supervisor
            Text(
                text = "Inspector: ${inspection.inspectorName}" +
                        (if (inspection.supervisorName.isNotBlank() && inspection.supervisorName != "-") " • Sup: ${inspection.supervisorName}" else ""),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = QcTextSecondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Duration meter / 840 mins limit progress
            val totalMins = inspection.totalDurationMinutes
            val progress = (totalMins.toFloat() / 840f).coerceIn(0f, 1f)
            val progressColor = when {
                totalMins >= 840 -> GaugeDanger
                totalMins >= 600 -> GaugeWarning
                else -> GaugeSafe
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = progressColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Estimated Repair: $totalMins / 840 mins (${(progress * 100).toInt()}%)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = QcBlueDark,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Surface(
                        color = if (inspection.status == "Approved") Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = inspection.status,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (inspection.status == "Approved") Color(0xFF15803D) else Color(0xFFB45309),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = progressColor,
                    trackColor = Color(0xFFE2E8F0)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Resume / Input Findings
                Button(
                    onClick = onOpen,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = QcBluePrimary)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Input Findings", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Review Findings
                OutlinedButton(
                    onClick = onReview,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = QcBlueDark)
                ) {
                    Icon(Icons.Default.FormatListNumbered, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Review", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Quick Export
                IconButton(
                    onClick = onExport,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Export Excel",
                        tint = Color(0xFF0D9488),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Delete
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = GaugeDanger,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatMetricCard(
    label: String,
    value: String,
    valueColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = valueColor,
                    fontSize = 16.sp
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 9.sp
                )
            )
        }
    }
}

private fun getBuildingIcon(type: String): ImageVector {
    return when (type.lowercase()) {
        "house" -> Icons.Default.Home
        "camp" -> Icons.Default.Apartment
        "public facility" -> Icons.Default.CorporateFare
        "office" -> Icons.Default.Business
        else -> Icons.Default.LocationOn
    }
}
