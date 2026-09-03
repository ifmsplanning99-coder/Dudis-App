package com.example.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.Deck
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.HolidayVillage
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.data.model.BuildingType
import com.example.data.model.InspectionEntity
import com.example.ui.components.QcInputField
import com.example.ui.theme.QcBlueDark
import com.example.ui.theme.QcBlueLight
import com.example.ui.theme.QcBluePrimary
import com.example.ui.theme.QcOutline
import com.example.ui.theme.QcTextPrimary
import com.example.ui.theme.QcTextSecondary
import com.example.ui.viewmodel.QcViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun Phase1LocationScreen(
    viewModel: QcViewModel,
    onNavigateToFindings: () -> Unit
) {
    val context = LocalContext.current
    val selectedBuildingType by viewModel.buildingType.collectAsState()
    val selectedServiceType by viewModel.serviceType.collectAsState()
    val cluster by viewModel.clusterName.collectAsState()
    val houseNum by viewModel.houseNumber.collectAsState()
    val camp by viewModel.campName.collectAsState()
    val facility by viewModel.publicFacilityName.collectAsState()
    val office by viewModel.officeBuildingName.collectAsState()
    val date by viewModel.inspectionDate.collectAsState()
    val inspector by viewModel.inspectorName.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Step Banner
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
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "1",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "PHASE 1 : LOCATION & SERVICE IDENTIFICATION",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        )
                        Text(
                            text = "Select building type, maintenance service cycle & location details",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }

        // Section 1: Building Type
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Building Type",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = QcBlueDark
                        )
                    )
                    Text(
                        text = "Select the building category to inspect:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = QcTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val buildingOptions = listOf(
                        Triple(BuildingType.HOUSE, "House", Icons.Default.Home),
                        Triple(BuildingType.CAMP, "Camp", Icons.Default.HolidayVillage),
                        Triple(BuildingType.PUBLIC_FACILITY, "Public Facility", Icons.Default.Deck),
                        Triple(BuildingType.OFFICE, "Office", Icons.Default.CorporateFare)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        buildingOptions.chunked(2).forEach { rowList ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowList.forEach { (type, label, icon) ->
                                    val isSelected = (selectedBuildingType == type)
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { viewModel.buildingType.value = type }
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) QcBluePrimary else QcOutline,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .testTag("building_type_${type.name.lowercase()}"),
                                        color = if (isSelected) QcBlueLight else Color.White
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = label,
                                                tint = if (isSelected) QcBluePrimary else Color(0xFF475569),
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                    color = if (isSelected) QcBlueDark else QcTextPrimary,
                                                    fontSize = 13.sp
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

        // Section 2: Maintenance Service (1 PM, 3 PM, 6 PM, Deep check, CM)
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
                            text = "2. Maintenance Service",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = QcBlueDark
                            )
                        )
                        Surface(
                            color = QcBlueLight,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Selected: $selectedServiceType",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = QcBluePrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Text(
                        text = "Choose service type (1 PM, 3 PM, 6 PM, Deep check, CM):",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = QcTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val serviceOptions = listOf(
                        Pair("1 PM", "1 PM (Monthly Routine)"),
                        Pair("3 PM", "3 PM (Quarterly PM)"),
                        Pair("6 PM", "6 PM (Semi-Annual PM)"),
                        Pair("Deep check", "Deep Check (Full Audit)"),
                        Pair("CM", "CM (Corrective Repair)")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        serviceOptions.forEach { (code, title) ->
                            val isSelected = (selectedServiceType == code)
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.serviceType.value = code }
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) QcBluePrimary else Color(0xFFE2E8F0),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .testTag("service_chip_$code"),
                                color = if (isSelected) QcBlueLight else Color(0xFFFAFAFA)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSelected) QcBluePrimary else Color(0xFFCBD5E1)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (code.length <= 4) code else "DC",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = if (isSelected) Color.White else QcBlueDark,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp
                                                )
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = title,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                    color = if (isSelected) QcBlueDark else QcTextPrimary,
                                                    fontSize = 13.sp
                                                )
                                            )
                                            Text(
                                                text = when (code) {
                                                    "1 PM" -> "Monthly preventive maintenance checklist"
                                                    "3 PM" -> "Quarterly deep mechanical & architectural inspection"
                                                    "6 PM" -> "Semi-annual structural, roof & major overhaul"
                                                    "Deep check" -> "Comprehensive quality assurance & audit cycle"
                                                    "CM" -> "Corrective maintenance & emergency defect repair"
                                                    else -> "Standard maintenance"
                                                },
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = QcTextSecondary,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = QcBluePrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Dynamic Location Form
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. Building Location (${selectedBuildingType.displayName})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = QcBlueDark
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    when (selectedBuildingType) {
                        BuildingType.HOUSE -> {
                            QcInputField(
                                value = cluster,
                                onValueChange = { viewModel.clusterName.value = it },
                                label = "Cluster Name",
                                placeholder = "e.g. Emerald Lake Cluster",
                                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = QcBluePrimary) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_cluster_name"),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            QcInputField(
                                value = houseNum,
                                onValueChange = { viewModel.houseNumber.value = it },
                                label = "House / Unit Number",
                                placeholder = "e.g. Block B3 No. 12",
                                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = QcBluePrimary) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_house_number"),
                                singleLine = true
                            )
                        }

                        BuildingType.CAMP -> {
                            QcInputField(
                                value = camp,
                                onValueChange = { viewModel.campName.value = it },
                                label = "Camp Name",
                                placeholder = "e.g. North Sector Construction Workers Camp",
                                leadingIcon = { Icon(Icons.Default.HolidayVillage, contentDescription = null, tint = QcBluePrimary) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_camp_name"),
                                singleLine = true
                            )
                        }

                        BuildingType.PUBLIC_FACILITY -> {
                            QcInputField(
                                value = facility,
                                onValueChange = { viewModel.publicFacilityName.value = it },
                                label = "Facility Name",
                                placeholder = "e.g. Clubhouse, Community Hall, Security Post",
                                leadingIcon = { Icon(Icons.Default.Deck, contentDescription = null, tint = QcBluePrimary) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_facility_name"),
                                singleLine = true
                            )
                        }

                        BuildingType.OFFICE -> {
                            QcInputField(
                                value = office,
                                onValueChange = { viewModel.officeBuildingName.value = it },
                                label = "Building / Office Name",
                                placeholder = "e.g. Marketing Gallery Fl 2, Site Office",
                                leadingIcon = { Icon(Icons.Default.CorporateFare, contentDescription = null, tint = QcBluePrimary) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_office_name"),
                                singleLine = true
                            )
                        }
                    }
                }
            }
        }

        // Section 4: Inspection Date Selection (with Day, Month, Year menus & Picker Dialog)
        item {
            InspectionDatePickerCard(
                currentDateStr = date,
                onDateSelected = { newDate ->
                    viewModel.inspectionDate.value = newDate
                }
            )
        }

        // Section 5: Inspector Name
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "5. Inspector QC Name",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = QcBlueDark
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    QcInputField(
                        value = inspector,
                        onValueChange = { viewModel.inspectorName.value = it },
                        label = "Inspector QC Full Name",
                        placeholder = "e.g. Ahmad Pratama",
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = QcBluePrimary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_inspector_name"),
                        singleLine = true
                    )
                }
            }
        }

        // Large CTA Button: "Continue to Findings"
        item {
            Button(
                onClick = {
                    viewModel.startNewInspection {
                        onNavigateToFindings()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("btn_proceed_to_findings"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = QcBluePrimary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
            ) {
                Text(
                    text = "Continue to Findings",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next",
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * Inspection Date Selection Component with interactive Day, Month, Year dropdown menus,
 * quick date chips, and native DatePickerDialog integration.
 */
@Composable
fun InspectionDatePickerCard(
    currentDateStr: String,
    onDateSelected: (String) -> Unit
) {
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    // Parse date components safely
    val parsedDate = remember(currentDateStr) {
        try {
            sdf.parse(currentDateStr) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }

    val cal = remember(parsedDate) {
        Calendar.getInstance().apply { time = parsedDate }
    }

    val currentYear = cal.get(Calendar.YEAR)
    val currentMonth = cal.get(Calendar.MONTH) + 1 // 1-12
    val currentDay = cal.get(Calendar.DAY_OF_MONTH) // 1-31

    // Dropdown visibility states
    var dayMenuExpanded by remember { mutableStateOf(false) }
    var monthMenuExpanded by remember { mutableStateOf(false) }
    var yearMenuExpanded by remember { mutableStateOf(false) }

    val monthNames = listOf(
        "01 - January", "02 - February", "03 - March", "04 - April",
        "05 - May", "06 - June", "07 - July", "08 - August",
        "09 - September", "10 - October", "11 - November", "12 - December"
    )

    val yearList = (2024..2030).toList()

    fun updateDate(y: Int, m: Int, d: Int) {
        val newCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, y)
            set(Calendar.MONTH, m - 1)
            // Ensure day doesn't exceed maximum days in selected month
            val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
            set(Calendar.DAY_OF_MONTH, d.coerceAtMost(maxDay))
        }
        onDateSelected(sdf.format(newCal.time))
    }

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
                    text = "3. Inspection Date & Schedule",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = QcBlueDark
                    )
                )

                Surface(
                    color = QcBlueLight,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = currentDateStr,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = QcBluePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle: Pilihan Menu Tanggal, Bulan, Tahun
            Text(
                text = "PILIH TANGGAL, BULAN & TAHUN (SELECT DATE):",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = QcTextSecondary,
                    letterSpacing = 0.5.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Three Selector Dropdown Boxes: Day, Month, Year
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Day / Tanggal Selector (1..31)
                Box(modifier = Modifier.weight(1f)) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { dayMenuExpanded = true }
                            .border(1.dp, QcOutline, RoundedCornerShape(10.dp)),
                        color = Color(0xFFFAFAFA)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                            Text(
                                text = "Tanggal (Day)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = QcTextSecondary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = String.format(Locale.getDefault(), "%02d", currentDay),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = QcBlueDark
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Day",
                                    tint = QcBluePrimary
                                )
                            }
                        }
                    }

                    DropdownMenu(
                        expanded = dayMenuExpanded,
                        onDismissRequest = { dayMenuExpanded = false }
                    ) {
                        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                        (1..maxDay).forEach { d ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(String.format(Locale.getDefault(), "%02d", d))
                                        if (d == currentDay) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = QcBluePrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    dayMenuExpanded = false
                                    updateDate(currentYear, currentMonth, d)
                                }
                            )
                        }
                    }
                }

                // 2. Month / Bulan Selector (1..12)
                Box(modifier = Modifier.weight(1.5f)) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { monthMenuExpanded = true }
                            .border(1.dp, QcOutline, RoundedCornerShape(10.dp)),
                        color = Color(0xFFFAFAFA)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                            Text(
                                text = "Bulan (Month)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = QcTextSecondary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = monthNames.getOrElse(currentMonth - 1) { "Month" }.substringAfter("- "),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = QcBlueDark,
                                        fontSize = 14.sp
                                    ),
                                    maxLines = 1
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Month",
                                    tint = QcBluePrimary
                                )
                            }
                        }
                    }

                    DropdownMenu(
                        expanded = monthMenuExpanded,
                        onDismissRequest = { monthMenuExpanded = false }
                    ) {
                        monthNames.forEachIndexed { idx, mName ->
                            val mNum = idx + 1
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(mName)
                                        if (mNum == currentMonth) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = QcBluePrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    monthMenuExpanded = false
                                    updateDate(currentYear, mNum, currentDay)
                                }
                            )
                        }
                    }
                }

                // 3. Year / Tahun Selector (2024..2030)
                Box(modifier = Modifier.weight(1f)) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { yearMenuExpanded = true }
                            .border(1.dp, QcOutline, RoundedCornerShape(10.dp)),
                        color = Color(0xFFFAFAFA)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                            Text(
                                text = "Tahun (Year)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = QcTextSecondary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$currentYear",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = QcBlueDark
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Year",
                                    tint = QcBluePrimary
                                )
                            }
                        }
                    }

                    DropdownMenu(
                        expanded = yearMenuExpanded,
                        onDismissRequest = { yearMenuExpanded = false }
                    ) {
                        yearList.forEach { yr ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("$yr")
                                        if (yr == currentYear) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = QcBluePrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    yearMenuExpanded = false
                                    updateDate(yr, currentMonth, currentDay)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

