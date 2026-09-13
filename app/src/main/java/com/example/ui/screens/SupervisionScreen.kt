package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.SchoolEntity
import com.example.data.entity.SupervisionVisitEntity
import com.example.data.entity.SupervisorEntity
import com.example.ui.components.ElectronicSignaturePad
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisionScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val supervisors by viewModel.allSupervisors.collectAsStateWithLifecycle()
    val schools by viewModel.schools.collectAsStateWithLifecycle()
    val filteredVisits by viewModel.filteredVisits.collectAsStateWithLifecycle()

    val monthFilter by viewModel.selectedMonthFilter.collectAsStateWithLifecycle()
    val supervisorFilter by viewModel.selectedSupervisorFilter.collectAsStateWithLifecycle()
    val statusFilter by viewModel.selectedStatusFilter.collectAsStateWithLifecycle()

    val showPlanBuilder by viewModel.showPlanBuilderModal.collectAsStateWithLifecycle()
    val selectedVisitForCompletion by viewModel.selectedVisitForCompletion.collectAsStateWithLifecycle()
    val selectedDelayedVisit by viewModel.selectedVisitForDelayModal.collectAsStateWithLifecycle()

    val monthsList = listOf("الكل", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر", "يناير", "فبراير", "مارس", "أبريل", "مايو")
    val statusList = listOf("الكل", "SCHEDULED", "COMPLETED", "DELAYED", "POSTPONED")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("supervision_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        // 1. Top Action Card: Monthly Plan Builder & Direct DOCX Export
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("plan_builder_action_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldPrimary)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "إدارة المتابعة والتوجيه الميداني",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "منشئ خطة المتابعة الشهرية وتوليد النماذج الرسمية (DOCX / Word)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MintContainer
                            )
                        }
                        Icon(
                            Icons.Default.AssignmentTurnedIn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.showPlanBuilderModal.value = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_plan_builder_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("جدولة زيارة جديدة", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                        }

                        FilledTonalButton(
                            onClick = {
                                val currentSup = supervisors.firstOrNull()?.name ?: "توجيه اللغة العربية"
                                val currentMonth = if (monthFilter == "الكل") "سبتمبر" else monthFilter
                                viewModel.exportDocxPlan(context, currentSup, currentMonth)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_docx_btn"),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MintContainer,
                                contentColor = EmeraldDark
                            )
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تصدير DOCX رسمي", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Supervisor Coverage Overview & Filter
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "موجهو إدارة العريش وتوزيع المدارس",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "اختر موجه لمعرفة المدارس المغطاة وزيارات الخطة المعتمدة",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Horizontal chips for supervisors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = supervisorFilter == null,
                            onClick = { viewModel.selectedSupervisorFilter.value = null },
                            label = { Text("جميع الموجهين") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MintContainer,
                                selectedLabelColor = EmeraldDark
                            )
                        )
                        supervisors.take(3).forEach { sup ->
                            FilterChip(
                                selected = supervisorFilter == sup.id,
                                onClick = {
                                    viewModel.selectedSupervisorFilter.value = if (supervisorFilter == sup.id) null else sup.id
                                },
                                label = { Text(sup.name.split(" ").take(2).joinToString(" ")) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MintContainer,
                                    selectedLabelColor = EmeraldDark
                                )
                            )
                        }
                    }
                }
            }
        }

        // 3. Advanced Filter Bar (Month & Status)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MintLight)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تصفية الزيارات الميدانية",
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark
                        )
                        // Export filtered visits to CSV
                        IconButton(
                            onClick = { viewModel.exportVisitsCsv(context) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = "تصدير السجل", tint = EmeraldPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Month filter row
                    Text("الشهر:", style = MaterialTheme.typography.labelSmall, color = Slate600)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        monthsList.take(5).forEach { m ->
                            FilterChip(
                                selected = monthFilter == m,
                                onClick = { viewModel.selectedMonthFilter.value = m },
                                label = { Text(m, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Status filter row
                    Text("حالة الزيارة:", style = MaterialTheme.typography.labelSmall, color = Slate600)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        statusList.forEach { s ->
                            val label = when (s) {
                                "SCHEDULED" -> "مجدولة"
                                "COMPLETED" -> "مكتملة"
                                "DELAYED" -> "متأخرة"
                                "POSTPONED" -> "مؤجلة"
                                else -> "الكل"
                            }
                            FilterChip(
                                selected = statusFilter == s,
                                onClick = { viewModel.selectedStatusFilter.value = s },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // 4. Visits List Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سجل الزيارات الميدانية (${filteredVisits.size} زيارة)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "العام الدراسي: 2026/2027م",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate600
                )
            }
        }

        // 5. Visits Items
        if (filteredVisits.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد زيارات تطابق معايير التصفية الحالية.\nيمكنك إضافة زيارة جديدة عبر زر 'جدولة زيارة'.",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = Slate500
                    )
                }
            }
        } else {
            itemsIndexed(filteredVisits) { _, visit ->
                VisitCardItem(
                    visit = visit,
                    onOpenDelayAction = { viewModel.selectedVisitForDelayModal.value = visit },
                    onOpenCompletion = { viewModel.selectedVisitForCompletion.value = visit }
                )
            }
        }
    }

    // Modal 1: Monthly Plan Builder
    if (showPlanBuilder) {
        MonthlyPlanBuilderModal(
            supervisors = supervisors,
            schools = schools,
            onDismiss = { viewModel.showPlanBuilderModal.value = false },
            onCreateVisit = { supId, supName, schId, schName, date, month, type, obj ->
                viewModel.createVisit(supId, supName, schId, schName, date, month, type, obj)
                viewModel.showPlanBuilderModal.value = false
            }
        )
    }

    // Modal 2: Visit Completion with Electronic Signature & Evidence
    if (selectedVisitForCompletion != null) {
        CompleteVisitDialog(
            visit = selectedVisitForCompletion!!,
            onDismiss = { viewModel.selectedVisitForCompletion.value = null },
            onComplete = { supSig, prinSig, evidence ->
                viewModel.completeVisit(selectedVisitForCompletion!!, supSig, prinSig, evidence)
                viewModel.selectedVisitForCompletion.value = null
            }
        )
    }

    // Modal 3: Delayed Visit Dialog
    if (selectedDelayedVisit != null) {
        DelayedVisitActionDialog(
            visit = selectedDelayedVisit!!,
            onDismiss = { viewModel.selectedVisitForDelayModal.value = null },
            onSaveReason = { reason ->
                viewModel.updateVisitStatusAndReason(selectedDelayedVisit!!.id, "DELAYED", reason)
                viewModel.selectedVisitForDelayModal.value = null
            },
            onReschedule = { newDate ->
                viewModel.rescheduleVisit(selectedDelayedVisit!!.id, newDate)
                viewModel.selectedVisitForDelayModal.value = null
            }
        )
    }
}

@Composable
fun VisitCardItem(
    visit: SupervisionVisitEntity,
    onOpenDelayAction: () -> Unit,
    onOpenCompletion: () -> Unit
) {
    val isDelayed = visit.status == "DELAYED" || visit.status == "POSTPONED"
    val isCompleted = visit.status == "COMPLETED"

    val statusColor = when (visit.status) {
        "COMPLETED" -> SurplusGreenText
        "DELAYED", "POSTPONED" -> DeficitRedText
        else -> BalancedBlueText
    }
    val statusBg = when (visit.status) {
        "COMPLETED" -> SurplusGreenBg
        "DELAYED", "POSTPONED" -> DeficitRedBg
        else -> BalancedBlueBg
    }
    val statusTitle = when (visit.status) {
        "COMPLETED" -> "مكتملة"
        "DELAYED" -> "متأخرة"
        "POSTPONED" -> "مؤجلة"
        else -> "مجدولة"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("visit_card_${visit.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = visit.schoolName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "الموجه المتابع: ${visit.supervisorName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldDark
                    )
                }

                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.clickable {
                        if (isDelayed) onOpenDelayAction()
                    }
                ) {
                    Text(
                        text = statusTitle,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "التاريخ: ${visit.visitDate} (${visit.visitTime})",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate700
                )
                Text(
                    text = "النوع: ${visit.visitType}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate700
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "الهدف: ${visit.objective}",
                style = MaterialTheme.typography.bodySmall,
                color = EmeraldPrimary,
                fontWeight = FontWeight.SemiBold
            )

            if (visit.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ملاحظات: ${visit.notes}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate600
                )
            }

            // If Delayed, show alert reason banner with click action
            if (isDelayed) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = DeficitRedBg,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenDelayAction() }
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = DeficitRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "زيارة متأخرة - اضغط لمعالجة التأخير أو إعادة الجدولة",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = DeficitRedText
                            )
                            if (visit.delayReason.isNotBlank()) {
                                Text(
                                    text = "السبب: ${visit.delayReason}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DeficitRedText
                                )
                            }
                        }
                    }
                }
            }

            // If Completed, show signatures indicator
            if (isCompleted) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MintLight,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تم اعتماد الزيارة وإغلاق التقرير إلكترونياً",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDark
                            )
                        }
                        if (visit.evidenceDocUri.isNotBlank()) {
                            Text(
                                text = "المستند / المرفق: ${visit.evidenceDocUri}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate600
                            )
                        }
                    }
                }
            } else if (!isDelayed) {
                // Action to complete visit
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onOpenDelayAction,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertAmberText)
                    ) {
                        Text("تأجيل", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onOpenCompletion,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Icon(Icons.Default.Draw, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إتمام وتوقيع الزيارة", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// ----------------- Monthly Plan Builder Modal -----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyPlanBuilderModal(
    supervisors: List<SupervisorEntity>,
    schools: List<SchoolEntity>,
    onDismiss: () -> Unit,
    onCreateVisit: (
        supervisorId: Long,
        supervisorName: String,
        schoolId: Long,
        schoolName: String,
        visitDate: String,
        month: String,
        visitType: String,
        objective: String
    ) -> Unit
) {
    var selectedSupervisor by remember { mutableStateOf(supervisors.firstOrNull()) }
    var selectedSchool by remember { mutableStateOf(schools.firstOrNull()) }
    var month by remember { mutableStateOf("سبتمبر") }
    var visitDate by remember { mutableStateOf("2026-09-16") }
    var visitType by remember { mutableStateOf("ميدانية دورية") }
    var objective by remember { mutableStateOf("متابعة أنصبة وسد العجز") }

    val months = listOf("سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر", "يناير", "فبراير", "مارس", "أبريل", "مايو")
    val objectives = listOf(
        "متابعة أنصبة وسد العجز",
        "تقويم فني ومراجعة سجلات",
        "مراجعة تقييمات الطلاب",
        "متابعة توزيع المناهج الدراسية"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "منشئ خطة المتابعة الميدانية الشهرية",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary
            )
            Text(
                text = "العام الدراسي ٢٠٢٦ / ٢٠٢٧ م - إدارة العريش التعليمية",
                style = MaterialTheme.typography.bodySmall,
                color = Slate600
            )

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Supervisor Selection
                item {
                    Text("الموجه المتابع:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        supervisors.take(3).forEach { sup ->
                            FilterChip(
                                selected = selectedSupervisor?.id == sup.id,
                                onClick = { selectedSupervisor = sup },
                                label = { Text(sup.name.split(" ").take(2).joinToString(" ")) }
                            )
                        }
                    }
                }

                // Month Selection
                item {
                    Text("شهر الخطة:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        months.take(4).forEach { m ->
                            FilterChip(
                                selected = month == m,
                                onClick = { month = m },
                                label = { Text(m) }
                            )
                        }
                    }
                }

                // School Selection
                item {
                    Text("المدرسة المزارة:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    var schoolSearch by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = schoolSearch,
                        onValueChange = { schoolSearch = it },
                        placeholder = { Text("بحث لاختيار المدرسة...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val filteredSchools = remember(schools, schoolSearch) {
                        if (schoolSearch.isBlank()) schools.take(5) else schools.filter { it.name.contains(schoolSearch.trim()) }.take(5)
                    }
                    filteredSchools.forEach { s ->
                        Surface(
                            color = if (selectedSchool?.id == s.id) MintContainer else Color.Transparent,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedSchool = s }
                                .padding(vertical = 4.dp, horizontal = 6.dp)
                        ) {
                            Text(
                                text = s.name,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (selectedSchool?.id == s.id) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedSchool?.id == s.id) EmeraldDark else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Date
                item {
                    OutlinedTextField(
                        value = visitDate,
                        onValueChange = { visitDate = it },
                        label = { Text("تاريخ الزيارة (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Objective
                item {
                    Text("الهدف من الزيارة:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    objectives.forEach { obj ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { objective = obj }
                        ) {
                            RadioButton(selected = objective == obj, onClick = { objective = obj })
                            Text(obj, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (selectedSupervisor != null && selectedSchool != null) {
                            onCreateVisit(
                                selectedSupervisor!!.id,
                                selectedSupervisor!!.name,
                                selectedSchool!!.id,
                                selectedSchool!!.name,
                                visitDate,
                                month,
                                visitType,
                                objective
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إضافة إلى الخطة")
                }

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("إلغاء")
                }
            }
        }
    }
}

// ----------------- Complete Visit Dialog with Electronic Signatures -----------------
@Composable
fun CompleteVisitDialog(
    visit: SupervisionVisitEntity,
    onDismiss: () -> Unit,
    onComplete: (supervisorSig: String, principalSig: String, evidenceNote: String) -> Unit
) {
    var supervisorSignature by remember { mutableStateOf("تم التوقيع الإلكتروني - ${visit.supervisorName}") }
    var principalSignature by remember { mutableStateOf("تم التوقيع الإلكتروني - إدارة المدرسة") }
    var evidenceNote by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "اعتماد واكتمال الزيارة الميدانية",
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "المدرسة: ${visit.schoolName}\nالموجه: ${visit.supervisorName} | التاريخ: ${visit.visitDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate700
                    )
                }

                item {
                    ElectronicSignaturePad(
                        title = "توقيع الموجه المتابع",
                        signerRole = "الموجه: ${visit.supervisorName}",
                        onSignatureCaptured = { supervisorSignature = it }
                    )
                }

                item {
                    ElectronicSignaturePad(
                        title = "توقيع مدير المدرسة المزارة",
                        signerRole = "مدير مدرسة ${visit.schoolName}",
                        onSignatureCaptured = { principalSignature = it }
                    )
                }

                item {
                    OutlinedTextField(
                        value = evidenceNote,
                        onValueChange = { evidenceNote = it },
                        label = { Text("إرفاق بيان أو مستند كدليل على إتمام الزيارة") },
                        placeholder = { Text("مثال: صورة سجل الحصص / محضر المتابعة رقم 4") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onComplete(supervisorSignature, principalSignature, evidenceNote)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("اعتماد وإغلاق الزيارة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
