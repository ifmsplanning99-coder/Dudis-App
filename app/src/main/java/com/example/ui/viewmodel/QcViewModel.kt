package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.BuildingEntity
import com.example.data.model.BuildingType
import com.example.data.model.FindingEntity
import com.example.data.model.InspectionEntity
import com.example.data.model.MasterDurationEntity
import com.example.data.model.SeverityLevel
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.repository.QcRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QcViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: QcRepository

    val MAX_CAPACITY_MINUTES = 840 // 14 hours

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = QcRepository(database.qcDao())
    }

    // Role state
    private val _currentRole = MutableStateFlow(UserRole.INSPECTOR_QC)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    fun switchRole(role: UserRole) {
        _currentRole.value = role
    }

    // Active inspection ID
    private val _activeInspectionId = MutableStateFlow<Long?>(null)
    val activeInspectionId: StateFlow<Long?> = _activeInspectionId.asStateFlow()

    val allInspections: StateFlow<List<InspectionEntity>> = repository.allInspections
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMasterDurations: StateFlow<List<MasterDurationEntity>> = repository.allMasterDurations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBuildings: StateFlow<List<BuildingEntity>> = repository.allBuildings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeInspection: StateFlow<InspectionEntity?> = _activeInspectionId.flatMapLatest { id ->
        if (id != null) repository.getInspectionByIdFlow(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeFindings: StateFlow<List<FindingEntity>> = _activeInspectionId.flatMapLatest { id ->
        if (id != null) repository.getFindingsForInspectionFlow(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeTotalDuration: StateFlow<Int> = _activeInspectionId.flatMapLatest { id ->
        if (id != null) repository.getTotalDurationFlow(id) else flowOf(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val isCapacityReached: StateFlow<Boolean> = activeTotalDuration.combine(flowOf(MAX_CAPACITY_MINUTES)) { current, max ->
        current >= max
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // ==========================================
    // FASE 1: Location State
    // ==========================================
    val buildingType = MutableStateFlow(BuildingType.HOUSE)
    val serviceType = MutableStateFlow("1 PM") // "1 PM", "3 PM", "6 PM", "Deep check", "CM"
    val clusterName = MutableStateFlow("Emerald Lake Cluster")
    val houseNumber = MutableStateFlow("Block B3 No. 12")
    val campName = MutableStateFlow("Main Sector Contractor Camp")
    val publicFacilityName = MutableStateFlow("Clubhouse & Swimming Pool")
    val officeBuildingName = MutableStateFlow("Marketing Gallery Building A")
    val inspectionDate = MutableStateFlow(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    )
    val inspectorName = MutableStateFlow("Ahmad Pratama")

    fun selectInspection(inspection: InspectionEntity) {
        _activeInspectionId.value = inspection.id
        // Populate Phase 1 fields for editing if needed
        val bType = BuildingType.values().firstOrNull { it.displayName == inspection.buildingType } ?: BuildingType.HOUSE
        buildingType.value = bType
        serviceType.value = inspection.serviceType
        inspectionDate.value = inspection.inspectionDate
        inspectorName.value = inspection.inspectorName
    }

    fun startNewInspection(onComplete: (Long) -> Unit) {
        viewModelScope.launch {
            val location = when (buildingType.value) {
                BuildingType.HOUSE -> {
                    val cluster = clusterName.value.trim()
                    val unit = houseNumber.value.trim()
                    if (unit.isNotEmpty()) "$cluster - $unit" else cluster
                }
                BuildingType.CAMP -> campName.value.trim()
                BuildingType.PUBLIC_FACILITY -> publicFacilityName.value.trim()
                BuildingType.OFFICE -> officeBuildingName.value.trim()
            }

            val newInspection = InspectionEntity(
                buildingType = buildingType.value.displayName,
                locationName = if (location.isNotEmpty()) location else "Unspecified Location",
                serviceType = serviceType.value,
                inspectionDate = inspectionDate.value,
                inspectorName = if (inspectorName.value.isNotBlank()) inspectorName.value else "Inspector QC",
                status = "Draft",
                totalDurationMinutes = 0
            )

            val id = repository.insertInspection(newInspection)
            _activeInspectionId.value = id
            onComplete(id)
        }
    }

    // ==========================================
    // FASE 2: Finding Input State
    // ==========================================
    val findingPhotoPath = MutableStateFlow<String?>(null)
    val damageArea = MutableStateFlow("Walls")
    val damageDescription = MutableStateFlow("")
    val damageDimension = MutableStateFlow("")
    val severityLevel = MutableStateFlow(SeverityLevel.LOW)
    val estimatedDuration = MutableStateFlow(30)
    val selectedSubItemPreset = MutableStateFlow("Hairline crack on wall paint")
    val isManualDurationEdited = MutableStateFlow(false)

    fun onAreaChanged(newArea: String) {
        damageArea.value = newArea
        // Find matching presets for this area
        val presets = allMasterDurations.value.filter { it.category.equals(newArea, ignoreCase = true) }
        if (presets.isNotEmpty()) {
            val first = presets.first()
            selectedSubItemPreset.value = first.subItem
            val sev = SeverityLevel.values().firstOrNull { it.displayName == first.severityLevel } ?: SeverityLevel.LOW
            severityLevel.value = sev
            if (!isManualDurationEdited.value) {
                estimatedDuration.value = first.defaultDurationMinutes
            }
        } else {
            selectedSubItemPreset.value = "Defect in $newArea"
            if (!isManualDurationEdited.value) {
                estimatedDuration.value = 60
            }
        }
    }

    fun onPresetSelected(preset: MasterDurationEntity) {
        selectedSubItemPreset.value = preset.subItem
        val sev = SeverityLevel.values().firstOrNull { it.displayName == preset.severityLevel } ?: SeverityLevel.LOW
        severityLevel.value = sev
        if (damageDescription.value.isEmpty()) {
            damageDescription.value = preset.subItem
        }
        if (!isManualDurationEdited.value) {
            estimatedDuration.value = preset.defaultDurationMinutes
        }
    }

    fun onSeverityChanged(newSeverity: SeverityLevel) {
        severityLevel.value = newSeverity
        if (!isManualDurationEdited.value) {
            // Auto recalculate based on severity weight
            val base = when (newSeverity) {
                SeverityLevel.LOW -> 30
                SeverityLevel.MEDIUM -> 60
                SeverityLevel.HIGH -> 120
            }
            // Check if matching preset exists
            val preset = allMasterDurations.value.firstOrNull {
                it.category.equals(damageArea.value, ignoreCase = true) &&
                it.severityLevel.equals(newSeverity.displayName, ignoreCase = true)
            }
            estimatedDuration.value = preset?.defaultDurationMinutes ?: base
        }
    }

    fun onDurationManualChanged(minutes: Int) {
        isManualDurationEdited.value = true
        estimatedDuration.value = minutes
    }

    fun setFindingPhoto(path: String?) {
        findingPhotoPath.value = path
    }

    fun resetFindingForm() {
        findingPhotoPath.value = null
        damageDescription.value = ""
        damageDimension.value = ""
        isManualDurationEdited.value = false
        onAreaChanged(damageArea.value)
    }

    fun saveCurrentFinding(onSuccess: (Boolean) -> Unit) {
        val inspectionId = _activeInspectionId.value
        if (inspectionId == null) {
            onSuccess(false)
            return
        }

        // Capacity Rule Check:
        val currentTotal = activeTotalDuration.value
        if (currentTotal >= MAX_CAPACITY_MINUTES) {
            onSuccess(false)
            return
        }

        viewModelScope.launch {
            val desc = if (damageDescription.value.isNotBlank()) {
                damageDescription.value.trim()
            } else {
                "${damageArea.value}: ${selectedSubItemPreset.value}"
            }

            val dim = if (damageDimension.value.isNotBlank()) {
                damageDimension.value.trim()
            } else {
                "1 titik"
            }

            val finding = FindingEntity(
                inspectionId = inspectionId,
                photoUri = findingPhotoPath.value,
                damageArea = damageArea.value,
                damageDescription = desc,
                damageDimension = dim,
                severityLevel = severityLevel.value.displayName,
                durationMinutes = estimatedDuration.value,
                isSupervisorEdited = (currentRole.value == UserRole.SUPERVISOR_QC)
            )

            repository.insertFinding(finding)
            resetFindingForm()
            onSuccess(true)
        }
    }

    fun updateFinding(finding: FindingEntity) {
        viewModelScope.launch {
            repository.updateFinding(finding)
        }
    }

    fun deleteFinding(finding: FindingEntity) {
        viewModelScope.launch {
            repository.deleteFinding(finding)
        }
    }

    fun deleteInspection(inspection: InspectionEntity) {
        viewModelScope.launch {
            repository.deleteInspection(inspection)
            if (_activeInspectionId.value == inspection.id) {
                _activeInspectionId.value = null
            }
        }
    }

    fun approveInspection(inspectionId: Long) {
        viewModelScope.launch {
            val insp = repository.getInspectionById(inspectionId)
            if (insp != null) {
                repository.updateInspection(
                    insp.copy(status = "Approved")
                )
            }
        }
    }

    // ==========================================
    // Master Data Operations
    // ==========================================
    fun addMasterDuration(
        category: String,
        subItem: String,
        severityLevel: String,
        defaultDuration: Int,
        serviceType: String = "All"
    ) {
        viewModelScope.launch {
            repository.insertMasterDuration(
                MasterDurationEntity(
                    category = category,
                    subItem = subItem,
                    severityLevel = severityLevel,
                    defaultDurationMinutes = defaultDuration,
                    serviceType = serviceType
                )
            )
        }
    }

    fun updateMasterDuration(item: MasterDurationEntity) {
        viewModelScope.launch {
            repository.updateMasterDuration(item)
        }
    }

    fun deleteMasterDuration(item: MasterDurationEntity) {
        viewModelScope.launch {
            repository.deleteMasterDuration(item)
        }
    }

    fun importMasterDurations(
        items: List<MasterDurationEntity>,
        replaceExisting: Boolean,
        onComplete: (Int) -> Unit
    ) {
        viewModelScope.launch {
            if (replaceExisting) {
                repository.deleteAllMasterDurations()
            }
            repository.insertAllMasterDurations(items)
            onComplete(items.size)
        }
    }

    fun resetToStandardMasterPresets(onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val standardList = com.example.util.MasterTemplateHelper.getStandardIndustryPresets()
            repository.deleteAllMasterDurations()
            repository.insertAllMasterDurations(standardList)
            onComplete(standardList.size)
        }
    }

    fun addUser(name: String, email: String, role: String, phone: String) {
        viewModelScope.launch {
            repository.insertUser(
                UserEntity(name = name, email = email, role = role, phone = phone)
            )
        }
    }
}
