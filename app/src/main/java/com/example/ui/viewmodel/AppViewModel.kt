package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.SchoolEntity
import com.example.data.entity.SupervisionVisitEntity
import com.example.data.entity.SupervisorEntity
import com.example.data.entity.TeacherEntity
import com.example.data.repository.AppRepository
import com.example.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = AppRepository(db)
    }

    // Navigation Tab (0: Dashboard, 1: Schools, 2: Supervision, 3: Reference)
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    fun setTab(index: Int) {
        _currentTab.value = index
    }

    // Schools Data
    val schools: StateFlow<List<SchoolEntity>> = repository.allSchools
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // School Search & Reference Quota & Severity Classification Filter
    val schoolSearchQuery = MutableStateFlow("")
    val referenceQuota = MutableStateFlow(24)
    val selectedSchoolSeverityFilter = MutableStateFlow<QuotaDeficitSeverity?>(null) // null = all

    // Filtered Schools with Calculations and Severity Classification
    val filteredSchoolResults: StateFlow<List<SchoolQuotaResult>> = combine(
        schools,
        schoolSearchQuery,
        referenceQuota,
        selectedSchoolSeverityFilter
    ) { schoolList, query, refQuota, severityFilter ->
        val adjusted = schoolList.map { it.copy(referenceQuota = refQuota) }
        val calculated = adjusted.map { QuotaCalculations.calculateSchool(it) }
        calculated.filter { item ->
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                item.school.name.contains(query.trim(), ignoreCase = true)
            }
            val matchesSeverity = severityFilter == null || item.deficitSeverity == severityFilter
            matchesQuery && matchesSeverity
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All School Results for Summary
    val allSchoolResults: StateFlow<List<SchoolQuotaResult>> = combine(
        schools,
        referenceQuota
    ) { schoolList, refQuota ->
        val adjusted = schoolList.map { it.copy(referenceQuota = refQuota) }
        adjusted.map { QuotaCalculations.calculateSchool(it) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dedicated stream for schools with critical / severe deficit (عجز حاد)
    val criticalDeficitSchools: StateFlow<List<SchoolQuotaResult>> = allSchoolResults.map { list ->
        list.filter { it.isCriticalDeficit }.sortedBy { it.periodGap } // most severe first
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectSchoolSeverityFilter(severity: QuotaDeficitSeverity?) {
        selectedSchoolSeverityFilter.value = severity
    }

    fun showCriticalDeficitSchoolsOnly() {
        selectedSchoolSeverityFilter.value = QuotaDeficitSeverity.CRITICAL
    }

    fun clearSchoolSeverityFilter() {
        selectedSchoolSeverityFilter.value = null
    }

    // Summary KPIs
    val summary: StateFlow<TotalAdministrationSummary> = schools.map { schoolList ->
        QuotaCalculations.calculateSummary(schoolList)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        QuotaCalculations.calculateSummary(emptyList())
    )

    // Teachers
    val allTeachers: StateFlow<List<TeacherEntity>> = repository.allTeachers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Teacher Search & Filters
    val teacherSearchQuery = MutableStateFlow("")
    val selectedTeacherSchoolFilter = MutableStateFlow<Long?>(null) // null = all
    val selectedTeacherStatusFilter = MutableStateFlow("الكل") // الكل / عجز حاد / عجز / زيادة / متوازن

    // Combined Teachers with School Quota & Status
    val teachersWithSchoolQuota: StateFlow<List<TeacherWithSchoolQuota>> = combine(
        allTeachers,
        allSchoolResults,
        teacherSearchQuery,
        selectedTeacherSchoolFilter,
        selectedTeacherStatusFilter
    ) { teacherList, schoolResultsList, query, schoolFilterId, statusFilter ->
        val schoolResultsMap = schoolResultsList.associateBy { it.school.id }

        teacherList.map { teacher ->
            val schoolResult = schoolResultsMap[teacher.schoolId]
            val sName = schoolResult?.school?.name ?: "مدرسة العريش"
            val gap = schoolResult?.periodGap ?: 0
            val isDef = schoolResult?.isDeficit ?: false
            val isSur = schoolResult?.isSurplus ?: false
            val isBal = schoolResult?.isBalanced ?: true
            val severity = schoolResult?.deficitSeverity ?: QuotaDeficitSeverity.BALANCED
            val legal = QuotaCalculations.getLegalQuotaForCadre(teacher.cadreDegree)
            val diff = teacher.weeklyPeriods - legal

            TeacherWithSchoolQuota(
                teacher = teacher,
                schoolName = sName,
                schoolDeficitOrSurplus = gap,
                isSchoolDeficit = isDef,
                isSchoolSurplus = isSur,
                isSchoolBalanced = isBal,
                legalQuota = legal,
                quotaDifference = diff,
                schoolSeverity = severity
            )
        }.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.teacher.fullName.contains(query.trim(), ignoreCase = true) ||
                    item.teacher.teacherCode.contains(query.trim()) ||
                    item.teacher.nationalId.contains(query.trim()) ||
                    item.schoolName.contains(query.trim(), ignoreCase = true)

            val matchesSchool = schoolFilterId == null || item.teacher.schoolId == schoolFilterId

            val matchesStatus = when (statusFilter) {
                "عجز حاد" -> item.schoolSeverity == QuotaDeficitSeverity.CRITICAL
                "عجز" -> item.isSchoolDeficit
                "زيادة" -> item.isSchoolSurplus
                "متوازن" -> item.isSchoolBalanced
                else -> true
            }

            matchesQuery && matchesSchool && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Supervisors
    val allSupervisors: StateFlow<List<SupervisorEntity>> = repository.allSupervisors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Visits
    val allVisits: StateFlow<List<SupervisionVisitEntity>> = repository.allVisits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Supervision Filters
    val selectedMonthFilter = MutableStateFlow("الكل")
    val selectedSupervisorFilter = MutableStateFlow<Long?>(null)
    val selectedStatusFilter = MutableStateFlow("الكل")

    val filteredVisits: StateFlow<List<SupervisionVisitEntity>> = combine(
        allVisits,
        selectedMonthFilter,
        selectedSupervisorFilter,
        selectedStatusFilter
    ) { visits, month, supId, status ->
        visits.filter { v ->
            val matchMonth = if (month == "الكل") true else v.month.contains(month)
            val matchSup = if (supId == null) true else v.supervisorId == supId
            val matchStatus = if (status == "الكل") true else v.status == status
            matchMonth && matchSup && matchStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 48-hour upcoming visits
    val upcomingVisits48h: StateFlow<List<SupervisionVisitEntity>> = allVisits.map { list ->
        val now = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayStr = dateFormat.format(now.time)
        now.add(Calendar.DAY_OF_YEAR, 2)
        val in48hStr = dateFormat.format(now.time)

        list.filter {
            it.status == "SCHEDULED" && it.visitDate >= todayStr && it.visitDate <= in48hStr
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Delayed or Postponed visits
    val delayedVisits: StateFlow<List<SupervisionVisitEntity>> = allVisits.map { list ->
        list.filter { it.status == "DELAYED" || it.status == "POSTPONED" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Supervisor Completion Progress
    val supervisorProgressList: StateFlow<List<Pair<String, Float>>> = combine(
        allSupervisors,
        allVisits
    ) { sups, visits ->
        sups.map { sup ->
            val supVisits = visits.filter { it.supervisorId == sup.id }
            val completed = supVisits.count { it.status == "COMPLETED" }
            val rate = if (supVisits.isNotEmpty()) completed.toFloat() / supVisits.size else 0.5f
            Pair(sup.name, rate)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Modals & Selected Objects State
    val editingSchool = MutableStateFlow<SchoolEntity?>(null)
    val managingTeachersForSchool = MutableStateFlow<SchoolEntity?>(null)
    val selectedVisitForDelayModal = MutableStateFlow<SupervisionVisitEntity?>(null)
    val selectedVisitForCompletion = MutableStateFlow<SupervisionVisitEntity?>(null)
    val showPlanBuilderModal = MutableStateFlow(false)
    val showTransferTeacherModal = MutableStateFlow<TeacherEntity?>(null)

    // Notifications / SnackBar
    val snackbarMessage = MutableStateFlow<String?>(null)

    fun showSnackbar(message: String) {
        snackbarMessage.value = message
    }

    fun clearSnackbar() {
        snackbarMessage.value = null
    }

    // CRUD Actions on School
    fun updateSchool(school: SchoolEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSchool(school)
            showSnackbar("تم حفظ بيانات مدرسة ${school.name} بنجاح")
        }
    }

    // Quick Edit School
    fun openSchoolQuickEdit(school: SchoolEntity) {
        editingSchool.value = school
    }

    fun closeSchoolQuickEdit() {
        editingSchool.value = null
    }

    // Teacher Management
    fun openManageTeachers(school: SchoolEntity) {
        managingTeachersForSchool.value = school
    }

    fun closeManageTeachers() {
        managingTeachersForSchool.value = null
    }

    fun addTeacher(teacher: TeacherEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertTeacher(teacher)
            showSnackbar("تمت إضافة المعلم ${teacher.fullName} بنجاح")
        }
    }

    fun updateTeacher(teacher: TeacherEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTeacher(teacher)
            showSnackbar("تم تعديل بيانات المعلم ${teacher.fullName}")
        }
    }

    fun deleteTeacher(teacher: TeacherEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTeacher(teacher)
            showSnackbar("تم حذف المعلم ${teacher.fullName}")
        }
    }

    fun transferTeacher(teacherId: Long, targetSchoolId: Long, targetSchoolName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.transferTeacher(teacherId, targetSchoolId)
            showSnackbar("تم نقل المعلم إلى $targetSchoolName بنجاح")
        }
    }

    // Supervision & Visits Actions
    fun createVisit(
        supervisorId: Long,
        supervisorName: String,
        schoolId: Long,
        schoolName: String,
        visitDate: String,
        month: String,
        visitType: String,
        objective: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val visit = SupervisionVisitEntity(
                supervisorId = supervisorId,
                supervisorName = supervisorName,
                schoolId = schoolId,
                schoolName = schoolName,
                visitDate = visitDate,
                month = month,
                academicYear = "2026/2027",
                visitType = visitType,
                objective = objective,
                status = "SCHEDULED"
            )
            repository.insertVisit(visit)
            showSnackbar("تمت جدولة الزيارة إلى $schoolName بنجاح")
        }
    }

    fun updateVisitStatusAndReason(visitId: Long, status: String, reason: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateVisitStatusAndReason(visitId, status, reason)
            showSnackbar("تم تحديث حالة الزيارة إلى $status")
        }
    }

    fun rescheduleVisit(visitId: Long, newDate: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.rescheduleVisit(visitId, newDate)
            showSnackbar("تمت إعادة جدولة الزيارة إلى تاريخ $newDate")
        }
    }

    fun completeVisit(
        visit: SupervisionVisitEntity,
        supervisorSignature: String,
        principalSignature: String,
        evidenceNote: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = visit.copy(
                status = "COMPLETED",
                supervisorSignature = supervisorSignature,
                principalSignature = principalSignature,
                evidenceDocUri = evidenceNote
            )
            repository.updateVisit(updated)
            showSnackbar("تم اعتماد واكتمال الزيارة لمدرسة ${visit.schoolName}")
        }
    }

    // Export Helpers
    fun exportDocxPlan(context: Context, supervisorName: String, month: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val visits = allVisits.value.filter {
                    it.supervisorName.contains(supervisorName) || supervisorName == "الكل"
                }
                val file = DocxExportHelper.generateMonthlyPlanDocx(
                    context = context,
                    supervisorName = supervisorName,
                    month = month,
                    academicYear = "2026/2027",
                    visits = visits
                )
                DocxExportHelper.shareOrOpenFile(context, file)
            } catch (e: Exception) {
                e.printStackTrace()
                showSnackbar("تعذر تصدير ملف Word: ${e.message}")
            }
        }
    }

    fun exportSchoolsCsv(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = CsvExportHelper.exportSchoolsCsv(context, allSchoolResults.value)
                CsvExportHelper.shareCsvFile(context, file)
            } catch (e: Exception) {
                showSnackbar("تعذر تصدير Excel/CSV: ${e.message}")
            }
        }
    }

    fun exportVisitsCsv(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = CsvExportHelper.exportVisitsCsv(context, filteredVisits.value)
                CsvExportHelper.shareCsvFile(context, file)
            } catch (e: Exception) {
                showSnackbar("تعذر تصدير ملف الزيارات: ${e.message}")
            }
        }
    }

    fun exportTeachersCsv(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = CsvExportHelper.exportTeachersCsv(context, teachersWithSchoolQuota.value)
                CsvExportHelper.shareCsvFile(context, file)
            } catch (e: Exception) {
                showSnackbar("تعذر تصدير كشف المعلمين CSV: ${e.message}")
            }
        }
    }

    fun exportTeachersPdf(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = PdfExportHelper.exportTeachersQuotasPdf(
                    context,
                    teachersWithSchoolQuota.value,
                    summary.value
                )
                PdfExportHelper.sharePdfFile(context, file)
            } catch (e: Exception) {
                showSnackbar("تعذر إنشاء تقرير المعلمين PDF: ${e.message}")
            }
        }
    }

    fun exportDashboardPdf(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = PdfExportHelper.exportDashboardPdf(
                    context,
                    summary.value,
                    allSchoolResults.value
                )
                PdfExportHelper.sharePdfFile(context, file)
            } catch (e: Exception) {
                showSnackbar("تعذر إنشاء ملف PDF: ${e.message}")
            }
        }
    }
}
