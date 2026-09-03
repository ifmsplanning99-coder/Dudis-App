package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.MasterDurationEntity
import com.example.ui.theme.*
import com.example.util.MasterTemplateHelper

/**
 * Excel Data Box Dialog for importing, exporting, and managing defect items per Damage Area.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExcelDataBoxDialog(
    initialArea: String = "Walls",
    allPresets: List<MasterDurationEntity>,
    onDismiss: () -> Unit,
    onSaveNewItem: (category: String, subItem: String, severity: String, duration: Int, serviceType: String) -> Unit,
    onImportPresets: (List<MasterDurationEntity>, replaceExisting: Boolean) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Excel Grid View, 1: Quick Add Item, 2: Import / Paste Excel
    var filterArea by remember { mutableStateOf(if (initialArea.isNotBlank()) initialArea else "All") }
    
    // Quick Add Form State
    var newCategory by remember { mutableStateOf(if (initialArea.isNotBlank() && initialArea != "All") initialArea else "Walls") }
    var newSubItem by remember { mutableStateOf("") }
    var newSeverity by remember { mutableStateOf("Medium") }
    var newDurationText by remember { mutableStateOf("45") }
    var newServiceType by remember { mutableStateOf("All") }

    // Import / Paste Box State
    var rawPasteText by remember { mutableStateOf("") }
    var replaceExistingMode by remember { mutableStateOf(false) }

    // File picker launcher for CSV/JSON files
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val content = MasterTemplateHelper.readTextFromUri(context, uri)
            if (!content.isNullOrBlank()) {
                rawPasteText = content
                Toast.makeText(context, "File Excel/CSV berhasil dimuat ke Excel Box!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Gagal membaca isi file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val availableAreas = remember(allPresets) {
        val list = mutableListOf("All", "Walls", "Flooring", "Roofing", "Ceiling", "Doors", "Windows", "Sanitary", "Electrical", "Mechanical", "Landscaping", "Others")
        allPresets.forEach { if (!list.contains(it.category)) list.add(it.category) }
        list
    }

    val filteredItems = remember(allPresets, filterArea) {
        if (filterArea == "All") allPresets else allPresets.filter { it.category.equals(filterArea, ignoreCase = true) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(16.dp)),
            color = Color.White,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = QcBlueLight,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.TableChart, contentDescription = null, tint = QcBluePrimary)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Excel Data Box - Damage Area & Items",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = QcBlueDark
                                )
                            )
                            Text(
                                text = "Kelola, Tambah, Import & Export Item Defect per Area",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = QcTextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = QcTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Navigation Tabs
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFFF1F5F9),
                    contentColor = QcBluePrimary,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TableView, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tabel Data (${filteredItems.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("+ Tambah Item", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Impor / Ekspor", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Content
                when (selectedTab) {
                    0 -> {
                        // TAB 0: EXCEL GRID VIEW & ACTIONS
                        Column(modifier = Modifier.weight(1f)) {
                            // Filter by Area chips
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Filter Area:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = QcTextSecondary))
                                availableAreas.forEach { area ->
                                    val isSel = (filterArea == area)
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { filterArea = area },
                                        label = { Text(area, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.SemiBold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = QcBluePrimary,
                                            selectedLabelColor = Color.White,
                                            containerColor = Color(0xFFF8FAFC),
                                            labelColor = QcTextPrimary
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Excel Grid Table Container
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp)),
                                color = Color.White,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                if (filteredItems.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.Inbox, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("Belum ada item untuk area $filterArea", color = QcTextSecondary, fontWeight = FontWeight.Medium)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            OutlinedButton(onClick = {
                                                newCategory = if (filterArea != "All") filterArea else "Walls"
                                                selectedTab = 1
                                            }) {
                                                Text("+ Tambah Item di $newCategory")
                                            }
                                        }
                                    }
                                } else {
                                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                                        // Excel Header Row
                                        item {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFFE2E8F0))
                                                    .padding(vertical = 8.dp, horizontal = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("#", modifier = Modifier.width(28.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = QcBlueDark)
                                                Text("DAMAGE AREA", modifier = Modifier.width(90.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = QcBlueDark)
                                                Text("NAMA DEFECT / ITEM", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = QcBlueDark)
                                                Text("SEV", modifier = Modifier.width(45.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = QcBlueDark)
                                                Text("DURASI", modifier = Modifier.width(55.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = QcBlueDark)
                                                Text("SRV", modifier = Modifier.width(50.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = QcBlueDark)
                                            }
                                            HorizontalDivider(color = Color(0xFFCBD5E1))
                                        }

                                        // Excel Data Rows
                                        itemsIndexed(filteredItems) { idx, item ->
                                            val rowBg = if (idx % 2 == 0) Color.White else Color(0xFFF8FAFC)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(rowBg)
                                                    .padding(vertical = 8.dp, horizontal = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("${idx + 1}", modifier = Modifier.width(28.dp), fontSize = 11.sp, color = QcTextSecondary, fontWeight = FontWeight.Medium)
                                                Text(item.category, modifier = Modifier.width(90.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = QcBlueDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text(item.subItem, modifier = Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = QcTextPrimary)
                                                
                                                // Severity badge
                                                Surface(
                                                    color = when (item.severityLevel.lowercase()) {
                                                        "high" -> SeverityHighBg
                                                        "low" -> SeverityLowBg
                                                        else -> SeverityMediumBg
                                                    },
                                                    shape = RoundedCornerShape(4.dp),
                                                    modifier = Modifier.width(45.dp)
                                                ) {
                                                    Text(
                                                        text = item.severityLevel.take(3),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = when (item.severityLevel.lowercase()) {
                                                            "high" -> SeverityHighText
                                                            "low" -> SeverityLowText
                                                            else -> SeverityMediumText
                                                        },
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("${item.defaultDurationMinutes}m", modifier = Modifier.width(55.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                                Text(item.serviceType, modifier = Modifier.width(50.dp), fontSize = 10.sp, color = QcTextSecondary, maxLines = 1)
                                            }
                                            HorizontalDivider(color = Color(0xFFF1F5F9))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        newCategory = if (filterArea != "All") filterArea else "Walls"
                                        selectedTab = 1
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = QcBluePrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Tambah Item", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        // Copy Excel TSV format directly to clipboard
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val csvContent = MasterTemplateHelper.exportToCsv(filteredItems)
                                        val clip = ClipData.newPlainText("Excel QC Items", csvContent)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Tabel (${filteredItems.size} item) disalin ke Clipboard! Siap di-paste ke Excel.", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = QcBlueDark),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Salin ke Excel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    1 -> {
                        // TAB 1: QUICK ADD ITEM TO SPECIFIC DAMAGE AREA
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Tambah Item Defect Baru untuk Damage Area",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = QcBlueDark)
                            )

                            // Area selector
                            Text("1. Pilih Damage Area:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = QcTextSecondary))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                availableAreas.filter { it != "All" }.forEach { area ->
                                    val isSel = (newCategory == area)
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { newCategory = area },
                                        label = { Text(area, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = QcBluePrimary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }

                            // Defect item name
                            QcInputField(
                                value = newSubItem,
                                onValueChange = { newSubItem = it },
                                label = "2. Nama Item / Defect pada $newCategory",
                                placeholder = "Contoh: Retak rambut plesteran dinding / Cat mengelupas",
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            // Severity
                            Text("3. Tingkat Severity:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = QcTextSecondary))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Low" to SeverityLowBg, "Medium" to SeverityMediumBg, "High" to SeverityHighBg).forEach { (sev, bg) ->
                                    val isSel = (newSeverity == sev)
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(
                                                width = if (isSel) 2.dp else 1.dp,
                                                color = if (isSel) QcBluePrimary else Color(0xFFCBD5E1),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { newSeverity = sev }
                                            .padding(vertical = 8.dp),
                                        color = if (isSel) bg else Color(0xFFF8FAFC)
                                    ) {
                                        Text(
                                            text = sev,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp,
                                            color = if (isSel) Color.Black else Color.Gray,
                                            modifier = Modifier.wrapContentWidth(Alignment.CenterHorizontally)
                                        )
                                    }
                                }
                            }

                            // Duration & Service Type
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                QcInputField(
                                    value = newDurationText,
                                    onValueChange = { newDurationText = it.filter { c -> c.isDigit() } },
                                    label = "Durasi (Menit)",
                                    placeholder = "45",
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )

                                QcInputField(
                                    value = newServiceType,
                                    onValueChange = { newServiceType = it },
                                    label = "Service Type",
                                    placeholder = "1 PM / 3 PM / CM",
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Button(
                                onClick = {
                                    if (newSubItem.isBlank()) {
                                        Toast.makeText(context, "Nama item defect tidak boleh kosong", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    val dur = newDurationText.toIntOrNull() ?: 45
                                    onSaveNewItem(newCategory, newSubItem.trim(), newSeverity, dur, newServiceType.trim().ifBlank { "All" })
                                    Toast.makeText(context, "Item berhasil ditambahkan ke $newCategory!", Toast.LENGTH_SHORT).show()
                                    newSubItem = ""
                                    selectedTab = 0
                                    filterArea = newCategory
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = QcBluePrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Simpan Item ke $newCategory", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    2 -> {
                        // TAB 2: EXCEL IMPORT / EXPORT BOX
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Excel Box - Import & Export Master Template",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = QcBlueDark)
                            )

                            // Quick File Actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { filePickerLauncher.launch("*/*") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = QcBlueDark)
                                ) {
                                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Buka File CSV/Excel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { MasterTemplateHelper.shareTemplateFile(context, allPresets, asJson = false) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Export File CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Text(
                                text = "Atau Tempel / Paste data dari Excel / Google Sheets di bawah:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = QcTextSecondary)
                            )

                            QcInputField(
                                value = rawPasteText,
                                onValueChange = { rawPasteText = it },
                                placeholder = "Format kolom Excel/CSV:\nCategory,Defect Item,Severity,Duration,ServiceType\n\nContoh:\nWalls,Plesteran retak rambut,Low,30,1 PM\nWalls,Cat terkelupas,Medium,45,3 PM\nFlooring,Nat keramik lepas,Low,30,1 PM",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                minLines = 5
                            )

                            // Live parsing info
                            val parsedCount = remember(rawPasteText) {
                                if (rawPasteText.isBlank()) 0
                                else MasterTemplateHelper.parseMasterDurations(rawPasteText).size
                            }

                            if (rawPasteText.isNotBlank()) {
                                Surface(
                                    color = if (parsedCount > 0) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            if (parsedCount > 0) Icons.Default.CheckCircle else Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = if (parsedCount > 0) Color(0xFF059669) else Color(0xFFDC2626),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (parsedCount > 0) "$parsedCount item terdeteksi valid dari teks Excel!" else "Belum ada item yang terdeteksi valid dari teks.",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (parsedCount > 0) Color(0xFF065F46) else Color(0xFF991B1B)
                                        )
                                    }
                                }
                            }

                            // Mode replace checkbox
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { replaceExistingMode = !replaceExistingMode }
                            ) {
                                Checkbox(
                                    checked = replaceExistingMode,
                                    onCheckedChange = { replaceExistingMode = it }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Ganti seluruh data saat ini (Overwrite mode)",
                                    fontSize = 11.sp,
                                    color = QcTextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Process Import Button
                            Button(
                                onClick = {
                                    if (rawPasteText.isBlank()) {
                                        Toast.makeText(context, "Silakan tempel data Excel atau pilih file terlebih dahulu", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    val parsed = MasterTemplateHelper.parseMasterDurations(rawPasteText)
                                    if (parsed.isEmpty()) {
                                        Toast.makeText(context, "Format data tidak valid. Pastikan ada minimal 2 kolom (Category & Defect Item)", Toast.LENGTH_LONG).show()
                                        return@Button
                                    }
                                    onImportPresets(parsed, replaceExistingMode)
                                    Toast.makeText(context, "Berhasil mengimpor ${parsed.size} item ke database!", Toast.LENGTH_SHORT).show()
                                    rawPasteText = ""
                                    selectedTab = 0
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = QcBluePrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Terapkan & Simpan ($parsedCount Item) ke Input", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
