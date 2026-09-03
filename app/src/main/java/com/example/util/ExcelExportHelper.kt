package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.FindingEntity
import com.example.data.model.InspectionEntity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ExcelExportHelper {

    data class PhotoAttachment(
        val index: Int,
        val findingId: Long,
        val rowZeroIndexed: Int,
        val imageFileName: String,
        val relId: String,
        val imageBytes: ByteArray
    )

    /**
     * Generates a genuine Microsoft Excel (.xlsx) file using OpenXML package standards,
     * including standard IFMS inspection template styling, formulas, and embedded defect photos.
     */
    fun createXlsxFile(
        context: Context,
        inspection: InspectionEntity,
        findings: List<FindingEntity>
    ): File {
        val sanitizedName = inspection.locationName.replace(Regex("[^a-zA-Z0-9.-]"), "_")
        val fileName = "IFMS_QC_Report_${sanitizedName}_${inspection.inspectionDate}.xlsx"
        val outputFile = File(context.cacheDir, fileName)

        // Process photos for each finding
        val photoAttachments = mutableListOf<PhotoAttachment>()
        val startDataRowIndex = 10 // 1-indexed row in sheet (header is row 10, first data is row 11)

        findings.forEachIndexed { i, finding ->
            val rowZeroIndexed = startDataRowIndex + i // row 10 in 0-indexed = row 11 in Excel
            val bytes = loadBitmapBytes(context, finding.photoUri)
            if (bytes != null && bytes.isNotEmpty()) {
                val picIndex = photoAttachments.size + 1
                photoAttachments.add(
                    PhotoAttachment(
                        index = picIndex,
                        findingId = finding.id,
                        rowZeroIndexed = rowZeroIndexed,
                        imageFileName = "image$picIndex.jpg",
                        relId = "rIdImg$picIndex",
                        imageBytes = bytes
                    )
                )
            }
        }

        val hasDrawings = photoAttachments.isNotEmpty()

        ZipOutputStream(FileOutputStream(outputFile)).use { zip ->
            // 1. [Content_Types].xml
            addZipEntry(zip, "[Content_Types].xml", getContentTypesXml(hasDrawings))

            // 2. _rels/.rels
            addZipEntry(zip, "_rels/.rels", getRootRelsXml())

            // 3. docProps/app.xml & docProps/core.xml
            addZipEntry(zip, "docProps/app.xml", getDocPropsAppXml())
            addZipEntry(zip, "docProps/core.xml", getDocPropsCoreXml(inspection))

            // 4. xl/_rels/workbook.xml.rels
            addZipEntry(zip, "xl/_rels/workbook.xml.rels", getWorkbookRelsXml())

            // 5. xl/styles.xml
            addZipEntry(zip, "xl/styles.xml", getStylesXml())

            // 6. xl/workbook.xml
            addZipEntry(zip, "xl/workbook.xml", getWorkbookXml())

            // 7. xl/worksheets/_rels/sheet1.xml.rels (if has drawings)
            if (hasDrawings) {
                addZipEntry(zip, "xl/worksheets/_rels/sheet1.xml.rels", getSheetRelsXml())
                addZipEntry(zip, "xl/drawings/_rels/drawing1.xml.rels", getDrawingRelsXml(photoAttachments))
                addZipEntry(zip, "xl/drawings/drawing1.xml", getDrawingXml(photoAttachments))

                // Write photo media files
                for (photo in photoAttachments) {
                    addZipBinaryEntry(zip, "xl/media/${photo.imageFileName}", photo.imageBytes)
                }
            }

            // 8. xl/worksheets/sheet1.xml
            addZipEntry(zip, "xl/worksheets/sheet1.xml", getWorksheetXml(inspection, findings, photoAttachments))
        }

        return outputFile
    }

    private fun addZipEntry(zip: ZipOutputStream, path: String, content: String) {
        val entry = ZipEntry(path)
        zip.putNextEntry(entry)
        zip.write(content.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    private fun addZipBinaryEntry(zip: ZipOutputStream, path: String, bytes: ByteArray) {
        val entry = ZipEntry(path)
        zip.putNextEntry(entry)
        zip.write(bytes)
        zip.closeEntry()
    }

    private fun loadBitmapBytes(context: Context, photoPath: String?): ByteArray? {
        if (photoPath.isNullOrBlank()) return null
        return try {
            val bitmap = if (photoPath.startsWith("content://")) {
                val uri = Uri.parse(photoPath)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            } else {
                val file = File(photoPath)
                if (file.exists() && file.length() > 0) {
                    BitmapFactory.decodeFile(file.absolutePath)
                } else null
            } ?: return null

            // Resize image to max 480x360 for crisp yet lightweight Excel file size
            val maxDim = 480
            val maxSide = maxOf(bitmap.width, bitmap.height)
            val scale = if (maxSide > maxDim) maxDim.toFloat() / maxSide else 1.0f
            val scaledW = (bitmap.width * scale).toInt().coerceAtLeast(1)
            val scaledH = (bitmap.height * scale).toInt().coerceAtLeast(1)

            val scaledBitmap = if (scale < 1.0f) {
                Bitmap.createScaledBitmap(bitmap, scaledW, scaledH, true)
            } else {
                bitmap
            }

            val out = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            out.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getContentTypesXml(hasDrawings: Boolean): String {
        val drawingType = if (hasDrawings) {
            """
  <Default Extension="jpeg" ContentType="image/jpeg"/>
  <Default Extension="jpg" ContentType="image/jpeg"/>
  <Default Extension="png" ContentType="image/png"/>
  <Override PartName="/xl/drawings/drawing1.xml" ContentType="application/vnd.openxmlformats-officedocument.drawing+xml"/>"""
        } else ""

        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>$drawingType
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
  <Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>
  <Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>
</Types>"""
    }

    private fun getRootRelsXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
</Relationships>"""
    }

    private fun getDocPropsAppXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties">
  <Application>IFMS QC Mobile Inspection System</Application>
  <Company>IFMS Property &amp; Facilities Management</Company>
</Properties>"""
    }

    private fun getDocPropsCoreXml(inspection: InspectionEntity): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <dc:title>IFMS QC Inspection Report - ${escapeXml(inspection.locationName)}</dc:title>
  <dc:subject>Standard Quality Control Defect Report</dc:subject>
  <dc:creator>${escapeXml(inspection.inspectorName)}</dc:creator>
  <cp:lastModifiedBy>IFMS QC Android</cp:lastModifiedBy>
</cp:coreProperties>"""
    }

    private fun getWorkbookRelsXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""
    }

    private fun getWorkbookXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="QC Inspection Report" sheetId="1" r:id="rId1"/>
  </sheets>
</workbook>"""
    }

    private fun getSheetRelsXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rIdDrawing1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/drawing" Target="../drawings/drawing1.xml"/>
</Relationships>"""
    }

    private fun getDrawingRelsXml(photos: List<PhotoAttachment>): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">""")
        for (p in photos) {
            sb.append("""
  <Relationship Id="${p.relId}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" Target="../media/${p.imageFileName}"/>""")
        }
        sb.append("""
</Relationships>""")
        return sb.toString()
    }

    private fun getDrawingXml(photos: List<PhotoAttachment>): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<xdr:wsDr xmlns:xdr="http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing" xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main">""")

        photos.forEachIndexed { i, photo ->
            val picId = i + 1
            // Col G is index 6 (0-indexed). Row is photo.rowZeroIndexed
            sb.append("""
  <xdr:twoCellAnchor editAs="oneCell">
    <xdr:from>
      <xdr:col>6</xdr:col>
      <xdr:colOff>35000</xdr:colOff>
      <xdr:row>${photo.rowZeroIndexed}</xdr:row>
      <xdr:rowOff>25000</xdr:rowOff>
    </xdr:from>
    <xdr:to>
      <xdr:col>7</xdr:col>
      <xdr:colOff>-35000</xdr:colOff>
      <xdr:row>${photo.rowZeroIndexed + 1}</xdr:row>
      <xdr:rowOff>-25000</xdr:rowOff>
    </xdr:to>
    <xdr:pic>
      <xdr:nvPicPr>
        <xdr:cNvPr id="$picId" name="DefectPhoto_$picId"/>
        <xdr:cNvPicPr>
          <a:picLocks noChangeAspect="1"/>
        </xdr:cNvPicPr>
      </xdr:nvPicPr>
      <xdr:blipFill>
        <a:blip xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" r:embed="${photo.relId}"/>
        <a:stretch>
          <a:fillRect/>
        </a:stretch>
      </xdr:blipFill>
      <xdr:spPr>
        <a:xfrm>
          <a:off x="0" y="0"/>
          <a:ext cx="1200000" cy="900000"/>
        </a:xfrm>
        <a:prstGeom prst="rect">
          <a:avLst/>
        </a:prstGeom>
      </xdr:spPr>
    </xdr:pic>
    <xdr:clientData/>
  </xdr:twoCellAnchor>""")
        }

        sb.append("""
</xdr:wsDr>""")
        return sb.toString()
    }

    private fun getStylesXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <fonts count="9">
    <font><name val="Calibri"/><sz val="11"/><color rgb="FF0F172A"/></font>
    <font><b/><name val="Calibri"/><sz val="15"/><color rgb="FFFFFFFF"/></font>
    <font><b/><name val="Calibri"/><sz val="11"/><color rgb="FFFFFFFF"/></font>
    <font><b/><name val="Calibri"/><sz val="11"/><color rgb="FF0D47A1"/></font>
    <font><i/><name val="Calibri"/><sz val="10"/><color rgb="FF64748B"/></font>
    <font><b/><name val="Calibri"/><sz val="11"/><color rgb="FF0F172A"/></font>
    <font><b/><name val="Calibri"/><sz val="10"/><color rgb="FF166534"/></font>
    <font><b/><name val="Calibri"/><sz val="10"/><color rgb="FF9A3412"/></font>
    <font><b/><name val="Calibri"/><sz val="10"/><color rgb="FF991B1B"/></font>
  </fonts>
  <fills count="10">
    <fill><patternFill patternType="none"/></fill>
    <fill><patternFill patternType="gray125"/></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FF0D47A1"/><bgColor indexed="64"/></patternFill></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FF1565C0"/><bgColor indexed="64"/></patternFill></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FFE3F2FD"/><bgColor indexed="64"/></patternFill></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FFF8FAFC"/><bgColor indexed="64"/></patternFill></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FFDCFCE7"/><bgColor indexed="64"/></patternFill></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FFFEF3C7"/><bgColor indexed="64"/></patternFill></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FFFEE2E2"/><bgColor indexed="64"/></patternFill></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FFE2E8F0"/><bgColor indexed="64"/></patternFill></fill>
  </fills>
  <borders count="4">
    <border><left/><right/><top/><bottom/><diagonal/></border>
    <border>
      <left style="thin"><color rgb="FFCBD5E1"/></left>
      <right style="thin"><color rgb="FFCBD5E1"/></right>
      <top style="thin"><color rgb="FFCBD5E1"/></top>
      <bottom style="thin"><color rgb="FFCBD5E1"/></bottom>
    </border>
    <border>
      <left style="thin"><color rgb="FF0D47A1"/></left>
      <right style="thin"><color rgb="FF0D47A1"/></right>
      <top style="thin"><color rgb="FF0D47A1"/></top>
      <bottom style="medium"><color rgb="FF0D47A1"/></bottom>
    </border>
    <border>
      <left style="thin"><color rgb="FF94A3B8"/></left>
      <right style="thin"><color rgb="FF94A3B8"/></right>
      <top style="thin"><color rgb="FF94A3B8"/></top>
      <bottom style="double"><color rgb="FF0D47A1"/></bottom>
    </border>
  </borders>
  <cellStyleXfs count="1">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0"/>
  </cellStyleXfs>
  <cellXfs count="11">
    <!-- 0: Default -->
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
    <!-- 1: Main Title Banner -->
    <xf numFmtId="0" fontId="1" fillId="2" borderId="0" xfId="0" applyFont="1" applyFill="1" applyAlignment="1"><alignment horizontal="left" vertical="center"/></xf>
    <!-- 2: Table Header -->
    <xf numFmtId="0" fontId="2" fillId="3" borderId="2" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="center" vertical="center" wrapText="1"/></xf>
    <!-- 3: Data Cell Left -->
    <xf numFmtId="0" fontId="0" fillId="0" borderId="1" xfId="0" applyBorder="1" applyAlignment="1"><alignment horizontal="left" vertical="center" wrapText="1"/></xf>
    <!-- 4: Data Cell Alternate Left -->
    <xf numFmtId="0" fontId="0" fillId="5" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="left" vertical="center" wrapText="1"/></xf>
    <!-- 5: Label Bold Header Cell -->
    <xf numFmtId="0" fontId="3" fillId="4" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="left" vertical="center"/></xf>
    <!-- 6: Italic Subtitle Note -->
    <xf numFmtId="0" fontId="4" fillId="0" borderId="0" xfId="0" applyFont="1"/>
    <!-- 7: Data Cell Center -->
    <xf numFmtId="0" fontId="0" fillId="0" borderId="1" xfId="0" applyBorder="1" applyAlignment="1"><alignment horizontal="center" vertical="center"/></xf>
    <!-- 8: Low Severity Badge Cell -->
    <xf numFmtId="0" fontId="6" fillId="6" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="center" vertical="center"/></xf>
    <!-- 9: Medium Severity Badge Cell -->
    <xf numFmtId="0" fontId="7" fillId="7" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="center" vertical="center"/></xf>
    <!-- 10: High Severity Badge Cell -->
    <xf numFmtId="0" fontId="8" fillId="8" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="center" vertical="center"/></xf>
    <!-- 11: Summary Row Bold -->
    <xf numFmtId="0" fontId="5" fillId="9" borderId="3" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="left" vertical="center"/></xf>
  </cellXfs>
</styleSheet>"""
    }

    private fun getWorksheetXml(
        inspection: InspectionEntity,
        findings: List<FindingEntity>,
        photos: List<PhotoAttachment>
    ): String {
        val totalMinutes = findings.sumOf { it.durationMinutes }
        val totalHours = totalMinutes / 60
        val remainingMins = totalMinutes % 60
        val photoMap = photos.associateBy { it.findingId }

        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <cols>
    <col min="1" max="1" width="6" customWidth="1"/>
    <col min="2" max="2" width="22" customWidth="1"/>
    <col min="3" max="3" width="38" customWidth="1"/>
    <col min="4" max="4" width="22" customWidth="1"/>
    <col min="5" max="5" width="16" customWidth="1"/>
    <col min="6" max="6" width="18" customWidth="1"/>
    <col min="7" max="7" width="26" customWidth="1"/>
  </cols>
  <sheetData>""")

        var r = 1
        // 1. Title Row (Style 1)
        sb.append("""
    <row r="$r" ht="34" customHeight="1">
      <c r="A$r" s="1" t="inlineStr"><is><t>  IFMS QUALITY CONTROL (QC) FIELD INSPECTION REPORT</t></is></c>
      <c r="B$r" s="1"/><c r="C$r" s="1"/><c r="D$r" s="1"/><c r="E$r" s="1"/><c r="F$r" s="1"/><c r="G$r" s="1"/>
    </row>""")
        r++

        // 2. Subtitle Row
        sb.append("""
    <row r="$r" ht="18" customHeight="1">
      <c r="A$r" s="6" t="inlineStr"><is><t>Standard Property Inspection &amp; Maintenance Quality Assurance System | Document ID: IFMS-QC-${inspection.id}-${inspection.inspectionDate.replace("-", "")}</t></is></c>
    </row>""")
        r++

        // 3. Blank spacer
        r++

        // 4. Metadata block
        sb.append("""
    <row r="$r" ht="20" customHeight="1">
      <c r="A$r" s="5" t="inlineStr"><is><t>Inspection Date:</t></is></c>
      <c r="B$r" s="3" t="inlineStr"><is><t>${escapeXml(inspection.inspectionDate)}</t></is></c>
      <c r="D$r" s="5" t="inlineStr"><is><t>Building Type:</t></is></c>
      <c r="E$r" s="3" t="inlineStr"><is><t>${escapeXml(inspection.buildingType)}</t></is></c>
    </row>""")
        r++

        sb.append("""
    <row r="$r" ht="20" customHeight="1">
      <c r="A$r" s="5" t="inlineStr"><is><t>Inspector QC:</t></is></c>
      <c r="B$r" s="3" t="inlineStr"><is><t>${escapeXml(inspection.inspectorName)}</t></is></c>
      <c r="D$r" s="5" t="inlineStr"><is><t>Location / Unit:</t></is></c>
      <c r="E$r" s="3" t="inlineStr"><is><t>${escapeXml(inspection.locationName)}</t></is></c>
    </row>""")
        r++

        val capacityPct = (totalMinutes * 100) / 840
        val complianceStatus = if (totalMinutes <= 840) "PASSED (Under 840 Min Limit)" else "EXCEEDED LIMIT"
        sb.append("""
    <row r="$r" ht="20" customHeight="1">
      <c r="A$r" s="5" t="inlineStr"><is><t>Supervisor QC:</t></is></c>
      <c r="B$r" s="3" t="inlineStr"><is><t>${escapeXml(inspection.supervisorName)}</t></is></c>
      <c r="D$r" s="5" t="inlineStr"><is><t>Maintenance Service:</t></is></c>
      <c r="E$r" s="3" t="inlineStr"><is><t>${escapeXml(inspection.serviceType)}</t></is></c>
    </row>""")
        r++

        sb.append("""
    <row r="$r" ht="20" customHeight="1">
      <c r="A$r" s="5" t="inlineStr"><is><t>Capacity (840m Limit):</t></is></c>
      <c r="B$r" s="3" t="inlineStr"><is><t>$totalMinutes / 840 Mins ($capacityPct%) - $complianceStatus</t></is></c>
      <c r="D$r" s="5" t="inlineStr"><is><t>Inspection Status:</t></is></c>
      <c r="E$r" s="3" t="inlineStr"><is><t>${escapeXml(inspection.status)}</t></is></c>
    </row>""")
        r++

        // Blank spacer
        r++

        // Table Header (Style 2)
        sb.append("""
    <row r="$r" ht="26" customHeight="1">
      <c r="A$r" s="2" t="inlineStr"><is><t>No</t></is></c>
      <c r="B$r" s="2" t="inlineStr"><is><t>Damage Area</t></is></c>
      <c r="C$r" s="2" t="inlineStr"><is><t>Defect Description</t></is></c>
      <c r="D$r" s="2" t="inlineStr"><is><t>Dimension / Boundary</t></is></c>
      <c r="E$r" s="2" t="inlineStr"><is><t>Severity Level</t></is></c>
      <c r="F$r" s="2" t="inlineStr"><is><t>Duration (Mins)</t></is></c>
      <c r="G$r" s="2" t="inlineStr"><is><t>Photo Evidence</t></is></c>
    </row>""")
        r++

        // Data rows
        findings.forEachIndexed { index, finding ->
            val isEven = index % 2 == 0
            val defaultStyle = if (isEven) "3" else "4"
            val hasPhoto = photoMap.containsKey(finding.id)
            val rowHeight = if (hasPhoto) "72" else "26"

            val severityStyle = when (finding.severityLevel.uppercase()) {
                "LOW", "RINGAN" -> "8"
                "MEDIUM", "SEDANG" -> "9"
                "HIGH", "BERAT" -> "10"
                else -> defaultStyle
            }

            val photoCellText = if (hasPhoto) "" else "No Photo Attached"

            sb.append("""
    <row r="$r" ht="$rowHeight" customHeight="1">
      <c r="A$r" s="7" t="inlineStr"><is><t>${index + 1}</t></is></c>
      <c r="B$r" s="$defaultStyle" t="inlineStr"><is><t>${escapeXml(finding.damageArea)}</t></is></c>
      <c r="C$r" s="$defaultStyle" t="inlineStr"><is><t>${escapeXml(finding.damageDescription)}</t></is></c>
      <c r="D$r" s="$defaultStyle" t="inlineStr"><is><t>${escapeXml(finding.damageDimension)}</t></is></c>
      <c r="E$r" s="$severityStyle" t="inlineStr"><is><t>${escapeXml(finding.severityLevel)}</t></is></c>
      <c r="F$r" s="7"><v>${finding.durationMinutes}</v></c>
      <c r="G$r" s="7" t="inlineStr"><is><t>$photoCellText</t></is></c>
    </row>""")
            r++
        }

        // Summary Row (Style 11)
        sb.append("""
    <row r="$r" ht="26" customHeight="1">
      <c r="A$r" s="11" t="inlineStr"><is><t>TOTAL FINDINGS: ${findings.size} DEFECTS</t></is></c>
      <c r="B$r" s="11"/><c r="C$r" s="11"/><c r="D$r" s="11"/>
      <c r="E$r" s="11" t="inlineStr"><is><t>TOTAL DURATION:</t></is></c>
      <c r="F$r" s="11"><v>$totalMinutes</v></c>
      <c r="G$r" s="11" t="inlineStr"><is><t>$totalHours hrs $remainingMins mins</t></is></c>
    </row>""")
        r++

        // Blank rows
        r += 2

        // Signatures Block
        sb.append("""
    <row r="$r">
      <c r="B$r" s="6" t="inlineStr"><is><t>Prepared &amp; Inspected By:</t></is></c>
      <c r="E$r" s="6" t="inlineStr"><is><t>Reviewed &amp; Approved By:</t></is></c>
    </row>""")
        r++

        sb.append("""
    <row r="$r">
      <c r="B$r" s="5" t="inlineStr"><is><t>Field QC Inspector</t></is></c>
      <c r="E$r" s="5" t="inlineStr"><is><t>Quality Control Supervisor</t></is></c>
    </row>""")
        r += 3

        sb.append("""
    <row r="$r">
      <c r="B$r" s="5" t="inlineStr"><is><t>(${escapeXml(inspection.inspectorName)})</t></is></c>
      <c r="E$r" s="5" t="inlineStr"><is><t>(${escapeXml(inspection.supervisorName)})</t></is></c>
    </row>""")

        sb.append("""
  </sheetData>""")

        if (photos.isNotEmpty()) {
            sb.append("""
  <drawing r:id="rIdDrawing1"/>""")
        }

        sb.append("""
</worksheet>""")
        return sb.toString()
    }

    /**
     * Generates tab-separated text table suitable for copying and pasting directly into Excel or Sheets.
     */
    fun generateCsvContent(
        inspection: InspectionEntity,
        findings: List<FindingEntity>
    ): String = generateTabularText(inspection, findings)

    fun generateTabularText(
        inspection: InspectionEntity,
        findings: List<FindingEntity>
    ): String {
        val sb = StringBuilder()
        sb.append("IFMS QUALITY CONTROL (QC) INSPECTION REPORT\n")
        sb.append("Location:\t${inspection.locationName}\n")
        sb.append("Date:\t${inspection.inspectionDate}\n")
        sb.append("Building Type:\t${inspection.buildingType}\n")
        sb.append("Inspector:\t${inspection.inspectorName}\n")
        sb.append("Supervisor:\t${inspection.supervisorName}\n")
        sb.append("Status:\t${inspection.status}\n\n")

        sb.append("No\tDamage Area\tDefect Description\tDimension / Boundary\tSeverity Level\tDuration (Mins)\tPhoto Evidence\n")
        var totalMinutes = 0
        findings.forEachIndexed { index, finding ->
            totalMinutes += finding.durationMinutes
            val photo = if (!finding.photoUri.isNullOrEmpty()) "Yes (Embedded)" else "No"
            sb.append("${index + 1}\t${finding.damageArea}\t${finding.damageDescription}\t${finding.damageDimension}\t${finding.severityLevel}\t${finding.durationMinutes}\t$photo\n")
        }

        sb.append("\nTOTAL FINDINGS:\t${findings.size} Defects\n")
        sb.append("TOTAL DURATION:\t$totalMinutes Minutes (${totalMinutes / 60}h ${totalMinutes % 60}m)\n")
        sb.append("CAPACITY LIMIT:\t840 Minutes (14 Hours)\n")
        return sb.toString()
    }

    /**
     * Shares the generated Microsoft Excel (.xlsx) file with embedded photos via Android Intent.
     */
    fun shareExportFile(context: Context, inspection: InspectionEntity, findings: List<FindingEntity>) {
        try {
            val xlsxFile = createXlsxFile(context, inspection, findings)
            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                xlsxFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                putExtra(Intent.EXTRA_SUBJECT, "IFMS QC Inspection Report - ${inspection.locationName}")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Attached is the official IFMS QC Inspection Report (.xlsx) for ${inspection.locationName} (${findings.size} defect findings with embedded photos, total duration ${inspection.totalDurationMinutes} mins)."
                )
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share IFMS Excel Report (.xlsx)"))
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to text sharing if file sharing meets permission issues
            val shareTextIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "IFMS QC Inspection Report - ${inspection.locationName}")
                putExtra(Intent.EXTRA_TEXT, generateTabularText(inspection, findings))
            }
            context.startActivity(Intent.createChooser(shareTextIntent, "Share QC Inspection Text"))
            Toast.makeText(context, "Exported as text: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun escapeXml(value: String): String {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
