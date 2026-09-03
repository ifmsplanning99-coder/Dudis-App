package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.MasterDurationEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MasterTemplateHelper {

    /**
     * Converts master duration preset entities into a standard formatted JSON string.
     */
    fun exportToJson(presets: List<MasterDurationEntity>): String {
        val root = JSONObject()
        root.put("templateVersion", "1.0")
        root.put("system", "IFMS QC Defect & Duration Master System")
        root.put("exportedAt", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
        root.put("totalPresets", presets.size)

        val array = JSONArray()
        for (item in presets) {
            val obj = JSONObject()
            obj.put("category", item.category)
            obj.put("subItem", item.subItem)
            obj.put("severityLevel", item.severityLevel)
            obj.put("defaultDurationMinutes", item.defaultDurationMinutes)
            obj.put("serviceType", item.serviceType)
            array.put(obj)
        }
        root.put("presets", array)
        return root.toString(2)
    }

    /**
     * Converts master duration preset entities into standard CSV format.
     */
    fun exportToCsv(presets: List<MasterDurationEntity>): String {
        val sb = StringBuilder()
        sb.append("Category,Defect Item,Severity,Duration (Minutes),Maintenance Service\n")
        for (item in presets) {
            val cat = escapeCsv(item.category)
            val sub = escapeCsv(item.subItem)
            val sev = escapeCsv(item.severityLevel)
            val dur = item.defaultDurationMinutes
            val srv = escapeCsv(item.serviceType)
            sb.append("$cat,$sub,$sev,$dur,$srv\n")
        }
        return sb.toString()
    }

    /**
     * Parses JSON string into a list of MasterDurationEntity.
     */
    fun parseJson(jsonString: String): List<MasterDurationEntity> {
        val result = mutableListOf<MasterDurationEntity>()
        val trimmed = jsonString.trim()
        if (trimmed.startsWith("{")) {
            val root = JSONObject(trimmed)
            if (root.has("presets")) {
                val array = root.getJSONArray("presets")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    result.add(
                        MasterDurationEntity(
                            category = obj.optString("category", "Others"),
                            subItem = obj.optString("subItem", "General Defect"),
                            severityLevel = obj.optString("severityLevel", "Medium"),
                            defaultDurationMinutes = obj.optInt("defaultDurationMinutes", 60),
                            serviceType = obj.optString("serviceType", "All")
                        )
                    )
                }
            }
        } else if (trimmed.startsWith("[")) {
            val array = JSONArray(trimmed)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                result.add(
                    MasterDurationEntity(
                        category = obj.optString("category", "Others"),
                        subItem = obj.optString("subItem", "General Defect"),
                        severityLevel = obj.optString("severityLevel", "Medium"),
                        defaultDurationMinutes = obj.optInt("defaultDurationMinutes", 60),
                        serviceType = obj.optString("serviceType", "All")
                    )
                )
            }
        }
        return result
    }

    /**
     * Parses CSV or tab-separated string into a list of MasterDurationEntity.
     */
    fun parseCsv(csvString: String): List<MasterDurationEntity> {
        val result = mutableListOf<MasterDurationEntity>()
        val lines = csvString.lines().filter { it.isNotBlank() }
        for ((index, line) in lines.withIndex()) {
            if (index == 0 && (line.contains("Category", ignoreCase = true) || line.contains("Defect", ignoreCase = true))) {
                // Header line, skip
                continue
            }
            val delimiter = if (line.contains("\t")) "\t" else ","
            val tokens = parseCsvLine(line, delimiter)
            if (tokens.size >= 2) {
                val category = tokens.getOrNull(0)?.trim() ?: "Others"
                val subItem = tokens.getOrNull(1)?.trim() ?: "General Defect"
                val severity = tokens.getOrNull(2)?.trim() ?: "Medium"
                val duration = tokens.getOrNull(3)?.trim()?.toIntOrNull() ?: 60
                val serviceType = tokens.getOrNull(4)?.trim() ?: "All"

                result.add(
                    MasterDurationEntity(
                        category = category.ifBlank { "Others" },
                        subItem = subItem.ifBlank { "General Defect" },
                        severityLevel = if (severity.equals("High", true) || severity.equals("Medium", true) || severity.equals("Low", true)) severity else "Medium",
                        defaultDurationMinutes = duration.coerceIn(5, 480),
                        serviceType = if (serviceType.isNotBlank()) serviceType else "All"
                    )
                )
            }
        }
        return result
    }

    private fun parseCsvLine(line: String, delimiter: String): List<String> {
        if (delimiter == "\t") return line.split("\t")
        val tokens = mutableListOf<String>()
        var inQuotes = false
        val sb = StringBuilder()
        for (ch in line) {
            when (ch) {
                '\"' -> inQuotes = !inQuotes
                ',' -> {
                    if (inQuotes) {
                        sb.append(ch)
                    } else {
                        tokens.add(sb.toString().trim())
                        sb.clear()
                    }
                }
                else -> sb.append(ch)
            }
        }
        tokens.add(sb.toString().trim())
        return tokens
    }

    /**
     * Shares exported master template file (.json or .csv) via Android Share Sheet.
     */
    fun shareTemplateFile(context: Context, presets: List<MasterDurationEntity>, asJson: Boolean) {
        try {
            val extension = if (asJson) "json" else "csv"
            val mimeType = if (asJson) "application/json" else "text/csv"
            val content = if (asJson) exportToJson(presets) else exportToCsv(presets)
            val fileName = "IFMS_Master_Duration_Presets_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}.$extension"

            val outputFile = File(context.cacheDir, fileName)
            outputFile.writeText(content, Charsets.UTF_8)

            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                outputFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_SUBJECT, "IFMS QC Master Duration & Damage Area Presets")
                putExtra(Intent.EXTRA_TEXT, "Exported IFMS QC Master Duration Template (${presets.size} preset items).")
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Export Master Template ($extension)"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Export error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Reads file content from Uri (e.g. from File Picker).
     */
    fun readTextFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    /**
     * Helper to parse either JSON or CSV automatically.
     */
    fun parseMasterDurations(text: String): List<MasterDurationEntity> {
        val trimmed = text.trim()
        return if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            parseJson(trimmed)
        } else {
            parseCsv(trimmed)
        }
    }

    /**
     * Convenience method to export and share master presets via Android share sheet.
     */
    fun exportAndShareMasterTemplate(context: Context, presets: List<MasterDurationEntity>, format: String = "json") {
        shareTemplateFile(context, presets, format.equals("json", ignoreCase = true))
    }

    /**
     * Pre-defined industry master presets library for quick restoration / reset.
     */
    fun getStandardIndustryPresets(): List<MasterDurationEntity> {
        return listOf(
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
    }
}
