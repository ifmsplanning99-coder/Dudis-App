package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BuildingEntity
import com.example.data.model.FindingEntity
import com.example.data.model.InspectionEntity
import com.example.data.model.MasterDurationEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BuildingEntity::class,
        InspectionEntity::class,
        FindingEntity::class,
        MasterDurationEntity::class,
        UserEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun qcDao(): QcDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "qc_property_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.qcDao())
                    }
                }
            }

            private suspend fun populateInitialData(dao: QcDao) {
                // Master Durations with Category, Defect Item, Severity, Default Duration, and Service Type
                val initialMasterDurations = listOf(
                    // 1 PM (Monthly Routine Checks)
                    MasterDurationEntity(category = "Walls", subItem = "Wall paint hairline crack / surface blemish", severityLevel = "Low", defaultDurationMinutes = 30, serviceType = "1 PM"),
                    MasterDurationEntity(category = "Flooring", subItem = "Tile grout peeling / hollow gap", severityLevel = "Low", defaultDurationMinutes = 30, serviceType = "1 PM"),
                    MasterDurationEntity(category = "Doors", subItem = "Door latch lubrication / handle tightening", severityLevel = "Low", defaultDurationMinutes = 30, serviceType = "1 PM"),
                    MasterDurationEntity(category = "Sanitary", subItem = "Faucet aerator & trap cleaning / dripping", severityLevel = "Low", defaultDurationMinutes = 30, serviceType = "1 PM"),
                    MasterDurationEntity(category = "Electrical", subItem = "Loose light switch / power socket", severityLevel = "Low", defaultDurationMinutes = 30, serviceType = "1 PM"),

                    // 3 PM (Quarterly Servicing)
                    MasterDurationEntity(category = "Walls", subItem = "Peeling paint / dampness discoloration", severityLevel = "Medium", defaultDurationMinutes = 45, serviceType = "3 PM"),
                    MasterDurationEntity(category = "Flooring", subItem = "Chipped tile / broken corner edge", severityLevel = "Medium", defaultDurationMinutes = 60, serviceType = "3 PM"),
                    MasterDurationEntity(category = "Doors", subItem = "Door sticking / tight hinge friction adjustment", severityLevel = "Medium", defaultDurationMinutes = 60, serviceType = "3 PM"),
                    MasterDurationEntity(category = "Windows", subItem = "Window casement latch & friction stay replacement", severityLevel = "Medium", defaultDurationMinutes = 45, serviceType = "3 PM"),
                    MasterDurationEntity(category = "Ceiling", subItem = "Gypsum ceiling water stain touch-up", severityLevel = "Medium", defaultDurationMinutes = 60, serviceType = "3 PM"),
                    MasterDurationEntity(category = "Sanitary", subItem = "Floor drain clogged / odor trap replacement", severityLevel = "Medium", defaultDurationMinutes = 60, serviceType = "3 PM"),
                    MasterDurationEntity(category = "Landscaping", subItem = "Uneven / sunken paving blocks re-leveling", severityLevel = "Medium", defaultDurationMinutes = 60, serviceType = "3 PM"),

                    // 6 PM (Semi-Annual Maintenance)
                    MasterDurationEntity(category = "Roofing", subItem = "Slightly shifted roof tile & flashing realignment", severityLevel = "Medium", defaultDurationMinutes = 60, serviceType = "6 PM"),
                    MasterDurationEntity(category = "Doors", subItem = "Loose handle / broken mortise lockset replacement", severityLevel = "Medium", defaultDurationMinutes = 90, serviceType = "6 PM"),
                    MasterDurationEntity(category = "Windows", subItem = "Window weatherstrip & sealant re-caulking", severityLevel = "Medium", defaultDurationMinutes = 60, serviceType = "6 PM"),
                    MasterDurationEntity(category = "Mechanical", subItem = "AC condensate drain line cleaning & pump check", severityLevel = "Medium", defaultDurationMinutes = 60, serviceType = "6 PM"),

                    // Deep check (Comprehensive Full Audit)
                    MasterDurationEntity(category = "Walls", subItem = "Plaster through-crack / structural fissure", severityLevel = "High", defaultDurationMinutes = 90, serviceType = "Deep check"),
                    MasterDurationEntity(category = "Flooring", subItem = "Hollow / popping tiles across room section", severityLevel = "High", defaultDurationMinutes = 120, serviceType = "Deep check"),
                    MasterDurationEntity(category = "Roofing", subItem = "Roof leak / gutter valley seepage overhaul", severityLevel = "High", defaultDurationMinutes = 120, serviceType = "Deep check"),
                    MasterDurationEntity(category = "Ceiling", subItem = "Ceiling board collapse / severe fracture", severityLevel = "High", defaultDurationMinutes = 90, serviceType = "Deep check"),
                    MasterDurationEntity(category = "Mechanical", subItem = "AC refrigerant leak / compressor vibration", severityLevel = "High", defaultDurationMinutes = 120, serviceType = "Deep check"),

                    // CM (Corrective Maintenance / Emergency Snags)
                    MasterDurationEntity(category = "Doors", subItem = "Warped door frame / termite damage replacement", severityLevel = "High", defaultDurationMinutes = 150, serviceType = "CM"),
                    MasterDurationEntity(category = "Sanitary", subItem = "Burst main supply pipe / severe leakage", severityLevel = "High", defaultDurationMinutes = 120, serviceType = "CM"),
                    MasterDurationEntity(category = "Electrical", subItem = "MCB tripping / short circuit investigation", severityLevel = "High", defaultDurationMinutes = 90, serviceType = "CM"),
                    MasterDurationEntity(category = "Others", subItem = "General civil / architectural emergency snag", severityLevel = "Medium", defaultDurationMinutes = 60, serviceType = "CM")
                )
                dao.insertAllMasterDurations(initialMasterDurations)

                // Default Users
                val initialUsers = listOf(
                    UserEntity(name = "David Miller", email = "david.qc@ifms-property.com", role = "Inspector QC", phone = "+1 (555) 234-5678"),
                    UserEntity(name = "Robert Johnson, P.E.", email = "robert.spv@ifms-property.com", role = "Supervisor QC", phone = "+1 (555) 876-5432"),
                    UserEntity(name = "System Administrator", email = "admin@ifms-property.com", role = "Administrator", phone = "+1 (555) 999-0000")
                )
                dao.insertAllUsers(initialUsers)

                // Sample Buildings
                val initialBuildings = listOf(
                    BuildingEntity(type = "House", nameOrCluster = "Emerald Lake Cluster", unitOrNumber = "Block B3 No. 12"),
                    BuildingEntity(type = "House", nameOrCluster = "Sapphire Garden Cluster", unitOrNumber = "Block A1 No. 05"),
                    BuildingEntity(type = "Camp", nameOrCluster = "North Sector Construction Workers Camp", unitOrNumber = ""),
                    BuildingEntity(type = "Public Facility", nameOrCluster = "Clubhouse & Swimming Pool Facility", unitOrNumber = ""),
                    BuildingEntity(type = "Office", nameOrCluster = "Marketing Gallery & Site Office Fl 2", unitOrNumber = "")
                )
                dao.insertAllBuildings(initialBuildings)

                // Sample Inspection & Findings for instant preview/testing
                val sampleInspectionId = dao.insertInspection(
                    InspectionEntity(
                        buildingType = "House",
                        locationName = "Emerald Lake Cluster - Block B3 No. 12",
                        serviceType = "1 PM",
                        inspectionDate = "2026-09-01",
                        inspectorName = "David Miller",
                        supervisorName = "Robert Johnson, P.E.",
                        status = "Submitted",
                        totalDurationMinutes = 300
                    )
                )

                dao.insertFinding(
                    FindingEntity(
                        inspectionId = sampleInspectionId,
                        photoUri = null,
                        damageArea = "Walls",
                        damageDescription = "Living room wall paint hairline crack with slight moisture dampness.",
                        damageDimension = "1.5 x 2.0 m (3.0 m²)",
                        severityLevel = "Low",
                        durationMinutes = 30
                    )
                )

                dao.insertFinding(
                    FindingEntity(
                        inspectionId = sampleInspectionId,
                        photoUri = null,
                        damageArea = "Flooring",
                        damageDescription = "Master bedroom porcelain tile cracked at corner near door threshold.",
                        damageDimension = "2 tiles (40x40 cm)",
                        severityLevel = "Medium",
                        durationMinutes = 60
                    )
                )

                dao.insertFinding(
                    FindingEntity(
                        inspectionId = sampleInspectionId,
                        photoUri = null,
                        damageArea = "Doors",
                        damageDescription = "Main entrance door handle is loose and door drags against frame.",
                        damageDimension = "1 solid timber door unit",
                        severityLevel = "Medium",
                        durationMinutes = 90
                    )
                )

                dao.insertFinding(
                    FindingEntity(
                        inspectionId = sampleInspectionId,
                        photoUri = null,
                        damageArea = "Roofing",
                        damageDescription = "Roof valley flashing leaking and dripping onto gypsum ceiling during rain.",
                        damageDimension = "2.0 meters span",
                        severityLevel = "High",
                        durationMinutes = 120
                    )
                )
            }
        }
    }
}
