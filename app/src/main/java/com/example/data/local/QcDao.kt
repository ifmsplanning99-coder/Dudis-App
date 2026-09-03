package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BuildingEntity
import com.example.data.model.FindingEntity
import com.example.data.model.InspectionEntity
import com.example.data.model.MasterDurationEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QcDao {

    // Inspections
    @Query("SELECT * FROM inspections ORDER BY createdAt DESC")
    fun getAllInspectionsFlow(): Flow<List<InspectionEntity>>

    @Query("SELECT * FROM inspections WHERE id = :id LIMIT 1")
    suspend fun getInspectionById(id: Long): InspectionEntity?

    @Query("SELECT * FROM inspections WHERE id = :id LIMIT 1")
    fun getInspectionByIdFlow(id: Long): Flow<InspectionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspection(inspection: InspectionEntity): Long

    @Update
    suspend fun updateInspection(inspection: InspectionEntity)

    @Delete
    suspend fun deleteInspection(inspection: InspectionEntity)

    // Findings
    @Query("SELECT * FROM findings WHERE inspectionId = :inspectionId ORDER BY createdAt ASC")
    fun getFindingsForInspectionFlow(inspectionId: Long): Flow<List<FindingEntity>>

    @Query("SELECT * FROM findings WHERE inspectionId = :inspectionId ORDER BY createdAt ASC")
    suspend fun getFindingsForInspectionList(inspectionId: Long): List<FindingEntity>

    @Query("SELECT COALESCE(SUM(durationMinutes), 0) FROM findings WHERE inspectionId = :inspectionId")
    fun getTotalDurationFlow(inspectionId: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(durationMinutes), 0) FROM findings WHERE inspectionId = :inspectionId")
    suspend fun getTotalDuration(inspectionId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinding(finding: FindingEntity): Long

    @Update
    suspend fun updateFinding(finding: FindingEntity)

    @Delete
    suspend fun deleteFinding(finding: FindingEntity)

    @Query("DELETE FROM findings WHERE id = :findingId")
    suspend fun deleteFindingById(findingId: Long)

    // Master Durations
    @Query("SELECT * FROM master_durations ORDER BY category ASC, subItem ASC")
    fun getAllMasterDurationsFlow(): Flow<List<MasterDurationEntity>>

    @Query("SELECT * FROM master_durations WHERE category = :category")
    suspend fun getMasterDurationsByCategory(category: String): List<MasterDurationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMasterDuration(item: MasterDurationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMasterDurations(items: List<MasterDurationEntity>)

    @Update
    suspend fun updateMasterDuration(item: MasterDurationEntity)

    @Delete
    suspend fun deleteMasterDuration(item: MasterDurationEntity)

    @Query("DELETE FROM master_durations")
    suspend fun deleteAllMasterDurations()

    // Buildings
    @Query("SELECT * FROM buildings ORDER BY createdAt DESC")
    fun getAllBuildingsFlow(): Flow<List<BuildingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuilding(building: BuildingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBuildings(buildings: List<BuildingEntity>)

    // Users
    @Query("SELECT * FROM users ORDER BY role ASC, name ASC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllUsers(users: List<UserEntity>)
}
