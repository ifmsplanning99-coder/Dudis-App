package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.SeverityLevel
import com.example.ui.components.DurationCapacityBar
import com.example.ui.components.ExcelDataBoxDialog
import com.example.ui.components.QcInputField
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
import com.example.util.ImageHelper
import java.io.File

@Composable
fun Phase2FindingInputScreen(
    viewModel: QcViewModel,
    onNavigateToReview: () -> Unit,
    onNavigateToLocation: () -> Unit
) {
    val context = LocalContext.current

    val activeInspection by viewModel.activeInspection.collectAsState()
    val findings by viewModel.activeFindings.collectAsState()
    val totalDuration by viewModel.activeTotalDuration.collectAsState()
    val isCapacityReached by viewModel.isCapacityReached.collectAsState()

    val currentRole by viewModel.currentRole.collectAsState()
    val photoPath by viewModel.findingPhotoPath.collectAsState()
    val damageArea by viewModel.damageArea.collectAsState()
    val damageDescription by viewModel.damageDescription.collectAsState()
    val damageDimension by viewModel.damageDimension.collectAsState()
    val severityLevel by viewModel.severityLevel.collectAsState()
    val estimatedDuration by viewModel.estimatedDuration.collectAsState()
    val selectedSubItemPreset by viewModel.selectedSubItemPreset.collectAsState()
    val isManualDurationEdited by viewModel.isManualDurationEdited.collectAsState()
    val masterDurations by viewModel.allMasterDurations.collectAsState()

    // Activity Result Launchers for Camera & Photo Picker
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val savedPath = ImageHelper.saveBitmapToInternalStorage(context, bitmap)
            viewModel.setFindingPhoto(savedPath)
            Toast.makeText(context, "Camera photo saved successfully", Toast.LENGTH_SHORT).show()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val copiedPath = ImageHelper.copyUriToInternalStorage(context, uri)
            viewModel.setFindingPhoto(copiedPath)
            Toast.makeText(context, "Gallery photo selected successfully", Toast.LENGTH_SHORT).show()
        }
    }

    val damageAreaOptions = listOf(
        "Walls", "Flooring", "Roofing", "Ceiling", "Doors", "Windows",
        "Sanitary", "Electrical", "Mechanical", "Landscaping", "Others"
    )

    val currentPresets = masterDurations.filter { it.category.equals(damageArea, ignoreCase = true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Location context card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = QcBlueLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = QcBluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = activeInspection?.locationName ?: "New Inspection",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = QcBlueDark
                                )
                            )
                            Text(
                                text = "${activeInspection?.buildingType ?: "Property"} • ${activeInspection?.inspectionDate ?: ""}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = QcTextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { onNavigateToLocation() },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = QcBluePrimary)
                    ) {
                        Text("Change", fontSize = 12.sp)
                    }
                }
            }
        }

        // Capacity Progress Gauge (0 - 840 min)
        item {
            DurationCapacityBar(
                totalDurationMinutes = totalDuration,
                maxCapacityMinutes = viewModel.MAX_CAPACITY_MINUTES,
                findingCount = findings.size
            )
        }

        // PHASE 2 Form Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PHASE 2 : DEFECT FINDINGS INPUT",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = QcBlueDark
                    )
                )

                if (findings.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onNavigateToReview() },
                        color = QcBluePrimary
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatListNumbered,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Review (${findings.size})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }

        // 1. Defect Photo (Camera & Gallery)
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "1. Defect Photo",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = QcBlueDark
                            )
                        )
                        Text(
                            text = if (photoPath != null) "✓ Photo Attached" else "Optional",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (photoPath != null) QcBluePrimary else QcTextSecondary,
                                fontWeight = if (photoPath != null) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (photoPath != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, QcOutline, RoundedCornerShape(10.dp))
                        ) {
                            AsyncImage(
                                model = File(photoPath!!),
                                contentDescription = "Defect Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            IconButton(
                                onClick = { viewModel.setFindingPhoto(null) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                                    .size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove Photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Mobile Camera button
                            Button(
                                onClick = {
                                    cameraLauncher.launch(null)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("btn_capture_camera"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = QcBluePrimary)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Camera", fontWeight = FontWeight.Bold)
                            }

                            // Upload Photo button
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("btn_upload_photo"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = QcBluePrimary),
                                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = QcBluePrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Upload Photo", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 2. Damage Area
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. Damage Area",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = QcBlueDark
                        )
                    )
                    Text(
                        text = "Select finding area (automatically loads duration presets)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = QcTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val scrollState = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        damageAreaOptions.forEach { area ->
                            val isSelected = (damageArea == area)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.onAreaChanged(area) },
                                label = {
                                    Text(
                                        text = area,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = QcBluePrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = QcSurfaceVariant,
                                    labelColor = QcTextPrimary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("area_chip_$area")
                            )
                        }
                    }

                    // Preset Quick Selection if available
                    if (currentPresets.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Duration Master Presets ($damageArea):",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = QcBlueSecondary
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            currentPresets.forEach { preset ->
                                val isSelected = (selectedSubItemPreset == preset.subItem)
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.onPresetSelected(preset) }
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) QcBluePrimary else Color(0xFFE2E8F0),
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    color = if (isSelected) QcBlueLight else Color(0xFFFAFAFA)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = preset.subItem,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                color = if (isSelected) QcBlueDark else QcTextPrimary
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                        Surface(
                                            color = QcBluePrimary.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "${preset.defaultDurationMinutes} mins",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = QcBluePrimary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Defect Description
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. Defect Description",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = QcBlueDark
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    QcInputField(
                        value = damageDescription,
                        onValueChange = { viewModel.damageDescription.value = it },
                        placeholder = "Contoh: Retak rambut pada dinding ruang tamu dekat kusen jendela...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_damage_description"),
                        minLines = 3
                    )
                }
            }
        }

        // 4. Dimension / Boundary
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "4. Dimension / Boundary",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = QcBlueDark
                        )
                    )
                    Text(
                        text = "Length x Width, number of spots, or Area m²",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = QcTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    QcInputField(
                        value = damageDimension,
                        onValueChange = { viewModel.damageDimension.value = it },
                        leadingIcon = { Icon(Icons.Default.Straighten, contentDescription = null, tint = QcBluePrimary) },
                        placeholder = "Contoh: 1.5 x 2.0 m / 3 titik / 4 m²",
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_damage_dimension"),
                        singleLine = true
                    )
                }
            }
        }

        // 5. Severity Level
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "5. Severity Level",
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
                        SeverityLevel.values().forEach { level ->
                            val isSelected = (severityLevel == level)
                            val (bgCol, textCol) = when (level) {
                                SeverityLevel.LOW -> Pair(SeverityLowBg, SeverityLowText)
                                SeverityLevel.MEDIUM -> Pair(SeverityMediumBg, SeverityMediumText)
                                SeverityLevel.HIGH -> Pair(SeverityHighBg, SeverityHighText)
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.onSeverityChanged(level) }
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) textCol else Color(0xFFE2E8F0),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .testTag("severity_level_${level.name.lowercase()}"),
                                color = if (isSelected) bgCol else Color(0xFFF8FAFC)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = textCol,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = level.displayName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = if (isSelected) textCol else QcTextPrimary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Estimated Repair Duration (AUTO CALCULATION + Supervisor/Inspector edit)
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "6. Estimated Repair Duration",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = QcBlueDark
                            )
                        )

                        Surface(
                            color = Color(0xFFE0F2FE),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = QcBluePrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isManualDurationEdited) "Manual Edit" else "Auto Calculated",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = QcBluePrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = QcBluePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$estimatedDuration Minutes",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = QcBlueDark
                                )
                            )
                            Text(
                                text = " (${estimatedDuration / 60}h ${estimatedDuration % 60}m)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = QcTextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        // Stepper +/-
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        val newDur = (estimatedDuration - 15).coerceAtLeast(15)
                                        viewModel.onDurationManualChanged(newDur)
                                    },
                                color = QcSurfaceVariant
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("-15", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = QcBlueDark)
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        val newDur = estimatedDuration + 15
                                        viewModel.onDurationManualChanged(newDur)
                                    },
                                color = QcSurfaceVariant
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("+15", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = QcBlueDark)
                                }
                            }
                        }
                    }

                    // Quick duration presets
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(30, 45, 60, 90, 120).forEach { mins ->
                            val isSel = (estimatedDuration == mins)
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { viewModel.onDurationManualChanged(mins) }
                                    .border(
                                        1.dp,
                                        if (isSel) QcBluePrimary else Color(0xFFE2E8F0),
                                        RoundedCornerShape(6.dp)
                                    ),
                                color = if (isSel) QcBlueLight else Color.White
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${mins}m",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.SemiBold,
                                            color = if (isSel) QcBlueDark else QcTextPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons: 7. Save Finding & 8. Add Another Finding
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Button 8: Add Another Finding (Disabled if capacity >= 840 mins)
                Button(
                    onClick = {
                        viewModel.saveCurrentFinding { success ->
                            if (success) {
                                Toast.makeText(context, "Finding saved. You can enter the next finding.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed: Capacity limit reached (840 minutes)!", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    enabled = !isCapacityReached,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_add_another_finding"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = QcBluePrimary,
                        disabledContainerColor = Color(0xFFCBD5E1)
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCapacityReached) "Add Finding (Capacity Full)" else "8. Add Another Finding",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                }

                // Button 7: Save Finding (Saves & navigates to Review)
                Button(
                    onClick = {
                        if (damageDescription.isNotBlank() || damageDimension.isNotBlank() || photoPath != null) {
                            viewModel.saveCurrentFinding { success ->
                                onNavigateToReview()
                            }
                        } else {
                            onNavigateToReview()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_save_finding"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = QcBlueDark)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "7. Save Finding & Open Review",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                }
            }
        }
    }
}

