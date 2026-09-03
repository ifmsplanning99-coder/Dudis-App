package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.QcBlueDark
import com.example.ui.theme.QcBlueLight
import com.example.ui.theme.QcBluePrimary
import com.example.ui.theme.QcOutline
import com.example.ui.theme.QcSurfaceVariant
import com.example.ui.theme.QcTextPrimary
import com.example.ui.theme.QcTextSecondary
import com.example.ui.viewmodel.QcViewModel
import com.example.util.ExcelExportHelper

@Composable
fun ExportReportScreen(
    viewModel: QcViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val activeInspection by viewModel.activeInspection.collectAsState()
    val findings by viewModel.activeFindings.collectAsState()
    val totalDuration by viewModel.activeTotalDuration.collectAsState()

    val totalHours = totalDuration / 60
    val remainingMins = totalDuration % 60

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Controls & Export Actions
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
                            text = "EXCEL REPORT & EXPORT",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = QcBlueDark
                            )
                        )
                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "A4 Landscape Ready",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF15803D),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Main Export to Excel Button
                        Button(
                            onClick = {
                                activeInspection?.let { insp ->
                                    ExcelExportHelper.shareExportFile(context, insp, findings)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_export_excel_action"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857))
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export & Share", fontWeight = FontWeight.Bold)
                        }

                        // Copy Table to Clipboard
                        OutlinedButton(
                            onClick = {
                                activeInspection?.let { insp ->
                                    val csv = ExcelExportHelper.generateCsvContent(insp, findings)
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("QC Report Table", csv)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Table data copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_copy_excel_table"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = QcBlueDark)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Table", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Live Excel Spreadsheet Preview Container
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "EXCEL SPREADSHEET PREVIEW",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = QcTextSecondary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Company & Report Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(QcBlueDark)
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Apartment,
                                        contentDescription = null,
                                        tint = QcBlueDark,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "IFMS PROPERTY INSPECTION",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    )
                                    Text(
                                        text = "QUALITY CONTROL (QC) FINDINGS REPORT",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF90CAF9),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Inspection Meta Grid
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, QcOutline, RoundedCornerShape(8.dp)),
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Inspection Date: ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = QcBlueDark, modifier = Modifier.width(130.dp))
                                Text(activeInspection?.inspectionDate ?: "-", fontSize = 12.sp, color = QcTextPrimary)
                            }
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Inspector Name: ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = QcBlueDark, modifier = Modifier.width(130.dp))
                                Text(activeInspection?.inspectorName ?: "-", fontSize = 12.sp, color = QcTextPrimary)
                            }
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Building Type: ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = QcBlueDark, modifier = Modifier.width(130.dp))
                                Text(activeInspection?.buildingType ?: "-", fontSize = 12.sp, color = QcTextPrimary)
                            }
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Location: ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = QcBlueDark, modifier = Modifier.width(130.dp))
                                Text(activeInspection?.locationName ?: "-", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = QcTextPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Horizontal scrollable Excel table preview
                    val hScroll = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(hScroll)
                            .border(1.dp, Color(0xFFCBD5E1))
                    ) {
                        // Table Header Row (Dark Blue background, White text, Calibri styling)
                        Row(
                            modifier = Modifier
                                .background(QcBlueDark)
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ExcelHeaderCell("No", 40.dp)
                            ExcelHeaderCell("Photo", 60.dp)
                            ExcelHeaderCell("Defect Area", 120.dp)
                            ExcelHeaderCell("Defect Description", 200.dp)
                            ExcelHeaderCell("Dimension / Boundary", 140.dp)
                            ExcelHeaderCell("Level", 80.dp)
                            ExcelHeaderCell("Duration (Min)", 90.dp)
                        }

                        // Table Data Rows
                        findings.forEachIndexed { index, finding ->
                            val isEven = (index % 2 == 0)
                            Row(
                                modifier = Modifier
                                    .background(if (isEven) Color.White else Color(0xFFF8FAFC))
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ExcelDataCell("${index + 1}", 40.dp, Alignment.Center)
                                ExcelDataCell(if (finding.photoUri != null) "📷 Yes" else "-", 60.dp, Alignment.Center)
                                ExcelDataCell(finding.damageArea, 120.dp, Alignment.CenterStart, fontWeight = FontWeight.SemiBold, color = QcBlueDark)
                                ExcelDataCell(finding.damageDescription, 200.dp, Alignment.CenterStart)
                                ExcelDataCell(finding.damageDimension, 140.dp, Alignment.CenterStart)
                                ExcelDataCell(finding.severityLevel, 80.dp, Alignment.Center)
                                ExcelDataCell("${finding.durationMinutes}", 90.dp, Alignment.CenterEnd, fontWeight = FontWeight.Bold, color = QcBluePrimary)
                            }
                        }

                        // Table Summary Row
                        Row(
                            modifier = Modifier
                                .background(Color(0xFFE2E8F0))
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL FINDINGS: ${findings.size} POINTS",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = QcBlueDark
                                ),
                                modifier = Modifier
                                    .width(420.dp)
                                    .padding(horizontal = 8.dp)
                            )
                            Text(
                                text = "TOTAL DURATION: $totalDuration Minutes",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = QcBlueDark
                                ),
                                modifier = Modifier
                                    .width(310.dp)
                                    .padding(horizontal = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Excel Summary Box
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFE3F2FD),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL ESTIMATED DURATION",
                                fontWeight = FontWeight.Bold,
                                color = QcBlueDark,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "$totalDuration Minutes ($totalHours hrs $remainingMins mins)",
                                fontWeight = FontWeight.Bold,
                                color = QcBluePrimary,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Signatures Section (Inspector QC & Supervisor QC)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Inspector Signature
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Prepared By,", fontSize = 11.sp, color = QcTextSecondary, fontWeight = FontWeight.Medium)
                            Text("QC Inspector", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = QcBlueDark)
                            Spacer(modifier = Modifier.height(40.dp))
                            Text(
                                text = "(${activeInspection?.inspectorName ?: "Inspector"})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = QcTextPrimary
                            )
                            Text("Field Quality Control", fontSize = 10.sp, color = QcTextSecondary, fontWeight = FontWeight.Medium)
                        }

                        // Supervisor Signature
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Approved By,", fontSize = 11.sp, color = QcTextSecondary, fontWeight = FontWeight.Medium)
                            Text("QC Supervisor", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = QcBlueDark)
                            Spacer(modifier = Modifier.height(40.dp))
                            Text(
                                text = "(${activeInspection?.supervisorName ?: "Supervisor"})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = QcTextPrimary
                            )
                            Text("QC Site Supervisor", fontSize = 10.sp, color = QcTextSecondary, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExcelHeaderCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
fun ExcelDataCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    alignment: Alignment = Alignment.CenterStart,
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color = Color(0xFF0F172A)
) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 6.dp),
        contentAlignment = alignment
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                color = color,
                fontWeight = fontWeight,
                fontSize = 11.sp
            ),
            maxLines = 2
        )
    }
}
