package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.entity.SupervisionVisitEntity
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object DocxExportHelper {

    fun generateMonthlyPlanDocx(
        context: Context,
        supervisorName: String,
        month: String,
        academicYear: String,
        visits: List<SupervisionVisitEntity>
    ): File {
        val fileName = "خطة_المتابعة_${supervisorName.replace(" ", "_")}_$month.docx"
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()
        val docxFile = File(exportDir, fileName)

        FileOutputStream(docxFile).use { fos ->
            ZipOutputStream(fos).use { zos ->
                // 1. [Content_Types].xml
                addZipEntry(zos, "[Content_Types].xml", getContentTypesXml())

                // 2. _rels/.rels
                addZipEntry(zos, "_rels/.rels", getRelsXml())

                // 3. word/_rels/document.xml.rels
                addZipEntry(zos, "word/_rels/document.xml.rels", getDocRelsXml())

                // 4. word/styles.xml
                addZipEntry(zos, "word/styles.xml", getStylesXml())

                // 5. word/document.xml
                val docXml = buildDocumentXml(supervisorName, month, academicYear, visits)
                addZipEntry(zos, "word/document.xml", docXml)
            }
        }

        return docxFile
    }

    fun shareOrOpenFile(context: Context, file: File, mimeType: String = "application/vnd.openxmlformats-officedocument.wordprocessingml.document") {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, file.name)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "مشاركة / فتح الملف عبر:"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addZipEntry(zos: ZipOutputStream, entryName: String, content: String) {
        val entry = ZipEntry(entryName)
        zos.putNextEntry(entry)
        val bytes = content.toByteArray(StandardCharsets.UTF_8)
        zos.write(bytes, 0, bytes.size)
        zos.closeEntry()
    }

    private fun getContentTypesXml(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
  <Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
</Types>"""

    private fun getRelsXml(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>"""

    private fun getDocRelsXml(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""

    private fun getStylesXml(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:docDefaults>
    <w:rPrDefault>
      <w:rPr>
        <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri" w:cs="Traditional Arabic"/>
        <w:sz w:val="24"/>
        <w:szCs w:val="28"/>
        <w:lang w:bidi="ar-EG"/>
      </w:rPr>
    </w:rPrDefault>
    <w:pPrDefault>
      <w:pPr>
        <w:bidi/>
        <w:jc w:val="right"/>
      </w:pPr>
    </w:pPrDefault>
  </w:docDefaults>
</w:styles>"""

    private fun escapeXml(str: String): String {
        return str.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun buildDocumentXml(
        supervisorName: String,
        month: String,
        academicYear: String,
        visits: List<SupervisionVisitEntity>
    ): String {
        val rowsBuilder = StringBuilder()

        // Table Header
        rowsBuilder.append("""
        <w:tr>
          <w:trPr><w:tblHeader/></w:trPr>
          <w:tc><w:tcPr><w:tcW w:w="600" w:type="dxa"/><w:shd w:val="clear" w:color="auto" w:fill="047857"/></w:tcPr><w:p><w:pPr><w:jc w:val="center"/><w:bidi/></w:pPr><w:r><w:rPr><w:b/><w:color w:val="FFFFFF"/><w:sz w:val="22"/><w:szCs w:val="22"/></w:rPr><w:t>م</w:t></w:r></w:p></w:tc>
          <w:tc><w:tcPr><w:tcW w:w="1600" w:type="dxa"/><w:shd w:val="clear" w:color="auto" w:fill="047857"/></w:tcPr><w:p><w:pPr><w:jc w:val="center"/><w:bidi/></w:pPr><w:r><w:rPr><w:b/><w:color w:val="FFFFFF"/><w:sz w:val="22"/><w:szCs w:val="22"/></w:rPr><w:t>اليوم والتاريخ</w:t></w:r></w:p></w:tc>
          <w:tc><w:tcPr><w:tcW w:w="2200" w:type="dxa"/><w:shd w:val="clear" w:color="auto" w:fill="047857"/></w:tcPr><w:p><w:pPr><w:jc w:val="center"/><w:bidi/></w:pPr><w:r><w:rPr><w:b/><w:color w:val="FFFFFF"/><w:sz w:val="22"/><w:szCs w:val="22"/></w:rPr><w:t>اسم المدرسة المزارة</w:t></w:r></w:p></w:tc>
          <w:tc><w:tcPr><w:tcW w:w="1300" w:type="dxa"/><w:shd w:val="clear" w:color="auto" w:fill="047857"/></w:tcPr><w:p><w:pPr><w:jc w:val="center"/><w:bidi/></w:pPr><w:r><w:rPr><w:b/><w:color w:val="FFFFFF"/><w:sz w:val="22"/><w:szCs w:val="22"/></w:rPr><w:t>نوع الزيارة</w:t></w:r></w:p></w:tc>
          <w:tc><w:tcPr><w:tcW w:w="1800" w:type="dxa"/><w:shd w:val="clear" w:color="auto" w:fill="047857"/></w:tcPr><w:p><w:pPr><w:jc w:val="center"/><w:bidi/></w:pPr><w:r><w:rPr><w:b/><w:color w:val="FFFFFF"/><w:sz w:val="22"/><w:szCs w:val="22"/></w:rPr><w:t>الهدف من الزيارة</w:t></w:r></w:p></w:tc>
          <w:tc><w:tcPr><w:tcW w:w="1600" w:type="dxa"/><w:shd w:val="clear" w:color="auto" w:fill="047857"/></w:tcPr><w:p><w:pPr><w:jc w:val="center"/><w:bidi/></w:pPr><w:r><w:rPr><w:b/><w:color w:val="FFFFFF"/><w:sz w:val="22"/><w:szCs w:val="22"/></w:rPr><w:t>الإدارة المدرسية</w:t></w:r></w:p></w:tc>
          <w:tc><w:tcPr><w:tcW w:w="1400" w:type="dxa"/><w:shd w:val="clear" w:color="auto" w:fill="047857"/></w:tcPr><w:p><w:pPr><w:jc w:val="center"/><w:bidi/></w:pPr><w:r><w:rPr><w:b/><w:color w:val="FFFFFF"/><w:sz w:val="22"/><w:szCs w:val="22"/></w:rPr><w:t>الملاحظات</w:t></w:r></w:p></w:tc>
        </w:tr>
        """.trimIndent())

        // Table Rows
        visits.forEachIndexed { index, visit ->
            val bg = if (index % 2 == 0) "ECFDF5" else "FFFFFF"
            val num = (index + 1).toString()
            val dateStr = escapeXml(visit.visitDate)
            val school = escapeXml(visit.schoolName)
            val type = escapeXml(visit.visitType)
            val obj = escapeXml(visit.objective)
            val notes = escapeXml(visit.notes.ifEmpty { "لا توجد ملاحظات" })

            rowsBuilder.append("""
            <w:tr>
              <w:tc><w:tcPr><w:tcW w:w="600" w:type="dxa"/><w:shd w:val="clear" w:color="auto" w:fill="$bg"/></w:tcPr><w:p><w:pPr><w:jc w:val="center"/><w:bidi/></w:pPr><w:r><w:rPr><w:sz w:val="20"/><w:szCs w:val="20"/></w:rPr><w:t>$num</w:t></w:r></w:p></w:tc>
              <w:tc><w:tcPr><w:tcW w:w="1600" w:type="dxa"/><w:shd w:val="clear" w:color="auto" w:fill="$bg"/></w:tcPr><w:p><w:pPr><w:jc w:val="center"/><w:bidi/></w:pPr><w:r><w:rPr><w:sz w:val="20"/><w:szCs w:val="20"/></w:rPr><w:t>$dateStr</w:t></w:r></w:p></w:tc>
              <w:tc><w:tcPr><w:tcW w:w="2200" w:type="dxa"/><w:shd w:val="clear" w:color="auto" w:fill="$bg"/></w:tcPr><w:p><w:pPr><w:jc w:val="right"/><w:bidi/></w:pPr><w:r><w:rPr><w:b/><w:sz w:val="20"/><w:szCs w:val="20"/></w:rPr><w:t>$school</w:t></w:r></w:p></w:tc>
              <w:tc><w:tcPr><w:tcW w:w="1300" w:type="dxa"/><w:shd w:val="clear" w:color="auto" w:fill="$bg"/></w:tcPr><w:p><w:pPr><w:jc w:val="center"/><w:bidi/></w:pPr><w:r><w:rPr><w:sz w:val="20"/><w:szCs w:val="20"/></w:rPr><w:t>$type</w:t></w:r></w:p></w:tc>
              <w:tc><w:tcPr><w:tcW w:w="1800" w:type="dxa"/><w:shd w:val="clear" w:color="auto" w:fill="$bg"/></w:tcPr><w:p><w:pPr><w:jc w:val="right"/><w:bidi/></w:pPr><w:r><w:rPr><w:sz w:val="20"/><w:szCs w:val="20"/></w:rPr><w:t>$obj</w:t></w:r></w:p></w:tc>
              <w:tc><w:tcPr><w:tcW w:w="1600" w:type="dxa"/><w:shd w:val="clear" w:color="auto" w:fill="$bg"/></w:tcPr><w:p><w:pPr><w:jc w:val="center"/><w:bidi/></w:pPr><w:r><w:rPr><w:sz w:val="20"/><w:szCs w:val="20"/></w:rPr><w:t>مدير / وكيل المدرسة</w:t></w:r></w:p></w:tc>
              <w:tc><w:tcPr><w:tcW w:w="1400" w:type="dxa"/><w:shd w:val="clear" w:color="auto" w:fill="$bg"/></w:tcPr><w:p><w:pPr><w:jc w:val="right"/><w:bidi/></w:pPr><w:r><w:rPr><w:sz w:val="20"/><w:szCs w:val="20"/></w:rPr><w:t>$notes</w:t></w:r></w:p></w:tc>
            </w:tr>
            """.trimIndent())
        }

        val escSup = escapeXml(supervisorName)
        val escMonth = escapeXml(month)
        val escYear = escapeXml(academicYear)

        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:body>
    <!-- Official Header -->
    <w:p>
      <w:pPr><w:jc w:val="center"/><w:bidi/></w:pPr>
      <w:r><w:rPr><w:b/><w:sz w:val="28"/><w:szCs w:val="28"/><w:color w:val="047857"/></w:rPr><w:t>جمهورية مصر العربية</w:t></w:r>
    </w:p>
    <w:p>
      <w:pPr><w:jc w:val="center"/><w:bidi/></w:pPr>
      <w:r><w:rPr><w:b/><w:sz w:val="26"/><w:szCs w:val="26"/></w:rPr><w:t>وزارة التربية والتعليم والتعليم الفني</w:t></w:r>
    </w:p>
    <w:p>
      <w:pPr><w:jc w:val="center"/><w:bidi/></w:pPr>
      <w:r><w:rPr><w:b/><w:sz w:val="26"/><w:szCs w:val="26"/></w:rPr><w:t>مديرية التربية والتعليم بشمال سيناء</w:t></w:r>
    </w:p>
    <w:p>
      <w:pPr><w:jc w:val="center"/><w:bidi/></w:pPr>
      <w:r><w:rPr><w:b/><w:sz w:val="26"/><w:szCs w:val="26"/></w:rPr><w:t>إدارة العريش التعليمية - توجيه اللغة العربية والتربية الدينية</w:t></w:r>
    </w:p>
    
    <w:p><w:pPr><w:jc w:val="center"/><w:bidi/></w:pPr><w:r><w:t></w:t></w:r></w:p>
    
    <!-- Title Banner -->
    <w:p>
      <w:pPr>
        <w:jc w:val="center"/><w:bidi/>
        <w:pBdr>
          <w:bottom w:val="single" w:sz="18" w:space="4" w:color="047857"/>
        </w:pBdr>
      </w:pPr>
      <w:r>
        <w:rPr><w:b/><w:sz w:val="32"/><w:szCs w:val="32"/><w:color w:val="047857"/></w:rPr>
        <w:t>خطة المتابعة الميدانية الشهرية لشهر $escMonth ($escYear م)</w:t>
      </w:r>
    </w:p>

    <!-- Meta Info -->
    <w:p>
      <w:pPr><w:jc w:val="right"/><w:bidi/></w:pPr>
      <w:r><w:rPr><w:b/><w:sz w:val="24"/><w:szCs w:val="24"/></w:rPr><w:t>الموجه المتابع: </w:t></w:r>
      <w:r><w:rPr><w:sz w:val="24"/><w:szCs w:val="24"/><w:color w:val="065F46"/></w:rPr><w:t>$escSup</w:t></w:r>
      <w:r><w:rPr><w:sz w:val="24"/><w:szCs w:val="24"/></w:rPr><w:t>     |     العام الدراسي: </w:t></w:r>
      <w:r><w:rPr><w:b/><w:sz w:val="24"/><w:szCs w:val="24"/></w:rPr><w:t>$escYear م</w:t></w:r>
      <w:r><w:rPr><w:sz w:val="24"/><w:szCs w:val="24"/></w:rPr><w:t>     |     عدد المدارس المجدولة: </w:t></w:r>
      <w:r><w:rPr><w:b/><w:sz w:val="24"/><w:szCs w:val="24"/><w:color w:val="047857"/></w:rPr><w:t>${visits.size}</w:t></w:r>
    </w:p>

    <!-- Table -->
    <w:tbl>
      <w:tblPr>
        <w:tblW w:w="10500" w:type="dxa"/>
        <w:bidiVisual/>
        <w:tblBorders>
          <w:top w:val="single" w:sz="6" w:space="0" w:color="047857"/>
          <w:left w:val="single" w:sz="6" w:space="0" w:color="047857"/>
          <w:bottom w:val="single" w:sz="6" w:space="0" w:color="047857"/>
          <w:right w:val="single" w:sz="6" w:space="0" w:color="047857"/>
          <w:insideH w:val="single" w:sz="4" w:space="0" w:color="D1FAE5"/>
          <w:insideV w:val="single" w:sz="4" w:space="0" w:color="D1FAE5"/>
        </w:tblBorders>
      </w:tblPr>
      $rowsBuilder
    </w:tbl>

    <w:p><w:pPr><w:jc w:val="center"/><w:bidi/></w:pPr><w:r><w:t></w:t></w:r></w:p>

    <!-- 3-Tier Approval & Seal Section -->
    <w:p>
      <w:pPr><w:jc w:val="center"/><w:bidi/></w:pPr>
      <w:r><w:rPr><w:b/><w:sz w:val="24"/><w:szCs w:val="24"/><w:color w:val="065F46"/></w:rPr><w:t>=== قسم الاعتماد والتصديق الثلاثي ===</w:t></w:r>
    </w:p>

    <w:tbl>
      <w:tblPr>
        <w:tblW w:w="10500" w:type="dxa"/>
        <w:bidiVisual/>
        <w:tblBorders>
          <w:top w:val="none"/><w:left w:val="none"/><w:bottom w:val="none"/><w:right w:val="none"/>
          <w:insideH w:val="none"/><w:insideV w:val="none"/>
        </w:tblBorders>
      </w:tblPr>
      <w:tr>
        <!-- Level 1: Supervisor -->
        <w:tc>
          <w:tcPr><w:tcW w:w="3500" w:type="dxa"/></w:tcPr>
          <w:p><w:pPr><w:jc w:val="center"/><w:bidi/></w:pPr><w:r><w:rPr><w:b/><w:sz w:val="22"/><w:szCs w:val="22"/></w:rPr><w:t>الموجه المتابع (مُعد الخطة)</w:t></w:r></w:p>
          <w:p><w:pPr><w:jc w:val="center"/><w:bidi/></w:pPr><w:r><w:rPr><w:sz w:val="20"/><w:szCs w:val="20"/></w:rPr><w:t>الاسم: $escSup</w:t></w:r></w:p>
          <w:p><w:pPr><w:jc w:val="center"/><w:bidi/></w:pPr><w:r><w:rPr><w:sz w:val="20"/><w:szCs w:val="20"/></w:rPr><w:t>التوقيع: ..........................</w:t></w:r></w:p>
          <w:p><w:pPr><w:jc w:val="center"/><w:bidi/></w:pPr><w:r><w:rPr><w:sz w:val="20"/><w:szCs w:val="20"/></w:rPr><w:t>التاريخ:      /     / 2026 م</w:t></w:r></w:p>
        </w:tc>
        <!-- Level 2: Senior Supervisor -->
        <w:tc>
          <w:tcPr><w:tcW w:w="3500" w:type="dxa"/></w:tcPr>
          <w:p><w:pPr><w:jc w:val="center"/><w:bidi/></w:pPr><w:r><w:rPr><w:b/><w:sz w:val="22"/><w:szCs w:val="22"/></w:rPr><w:t>موجه أول المادة (اعتماد الخطة)</w:t></w:r></w:p>
          <w:p><w:pPr><w:jc w:val="center"/><w:bidi/></w:pPr><w:r><w:rPr><w:sz w:val="20"/><w:szCs w:val="20"/></w:rPr><w:t>الاسم: أ. محمود عبد الرحمن</w:t></w:r></w:p>
          <w:p><w:pPr><w:jc w:val="center"/><w:bidi/></w:pPr><w:r><w:rPr><w:sz w:val="20"/><w:szCs w:val="20"/></w:rPr><w:t>التوقيع: ..........................</w:t></w:r></w:p>
          <w:p><w:pPr><w:jc w:val="center"/><w:bidi/></w:pPr><w:r><w:rPr><w:sz w:val="20"/><w:szCs w:val="20"/></w:rPr><w:t>التاريخ:      /     / 2026 م</w:t></w:r></w:p>
        </w:tc>
        <!-- Level 3: Directorate General Manager -->
        <w:tc>
          <w:tcPr><w:tcW w:w="3500" w:type="dxa"/></w:tcPr>
          <w:p><w:pPr><w:jc w:val="center"/><w:bidi/></w:pPr><w:r><w:rPr><w:b/><w:sz w:val="22"/><w:szCs w:val="22"/></w:rPr><w:t>مدير عام الإدارة التعليمية بالعريش</w:t></w:r></w:p>
          <w:p><w:pPr><w:jc w:val="center"/><w:bidi/></w:pPr><w:r><w:rPr><w:sz w:val="20"/><w:szCs w:val="20"/></w:rPr><w:t>(تصديق ورئاسة)</w:t></w:r></w:p>
          <w:p><w:pPr><w:jc w:val="center"/><w:bidi/></w:pPr><w:r><w:rPr><w:sz w:val="20"/><w:szCs w:val="20"/></w:rPr><w:t>التوقيع: ..........................</w:t></w:r></w:p>
          <w:p><w:pPr><w:jc w:val="center"/><w:bidi/></w:pPr><w:r><w:rPr><w:sz w:val="20"/><w:szCs w:val="20"/></w:rPr><w:t>[ خاتم شعار الجمهورية والإدارة ]</w:t></w:r></w:p>
        </w:tc>
      </w:tr>
    </w:tbl>

    <w:p><w:pPr><w:jc w:val="center"/><w:bidi/></w:pPr><w:r><w:t></w:t></w:r></w:p>

    <!-- App Credits -->
    <w:p>
      <w:pPr><w:jc w:val="center"/><w:bidi/></w:pPr>
      <w:r>
        <w:rPr><w:sz w:val="18"/><w:szCs w:val="18"/><w:color w:val="64748B"/></w:rPr>
        <w:t>تم استخراج هذا النموذج رسمياً بواسطة تطبيق أنصبة مدارس العريش - تصميم وبرمجة: د/ أحمد حمدي عاشور الغول</w:t>
      </w:r>
    </w:p>

    <w:sectPr>
      <w:pgSz w:w="12240" w:h="15840" w:orient="portrait"/>
      <w:pgMar w:top="1000" w:right="1000" w:bottom="1000" w:left="1000"/>
    </w:sectPr>
  </w:body>
</w:document>"""
    }
}
