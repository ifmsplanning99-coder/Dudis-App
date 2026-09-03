package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class BuildingType(val displayName: String) {
    HOUSE("House"),
    CAMP("Camp"),
    PUBLIC_FACILITY("Public Facility"),
    OFFICE("Office")
}

enum class MaintenanceServiceType(
    val code: String,
    val displayName: String,
    val description: String
) {
    PM_1("1 PM", "1 PM (1st Month)", "Monthly preventive maintenance check"),
    PM_3("3 PM", "3 PM (3rd Month)", "Quarterly preventive maintenance servicing"),
    PM_6("6 PM", "6 PM (6th Month)", "Semi-annual deep maintenance inspection"),
    DEEP_CHECK("Deep check", "Deep Check Audit", "Comprehensive architectural & structural audit"),
    CM("CM", "CM (Corrective)", "Corrective repair & defect rectification");

    companion object {
        fun fromCode(code: String): MaintenanceServiceType {
            return values().firstOrNull { it.code.equals(code.trim(), ignoreCase = true) } ?: PM_1
        }
    }
}

enum class SeverityLevel(val displayName: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High")
}

enum class UserRole(val displayName: String) {
    INSPECTOR_QC("Inspector QC"),
    SUPERVISOR_QC("Supervisor QC"),
    ADMINISTRATOR("Administrator")
}

@Entity(tableName = "buildings")
data class BuildingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val nameOrCluster: String,
    val unitOrNumber: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "inspections",
    indices = [Index("createdAt")]
)
data class InspectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val buildingType: String,
    val locationName: String,
    val serviceType: String = "1 PM", // "1 PM", "3 PM", "6 PM", "Deep check", "CM"
    val inspectionDate: String,
    val inspectorName: String,
    val supervisorName: String = "Supervisor QC (Ir. Hendra Wijaya)",
    val status: String = "Draft", // "Draft", "Submitted", "Approved"
    val totalDurationMinutes: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "findings",
    foreignKeys = [
        ForeignKey(
            entity = InspectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["inspectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("inspectionId")]
)
data class FindingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val inspectionId: Long,
    val photoUri: String? = null,
    val damageArea: String,
    val damageDescription: String,
    val damageDimension: String,
    val severityLevel: String,
    val durationMinutes: Int,
    val isSupervisorEdited: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "master_durations")
data class MasterDurationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val subItem: String,
    val severityLevel: String,
    val defaultDurationMinutes: Int,
    val serviceType: String = "All" // "All", "1 PM", "3 PM", "6 PM", "Deep check", "CM"
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val role: String,
    val phone: String = ""
)
