package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.FindingEntity
import com.example.data.model.SeverityLevel
import com.example.data.model.UserRole
import com.example.ui.components.DurationCapacityBar
import com.example.ui.components.QcInputField
import com.example.ui.theme.GaugeDanger
import com.example.ui.theme.QcBlueDark
import com.example.ui.theme.QcBlueLight
import com.example.ui.theme.QcBluePrimary
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
import java.io.File

@Composable
fun ReviewFindingsScreen(
    viewModel: QcViewModel,
    onNavigateToInputFinding: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToExport: () -> Unit
) {
    val activeInspection by viewModel.activeInspection.collectAsState()
    val findings by viewModel.activeFindings.collectAsState()
    val totalDuration by viewModel.activeTotalDuration.collectAsState()
    val isCapacityReached by viewModel.isCapacityReached.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()

    var editingFinding by remember { mutableStateOf<FindingEntity?>(null) }
    var deletingFinding by remember { mutableStateOf<FindingEntity?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Info & Inspection Details
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = QcBlueDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "FINDINGS REVIEW",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Surface(
                            color = if (activeInspection?.status == "Approved") Color(0xFF22C55E) else Color(0xFFF59E0B),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = activeInspection?.status ?: "Draft",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = activeInspection?.locationName ?: "Property Location",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 17.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Type: ${activeInspection?.buildingType ?: "-"} • Date: ${activeInspection?.inspectionDate ?: "-"}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    )
                    Text(
                        text = "Inspector: ${activeInspection?.inspectorName ?: "-"} • Supervisor: ${activeInspection?.supervisorName ?: "-"}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    )
                }
            }
        }

        // Capacity Bar
        item {
            DurationCapacityBar(
                totalDurationMinutes = totalDuration,
                maxCapacityMinutes = viewModel.MAX_CAPACITY_MINUTES,
                findingCount = findings.size
            )
        }

        // Action Toolbar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Add New Finding (Disabled if >= 840 min)
                Button(
                    onClick = { onNavigateToInputFinding() },
                    enabled = !isCapacityReached,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_review_add_finding"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = QcBluePrimary,
                        disabledContainerColor = Color(0xFFCBD5E1)
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isCapacityReached) "Full" else "Add Finding",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                // Export Excel Button
                Button(
                    onClick = { onNavigateToExport() },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_review_export_excel"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857))
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export Excel", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Dashboard Summary
                OutlinedButton(
                    onClick = { onNavigateToDashboard() },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_review_dashboard"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = QcBlueDark)
                ) {
                    Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Dashboard", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Supervisor QC Approval Banner (if Supervisor)
        if (currentRole == UserRole.SUPERVISOR_QC && activeInspection?.status != "Approved") {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(QcBluePrimary))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.SupervisorAccount, contentDescription = null, tint = QcBluePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Supervisor QC Mode",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = QcBlueDark
                                )
                                Text(
                                    text = "You can edit durations & approve this inspection report.",
                                    fontSize = 11.sp,
                                    color = QcTextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Button(
                            onClick = {
                                activeInspection?.id?.let { viewModel.approveInspection(it) }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Approve", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Findings List Cards
        item {
            Text(
                text = "Findings List (${findings.size} Items)",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = QcBlueDark
                )
            )
        }

        if (findings.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Findings Yet",
                            fontWeight = FontWeight.Bold,
                            color = QcTextPrimary
                        )
                        Text(
                            text = "Please click 'Add Finding' button to start logging defect findings in the field.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = QcTextSecondary,
                                fontWeight = FontWeight.Medium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        )
                    }
                }
            }
        } else {
            items(findings) { finding ->
                FindingReviewCard(
                    finding = finding,
                    onEdit = { editingFinding = finding },
                    onDelete = { deletingFinding = finding }
                )
            }
        }
    }

    // Edit Finding Dialog
    if (editingFinding != null) {
        EditFindingDialog(
            finding = editingFinding!!,
            onDismiss = { editingFinding = null },
            onSave = { updated ->
                viewModel.updateFinding(updated)
                editingFinding = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (deletingFinding != null) {
        AlertDialog(
            onDismissRequest = { deletingFinding = null },
            title = { Text("Delete Defect Finding?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Finding on area '${deletingFinding?.damageArea}' (${deletingFinding?.durationMinutes} mins) will be permanently deleted.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        deletingFinding?.let { viewModel.deleteFinding(it) }
                        deletingFinding = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GaugeDanger)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingFinding = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FindingReviewCard(
    finding: FindingEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("finding_card_${finding.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Area & Severity Badge & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = QcBluePrimary,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = finding.damageArea,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    val (bgCol, textCol) = when (finding.severityLevel.lowercase()) {
                        "low", "ringan" -> Pair(SeverityLowBg, SeverityLowText)
                        "medium", "sedang" -> Pair(SeverityMediumBg, SeverityMediumText)
                        else -> Pair(SeverityHighBg, SeverityHighText)
                    }

                    Surface(
                        color = bgCol,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = finding.severityLevel,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = textCol,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // Edit & Delete Icons
                Row {
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

            Spacer(modifier = Modifier.height(10.dp))

            // Body: Photo & Description
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (finding.photoUri != null && File(finding.photoUri).exists()) {
                    AsyncImage(
                        model = File(finding.photoUri),
                        contentDescription = "Photo",
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, QcOutline, RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(QcSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = finding.damageDescription,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = QcBlueDark,
                            fontSize = 14.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Dimension / Boundary: ${finding.damageDimension}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = QcTextSecondary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = QcBluePrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Duration: ${finding.durationMinutes} Mins (${finding.durationMinutes / 60}h ${finding.durationMinutes % 60}m)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = QcBluePrimary
                            )
                        )
                        if (finding.isSupervisorEdited) {
                            Text(
                                text = " • SPV Edited",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFF59E0B),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditFindingDialog(
    finding: FindingEntity,
    onDismiss: () -> Unit,
    onSave: (FindingEntity) -> Unit
) {
    var description by remember { mutableStateOf(finding.damageDescription) }
    var dimension by remember { mutableStateOf(finding.damageDimension) }
    var duration by remember { mutableStateOf(finding.durationMinutes.toString()) }
    var severity by remember { mutableStateOf(finding.severityLevel) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Finding: ${finding.damageArea}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                QcInputField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Defect Description",
                    placeholder = "Enter defect description...",
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                QcInputField(
                    value = dimension,
                    onValueChange = { dimension = it },
                    label = "Dimension / Boundary",
                    placeholder = "e.g. 1.5 x 2.0 m / 3 spots",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                QcInputField(
                    value = duration,
                    onValueChange = { duration = it.filter { char -> char.isDigit() } },
                    label = "Repair Duration (Minutes)",
                    placeholder = "e.g. 45",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    text = "Severity Level:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = QcTextPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Low", "Medium", "High").forEach { lvl ->
                        val isSel = (severity.equals(lvl, ignoreCase = true) ||
                                (lvl == "Low" && severity == "Ringan") ||
                                (lvl == "Medium" && severity == "Sedang") ||
                                (lvl == "High" && severity == "Berat"))
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { severity = lvl }
                                .border(
                                    1.dp,
                                    if (isSel) QcBluePrimary else Color(0xFFE2E8F0),
                                    RoundedCornerShape(6.dp)
                                ),
                            color = if (isSel) QcBlueLight else Color.White
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = lvl,
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
        },
        confirmButton = {
            Button(
                onClick = {
                    val durMinutes = duration.toIntOrNull() ?: finding.durationMinutes
                    onSave(
                        finding.copy(
                            damageDescription = description.trim(),
                            damageDimension = dimension.trim(),
                            durationMinutes = durMinutes,
                            severityLevel = severity,
                            isSupervisorEdited = true
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = QcBluePrimary)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

