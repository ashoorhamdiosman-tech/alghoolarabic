package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.entity.SupervisionVisitEntity
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

object CsvExportHelper {

    fun exportTeachersCsv(context: Context, teachers: List<TeacherWithSchoolQuota>): File {
        val fileName = "كشف_بيانات_المعلمين_والأنصبة_العريش_2026_2027.csv"
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()
        val file = File(exportDir, fileName)

        FileOutputStream(file).use { fos ->
            // Write UTF-8 BOM so Arabic displays properly in Excel
            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                // Header
                writer.write("م,اسم المعلم,كود المعلم,الرقم القومي,المدرسة,المادة,الكادر الوظيفي,المؤهل الدراسي,سنة التخرج,الهاتف,العنوان,النصاب القانوني,الحصص الفعلية,فرق النصاب,حالة المدرسة,فجوة حصص المدرسة\n")

                teachers.forEachIndexed { index, item ->
                    val t = item.teacher
                    val statusStr = if (item.isSchoolDeficit) "عجز (${item.schoolDeficitOrSurplus} حصة)"
                    else if (item.isSchoolSurplus) "زيادة (+${item.schoolDeficitOrSurplus} حصة)"
                    else "متوازن"

                    val line = listOf(
                        (index + 1).toString(),
                        "\"${t.fullName}\"",
                        "\"${t.teacherCode}\"",
                        "\"${t.nationalId}\"",
                        "\"${item.schoolName}\"",
                        "\"${t.subject}\"",
                        "\"${t.cadreDegree}\"",
                        "\"${t.qualification}\"",
                        "\"${t.qualificationDate}\"",
                        "\"${t.phone}\"",
                        "\"${t.address}\"",
                        item.legalQuota.toString(),
                        t.weeklyPeriods.toString(),
                        (if (item.quotaDifference > 0) "+${item.quotaDifference}" else "${item.quotaDifference}"),
                        "\"$statusStr\"",
                        item.schoolDeficitOrSurplus.toString()
                    ).joinToString(",")
                    writer.write(line + "\n")
                }
            }
        }
        return file
    }

    fun exportSchoolsCsv(context: Context, results: List<SchoolQuotaResult>): File {
        val fileName = "تقرير_أنصبة_مدارس_العريش_2026_2027.csv"
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()
        val file = File(exportDir, fileName)

        FileOutputStream(file).use { fos ->
            // Write UTF-8 BOM so Excel opens Arabic correctly
            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                // Header
                writer.write("م,اسم المدرسة,فصول (1-3),فصول (4-6),إجمالي الفصول,مطلوب لغة عربية,مطلوب تربية دينية,إجمالي المطلوب,معلم مساعد/معلم,معلم أول,معلم أول أ,معلم خبير,كبير معلمين,إجمالي المعلمين,الحصص المتوفرة,فجوة الحصص,المكافئ بالمعلمين,الحالة,مدير المدرسة,هاتف المدير,الموجه المتابع\n")

                results.forEachIndexed { index, res ->
                    val s = res.school
                    val statusStr = if (res.isDeficit) "عجز (${-res.periodGap} حصة)" else if (res.isSurplus) "زيادة (${res.periodGap} حصة)" else "متوازن"
                    val line = listOf(
                        (index + 1).toString(),
                        "\"${s.name}\"",
                        s.classesPrimary.toString(),
                        s.classesUpper.toString(),
                        res.totalClasses.toString(),
                        res.arabicRequired.toString(),
                        res.religionRequired.toString(),
                        res.totalRequired.toString(),
                        s.teachersAssistant.toString(),
                        s.teachersFirst.toString(),
                        s.teachersFirstA.toString(),
                        s.teachersExpert.toString(),
                        s.teachersSenior.toString(),
                        res.totalTeachers.toString(),
                        res.availablePeriods.toString(),
                        res.periodGap.toString(),
                        QuotaCalculations.formatDouble(res.teacherEquivalent),
                        "\"$statusStr\"",
                        "\"${s.principalName}\"",
                        "\"${s.principalPhone}\"",
                        "\"${s.supervisorName}\""
                    ).joinToString(",")
                    writer.write(line + "\n")
                }
            }
        }
        return file
    }

    fun exportVisitsCsv(context: Context, visits: List<SupervisionVisitEntity>): File {
        val fileName = "سجل_متابعات_العريش.csv"
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()
        val file = File(exportDir, fileName)

        FileOutputStream(file).use { fos ->
            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                writer.write("م,التاريخ,المدرسة,الموجه,نوع الزيارة,الهدف,الحالة,سبب التأخير,الملاحظات,توقيع الموجه,توقيع المدير\n")

                visits.forEachIndexed { index, v ->
                    val line = listOf(
                        (index + 1).toString(),
                        "\"${v.visitDate}\"",
                        "\"${v.schoolName}\"",
                        "\"${v.supervisorName}\"",
                        "\"${v.visitType}\"",
                        "\"${v.objective}\"",
                        "\"${v.status}\"",
                        "\"${v.delayReason}\"",
                        "\"${v.notes}\"",
                        "\"${v.supervisorSignature}\"",
                        "\"${v.principalSignature}\""
                    ).joinToString(",")
                    writer.write(line + "\n")
                }
            }
        }
        return file
    }

    fun shareCsvFile(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, file.name)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "مشاركة / فتح بـ Excel:"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
