package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppTopBar
import com.example.ui.theme.QcBlueDark
import com.example.ui.theme.QcBlueLight
import com.example.ui.theme.QcBluePrimary
import com.example.ui.viewmodel.QcViewModel

enum class AppDestination(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    PHASE1_LOCATION("Location", Icons.Default.LocationOn),
    PHASE2_FINDING("Input", Icons.Default.NoteAdd),
    REVIEW("Review", Icons.Default.FormatListNumbered),
    DASHBOARD("Dashboard", Icons.Default.Assessment),
    SAVED_INSPECTIONS("Saved", Icons.Default.History),
    EXPORT("Excel", Icons.Default.FileDownload),
    MASTER("Master", Icons.Default.AdminPanelSettings)
}

@Composable
fun MainAppScreen(
    viewModel: QcViewModel
) {
    var currentDestination by remember { mutableStateOf(AppDestination.PHASE1_LOCATION) }
    val currentRole by viewModel.currentRole.collectAsState()
    val findings by viewModel.activeFindings.collectAsState()
    val totalDuration by viewModel.activeTotalDuration.collectAsState()
    val activeInspection by viewModel.activeInspection.collectAsState()
    val allInspections by viewModel.allInspections.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                currentRole = currentRole,
                onRoleSelected = { viewModel.switchRole(it) },
                title = "IFMS QC Inspection",
                subtitle = activeInspection?.locationName
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                val destinations = if (currentRole == com.example.data.model.UserRole.ADMINISTRATOR) {
                    listOf(
                        AppDestination.PHASE1_LOCATION,
                        AppDestination.PHASE2_FINDING,
                        AppDestination.REVIEW,
                        AppDestination.DASHBOARD,
                        AppDestination.SAVED_INSPECTIONS,
                        AppDestination.EXPORT,
                        AppDestination.MASTER
                    )
                } else {
                    listOf(
                        AppDestination.PHASE1_LOCATION,
                        AppDestination.PHASE2_FINDING,
                        AppDestination.REVIEW,
                        AppDestination.DASHBOARD,
                        AppDestination.SAVED_INSPECTIONS,
                        AppDestination.EXPORT
                    )
                }

                destinations.forEach { destination ->
                    val isSelected = (currentDestination == destination)
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = destination },
                        icon = {
                            if (destination == AppDestination.REVIEW && findings.isNotEmpty()) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = QcBluePrimary) {
                                            Text("${findings.size}", color = Color.White, fontSize = 10.sp)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = destination.label,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            } else if (destination == AppDestination.SAVED_INSPECTIONS && allInspections.isNotEmpty()) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = Color(0xFF0284C7)) {
                                            Text("${allInspections.size}", color = Color.White, fontSize = 9.sp)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = destination.label,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.label,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = destination.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = QcBluePrimary,
                            selectedTextColor = QcBluePrimary,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = QcBlueLight
                        ),
                        modifier = Modifier.testTag("nav_item_${destination.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                AppDestination.PHASE1_LOCATION -> {
                    Phase1LocationScreen(
                        viewModel = viewModel,
                        onNavigateToFindings = { currentDestination = AppDestination.PHASE2_FINDING }
                    )
                }

                AppDestination.PHASE2_FINDING -> {
                    Phase2FindingInputScreen(
                        viewModel = viewModel,
                        onNavigateToReview = { currentDestination = AppDestination.REVIEW },
                        onNavigateToLocation = { currentDestination = AppDestination.PHASE1_LOCATION }
                    )
                }

                AppDestination.REVIEW -> {
                    ReviewFindingsScreen(
                        viewModel = viewModel,
                        onNavigateToInputFinding = { currentDestination = AppDestination.PHASE2_FINDING },
                        onNavigateToDashboard = { currentDestination = AppDestination.DASHBOARD },
                        onNavigateToExport = { currentDestination = AppDestination.EXPORT }
                    )
                }

                AppDestination.DASHBOARD -> {
                    DashboardSummaryScreen(
                        viewModel = viewModel,
                        onNavigateToReview = { currentDestination = AppDestination.REVIEW },
                        onNavigateToInput = { currentDestination = AppDestination.PHASE2_FINDING },
                        onNavigateToExport = { currentDestination = AppDestination.EXPORT },
                        onNavigateToLocation = { currentDestination = AppDestination.PHASE1_LOCATION }
                    )
                }

                AppDestination.SAVED_INSPECTIONS -> {
                    SavedInspectionsScreen(
                        viewModel = viewModel,
                        onOpenInspection = { currentDestination = AppDestination.PHASE2_FINDING },
                        onReviewInspection = { currentDestination = AppDestination.REVIEW },
                        onStartNewInspection = { currentDestination = AppDestination.PHASE1_LOCATION },
                        onExportInspection = { currentDestination = AppDestination.EXPORT }
                    )
                }

                AppDestination.EXPORT -> {
                    ExportReportScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentDestination = AppDestination.REVIEW }
                    )
                }

                AppDestination.MASTER -> {
                    MasterDataScreen(viewModel = viewModel)
                }
            }
        }
    }
}

