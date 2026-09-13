package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.SupervisionVisitEntity
import com.example.ui.components.DeficitSurplusDistributionGauge
import com.example.ui.components.SupervisorMonthlyProgressSection
import com.example.ui.components.TeacherCadreBarChart
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel
import com.example.util.QuotaCalculations

@Composable
fun DashboardScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val upcoming48h by viewModel.upcomingVisits48h.collectAsStateWithLifecycle()
    val delayedVisits by viewModel.delayedVisits.collectAsStateWithLifecycle()
    val supervisorProgress by viewModel.supervisorProgressList.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        // 1. Header Banner & Academic Year
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_header_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldPrimary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "إدارة العريش التعليمية",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "نظام حساب أنصبة المعلمين ومتابعة العجز والزيادة",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MintContainer
                            )
                        }
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "٢٠٢٦ / ٢٠٢٧ م",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.exportDashboardPdf(context) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_pdf_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                        ) {
                            Icon(
                                Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تصدير PDF", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.exportSchoolsCsv(context) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_excel_btn"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Icon(
                                Icons.Default.TableChart,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تصدير Excel", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 1.5 Visual Alert: Severe Deficit Schools (عجز حاد في الأنصبة)
        if (summary.schoolsWithCriticalDeficitCount > 0) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("critical_deficit_dashboard_alert_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CriticalDeficitBg),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CriticalDeficitRed))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CriticalDeficitRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "تنبيه طارئ: يوجد ${summary.schoolsWithCriticalDeficitCount} مدارس تعاني من عجز حاد في الأنصبة",
                                    fontWeight = FontWeight.Bold,
                                    color = CriticalDeficitText,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "مدارس بنقص 20 حصة أو أكثر (معلم فأكثر) تتطلب إعادة توزيع أو ندب عاجل من المدارس ذات الزيادة",
                                    color = CriticalDeficitText.copy(alpha = 0.85f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Visual Alert: Upcoming 48 Hours Visits
        if (upcoming48h.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upcoming_48h_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AlertAmberBg),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AlertAmber))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(AlertAmber),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "تنبيه الزيارات القادمة خلال ٤٨ ساعة (${upcoming48h.size} زيارة)",
                                    fontWeight = FontWeight.Bold,
                                    color = AlertAmberText,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "مواعيد الزيارات الميدانية المجدولة لتوجيه العريش",
                                    color = AlertAmberText.copy(alpha = 0.85f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        upcoming48h.forEach { visit ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = visit.schoolName,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "الموجه: ${visit.supervisorName} | ${visit.objective}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate600
                                        )
                                    }
                                    Surface(
                                        color = AlertAmberBg,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = visit.visitDate,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = AlertAmberText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Visual Alert: Delayed / Postponed Visits (Interactive Click to Reschedule or Record Reason)
        if (delayedVisits.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("delayed_visits_alert_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DeficitRedBg),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DeficitRed))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(DeficitRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "تنبيه: زيارات ميدانية متأخرة أو مؤجلة (${delayedVisits.size})",
                                    fontWeight = FontWeight.Bold,
                                    color = DeficitRedText,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "اضغط على أي زيارة لتسجيل أسباب التأخير أو إعادة الجدولة",
                                    color = DeficitRedText.copy(alpha = 0.85f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        delayedVisits.forEach { visit ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        viewModel.selectedVisitForDelayModal.value = visit
                                    }
                                    .testTag("delayed_visit_item_${visit.id}"),
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = visit.schoolName,
                                            fontWeight = FontWeight.Bold,
                                            color = DeficitRedText,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "الموجه: ${visit.supervisorName} | التاريخ: ${visit.visitDate}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate600
                                        )
                                        if (visit.delayReason.isNotBlank()) {
                                            Text(
                                                text = "السبب: ${visit.delayReason}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = DeficitRed,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                    FilledTonalButton(
                                        onClick = { viewModel.selectedVisitForDelayModal.value = visit },
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = DeficitRedBg,
                                            contentColor = DeficitRedText
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("معالجة", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. KPI Grid
        item {
            Text(
                text = "شبكة بطاقات الأداء العام (KPI Grid)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        title = "إجمالي المدارس",
                        value = "${summary.totalSchools}",
                        subtitle = "مدارس إدارة العريش",
                        icon = Icons.Default.School,
                        color = EmeraldPrimary,
                        bgColor = MintContainer,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "إجمالي الفصول",
                        value = "${summary.totalClasses}",
                        subtitle = "${summary.totalClassesPrimary} أولى + ${summary.totalClassesUpper} عليا",
                        icon = Icons.Default.Class,
                        color = Color(0xFF0D9488),
                        bgColor = Color(0xFFCCFBF1),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        title = "إجمالي المعلمين",
                        value = "${summary.totalTeachers}",
                        subtitle = "عربى ودين بجميع المدارس",
                        icon = Icons.Default.Groups,
                        color = Color(0xFF0284C7),
                        bgColor = Color(0xFFE0F2FE),
                        modifier = Modifier.weight(1f)
                    )
                    val gap = summary.totalPeriodGap
                    val isDef = gap < 0
                    KpiCard(
                        title = if (isDef) "صافي العجز (حصص)" else "صافي الزيادة (حصص)",
                        value = "${if (isDef) -gap else gap} حصة",
                        subtitle = "مكافئ: ${QuotaCalculations.formatDouble(summary.totalTeacherEquivalent)} معلم",
                        icon = if (isDef) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                        color = if (isDef) DeficitRedText else SurplusGreenText,
                        bgColor = if (isDef) DeficitRedBg else SurplusGreenBg,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 3: Available vs Required Periods
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "الحصص المتوفرة",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate600
                            )
                            Text(
                                text = "${summary.totalAvailablePeriods}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                            Text(
                                text = "وفق أنصبة الكادر",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate500
                            )
                        }

                        Divider(
                            modifier = Modifier
                                .height(40.dp)
                                .width(1.dp),
                            color = Slate200
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "الحصص المطلوبة",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate600
                            )
                            Text(
                                text = "${summary.totalRequiredPeriods}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706)
                            )
                            Text(
                                text = "لغة عربية ودين",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate500
                            )
                        }
                    }
                }
            }
        }

        // 5. Interactive Bar Chart for Teacher Cadre
        item {
            TeacherCadreBarChart(
                assistantCount = summary.totalTeachersAssistant,
                firstCount = summary.totalTeachersFirst,
                firstACount = summary.totalTeachersFirstA,
                expertCount = summary.totalTeachersExpert,
                seniorCount = summary.totalTeachersSenior
            )
        }

        // 6. Teacher Cadre Aggregation Table
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "جدول كادر المعلمين المعتمد والحصص المغطاة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "تجميع أعداد المعلمين وفق قانون كادر المعلمين رقم 156",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val cadreRows = listOf(
                        Triple("معلم مساعد / معلم", summary.totalTeachersAssistant, 24),
                        Triple("معلم أول", summary.totalTeachersFirst, 22),
                        Triple("معلم أول أ", summary.totalTeachersFirstA, 20),
                        Triple("معلم خبير", summary.totalTeachersExpert, 18),
                        Triple("كبير معلمين", summary.totalTeachersSenior, 16)
                    )

                    // Header
                    Surface(
                        color = EmeraldPrimary,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("المسمى على الكادر", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(2f))
                            Text("العدد", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                            Text("النصاب", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                            Text("إجمالي الحصص", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1.5f))
                        }
                    }

                    cadreRows.forEachIndexed { idx, row ->
                        val bg = if (idx % 2 == 0) MintLight else Color.Transparent
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bg)
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(row.first, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(2f))
                            Text("${row.second}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            Text("${row.third}", style = MaterialTheme.typography.bodySmall, color = Slate600, modifier = Modifier.weight(1f))
                            Text("${row.second * row.third} حصة", fontWeight = FontWeight.Bold, color = EmeraldPrimary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.5f))
                        }
                    }
                }
            }
        }

        // 7. Subject Distribution Table (Arabic vs Religion)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "جدول توزيع المواد وخطة الحصص الأسبوعية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "تفصيل الحصص المطلوبة مقسمة بين الصفوف الأولى (1–3) والصفوف العليا (4–6)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val arabicPrimary = summary.totalClassesPrimary * 9
                    val arabicUpper = summary.totalClassesUpper * 9
                    val religionPrimary = summary.totalClassesPrimary * 5
                    val religionUpper = summary.totalClassesUpper * 4

                    // Header
                    Surface(
                        color = Color(0xFF065F46),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("المادة الدراسية", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(2f))
                            Text("صفوف 1-3", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1.2f))
                            Text("صفوف 4-6", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1.2f))
                            Text("إجمالي الحصص", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1.5f))
                        }
                    }

                    // Arabic Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MintLight)
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("اللغة العربية (9 حصص/فصل)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(2f))
                        Text("$arabicPrimary حصة", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.2f))
                        Text("$arabicUpper حصة", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.2f))
                        Text("${summary.totalArabicRequired} حصة", fontWeight = FontWeight.Bold, color = EmeraldPrimary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.5f))
                    }

                    // Religion Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("التربية الدينية (5 أولى / 4 عليا)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(2f))
                        Text("$religionPrimary حصة", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.2f))
                        Text("$religionUpper حصة", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.2f))
                        Text("${summary.totalReligionRequired} حصة", fontWeight = FontWeight.Bold, color = EmeraldPrimary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.5f))
                    }
                }
            }
        }

        // 8. Deficit vs Surplus Distribution Gauge
        item {
            DeficitSurplusDistributionGauge(
                deficitCount = summary.schoolsWithDeficitCount,
                balancedCount = summary.schoolsBalancedCount,
                surplusCount = summary.schoolsWithSurplusCount,
                totalCount = summary.totalSchools,
                criticalDeficitCount = summary.schoolsWithCriticalDeficitCount
            )
        }

        // 9. Supervisor Progress Dashboard
        item {
            SupervisorMonthlyProgressSection(supervisorRates = supervisorProgress)
        }

        // 10. Credits & Developer Signature
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MintLight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Verified,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تصميم وبرمجة: دكتور/ أحمد حمدي عاشور الغول",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldDark
                    )
                }
            }
        }
    }

    // Modal Dialog for Delayed Visits Action (Reschedule or Record Delay Reason)
    val selectedDelayedVisit by viewModel.selectedVisitForDelayModal.collectAsStateWithLifecycle()
    selectedDelayedVisit?.let { currentVisit ->
        DelayedVisitActionDialog(
            visit = currentVisit,
            onDismiss = { viewModel.selectedVisitForDelayModal.value = null },
            onSaveReason = { reason ->
                viewModel.updateVisitStatusAndReason(currentVisit.id, "DELAYED", reason)
                viewModel.selectedVisitForDelayModal.value = null
            },
            onReschedule = { newDate ->
                viewModel.rescheduleVisit(currentVisit.id, newDate)
                viewModel.selectedVisitForDelayModal.value = null
            }
        )
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("kpi_card_${title.replace(" ", "_")}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = Slate500
            )
        }
    }
}

@Composable
fun DelayedVisitActionDialog(
    visit: SupervisionVisitEntity,
    onDismiss: () -> Unit,
    onSaveReason: (reason: String) -> Unit,
    onReschedule: (newDate: String) -> Unit
) {
    var reasonText by remember { mutableStateOf(visit.delayReason) }
    var newDateText by remember { mutableStateOf(visit.visitDate) }
    var selectedActionTab by remember { mutableStateOf(0) } // 0: record reason, 1: reschedule

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "معالجة زيارة: ${visit.schoolName}",
                fontWeight = FontWeight.Bold,
                color = DeficitRedText
            )
        },
        text = {
            Column {
                Text(
                    text = "الموجه: ${visit.supervisorName} | التاريخ السابق: ${visit.visitDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )
                Spacer(modifier = Modifier.height(12.dp))

                TabRow(selectedTabIndex = selectedActionTab) {
                    Tab(
                        selected = selectedActionTab == 0,
                        onClick = { selectedActionTab = 0 },
                        text = { Text("تسجيل سبب التأخير") }
                    )
                    Tab(
                        selected = selectedActionTab == 1,
                        onClick = { selectedActionTab = 1 },
                        text = { Text("إعادة الجدولة") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedActionTab == 0) {
                    OutlinedTextField(
                        value = reasonText,
                        onValueChange = { reasonText = it },
                        label = { Text("سبب التأخير أو الاعتذار الميداني") },
                        placeholder = { Text("مثال: تعذر المواصلات / ظروف طارئة بالمدرسة") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                } else {
                    OutlinedTextField(
                        value = newDateText,
                        onValueChange = { newDateText = it },
                        label = { Text("تاريخ الزيارة الجديد (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedActionTab == 0) {
                        onSaveReason(reasonText)
                    } else {
                        onReschedule(newDateText)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text(if (selectedActionTab == 0) "حفظ السبب" else "اعتماد الموعد الجديد")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
