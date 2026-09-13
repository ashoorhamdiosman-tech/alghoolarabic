package com.example.util

import com.example.data.entity.SchoolEntity
import com.example.data.entity.TeacherEntity
import java.util.Locale

data class TeacherWithSchoolQuota(
    val teacher: TeacherEntity,
    val schoolName: String,
    val schoolDeficitOrSurplus: Int,
    val isSchoolDeficit: Boolean,
    val isSchoolSurplus: Boolean,
    val isSchoolBalanced: Boolean,
    val legalQuota: Int,
    val quotaDifference: Int, // weeklyPeriods - legalQuota
    val schoolSeverity: QuotaDeficitSeverity = QuotaDeficitSeverity.BALANCED
)

enum class QuotaDeficitSeverity(
    val id: String,
    val title: String,
    val shortBadge: String,
    val iconName: String,
    val description: String,
    val alertLevel: Int // 0: None, 1: Mild, 2: Moderate, 3: Critical
) {
    CRITICAL(
        id = "critical",
        title = "عجز حاد",
        shortBadge = "عجز حاد 🚨",
        iconName = "emergency",
        description = "عجز ٢٠ حصة فأكثر (أكثر من معلم كامل)",
        alertLevel = 3
    ),
    MODERATE(
        id = "moderate",
        title = "عجز متوسط",
        shortBadge = "عجز متوسط ⚠️",
        iconName = "warning",
        description = "عجز من ١٠ إلى ١٩ حصة",
        alertLevel = 2
    ),
    MILD(
        id = "mild",
        title = "عجز طفيف",
        shortBadge = "عجز طفيف 🟡",
        iconName = "info",
        description = "عجز من ١ إلى ٩ حصص",
        alertLevel = 1
    ),
    BALANCED(
        id = "balanced",
        title = "متوازن",
        shortBadge = "متوازن ✓",
        iconName = "check",
        description = "النصاب مطابق تماماً للحصص المطلوبة",
        alertLevel = 0
    ),
    SURPLUS(
        id = "surplus",
        title = "زيادة",
        shortBadge = "زيادة 🟢",
        iconName = "add",
        description = "فائض في عدد الحصص المتوفرة بالمدرسة",
        alertLevel = 0
    )
}

data class SchoolQuotaResult(
    val school: SchoolEntity,
    val totalClasses: Int,
    val arabicRequired: Int,
    val religionRequired: Int,
    val totalRequired: Int,
    val totalTeachers: Int,
    val availablePeriods: Int,
    val periodGap: Int, // positive = surplus, negative = deficit
    val teacherEquivalent: Double,
    val isDeficit: Boolean,
    val isSurplus: Boolean,
    val isBalanced: Boolean,
    val deficitSeverity: QuotaDeficitSeverity = QuotaDeficitSeverity.BALANCED
) {
    val isCriticalDeficit: Boolean get() = deficitSeverity == QuotaDeficitSeverity.CRITICAL
    val isModerateDeficit: Boolean get() = deficitSeverity == QuotaDeficitSeverity.MODERATE
    val isMildDeficit: Boolean get() = deficitSeverity == QuotaDeficitSeverity.MILD
}

data class TotalAdministrationSummary(
    val totalSchools: Int,
    val totalClasses: Int,
    val totalClassesPrimary: Int,
    val totalClassesUpper: Int,
    val totalTeachers: Int,
    val totalTeachersAssistant: Int,
    val totalTeachersFirst: Int,
    val totalTeachersFirstA: Int,
    val totalTeachersExpert: Int,
    val totalTeachersSenior: Int,
    val totalArabicRequired: Int,
    val totalReligionRequired: Int,
    val totalRequiredPeriods: Int,
    val totalAvailablePeriods: Int,
    val totalPeriodGap: Int,
    val totalTeacherEquivalent: Double,
    val schoolsWithDeficitCount: Int,
    val schoolsWithSurplusCount: Int,
    val schoolsBalancedCount: Int,
    val schoolsWithCriticalDeficitCount: Int = 0,
    val schoolsWithModerateDeficitCount: Int = 0,
    val schoolsWithMildDeficitCount: Int = 0
)

object QuotaCalculations {

    fun calculateSchool(school: SchoolEntity): SchoolQuotaResult {
        val totalClasses = school.classesPrimary + school.classesUpper
        // اللغة العربية: (f_{1-3} * 9) + (f_{4-6} * 9)
        val arabicReq = (school.classesPrimary * 9) + (school.classesUpper * 9)
        // التربية الدينية: (f_{1-3} * 5) + (f_{4-6} * 4)
        val religionReq = (school.classesPrimary * 5) + (school.classesUpper * 4)
        val totalReq = arabicReq + religionReq

        // المعلمين والأنصبة
        // معلم مساعد / معلم = 24 حصة
        // معلم أول = 22 حصة
        // معلم أول أ = 20 حصة
        // معلم خبير = 18 حصة
        // كبير معلمين = 16 حصة
        val available = (school.teachersAssistant * 24) +
                (school.teachersFirst * 22) +
                (school.teachersFirstA * 20) +
                (school.teachersExpert * 18) +
                (school.teachersSenior * 16)

        val totalTeachers = school.teachersAssistant +
                school.teachersFirst +
                school.teachersFirstA +
                school.teachersExpert +
                school.teachersSenior

        val gap = available - totalReq
        val ref = if (school.referenceQuota > 0) school.referenceQuota else 24
        val equiv = gap.toDouble() / ref.toDouble()

        val severity = when {
            gap <= -20 -> QuotaDeficitSeverity.CRITICAL
            gap in -19..-10 -> QuotaDeficitSeverity.MODERATE
            gap in -9..-1 -> QuotaDeficitSeverity.MILD
            gap == 0 -> QuotaDeficitSeverity.BALANCED
            else -> QuotaDeficitSeverity.SURPLUS
        }

        return SchoolQuotaResult(
            school = school,
            totalClasses = totalClasses,
            arabicRequired = arabicReq,
            religionRequired = religionReq,
            totalRequired = totalReq,
            totalTeachers = totalTeachers,
            availablePeriods = available,
            periodGap = gap,
            teacherEquivalent = equiv,
            isDeficit = gap < 0,
            isSurplus = gap > 0,
            isBalanced = gap == 0,
            deficitSeverity = severity
        )
    }

    fun calculateSummary(schools: List<SchoolEntity>): TotalAdministrationSummary {
        val results = schools.map { calculateSchool(it) }

        val totalSchools = schools.size
        val totalClassesPrimary = schools.sumOf { it.classesPrimary }
        val totalClassesUpper = schools.sumOf { it.classesUpper }
        val totalClasses = totalClassesPrimary + totalClassesUpper

        val totalAssistant = schools.sumOf { it.teachersAssistant }
        val totalFirst = schools.sumOf { it.teachersFirst }
        val totalFirstA = schools.sumOf { it.teachersFirstA }
        val totalExpert = schools.sumOf { it.teachersExpert }
        val totalSenior = schools.sumOf { it.teachersSenior }
        val totalTeachers = totalAssistant + totalFirst + totalFirstA + totalExpert + totalSenior

        val totalArabic = results.sumOf { it.arabicRequired }
        val totalReligion = results.sumOf { it.religionRequired }
        val totalRequired = results.sumOf { it.totalRequired }
        val totalAvailable = results.sumOf { it.availablePeriods }
        val totalGap = totalAvailable - totalRequired

        val ref = if (schools.isNotEmpty()) schools.first().referenceQuota else 24
        val totalEquiv = if (ref > 0) totalGap.toDouble() / ref.toDouble() else 0.0

        val deficitCount = results.count { it.isDeficit }
        val surplusCount = results.count { it.isSurplus }
        val balancedCount = results.count { it.isBalanced }
        val criticalCount = results.count { it.deficitSeverity == QuotaDeficitSeverity.CRITICAL }
        val moderateCount = results.count { it.deficitSeverity == QuotaDeficitSeverity.MODERATE }
        val mildCount = results.count { it.deficitSeverity == QuotaDeficitSeverity.MILD }

        return TotalAdministrationSummary(
            totalSchools = totalSchools,
            totalClasses = totalClasses,
            totalClassesPrimary = totalClassesPrimary,
            totalClassesUpper = totalClassesUpper,
            totalTeachers = totalTeachers,
            totalTeachersAssistant = totalAssistant,
            totalTeachersFirst = totalFirst,
            totalTeachersFirstA = totalFirstA,
            totalTeachersExpert = totalExpert,
            totalTeachersSenior = totalSenior,
            totalArabicRequired = totalArabic,
            totalReligionRequired = totalReligion,
            totalRequiredPeriods = totalRequired,
            totalAvailablePeriods = totalAvailable,
            totalPeriodGap = totalGap,
            totalTeacherEquivalent = totalEquiv,
            schoolsWithDeficitCount = deficitCount,
            schoolsWithSurplusCount = surplusCount,
            schoolsBalancedCount = balancedCount,
            schoolsWithCriticalDeficitCount = criticalCount,
            schoolsWithModerateDeficitCount = moderateCount,
            schoolsWithMildDeficitCount = mildCount
        )
    }

    fun formatDouble(value: Double): String {
        return String.format(Locale.US, "%.1f", value)
    }

    fun getLegalQuotaForCadre(cadre: String): Int {
        return when {
            cadre.contains("مساعد") || cadre.contains("معلم/") || cadre == "معلم" -> 24
            cadre.contains("أول أ") || cadre.contains("أول (أ)") -> 20
            cadre.contains("أول") -> 22
            cadre.contains("خبير") -> 18
            cadre.contains("كبير") -> 16
            else -> 24
        }
    }
}
