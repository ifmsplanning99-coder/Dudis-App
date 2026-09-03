package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MasterDurationEntity
import com.example.data.model.UserEntity
import com.example.ui.components.QcInputField
import com.example.ui.theme.GaugeDanger
import com.example.ui.theme.QcBlueDark
import com.example.ui.theme.QcBlueLight
import com.example.ui.theme.QcBluePrimary
import com.example.ui.theme.QcOutline
import com.example.ui.theme.QcTextPrimary
import com.example.ui.theme.QcTextSecondary
import com.example.ui.viewmodel.QcViewModel
import com.example.util.MasterTemplateHelper
import java.io.InputStreamReader

@Composable
fun MasterDataScreen(
    viewModel: QcViewModel
) {
    val context = LocalContext.current
    val masterDurations by viewModel.allMasterDurations.collectAsState()
    val users by viewModel.allUsers.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var selectedServiceFilter by remember { mutableStateOf("All") }
    var selectedAreaFilter by remember { mutableStateOf("All") }

    var showAddDurationDialog by remember { mutableStateOf(false) }
    var editingDuration by remember { mutableStateOf<MasterDurationEntity?>(null) }
    var showAddUserDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    // File import launcher (JSON / CSV)
    val fileImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val content = context.contentResolver.openInputStream(uri)?.use { stream ->
                    InputStreamReader(stream).readText()
                }
                if (!content.isNullOrBlank()) {
                    val parsed = MasterTemplateHelper.parseMasterDurations(content)
                    if (parsed.isNotEmpty()) {
                        viewModel.importMasterDurations(parsed, replaceExisting = false) { count ->
                            Toast.makeText(context, "Successfully imported $count presets!", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(context, "No valid presets found in file.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val filteredDurations = masterDurations.filter { item ->
        val matchService = (selectedServiceFilter == "All") ||
                item.serviceType.equals(selectedServiceFilter, ignoreCase = true) ||
                item.serviceType.equals("All", ignoreCase = true)
        val matchArea = (selectedAreaFilter == "All") ||
                item.category.equals(selectedAreaFilter, ignoreCase = true)
        matchService && matchArea
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Banner
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = QcBlueDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "MASTER TEMPLATE & PRESETS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                        )
                        Text(
                            text = "Damage Area & Duration Presets with Maintenance Services (1 PM, 3 PM, 6 PM, Deep check, CM)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        // Tab Selector (Master Presets vs User Accounts)
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = QcBluePrimary,
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Duration Presets (${masterDurations.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("User List (${users.size})", fontWeight = FontWeight.Bold) }
                )
            }
        }

        if (selectedTab == 0) {
            // Master Template Import/Export Action Bar
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Master Template Tools",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = QcBlueDark
                                )
                            )

                            Text(
                                text = "${masterDurations.size} Presets Loaded",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = QcBluePrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Action Buttons (Export, Import, Add, Reset)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Export Button
                            OutlinedButton(
                                onClick = { showExportDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_export_master_template"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = QcBluePrimary)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Export", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            // Import Button
                            OutlinedButton(
                                onClick = { showImportDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_import_master_template"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0D9488))
                            ) {
                                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Import", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            // Add New Preset Button
                            Button(
                                onClick = { showAddDurationDialog = true },
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("btn_add_master_duration"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = QcBluePrimary)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Preset", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Reset to standard button
                        TextButton(
                            onClick = { showResetConfirmDialog = true },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = QcTextSecondary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset to Standard Industry Presets", fontSize = 11.sp, color = QcTextSecondary, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Maintenance Service Filters
            item {
                Column {
                    Text(
                        text = "FILTER BY SERVICE TYPE (1 PM / 3 PM / 6 PM / Deep check / CM):",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = QcTextSecondary,
                            letterSpacing = 0.5.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val serviceFilterList = listOf("All", "1 PM", "3 PM", "6 PM", "Deep check", "CM")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        serviceFilterList.forEach { sType ->
                            val isSelected = (selectedServiceFilter == sType)
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedServiceFilter = sType },
                                label = { Text(sType, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = QcBluePrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFF1F5F9),
                                    labelColor = QcBlueDark
                                )
                            )
                        }
                    }
                }
            }

            // Damage Area Filters
            item {
                Column {
                    Text(
                        text = "FILTER BY DAMAGE AREA:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = QcTextSecondary,
                            letterSpacing = 0.5.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val areaFilterList = listOf(
                        "All", "Walls", "Flooring", "Roofing", "Ceiling",
                        "Doors", "Windows", "Sanitary", "Electrical", "Mechanical", "Landscaping"
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        areaFilterList.forEach { area ->
                            val isSelected = (selectedAreaFilter == area)
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedAreaFilter = area },
                                label = { Text(area, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF0284C7),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFF8FAFC),
                                    labelColor = QcTextPrimary
                                )
                            )
                        }
                    }
                }
            }

            // Presets Count Summary
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Presets (${filteredDurations.size} of ${masterDurations.size})",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = QcBlueDark
                        )
                    )

                    if (selectedServiceFilter != "All" || selectedAreaFilter != "All") {
                        TextButton(onClick = {
                            selectedServiceFilter = "All"
                            selectedAreaFilter = "All"
                        }) {
                            Text("Clear Filters", fontSize = 11.sp, color = QcBluePrimary)
                        }
                    }
                }
            }

            if (filteredDurations.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No presets found matching current filters.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = QcTextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { showAddDurationDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = QcBluePrimary)
                            ) {
                                Text("Add New Preset")
                            }
                        }
                    }
                }
            }

            items(filteredDurations) { item ->
                MasterPresetCard(
                    preset = item,
                    onEdit = { editingDuration = item },
                    onDelete = { viewModel.deleteMasterDuration(item) }
                )
            }
        } else {
            // Users List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Users & Roles",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = QcBlueDark
                        )
                    )

                    Button(
                        onClick = { showAddUserDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = QcBluePrimary),
                        modifier = Modifier.testTag("btn_add_user")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add User", fontSize = 12.sp)
                    }
                }
            }

            items(users) { user ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(QcBlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = QcBluePrimary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user.name,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = QcBlueDark
                                )
                            )
                            Text(
                                text = "${user.email} • ${user.phone}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = QcTextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = user.role,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
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

    // ==========================================
    // Dialogs
    // ==========================================

    // 1. Add / Edit Master Duration Dialog (with Maintenance Service choices)
    if (showAddDurationDialog || editingDuration != null) {
        val currentEdit = editingDuration
        var cat by remember(currentEdit) { mutableStateOf(currentEdit?.category ?: "Walls") }
        var sub by remember(currentEdit) { mutableStateOf(currentEdit?.subItem ?: "") }
        var sev by remember(currentEdit) { mutableStateOf(currentEdit?.severityLevel ?: "Medium") }
        var dur by remember(currentEdit) { mutableStateOf(currentEdit?.defaultDurationMinutes?.toString() ?: "45") }
        var sType by remember(currentEdit) { mutableStateOf(currentEdit?.serviceType ?: "1 PM") }

        val areaList = listOf("Walls", "Flooring", "Roofing", "Ceiling", "Doors", "Windows", "Sanitary", "Electrical", "Mechanical", "Landscaping", "Others")
        val serviceChoices = listOf("All", "1 PM", "3 PM", "6 PM", "Deep check", "CM")
        val severityChoices = listOf("Low", "Medium", "High")

        AlertDialog(
            onDismissRequest = {
                showAddDurationDialog = false
                editingDuration = null
            },
            title = {
                Text(
                    text = if (currentEdit != null) "Edit Standard Preset" else "Add Standard Preset",
                    fontWeight = FontWeight.Bold,
                    color = QcBlueDark
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Damage Area
                    Text("Damage Area (Category):", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        areaList.forEach { a ->
                            FilterChip(
                                selected = (cat == a),
                                onClick = { cat = a },
                                label = { Text(a, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = QcBluePrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    // Maintenance Service
                    Text("Maintenance Service Cycle:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        serviceChoices.forEach { s ->
                            FilterChip(
                                selected = (sType == s),
                                onClick = { sType = s },
                                label = { Text(s, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF0284C7),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    // Defect Description / Sub-Item
                    QcInputField(
                        value = sub,
                        onValueChange = { sub = it },
                        label = "Defect Name / Description",
                        placeholder = "e.g. Hairline crack on plaster",
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_master_subitem"),
                        singleLine = true
                    )

                    // Severity Level
                    Text("Severity Level:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        severityChoices.forEach { sv ->
                            val isSel = (sev.equals(sv, ignoreCase = true))
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { sev = sv }
                                    .border(
                                        width = if (isSel) 2.dp else 1.dp,
                                        color = if (isSel) QcBluePrimary else Color.LightGray,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                color = if (isSel) QcBlueLight else Color.White
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = sv,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSel) QcBlueDark else Color.DarkGray
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Standard Duration (Minutes)
                    QcInputField(
                        value = dur,
                        onValueChange = { dur = it.filter { c -> c.isDigit() } },
                        label = "Standard Duration (Minutes)",
                        placeholder = "e.g. 45",
                        trailingIcon = { Text("mins", modifier = Modifier.padding(end = 8.dp), color = QcBlueDark, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_master_duration"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (sub.isNotBlank()) {
                            val durationMinutes = dur.toIntOrNull() ?: 45
                            if (currentEdit != null) {
                                viewModel.updateMasterDuration(
                                    currentEdit.copy(
                                        category = cat.trim(),
                                        subItem = sub.trim(),
                                        severityLevel = sev,
                                        defaultDurationMinutes = durationMinutes,
                                        serviceType = sType
                                    )
                                )
                            } else {
                                viewModel.addMasterDuration(
                                    category = cat.trim(),
                                    subItem = sub.trim(),
                                    severityLevel = sev,
                                    defaultDuration = durationMinutes,
                                    serviceType = sType
                                )
                            }
                            showAddDurationDialog = false
                            editingDuration = null
                            Toast.makeText(context, "Preset saved successfully", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = QcBluePrimary)
                ) {
                    Text("Save Preset")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDurationDialog = false
                    editingDuration = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 2. Export Master Template Dialog
    if (showExportDialog) {
        var exportFormat by remember { mutableStateOf("json") }

        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Text("Export Master Template", fontWeight = FontWeight.Bold, color = QcBlueDark)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Export your standardized Damage Area & Duration Presets for sharing or backup across devices.",
                        style = MaterialTheme.typography.bodySmall.copy(color = QcTextPrimary)
                    )

                    Text(
                        text = "Select Export Format:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { exportFormat = "json" }
                                .border(
                                    width = if (exportFormat == "json") 2.dp else 1.dp,
                                    color = if (exportFormat == "json") QcBluePrimary else Color.LightGray,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            color = if (exportFormat == "json") QcBlueLight else Color.White
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("JSON Template", fontWeight = FontWeight.Bold, color = QcBlueDark, fontSize = 13.sp)
                                Text("Universal structure, full attributes", fontSize = 10.sp, color = Color.Gray)
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { exportFormat = "csv" }
                                .border(
                                    width = if (exportFormat == "csv") 2.dp else 1.dp,
                                    color = if (exportFormat == "csv") QcBluePrimary else Color.LightGray,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            color = if (exportFormat == "csv") QcBlueLight else Color.White
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("CSV Spreadsheet", fontWeight = FontWeight.Bold, color = QcBlueDark, fontSize = 13.sp)
                                Text("Open in Excel or Sheets", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }

                    Text(
                        text = "Total Presets to Export: ${masterDurations.size}",
                        style = MaterialTheme.typography.labelSmall.copy(color = QcBluePrimary, fontWeight = FontWeight.Bold)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        MasterTemplateHelper.exportAndShareMasterTemplate(context, masterDurations, exportFormat)
                        showExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = QcBluePrimary)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export & Share")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 3. Import Master Template Dialog
    if (showImportDialog) {
        var rawTextToImport by remember { mutableStateOf("") }
        var replaceExisting by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = {
                Text("Import Master Template", fontWeight = FontWeight.Bold, color = QcBlueDark)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Import Damage Area & Duration presets from a file or paste JSON/CSV text below:",
                        style = MaterialTheme.typography.bodySmall.copy(color = QcTextPrimary)
                    )

                    OutlinedButton(
                        onClick = {
                            fileImportLauncher.launch("*/*")
                            showImportDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = QcBluePrimary)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pick File (JSON or CSV)")
                    }

                    HorizontalDivider()

                    Text("Or paste JSON / CSV content directly:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))

                    QcInputField(
                        value = rawTextToImport,
                        onValueChange = { rawTextToImport = it },
                        placeholder = "Paste JSON or CSV data here...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        minLines = 4
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = replaceExisting,
                            onCheckedChange = { replaceExisting = it }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Replace existing presets (Overwrites database)",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = QcTextPrimary)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (rawTextToImport.isNotBlank()) {
                            val parsed = MasterTemplateHelper.parseMasterDurations(rawTextToImport)
                            if (parsed.isNotEmpty()) {
                                viewModel.importMasterDurations(parsed, replaceExisting) { count ->
                                    Toast.makeText(context, "Successfully imported $count presets!", Toast.LENGTH_LONG).show()
                                    showImportDialog = false
                                }
                            } else {
                                Toast.makeText(context, "Could not parse presets from text.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = QcBluePrimary)
                ) {
                    Text("Import Text")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 4. Reset Confirmation Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Reset to Standard Presets?", fontWeight = FontWeight.Bold) },
            text = {
                Text("This will replace all current presets with the comprehensive standard industry catalogue covering 1 PM, 3 PM, 6 PM, Deep check, and CM cycles across all building areas.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetToStandardMasterPresets { count ->
                            Toast.makeText(context, "Restored $count standard industry presets", Toast.LENGTH_LONG).show()
                            showResetConfirmDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = QcBluePrimary)
                ) {
                    Text("Reset to Standard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 5. Add User Dialog
    if (showAddUserDialog) {
        var uName by remember { mutableStateOf("") }
        var uEmail by remember { mutableStateOf("") }
        var uRole by remember { mutableStateOf("Inspector QC") }
        var uPhone by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddUserDialog = false },
            title = { Text("Add User", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    QcInputField(
                        value = uName,
                        onValueChange = { uName = it },
                        label = "Full Name",
                        placeholder = "e.g. Budi Santoso",
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    QcInputField(
                        value = uEmail,
                        onValueChange = { uEmail = it },
                        label = "Email",
                        placeholder = "e.g. budi@example.com",
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    QcInputField(
                        value = uPhone,
                        onValueChange = { uPhone = it },
                        label = "Phone / WhatsApp",
                        placeholder = "e.g. +62 812 3456 7890",
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (uName.isNotBlank()) {
                            viewModel.addUser(uName.trim(), uEmail.trim(), uRole, uPhone.trim())
                            showAddUserDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddUserDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MasterPresetCard(
    preset: MasterDurationEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Category Chip
                    Surface(
                        color = QcBlueLight,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = preset.category,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = QcBluePrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Maintenance Service Tag
                    Surface(
                        color = Color(0xFFE0F2FE),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = preset.serviceType,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF0369A1),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Severity Badge
                    Surface(
                        color = when (preset.severityLevel.lowercase()) {
                            "low", "ringan" -> Color(0xFFDCFCE7)
                            "medium", "sedang" -> Color(0xFFFEF3C7)
                            else -> Color(0xFFFFE4E6)
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = preset.severityLevel,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = when (preset.severityLevel.lowercase()) {
                                    "low", "ringan" -> Color(0xFF15803D)
                                    "medium", "sedang" -> Color(0xFFB45309)
                                    else -> Color(0xFFBE123C)
                                }
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = preset.subItem,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = QcBlueDark)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${preset.defaultDurationMinutes} min",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = QcBlueDark
                        )
                    )
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = QcBluePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
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


