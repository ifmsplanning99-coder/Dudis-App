package com.example.data.repository

import com.example.data.local.QcDao
import com.example.data.model.BuildingEntity
import com.example.data.model.FindingEntity
import com.example.data.model.InspectionEntity
import com.example.data.model.MasterDurationEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

class QcRepository(private val qcDao: QcDao) {

    val allInspections: Flow<List<InspectionEntity>> = qcDao.getAllInspectionsFlow()
    val allMasterDurations: Flow<List<MasterDurationEntity>> = qcDao.getAllMasterDurationsFlow()
    val allBuildings: Flow<List<BuildingEntity>> = qcDao.getAllBuildingsFlow()
    val allUsers: Flow<List<UserEntity>> = qcDao.getAllUsersFlow()

    suspend fun getInspectionById(id: Long): InspectionEntity? = qcDao.getInspectionById(id)

    fun getInspectionByIdFlow(id: Long): Flow<InspectionEntity?> = qcDao.getInspectionByIdFlow(id)

    suspend fun insertInspection(inspection: InspectionEntity): Long = qcDao.insertInspection(inspection)

    suspend fun updateInspection(inspection: InspectionEntity) = qcDao.updateInspection(inspection)

    suspend fun deleteInspection(inspection: InspectionEntity) = qcDao.deleteInspection(inspection)

    fun getFindingsForInspectionFlow(inspectionId: Long): Flow<List<FindingEntity>> =
        qcDao.getFindingsForInspectionFlow(inspectionId)

    suspend fun getFindingsForInspectionList(inspectionId: Long): List<FindingEntity> =
        qcDao.getFindingsForInspectionList(inspectionId)

    fun getTotalDurationFlow(inspectionId: Long): Flow<Int> =
        qcDao.getTotalDurationFlow(inspectionId)

    suspend fun getTotalDuration(inspectionId: Long): Int =
        qcDao.getTotalDuration(inspectionId)

    suspend fun insertFinding(finding: FindingEntity): Long {
        val findingId = qcDao.insertFinding(finding)
        // update cached total in inspection
        val total = qcDao.getTotalDuration(finding.inspectionId)
        val inspection = qcDao.getInspectionById(finding.inspectionId)
        if (inspection != null) {
            qcDao.updateInspection(inspection.copy(totalDurationMinutes = total))
        }
        return findingId
    }

    suspend fun updateFinding(finding: FindingEntity) {
        qcDao.updateFinding(finding)
        val total = qcDao.getTotalDuration(finding.inspectionId)
        val inspection = qcDao.getInspectionById(finding.inspectionId)
        if (inspection != null) {
            qcDao.updateInspection(inspection.copy(totalDurationMinutes = total))
        }
    }

    suspend fun deleteFinding(finding: FindingEntity) {
        qcDao.deleteFinding(finding)
        val total = qcDao.getTotalDuration(finding.inspectionId)
        val inspection = qcDao.getInspectionById(finding.inspectionId)
        if (inspection != null) {
            qcDao.updateInspection(inspection.copy(totalDurationMinutes = total))
        }
    }

    suspend fun deleteFindingById(findingId: Long, inspectionId: Long) {
        qcDao.deleteFindingById(findingId)
        val total = qcDao.getTotalDuration(inspectionId)
        val inspection = qcDao.getInspectionById(inspectionId)
        if (inspection != null) {
            qcDao.updateInspection(inspection.copy(totalDurationMinutes = total))
        }
    }

    suspend fun getMasterDurationsByCategory(category: String): List<MasterDurationEntity> =
        qcDao.getMasterDurationsByCategory(category)

    suspend fun insertMasterDuration(item: MasterDurationEntity) = qcDao.insertMasterDuration(item)

    suspend fun insertAllMasterDurations(items: List<MasterDurationEntity>) = qcDao.insertAllMasterDurations(items)

    suspend fun updateMasterDuration(item: MasterDurationEntity) = qcDao.updateMasterDuration(item)

    suspend fun deleteMasterDuration(item: MasterDurationEntity) = qcDao.deleteMasterDuration(item)

    suspend fun deleteAllMasterDurations() = qcDao.deleteAllMasterDurations()

    suspend fun insertBuilding(building: BuildingEntity) = qcDao.insertBuilding(building)

    suspend fun insertUser(user: UserEntity) = qcDao.insertUser(user)
}
