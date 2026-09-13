package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object PdfExportHelper {

    fun exportDashboardPdf(
        context: Context,
        summary: TotalAdministrationSummary,
        schools: List<SchoolQuotaResult>
    ): File {
        val fileName = "تقرير_إحصائيات_إدارة_العريش_2026_2027.pdf"
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()
        val file = File(exportDir, fileName)

        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 at 72dpi
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas

        drawPdfContent(canvas, summary, schools)

        pdfDoc.finishPage(page)

        FileOutputStream(file).use { fos ->
            pdfDoc.writeTo(fos)
        }
        pdfDoc.close()

        return file
    }

    private fun drawPdfContent(
        canvas: Canvas,
        summary: TotalAdministrationSummary,
        schools: List<SchoolQuotaResult>
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Header Background Banner (Emerald Green)
        paint.color = 0xFF047857.toInt()
        canvas.drawRect(0f, 0f, 595f, 90f, paint)

        // Header Text
        paint.color = 0xFFFFFFFF.toInt()
        paint.textSize = 14f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("جمهورية مصر العربية - وزارة التربية والتعليم - مديرية شمال سيناء", 297f, 26f, paint)

        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("إدارة العريش التعليمية | تقرير الأنصبة وحساب العجز والزيادة", 297f, 52f, paint)

        paint.textSize = 12f
        paint.isFakeBoldText = false
        paint.color = 0xFFD1FAE5.toInt()
        canvas.drawText("العام الدراسي ٢٠٢٦ / ٢٠٢٧ م - قانون ١٥٦ ومنشور الحافز رقم (٨)", 297f, 74f, paint)

        // KPI Section Title
        paint.color = 0xFF065F46.toInt()
        paint.textSize = 14f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("المؤشرات العامة لمدارس إدارة العريش (٦٥ مدرسة):", 560f, 115f, paint)

        // Draw KPI boxes
        val kpis = listOf(
            Pair("إجمالي المدارس", "${summary.totalSchools} مدرسة"),
            Pair("إجمالي الفصول", "${summary.totalClasses} فصل"),
            Pair("إجمالي المعلمين", "${summary.totalTeachers} معلم"),
            Pair("الحصص المتوفرة", "${summary.totalAvailablePeriods} حصة"),
            Pair("الحصص المطلوبة", "${summary.totalRequiredPeriods} حصة"),
            Pair("صافي الفجوة", "${summary.totalPeriodGap} حصة (${if (summary.totalPeriodGap < 0) "عجز" else "زيادة"})")
        )

        var xPos = 560f
        var yPos = 130f
        val boxWidth = 165f
        val boxHeight = 50f
        val margin = 10f

        paint.textAlign = Paint.Align.CENTER
        kpis.forEachIndexed { i, kpi ->
            val col = i % 3
            val row = i / 3
            val left = 560f - (col + 1) * boxWidth - col * margin
            val right = left + boxWidth
            val top = yPos + row * (boxHeight + margin)
            val bottom = top + boxHeight

            // Box bg
            paint.color = if (i == 5) {
                if (summary.totalPeriodGap < 0) 0xFFFEE2E2.toInt() else 0xFFDCFCE7.toInt()
            } else {
                0xFFECFDF5.toInt()
            }
            val rect = RectF(left, top, right, bottom)
            canvas.drawRoundRect(rect, 8f, 8f, paint)

            // Box border
            paint.style = Paint.Style.STROKE
            paint.color = 0xFF047857.toInt()
            paint.strokeWidth = 1f
            canvas.drawRoundRect(rect, 8f, 8f, paint)
            paint.style = Paint.Style.FILL

            // Title
            paint.color = 0xFF475569.toInt()
            paint.textSize = 10f
            paint.isFakeBoldText = false
            canvas.drawText(kpi.first, (left + right) / 2, top + 18f, paint)

            // Value
            paint.color = if (i == 5) {
                if (summary.totalPeriodGap < 0) 0xFFB91C1C.toInt() else 0xFF15803D.toInt()
            } else {
                0xFF0F172A.toInt()
            }
            paint.textSize = 13f
            paint.isFakeBoldText = true
            canvas.drawText(kpi.second, (left + right) / 2, top + 38f, paint)
        }

        // Teacher Cadre Breakdown Table
        val startCadreY = 260f
        paint.color = 0xFF065F46.toInt()
        paint.textSize = 13f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("توزيع المعلمين على كادر الوظائف:", 560f, startCadreY, paint)

        // Cadre headers
        paint.color = 0xFF047857.toInt()
        canvas.drawRect(35f, startCadreY + 10f, 560f, startCadreY + 32f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        paint.textSize = 10f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("كبير معلمين (16)", 80f, startCadreY + 25f, paint)
        canvas.drawText("معلم خبير (18)", 175f, startCadreY + 25f, paint)
        canvas.drawText("معلم أول أ (20)", 270f, startCadreY + 25f, paint)
        canvas.drawText("معلم أول (22)", 365f, startCadreY + 25f, paint)
        canvas.drawText("معلم مساعد/معلم (24)", 480f, startCadreY + 25f, paint)

        // Cadre values
        paint.color = 0xFFF8FAFC.toInt()
        canvas.drawRect(35f, startCadreY + 32f, 560f, startCadreY + 54f, paint)
        paint.color = 0xFF0F172A.toInt()
        paint.isFakeBoldText = true
        paint.textSize = 11f
        canvas.drawText("${summary.totalTeachersSenior}", 80f, startCadreY + 47f, paint)
        canvas.drawText("${summary.totalTeachersExpert}", 175f, startCadreY + 47f, paint)
        canvas.drawText("${summary.totalTeachersFirstA}", 270f, startCadreY + 47f, paint)
        canvas.drawText("${summary.totalTeachersFirst}", 365f, startCadreY + 47f, paint)
        canvas.drawText("${summary.totalTeachersAssistant}", 480f, startCadreY + 47f, paint)

        // Sample top schools table preview
        val startTableY = 340f
        paint.color = 0xFF065F46.toInt()
        paint.textSize = 13f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("عينة من بيانات مدارس العريش وحساب العجز/الزيادة:", 560f, startTableY, paint)

        // Table Header
        paint.color = 0xFF047857.toInt()
        canvas.drawRect(35f, startTableY + 10f, 560f, startTableY + 32f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        paint.textSize = 9f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("الحالة", 65f, startTableY + 24f, paint)
        canvas.drawText("الفجوة", 115f, startTableY + 24f, paint)
        canvas.drawText("المتوفر", 165f, startTableY + 24f, paint)
        canvas.drawText("المطلوب", 215f, startTableY + 24f, paint)
        canvas.drawText("المعلمين", 265f, startTableY + 24f, paint)
        canvas.drawText("الفصول", 315f, startTableY + 24f, paint)
        canvas.drawText("اسم المدرسة", 460f, startTableY + 24f, paint)
        canvas.drawText("م", 545f, startTableY + 24f, paint)

        // Draw 10 sample rows
        var currY = startTableY + 32f
        val previewSchools = schools.take(10)
        previewSchools.forEachIndexed { idx, res ->
            val bg = if (idx % 2 == 0) 0xFFFFFFFF.toInt() else 0xFFF0FDF4.toInt()
            paint.color = bg
            canvas.drawRect(35f, currY, 560f, currY + 20f, paint)

            paint.textSize = 8.5f
            paint.textAlign = Paint.Align.CENTER

            // Status tag
            paint.color = if (res.isDeficit) 0xFFDC2626.toInt() else if (res.isSurplus) 0xFF16A34A.toInt() else 0xFF2563EB.toInt()
            paint.isFakeBoldText = true
            val status = if (res.isDeficit) "عجز" else if (res.isSurplus) "زيادة" else "متوازن"
            canvas.drawText(status, 65f, currY + 14f, paint)

            paint.color = 0xFF1E293B.toInt()
            paint.isFakeBoldText = false
            canvas.drawText("${res.periodGap}", 115f, currY + 14f, paint)
            canvas.drawText("${res.availablePeriods}", 165f, currY + 14f, paint)
            canvas.drawText("${res.totalRequired}", 215f, currY + 14f, paint)
            canvas.drawText("${res.totalTeachers}", 265f, currY + 14f, paint)
            canvas.drawText("${res.totalClasses}", 315f, currY + 14f, paint)

            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(res.school.name, 530f, currY + 14f, paint)

            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("${idx + 1}", 545f, currY + 14f, paint)

            currY += 20f
        }

        // Tri-partite sign-off
        val signY = 650f
        paint.color = 0xFF047857.toInt()
        paint.textSize = 12f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("اعتماد توجيه اللغة العربية والتربية الدينية بإدارة العريش التعليمية", 297f, signY, paint)

        paint.color = 0xFF334155.toInt()
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText("الموجه الأول: ..............................", 130f, signY + 35f, paint)
        canvas.drawText("مدير التعليم الابتدائي: ..............................", 297f, signY + 35f, paint)
        canvas.drawText("مدير عام الإدارة: ..............................", 460f, signY + 35f, paint)

        // Footer & Credits
        val footerY = 810f
        paint.color = 0xFFE2E8F0.toInt()
        canvas.drawLine(35f, footerY - 15f, 560f, footerY - 15f, paint)

        paint.color = 0xFF065F46.toInt()
        paint.textSize = 10f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("تصميم وبرمجة: دكتور/ أحمد حمدي عاشور الغول - تطبيق إدارة أنصبة مدارس العريش ٢٠٢٦/٢٠٢٧م", 297f, footerY, paint)
    }

    fun exportTeachersQuotasPdf(
        context: Context,
        teachers: List<TeacherWithSchoolQuota>,
        summary: TotalAdministrationSummary
    ): File {
        val fileName = "كشف_أنصبة_المعلمين_والتوجيه_الفني_العريش_2026_2027.pdf"
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()
        val file = File(exportDir, fileName)

        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas

        drawTeachersPdfContent(canvas, teachers, summary)

        pdfDoc.finishPage(page)

        FileOutputStream(file).use { fos ->
            pdfDoc.writeTo(fos)
        }
        pdfDoc.close()

        return file
    }

    private fun drawTeachersPdfContent(
        canvas: Canvas,
        teachers: List<TeacherWithSchoolQuota>,
        summary: TotalAdministrationSummary
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Header Background Banner (Emerald Green)
        paint.color = 0xFF047857.toInt()
        canvas.drawRect(0f, 0f, 595f, 90f, paint)

        // Header Text
        paint.color = 0xFFFFFFFF.toInt()
        paint.textSize = 13f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("جمهورية مصر العربية - وزارة التربية والتعليم - مديرية شمال سيناء", 297f, 25f, paint)

        paint.textSize = 17f
        paint.isFakeBoldText = true
        canvas.drawText("إدارة العريش التعليمية | كشف أنصبة المعلمين ومتابعة التوجيه الفني", 297f, 50f, paint)

        paint.textSize = 11f
        paint.isFakeBoldText = false
        paint.color = 0xFFD1FAE5.toInt()
        canvas.drawText("بيان تفصيلي بأنصبة المعلمين وموقف العجز والزيادة - العام الدراسي ٢٠٢٦ / ٢٠٢٧ م", 297f, 72f, paint)

        // Brief KPI strip
        paint.color = 0xFFECFDF5.toInt()
        val kpiRect = RectF(30f, 100f, 565f, 135f)
        canvas.drawRoundRect(kpiRect, 6f, 6f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = 0xFF059669.toInt()
        paint.strokeWidth = 1f
        canvas.drawRoundRect(kpiRect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.color = 0xFF065F46.toInt()
        paint.textSize = 10.5f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(
            "إجمالي المدارس: ${summary.totalSchools}  |  المعلمون: ${summary.totalTeachers}  |  مدارس بعجز: ${summary.schoolsWithDeficitCount}  |  مدارس بزيادة: ${summary.schoolsWithSurplusCount}  |  مدارس متوازنة: ${summary.schoolsBalancedCount}",
            297f,
            122f,
            paint
        )

        // Table Title
        val startTableY = 150f
        paint.color = 0xFF065F46.toInt()
        paint.textSize = 12f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("كشف المعلمين والأنصبة الحالية وموقف المدرسة من العجز / الزيادة:", 565f, startTableY, paint)

        // Table Header
        paint.color = 0xFF047857.toInt()
        canvas.drawRect(30f, startTableY + 8f, 565f, startTableY + 30f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        paint.textSize = 8.5f
        paint.textAlign = Paint.Align.CENTER

        canvas.drawText("حالة المدرسة", 65f, startTableY + 22f, paint)
        canvas.drawText("فجوة المدرسة", 115f, startTableY + 22f, paint)
        canvas.drawText("الفعلي", 155f, startTableY + 22f, paint)
        canvas.drawText("القانوني", 195f, startTableY + 22f, paint)
        canvas.drawText("الرتبة / الكادر", 255f, startTableY + 22f, paint)
        canvas.drawText("المدرسة", 355f, startTableY + 22f, paint)
        canvas.drawText("اسم المعلم", 475f, startTableY + 22f, paint)
        canvas.drawText("م", 552f, startTableY + 22f, paint)

        // Table Rows (Up to 18 rows on standard A4 preview)
        var currY = startTableY + 30f
        val displayList = teachers.take(18)
        displayList.forEachIndexed { idx, item ->
            val bg = if (idx % 2 == 0) 0xFFFFFFFF.toInt() else 0xFFF0FDF4.toInt()
            paint.color = bg
            canvas.drawRect(30f, currY, 565f, currY + 22f, paint)

            paint.textSize = 8f
            paint.textAlign = Paint.Align.CENTER

            // School status tag
            paint.color = if (item.isSchoolDeficit) 0xFFDC2626.toInt() else if (item.isSchoolSurplus) 0xFF16A34A.toInt() else 0xFF2563EB.toInt()
            paint.isFakeBoldText = true
            val statusText = if (item.isSchoolDeficit) "عجز" else if (item.isSchoolSurplus) "زيادة" else "متوازن"
            canvas.drawText(statusText, 65f, currY + 15f, paint)

            paint.color = 0xFF1E293B.toInt()
            paint.isFakeBoldText = false
            val gapStr = if (item.schoolDeficitOrSurplus > 0) "+${item.schoolDeficitOrSurplus}" else "${item.schoolDeficitOrSurplus}"
            canvas.drawText(gapStr, 115f, currY + 15f, paint)

            // Assigned periods
            paint.isFakeBoldText = true
            canvas.drawText("${item.teacher.weeklyPeriods}", 155f, currY + 15f, paint)

            // Legal quota
            paint.isFakeBoldText = false
            paint.color = 0xFF475569.toInt()
            canvas.drawText("${item.legalQuota}", 195f, currY + 15f, paint)

            // Cadre
            paint.color = 0xFF0F172A.toInt()
            canvas.drawText(item.teacher.cadreDegree, 255f, currY + 15f, paint)

            // School name
            paint.textAlign = Paint.Align.RIGHT
            val sName = if (item.schoolName.length > 20) item.schoolName.take(19) + "..." else item.schoolName
            canvas.drawText(sName, 405f, currY + 15f, paint)

            // Teacher Name
            paint.isFakeBoldText = true
            val tName = if (item.teacher.fullName.length > 22) item.teacher.fullName.take(21) + "..." else item.teacher.fullName
            canvas.drawText(tName, 535f, currY + 15f, paint)

            // Index
            paint.textAlign = Paint.Align.CENTER
            paint.isFakeBoldText = false
            paint.color = 0xFF64748B.toInt()
            canvas.drawText("${idx + 1}", 552f, currY + 15f, paint)

            currY += 22f
        }

        // Tri-partite sign-off
        val signY = 670f
        paint.color = 0xFF047857.toInt()
        paint.textSize = 12f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("اعتماد التوجيه الفني للغة العربية والتربية الدينية بإدارة العريش", 297f, signY, paint)

        paint.color = 0xFF334155.toInt()
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText("الموجه الأول: ..............................", 130f, signY + 35f, paint)
        canvas.drawText("مدير التعليم الابتدائي: ..............................", 297f, signY + 35f, paint)
        canvas.drawText("مدير عام الإدارة: ..............................", 460f, signY + 35f, paint)

        // Footer & Credits
        val footerY = 810f
        paint.color = 0xFFE2E8F0.toInt()
        canvas.drawLine(30f, footerY - 15f, 565f, footerY - 15f, paint)

        paint.color = 0xFF065F46.toInt()
        paint.textSize = 10f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("تصميم وبرمجة: دكتور/ أحمد حمدي عاشور الغول - تطبيق إدارة أنصبة مدارس العريش ٢٠٢٦/٢٠٢٧م", 297f, footerY, paint)
    }

    fun sharePdfFile(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, file.name)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "مشاركة / فتح بـ PDF Viewer:"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
