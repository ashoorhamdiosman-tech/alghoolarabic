package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.SchoolEntity
import com.example.data.entity.TeacherEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel
import com.example.util.LocationHelper
import com.example.util.QuotaCalculations
import com.example.util.SchoolQuotaResult
import com.example.util.TeacherWithSchoolQuota
import androidx.compose.foundation.ScrollState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolsScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val searchQuery by viewModel.schoolSearchQuery.collectAsStateWithLifecycle()
    val refQuota by viewModel.referenceQuota.collectAsStateWithLifecycle()
    val schoolResults by viewModel.filteredSchoolResults.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val allSchools by viewModel.schools.collectAsStateWithLifecycle()

    val editingSchool by viewModel.editingSchool.collectAsStateWithLifecycle()
    val managingSchoolTeachers by viewModel.managingTeachersForSchool.collectAsStateWithLifecycle()
    val transferTeacherTarget by viewModel.showTransferTeacherModal.collectAsStateWithLifecycle()

    // Horizontal scroll state for frozen/sticky table
    val horizontalScrollState = rememberScrollState()

    var activeSubTab by remember { mutableIntStateOf(0) } // 0: جدول أنصبة المدارس, 1: جدول بيانات المدرسين
    var showReportsDialog by remember { mutableStateOf(false) }
    var showClassificationGuideDialog by remember { mutableStateOf(false) }
    var showAddGlobalTeacherDialog by remember { mutableStateOf(false) }
    var schoolFilterExpanded by remember { mutableStateOf(false) }

    val selectedSchoolSeverityFilter by viewModel.selectedSchoolSeverityFilter.collectAsStateWithLifecycle()

    val teacherHorizontalScrollState = rememberScrollState()
    val teachersWithQuota by viewModel.teachersWithSchoolQuota.collectAsStateWithLifecycle()
    val teacherSearchQuery by viewModel.teacherSearchQuery.collectAsStateWithLifecycle()
    val selectedTeacherSchoolFilter by viewModel.selectedTeacherSchoolFilter.collectAsStateWithLifecycle()
    val selectedTeacherStatusFilter by viewModel.selectedTeacherStatusFilter.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("schools_screen"),
        topBar = {
            // Control Bar above Table
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 3.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Sub Tabs Switcher
                    TabRow(
                        selectedTabIndex = activeSubTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = EmeraldPrimary,
                        modifier = Modifier.testTag("schools_sub_tabs")
                    ) {
                        Tab(
                            selected = activeSubTab == 0,
                            onClick = { activeSubTab = 0 },
                            text = { Text("جدول أنصبة المدارس (${schoolResults.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                            icon = { Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.testTag("tab_schools_quotas")
                        )
                        Tab(
                            selected = activeSubTab == 1,
                            onClick = { activeSubTab = 1 },
                            text = { Text("جدول بيانات المدرسين (${teachersWithQuota.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                            icon = { Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.testTag("tab_teachers_data")
                        )
                    }

                    if (activeSubTab == 0) {
                        // School Controls
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            // Search and Actions Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { viewModel.schoolSearchQuery.value = it },
                                    placeholder = { Text("بحث عن اسم المدرسة (من 65 مدرسة)...") },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = EmeraldPrimary) },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { viewModel.schoolSearchQuery.value = "" }) {
                                                Icon(Icons.Default.Clear, contentDescription = "مسح البحث")
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("school_search_input"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // Export Reports Button (PDF / CSV)
                                FilledTonalButton(
                                    onClick = { showReportsDialog = true },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = MintContainer,
                                        contentColor = EmeraldDark
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("export_reports_btn")
                                ) {
                                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("التقارير والتصدير", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 🚨 Urgent Severe Deficit Alert Card (تنبيه المدارس ذات العجز الحاد)
                            AnimatedVisibility(visible = summary.schoolsWithCriticalDeficitCount > 0) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                        .testTag("critical_deficit_alert_banner"),
                                    color = CriticalDeficitBg,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, CriticalDeficitBorder.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .background(CriticalDeficitRed),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        Icons.Default.Warning,
                                                        contentDescription = "تحذير عجز حاد",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = "تنبيه: ${summary.schoolsWithCriticalDeficitCount} مدارس تعاني من عجز حاد في الأنصبة",
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = CriticalDeficitText
                                                    )
                                                    Text(
                                                        text = "عجز يبلغ 20 حصة فأكثر (أكثر من معلم كامل) - يتطلب تدخلاً عاجلاً لإعادة الندب والتوزيع.",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = CriticalDeficitText.copy(alpha = 0.85f),
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            FilledTonalButton(
                                                onClick = {
                                                    if (selectedSchoolSeverityFilter == QuotaDeficitSeverity.CRITICAL) {
                                                        viewModel.clearSchoolSeverityFilter()
                                                    } else {
                                                        viewModel.showCriticalDeficitSchoolsOnly()
                                                    }
                                                },
                                                colors = ButtonDefaults.filledTonalButtonColors(
                                                    containerColor = if (selectedSchoolSeverityFilter == QuotaDeficitSeverity.CRITICAL) CriticalDeficitRed else Color.White,
                                                    contentColor = if (selectedSchoolSeverityFilter == QuotaDeficitSeverity.CRITICAL) Color.White else CriticalDeficitRed
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier.testTag("filter_critical_deficit_quick_btn")
                                            ) {
                                                Text(
                                                    text = if (selectedSchoolSeverityFilter == QuotaDeficitSeverity.CRITICAL) "إلغاء التصفية ✕" else "حصر العجز الحاد (${summary.schoolsWithCriticalDeficitCount})",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Automatic Classification Filter Chips Row (التصنيفات التلقائية للأنصبة)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(bottom = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // "كافة المدارس" chip
                                FilterChip(
                                    selected = selectedSchoolSeverityFilter == null,
                                    onClick = { viewModel.clearSchoolSeverityFilter() },
                                    label = { Text("كافة المدارس (${allSchools.size})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MintContainer,
                                        selectedLabelColor = EmeraldDark
                                    ),
                                    modifier = Modifier.testTag("chip_severity_all")
                                )

                                // "عجز حاد" chip
                                FilterChip(
                                    selected = selectedSchoolSeverityFilter == QuotaDeficitSeverity.CRITICAL,
                                    onClick = {
                                        if (selectedSchoolSeverityFilter == QuotaDeficitSeverity.CRITICAL) viewModel.clearSchoolSeverityFilter()
                                        else viewModel.selectSchoolSeverityFilter(QuotaDeficitSeverity.CRITICAL)
                                    },
                                    label = {
                                        Text(
                                            text = "🚨 عجز حاد (${summary.schoolsWithCriticalDeficitCount})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CriticalDeficitBg,
                                        selectedLabelColor = CriticalDeficitText,
                                        containerColor = CriticalDeficitBg.copy(alpha = 0.3f),
                                        labelColor = CriticalDeficitText
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selectedSchoolSeverityFilter == QuotaDeficitSeverity.CRITICAL,
                                        borderColor = if (selectedSchoolSeverityFilter == QuotaDeficitSeverity.CRITICAL) CriticalDeficitBorder else CriticalDeficitBorder.copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier.testTag("chip_severity_critical")
                                )

                                // "عجز متوسط" chip
                                FilterChip(
                                    selected = selectedSchoolSeverityFilter == QuotaDeficitSeverity.MODERATE,
                                    onClick = {
                                        if (selectedSchoolSeverityFilter == QuotaDeficitSeverity.MODERATE) viewModel.clearSchoolSeverityFilter()
                                        else viewModel.selectSchoolSeverityFilter(QuotaDeficitSeverity.MODERATE)
                                    },
                                    label = {
                                        Text(
                                            text = "⚠️ عجز متوسط (${summary.schoolsWithModerateDeficitCount})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ModerateDeficitBg,
                                        selectedLabelColor = ModerateDeficitText,
                                        containerColor = ModerateDeficitBg.copy(alpha = 0.3f),
                                        labelColor = ModerateDeficitText
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selectedSchoolSeverityFilter == QuotaDeficitSeverity.MODERATE,
                                        borderColor = if (selectedSchoolSeverityFilter == QuotaDeficitSeverity.MODERATE) ModerateDeficitBorder else ModerateDeficitBorder.copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier.testTag("chip_severity_moderate")
                                )

                                // "عجز طفيف" chip
                                FilterChip(
                                    selected = selectedSchoolSeverityFilter == QuotaDeficitSeverity.MILD,
                                    onClick = {
                                        if (selectedSchoolSeverityFilter == QuotaDeficitSeverity.MILD) viewModel.clearSchoolSeverityFilter()
                                        else viewModel.selectSchoolSeverityFilter(QuotaDeficitSeverity.MILD)
                                    },
                                    label = {
                                        Text(
                                            text = "🟡 عجز طفيف (${summary.schoolsWithMildDeficitCount})",
                                            fontSize = 11.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MildDeficitBg,
                                        selectedLabelColor = MildDeficitText,
                                        containerColor = MildDeficitBg.copy(alpha = 0.3f),
                                        labelColor = MildDeficitText
                                    ),
                                    modifier = Modifier.testTag("chip_severity_mild")
                                )

                                // "متوازن" chip
                                FilterChip(
                                    selected = selectedSchoolSeverityFilter == QuotaDeficitSeverity.BALANCED,
                                    onClick = {
                                        if (selectedSchoolSeverityFilter == QuotaDeficitSeverity.BALANCED) viewModel.clearSchoolSeverityFilter()
                                        else viewModel.selectSchoolSeverityFilter(QuotaDeficitSeverity.BALANCED)
                                    },
                                    label = {
                                        Text(
                                            text = "✓ متوازن (${summary.schoolsBalancedCount})",
                                            fontSize = 11.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BalancedBlueBg,
                                        selectedLabelColor = BalancedBlueText
                                    ),
                                    modifier = Modifier.testTag("chip_severity_balanced")
                                )

                                // "زيادة" chip
                                FilterChip(
                                    selected = selectedSchoolSeverityFilter == QuotaDeficitSeverity.SURPLUS,
                                    onClick = {
                                        if (selectedSchoolSeverityFilter == QuotaDeficitSeverity.SURPLUS) viewModel.clearSchoolSeverityFilter()
                                        else viewModel.selectSchoolSeverityFilter(QuotaDeficitSeverity.SURPLUS)
                                    },
                                    label = {
                                        Text(
                                            text = "🟢 زيادة (${summary.schoolsWithSurplusCount})",
                                            fontSize = 11.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SurplusGreenBg,
                                        selectedLabelColor = SurplusGreenText
                                    ),
                                    modifier = Modifier.testTag("chip_severity_surplus")
                                )

                                // Info Button for Automatic Classification Guide
                                IconButton(
                                    onClick = { showClassificationGuideDialog = true },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("btn_severity_guide")
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = "دليل التصنيفات التلقائية",
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Reference Quota Adjuster & Count
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "عرض ${schoolResults.size} من 65 مدرسة",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate600
                                )

                                // Quota Adjuster
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MintLight)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "النصاب المرجعي:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldDark,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = {
                                            if (refQuota > 12) viewModel.referenceQuota.value = refQuota - 1
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = "تقليل", tint = EmeraldPrimary)
                                    }
                                    Text(
                                        text = "$refQuota حصة",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                    IconButton(
                                        onClick = {
                                            if (refQuota < 30) viewModel.referenceQuota.value = refQuota + 1
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.AddCircleOutline, contentDescription = "زيادة", tint = EmeraldPrimary)
                                    }
                                }
                            }
                        }
                    } else {
                        // Teachers Controls
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            // Search and Actions Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = teacherSearchQuery,
                                    onValueChange = { viewModel.teacherSearchQuery.value = it },
                                    placeholder = { Text("بحث باسم المعلم، الكود، المدرسة...") },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = EmeraldPrimary) },
                                    trailingIcon = {
                                        if (teacherSearchQuery.isNotEmpty()) {
                                            IconButton(onClick = { viewModel.teacherSearchQuery.value = "" }) {
                                                Icon(Icons.Default.Clear, contentDescription = "مسح البحث")
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("teacher_search_input"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // Export Reports Button
                                FilledTonalButton(
                                    onClick = { showReportsDialog = true },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = MintContainer,
                                        contentColor = EmeraldDark
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("export_teacher_reports_btn")
                                ) {
                                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("تقارير", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                // Add Teacher Button
                                Button(
                                    onClick = { showAddGlobalTeacherDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("add_teacher_global_btn")
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("إضافة معلم", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Filters for Teachers: Status Chips & School Selector Dropdown
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val statuses = listOf("الكل", "عجز حاد", "عجز", "زيادة", "متوازن")
                                statuses.forEach { st ->
                                    FilterChip(
                                        selected = selectedTeacherStatusFilter == st,
                                        onClick = { viewModel.selectedTeacherStatusFilter.value = st },
                                        label = {
                                            Text(
                                                when (st) {
                                                    "الكل" -> "الكل"
                                                    "عجز حاد" -> "🚨 عجز حاد"
                                                    "عجز" -> "مدارس عجز"
                                                    "زيادة" -> "مدارس زيادة"
                                                    else -> "متوازن"
                                                },
                                                fontSize = 11.sp,
                                                fontWeight = if (st == "عجز حاد") FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = when (st) {
                                                "عجز حاد" -> CriticalDeficitBg
                                                "عجز" -> DeficitRedBg
                                                "زيادة" -> SurplusGreenBg
                                                "متوازن" -> BalancedBlueBg
                                                else -> MintContainer
                                            },
                                            selectedLabelColor = when (st) {
                                                "عجز حاد" -> CriticalDeficitText
                                                "عجز" -> DeficitRedText
                                                "زيادة" -> SurplusGreenText
                                                "متوازن" -> BalancedBlueText
                                                else -> EmeraldDark
                                            }
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                // School filter dropdown trigger
                                Box {
                                    val currentSchoolFilterName = allSchools.find { it.id == selectedTeacherSchoolFilter }?.name
                                    FilledTonalButton(
                                        onClick = { schoolFilterExpanded = true },
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = if (selectedTeacherSchoolFilter != null) MintContainer else Slate100,
                                            contentColor = if (selectedTeacherSchoolFilter != null) EmeraldDark else Slate800
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = currentSchoolFilterName?.take(14) ?: "تصفية بالمدرسة",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }

                                    DropdownMenu(
                                        expanded = schoolFilterExpanded,
                                        onDismissRequest = { schoolFilterExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("جميع المدارس (الكل)", fontWeight = FontWeight.Bold) },
                                            onClick = {
                                                viewModel.selectedTeacherSchoolFilter.value = null
                                                schoolFilterExpanded = false
                                            }
                                        )
                                        allSchools.forEach { s ->
                                            DropdownMenuItem(
                                                text = { Text(s.name) },
                                                onClick = {
                                                    viewModel.selectedTeacherSchoolFilter.value = s.id
                                                    schoolFilterExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (activeSubTab == 0) {
                // Sticky Footer Row (صف المجاميع المثبت لجميع مدارس إدارة العريش)
                Surface(
                    color = EmeraldDark,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "إجمالي إدارة العريش التعليمية",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${summary.totalClasses} فصل | ${summary.totalTeachers} معلم",
                                style = MaterialTheme.typography.labelSmall,
                                color = MintContainer
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("المتوفر", color = MintLight, style = MaterialTheme.typography.labelSmall)
                                Text("${summary.totalAvailablePeriods}", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("المطلوب", color = MintLight, style = MaterialTheme.typography.labelSmall)
                                Text("${summary.totalRequiredPeriods}", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            }

                            val totalGap = summary.totalPeriodGap
                            Surface(
                                color = if (totalGap < 0) DeficitRedBg else SurplusGreenBg,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (totalGap < 0) "عجز: ${-totalGap} حصة" else "زيادة: $totalGap حصة",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (totalGap < 0) DeficitRedText else SurplusGreenText
                                )
                            }
                        }
                    }
                }
            } else {
                // Sticky Footer Row for Teachers
                Surface(
                    color = EmeraldDark,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "إجمالي كشف المعلمين بإدارة العريش",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "معروض: ${teachersWithQuota.size} معلم | الحصص: ${teachersWithQuota.sumOf { it.teacher.weeklyPeriods }} حصة",
                                style = MaterialTheme.typography.labelSmall,
                                color = MintContainer
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val deficitCount = teachersWithQuota.count { it.isSchoolDeficit }
                            val surplusCount = teachersWithQuota.count { it.isSchoolSurplus }

                            Surface(
                                color = DeficitRedBg,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "في مدارس عجز: $deficitCount",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = DeficitRedText
                                )
                            }

                            Surface(
                                color = SurplusGreenBg,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "في مدارس زيادة: $surplusCount",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SurplusGreenText
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (activeSubTab == 0) {
            // Data Table with Frozen Header and Scrollable Content (Schools)
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                // Table Header (Frozen Sticky)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldPrimary)
                ) {
                    // Sticky First Column Header (School Name)
                    Box(
                        modifier = Modifier
                            .width(170.dp)
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "اسم المدرسة (تعديل)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    // Horizontally scrollable headers
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(horizontalScrollState)
                    ) {
                        HeaderCell(title = "فصول 1-3", width = 75.dp)
                        HeaderCell(title = "فصول 4-6", width = 75.dp)
                        HeaderCell(title = "مطلوب عربي", width = 85.dp)
                        HeaderCell(title = "مطلوب دين", width = 85.dp)
                        HeaderCell(title = "إجمالي المطلوب", width = 95.dp)
                        HeaderCell(title = "معلم/مساعد", width = 80.dp)
                        HeaderCell(title = "معلم أول", width = 75.dp)
                        HeaderCell(title = "معلم أول أ", width = 75.dp)
                        HeaderCell(title = "خبير", width = 65.dp)
                        HeaderCell(title = "كبير", width = 65.dp)
                        HeaderCell(title = "المتوفر", width = 80.dp)
                        HeaderCell(title = "فجوة الحصص", width = 90.dp)
                        HeaderCell(title = "مكافئ المعلمين", width = 95.dp)
                        HeaderCell(title = "التصنيف التلقائي", width = 105.dp)
                        HeaderCell(title = "معلمو المدرسة", width = 110.dp)
                    }
                }

                // Table Rows (Zebra Striping with Severity Highlighting)
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(schoolResults) { index, result ->
                        val school = result.school
                        val isEven = index % 2 == 0
                        val rowBg = when {
                            result.isCriticalDeficit -> if (isEven) Color(0xFFFFF1F2) else Color(0xFFFFE4E6)
                            result.isModerateDeficit -> if (isEven) Color(0xFFFFFBEB) else Color(0xFFFEF3C7).copy(alpha = 0.6f)
                            result.isMildDeficit -> if (isEven) Color(0xFFFEFCE8) else Color(0xFFFEF9C3).copy(alpha = 0.5f)
                            isEven -> Color.White
                            else -> MintLight
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(rowBg)
                                .testTag("school_row_${school.id}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Sticky Column 1: School Name (Clickable to Quick Edit Modal)
                            Box(
                                modifier = Modifier
                                    .width(170.dp)
                                    .clickable {
                                        viewModel.openSchoolQuickEdit(school)
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (result.isCriticalDeficit) {
                                        Surface(
                                            color = CriticalDeficitRed,
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.padding(end = 4.dp)
                                        ) {
                                            Text(
                                                text = "🚨 عجز حاد",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else if (result.isModerateDeficit) {
                                        Surface(
                                            color = ModerateDeficitOrange,
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.padding(end = 4.dp)
                                        ) {
                                            Text(
                                                text = "⚠️ متوسط",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "تعديل",
                                            tint = EmeraldPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = school.name,
                                        fontWeight = if (result.isCriticalDeficit) FontWeight.ExtraBold else FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (result.isCriticalDeficit) CriticalDeficitText else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 2
                                    )
                                }
                            }

                            // Horizontally Scrollable Cells
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .horizontalScroll(horizontalScrollState),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DataCell(text = "${school.classesPrimary}", width = 75.dp)
                                DataCell(text = "${school.classesUpper}", width = 75.dp)
                                DataCell(text = "${result.arabicRequired}", width = 85.dp, color = EmeraldDark)
                                DataCell(text = "${result.religionRequired}", width = 85.dp, color = EmeraldDark)
                                DataCell(text = "${result.totalRequired}", width = 95.dp, isBold = true)
                                DataCell(text = "${school.teachersAssistant}", width = 80.dp)
                                DataCell(text = "${school.teachersFirst}", width = 75.dp)
                                DataCell(text = "${school.teachersFirstA}", width = 75.dp)
                                DataCell(text = "${school.teachersExpert}", width = 65.dp)
                                DataCell(text = "${school.teachersSenior}", width = 65.dp)
                                DataCell(text = "${result.availablePeriods}", width = 80.dp, color = EmeraldPrimary, isBold = true)

                                // Gap in periods with Severity-aware Colors
                                val gap = result.periodGap
                                val (gapColor, gapBg, gapBorder) = when (result.deficitSeverity) {
                                    QuotaDeficitSeverity.CRITICAL -> Triple(CriticalDeficitText, CriticalDeficitBg, CriticalDeficitBorder)
                                    QuotaDeficitSeverity.MODERATE -> Triple(ModerateDeficitText, ModerateDeficitBg, ModerateDeficitBorder)
                                    QuotaDeficitSeverity.MILD -> Triple(MildDeficitText, MildDeficitBg, MildDeficitBorder)
                                    QuotaDeficitSeverity.BALANCED -> Triple(BalancedBlueText, BalancedBlueBg, Color.Transparent)
                                    QuotaDeficitSeverity.SURPLUS -> Triple(SurplusGreenText, SurplusGreenBg, Color.Transparent)
                                }

                                Box(
                                    modifier = Modifier
                                        .width(90.dp)
                                        .padding(horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        color = gapBg,
                                        shape = RoundedCornerShape(6.dp),
                                        border = if (gapBorder != Color.Transparent) BorderStroke(1.dp, gapBorder.copy(alpha = 0.6f)) else null
                                    ) {
                                        Text(
                                            text = if (gap > 0) "+$gap" else "$gap",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = gapColor
                                        )
                                    }
                                }

                                // Teacher Equivalent
                                DataCell(
                                    text = QuotaCalculations.formatDouble(result.teacherEquivalent),
                                    width = 95.dp,
                                    isBold = true,
                                    color = gapColor
                                )

                                // Automatic Classification Badge (التصنيف التلقائي)
                                Box(
                                    modifier = Modifier
                                        .width(105.dp)
                                        .padding(horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        color = gapBg,
                                        shape = RoundedCornerShape(6.dp),
                                        border = if (gapBorder != Color.Transparent) BorderStroke(1.dp, gapBorder.copy(alpha = 0.7f)) else null
                                    ) {
                                        Text(
                                            text = result.deficitSeverity.shortBadge,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = gapColor
                                        )
                                    }
                                }

                                // Teachers Button (Manage/Add/Transfer)
                                Box(
                                    modifier = Modifier
                                        .width(110.dp)
                                        .padding(horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    FilledTonalButton(
                                        onClick = { viewModel.openManageTeachers(school) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = MintContainer,
                                            contentColor = EmeraldDark
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("manage_teachers_btn_${school.id}")
                                    ) {
                                        Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("المعلمين", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        Divider(color = Slate200.copy(alpha = 0.5f))
                    }
                }
            }
        }
    } else {
        // Data Table for Teachers (جدول بيانات المدرسين مع العجز والزيادة)
        TeachersDataTable(
            teachers = teachersWithQuota,
            onTransferTeacher = { teacher ->
                viewModel.showTransferTeacherModal.value = teacher
            },
            onDeleteTeacher = { teacher ->
                viewModel.deleteTeacher(teacher)
            },
            horizontalScrollState = teacherHorizontalScrollState,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        )
    }
    }

    // Reports & Export Dialog (تصدير التقارير بصيغة PDF أو CSV)
    if (showReportsDialog) {
        ExportReportsDialog(
            onDismiss = { showReportsDialog = false },
            onExportDashboardPdf = { viewModel.exportDashboardPdf(context) },
            onExportTeachersPdf = { viewModel.exportTeachersPdf(context) },
            onExportSchoolsCsv = { viewModel.exportSchoolsCsv(context) },
            onExportTeachersCsv = { viewModel.exportTeachersCsv(context) }
        )
    }

    // Automatic Classification Guide Dialog (دليل التصنيفات التلقائية للعجز والأنصبة)
    if (showClassificationGuideDialog) {
        ClassificationGuideDialog(
            onDismiss = { showClassificationGuideDialog = false }
        )
    }

    // Global Add Teacher Dialog (إضافة معلم جديد مع اختيار المدرسة)
    if (showAddGlobalTeacherDialog) {
        AddTeacherDialog(
            schoolId = allSchools.firstOrNull()?.id ?: 1L,
            availableSchools = allSchools,
            onDismiss = { showAddGlobalTeacherDialog = false },
            onAdd = { newTeacher ->
                viewModel.addTeacher(newTeacher)
                showAddGlobalTeacherDialog = false
            }
        )
    }

    // Modal Sheet 1: Quick-Edit School Administration & Location
    if (editingSchool != null) {
        QuickEditSchoolModal(
            school = editingSchool!!,
            onDismiss = { viewModel.closeSchoolQuickEdit() },
            onSave = { updated ->
                viewModel.updateSchool(updated)
                viewModel.closeSchoolQuickEdit()
            }
        )
    }

    // Modal Sheet 2: Manage School Teachers
    if (managingSchoolTeachers != null) {
        ManageTeachersModal(
            school = managingSchoolTeachers!!,
            viewModel = viewModel,
            onDismiss = { viewModel.closeManageTeachers() }
        )
    }

    // Modal Dialog 3: Transfer Teacher to Another School
    if (transferTeacherTarget != null) {
        TransferTeacherDialog(
            teacher = transferTeacherTarget!!,
            schools = allSchools,
            onDismiss = { viewModel.showTransferTeacherModal.value = null },
            onTransfer = { targetSchoolId, targetSchoolName ->
                viewModel.transferTeacher(transferTeacherTarget!!.id, targetSchoolId, targetSchoolName)
                viewModel.showTransferTeacherModal.value = null
            }
        )
    }
}

@Composable
private fun HeaderCell(title: String, width: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DataCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    isBold: Boolean = false,
    color: Color = Color.Unspecified
) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            style = MaterialTheme.typography.bodySmall,
            color = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

// ----------------- Quick Edit School Modal -----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickEditSchoolModal(
    school: SchoolEntity,
    onDismiss: () -> Unit,
    onSave: (SchoolEntity) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(school.name) }
    var cPrimary by remember { mutableStateOf(school.classesPrimary.toString()) }
    var cUpper by remember { mutableStateOf(school.classesUpper.toString()) }

    var pName by remember { mutableStateOf(school.principalName) }
    var pPhone by remember { mutableStateOf(school.principalPhone) }
    var pJobTitle by remember { mutableStateOf(school.principalJobTitle) }

    var vp1Name by remember { mutableStateOf(school.vicePrincipal1Name) }
    var vp1Phone by remember { mutableStateOf(school.vicePrincipal1Phone) }
    var vp2Name by remember { mutableStateOf(school.vicePrincipal2Name) }
    var vp2Phone by remember { mutableStateOf(school.vicePrincipal2Phone) }

    var supName by remember { mutableStateOf(school.supervisorName) }
    var supPhone by remember { mutableStateOf(school.supervisorPhone) }
    var supDays by remember { mutableStateOf(school.supervisorVisitDays) }

    var lat by remember { mutableStateOf(school.latitude ?: 31.1321) }
    var lng by remember { mutableStateOf(school.longitude ?: 33.8033) }
    var isLocating by remember { mutableStateOf(false) }

    // Cadre numbers
    var tAssist by remember { mutableStateOf(school.teachersAssistant.toString()) }
    var tFirst by remember { mutableStateOf(school.teachersFirst.toString()) }
    var tFirstA by remember { mutableStateOf(school.teachersFirstA.toString()) }
    var tExpert by remember { mutableStateOf(school.teachersExpert.toString()) }
    var tSenior by remember { mutableStateOf(school.teachersSenior.toString()) }

    // Location Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            isLocating = true
            LocationHelper.getCurrentLocation(
                context,
                onLocationReceived = { latitude, longitude ->
                    lat = latitude
                    lng = longitude
                    isLocating = false
                },
                onError = {
                    isLocating = false
                }
            )
        }
    }

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
                text = "تعديل بيانات المدرسة والإدارة المدرسية",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary
            )
            Text(
                text = "حفظ فوري في قاعدة البيانات المحلية (Room DB)",
                style = MaterialTheme.typography.bodySmall,
                color = Slate600
            )

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // School Name
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("اسم المدرسة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Classes count
                item {
                    Text("أعداد الفصول:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = cPrimary,
                            onValueChange = { cPrimary = it },
                            label = { Text("صفوف أولية (1-3)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = cUpper,
                            onValueChange = { cUpper = it },
                            label = { Text("صفوف عليا (4-6)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Teachers Cadre Numbers
                item {
                    Text("أعداد المعلمين حسب الكادر:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(value = tAssist, onValueChange = { tAssist = it }, label = { Text("معلم (24)") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = tFirst, onValueChange = { tFirst = it }, label = { Text("أول (22)") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = tFirstA, onValueChange = { tFirstA = it }, label = { Text("أول أ (20)") }, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(value = tExpert, onValueChange = { tExpert = it }, label = { Text("خبير (18)") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = tSenior, onValueChange = { tSenior = it }, label = { Text("كبير (16)") }, modifier = Modifier.weight(1f))
                    }
                }

                // Principal Information
                item {
                    Text("بيانات مدير المدرسة:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = EmeraldDark)
                    OutlinedTextField(
                        value = pName,
                        onValueChange = { pName = it },
                        label = { Text("اسم مدير المدرسة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = pPhone,
                            onValueChange = { pPhone = it },
                            label = { Text("رقم الهاتف") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = pJobTitle,
                            onValueChange = { pJobTitle = it },
                            label = { Text("المسمى الوظيفي") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Vice Principals Information
                item {
                    Text("بيانات الوكلاء:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = EmeraldDark)
                    OutlinedTextField(
                        value = vp1Name,
                        onValueChange = { vp1Name = it },
                        label = { Text("وكيل الصفوف الأولية (1-3)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = vp1Phone,
                        onValueChange = { vp1Phone = it },
                        label = { Text("رقم هاتف وكيل الصفوف الأولية") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = vp2Name,
                        onValueChange = { vp2Name = it },
                        label = { Text("وكيل الصفوف العليا (4-6)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = vp2Phone,
                        onValueChange = { vp2Phone = it },
                        label = { Text("رقم هاتف وكيل الصفوف العليا") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Supervisor Information
                item {
                    Text("الموجه المتابع للمدرسة:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = EmeraldDark)
                    OutlinedTextField(
                        value = supName,
                        onValueChange = { supName = it },
                        label = { Text("اسم الموجه المتابع") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = supPhone,
                            onValueChange = { supPhone = it },
                            label = { Text("هاتف الموجه") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = supDays,
                            onValueChange = { supDays = it },
                            label = { Text("أيام الزيارات المخصصة") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // GPS Location Capture (Prompt requirement)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MintLight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("الموقع الجغرافي للمدرسة", fontWeight = FontWeight.Bold, color = EmeraldDark)
                                    Text(
                                        "خط العرض: ${String.format("%.4f", lat)} | خط الطول: ${String.format("%.4f", lng)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate600
                                    )
                                }
                                Button(
                                    onClick = {
                                        val fineGranted = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.ACCESS_FINE_LOCATION
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (fineGranted) {
                                            isLocating = true
                                            LocationHelper.getCurrentLocation(
                                                context,
                                                onLocationReceived = { l1, l2 ->
                                                    lat = l1
                                                    lng = l2
                                                    isLocating = false
                                                },
                                                onError = { isLocating = false }
                                            )
                                        } else {
                                            locationPermissionLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                                )
                                            )
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                                ) {
                                    Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isLocating) "جاري الالتقاط..." else "التقاط الموقع الحالي")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val updated = school.copy(
                            name = name,
                            classesPrimary = cPrimary.toIntOrNull() ?: school.classesPrimary,
                            classesUpper = cUpper.toIntOrNull() ?: school.classesUpper,
                            teachersAssistant = tAssist.toIntOrNull() ?: school.teachersAssistant,
                            teachersFirst = tFirst.toIntOrNull() ?: school.teachersFirst,
                            teachersFirstA = tFirstA.toIntOrNull() ?: school.teachersFirstA,
                            teachersExpert = tExpert.toIntOrNull() ?: school.teachersExpert,
                            teachersSenior = tSenior.toIntOrNull() ?: school.teachersSenior,
                            principalName = pName,
                            principalPhone = pPhone,
                            principalJobTitle = pJobTitle,
                            vicePrincipal1Name = vp1Name,
                            vicePrincipal1Phone = vp1Phone,
                            vicePrincipal2Name = vp2Name,
                            vicePrincipal2Phone = vp2Phone,
                            supervisorName = supName,
                            supervisorPhone = supPhone,
                            supervisorVisitDays = supDays,
                            latitude = lat,
                            longitude = lng
                        )
                        onSave(updated)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("حفظ التعديلات")
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

// ----------------- Manage Teachers Modal -----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageTeachersModal(
    school: SchoolEntity,
    viewModel: AppViewModel,
    onDismiss: () -> Unit
) {
    val allTeachers by viewModel.allTeachers.collectAsStateWithLifecycle()
    val schoolTeachers = remember(allTeachers, school.id) {
        allTeachers.filter { it.schoolId == school.id }
    }

    var showAddTeacherDialog by remember { mutableStateOf(false) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "معلمو: ${school.name}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                    Text(
                        text = "لغة عربية وتربية دينية (${schoolTeachers.size} معلم مسجل)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )
                }

                Button(
                    onClick = { showAddTeacherDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة معلم")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (schoolTeachers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا يوجد معلمون مسجلون بعد لهذه المدرسة.\nاضغط على 'إضافة معلم' لتسجيل معلم جديد.",
                        textAlign = TextAlign.Center,
                        color = Slate500
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(schoolTeachers) { _, teacher ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MintLight)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = teacher.fullName,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = "${teacher.subject} | ${teacher.cadreDegree} (${teacher.weeklyPeriods} حصة)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = EmeraldDark,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Row {
                                        // Transfer button
                                        IconButton(
                                            onClick = { viewModel.showTransferTeacherModal.value = teacher }
                                        ) {
                                            Icon(
                                                Icons.Default.CompareArrows,
                                                contentDescription = "نقل المعلم",
                                                tint = EmeraldPrimary
                                            )
                                        }
                                        // Delete button
                                        IconButton(
                                            onClick = { viewModel.deleteTeacher(teacher) }
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "حذف المعلم",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "كود المعلم: ${teacher.teacherCode}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate600
                                    )
                                    Text(
                                        text = "الرقم القومي: ${teacher.nationalId}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate600
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "المؤهل: ${teacher.qualification} (${teacher.qualificationDate})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate700
                                )
                                Text(
                                    text = "الهاتف: ${teacher.phone} | العنوان: ${teacher.address}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate700
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("إغلاق")
            }
        }
    }

    // Add Teacher Dialog
    if (showAddTeacherDialog) {
        AddTeacherDialog(
            schoolId = school.id,
            onDismiss = { showAddTeacherDialog = false },
            onAdd = { teacher ->
                viewModel.addTeacher(teacher)
                showAddTeacherDialog = false
            }
        )
    }
}

// ----------------- Add Teacher Dialog -----------------
@Composable
fun AddTeacherDialog(
    schoolId: Long,
    availableSchools: List<SchoolEntity> = emptyList(),
    onDismiss: () -> Unit,
    onAdd: (TeacherEntity) -> Unit
) {
    var assignedSchoolId by remember { mutableStateOf(schoolId) }
    var schoolMenuExpanded by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var nationalId by remember { mutableStateOf("") }
    var cadreDegree by remember { mutableStateOf("معلم أول") }
    var subject by remember { mutableStateOf("لغة عربية") }
    var qualification by remember { mutableStateOf("ليسانس آداب وتربية") }
    var qualificationDate by remember { mutableStateOf("2012/06/01") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("العريش") }
    var weeklyPeriods by remember { mutableStateOf("22") }

    val cadres = listOf("معلم مساعد / معلم", "معلم أول", "معلم أول أ", "معلم خبير", "كبير معلمين")
    val subjects = listOf("لغة عربية", "تربية دينية")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة معلم جديد", fontWeight = FontWeight.Bold, color = EmeraldPrimary) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (availableSchools.isNotEmpty()) {
                    item {
                        Text("المدرسة التابع لها المعلم:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                        val currentSchoolName = availableSchools.find { it.id == assignedSchoolId }?.name ?: "اختر المدرسة"
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { schoolMenuExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(currentSchoolName, color = EmeraldDark, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }

                            DropdownMenu(
                                expanded = schoolMenuExpanded,
                                onDismissRequest = { schoolMenuExpanded = false },
                                modifier = Modifier.heightIn(max = 260.dp)
                            ) {
                                availableSchools.forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text(s.name) },
                                        onClick = {
                                            assignedSchoolId = s.id
                                            schoolMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("الاسم بالكامل") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("كود المعلم") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = nationalId, onValueChange = { nationalId = it }, label = { Text("الرقم القومي") }, modifier = Modifier.weight(1.5f))
                    }
                }
                item {
                    Text("المادة:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        subjects.forEach { s ->
                            FilterChip(
                                selected = subject == s,
                                onClick = { subject = s },
                                label = { Text(s) }
                            )
                        }
                    }
                }
                item {
                    Text("الدرجة الوظيفية على الكادر:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                    Column {
                        cadres.forEach { c ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        cadreDegree = c
                                        weeklyPeriods = when (c) {
                                            "معلم مساعد / معلم" -> "24"
                                            "معلم أول" -> "22"
                                            "معلم أول أ" -> "20"
                                            "معلم خبير" -> "18"
                                            else -> "16"
                                        }
                                    }
                            ) {
                                RadioButton(selected = cadreDegree == c, onClick = {
                                    cadreDegree = c
                                    weeklyPeriods = when (c) {
                                        "معلم مساعد / معلم" -> "24"
                                        "معلم أول" -> "22"
                                        "معلم أول أ" -> "20"
                                        "معلم خبير" -> "18"
                                        else -> "16"
                                    }
                                })
                                Text(c, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(value = qualification, onValueChange = { qualification = it }, label = { Text("المؤهل الدراسي") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = qualificationDate, onValueChange = { qualificationDate = it }, label = { Text("تاريخ الحصول عليه") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("رقم الموبايل") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("العنوان") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = weeklyPeriods, onValueChange = { weeklyPeriods = it }, label = { Text("جدول الحصص الأسبوعية") }, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.isNotBlank()) {
                        onAdd(
                            TeacherEntity(
                                schoolId = assignedSchoolId,
                                fullName = fullName,
                                teacherCode = code,
                                nationalId = nationalId,
                                cadreDegree = cadreDegree,
                                subject = subject,
                                qualification = qualification,
                                qualificationDate = qualificationDate,
                                phone = phone,
                                address = address,
                                weeklyPeriods = weeklyPeriods.toIntOrNull() ?: 24
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("إضافة المعلم")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

// ----------------- Transfer Teacher Dialog -----------------
@Composable
fun TransferTeacherDialog(
    teacher: TeacherEntity,
    schools: List<SchoolEntity>,
    onDismiss: () -> Unit,
    onTransfer: (targetSchoolId: Long, targetSchoolName: String) -> Unit
) {
    var selectedSchoolId by remember { mutableStateOf<Long?>(null) }
    var selectedSchoolName by remember { mutableStateOf("") }
    var filterQuery by remember { mutableStateOf("") }

    val filtered = remember(schools, filterQuery) {
        if (filterQuery.isBlank()) schools else schools.filter { it.name.contains(filterQuery.trim()) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "نقل المعلم: ${teacher.fullName}",
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "اختر المدرسة المراد نقل المعلم إليها من مدارس إدارة العريش:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = filterQuery,
                    onValueChange = { filterQuery = it },
                    placeholder = { Text("بحث عن اسم المدرسة...") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    itemsIndexed(filtered) { _, school ->
                        val isSelected = selectedSchoolId == school.id
                        Surface(
                            color = if (isSelected) MintContainer else Color.Transparent,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedSchoolId = school.id
                                    selectedSchoolName = school.name
                                }
                                .padding(vertical = 6.dp, horizontal = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        selectedSchoolId = school.id
                                        selectedSchoolName = school.name
                                    }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = school.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedSchoolId != null) {
                        onTransfer(selectedSchoolId!!, selectedSchoolName)
                    }
                },
                enabled = selectedSchoolId != null,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("تأكيد النقل")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

// ----------------- Teachers Data Table (جدول بيانات المدرسين مع العجز والزيادة) -----------------
@Composable
fun TeachersDataTable(
    teachers: List<TeacherWithSchoolQuota>,
    onTransferTeacher: (TeacherEntity) -> Unit,
    onDeleteTeacher: (TeacherEntity) -> Unit,
    horizontalScrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    if (teachers.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MintLight,
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                Text(
                    text = "لا توجد نتائج مطابقة لبحث المعلمين",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate800
                )
                Text(
                    text = "جرّب تغيير كلمات البحث أو إعادة ضبط فلتر المدرسة وحالة العجز والزيادة.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        Box(modifier = modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Table Header (Frozen Sticky)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldPrimary)
                ) {
                    // Sticky First Column (Teacher Name & Info)
                    Box(
                        modifier = Modifier
                            .width(190.dp)
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "اسم المعلم والبيانات",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    // Horizontally Scrollable Header Columns
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(horizontalScrollState),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HeaderCell(title = "المدرسة التابع لها", width = 170.dp)
                        HeaderCell(title = "موقف المدرسة", width = 110.dp)
                        HeaderCell(title = "فجوة المدرسة", width = 90.dp)
                        HeaderCell(title = "المادة", width = 90.dp)
                        HeaderCell(title = "الكادر الوظيفي", width = 120.dp)
                        HeaderCell(title = "النصاب القانوني", width = 90.dp)
                        HeaderCell(title = "الحصص المسندة", width = 90.dp)
                        HeaderCell(title = "فرق النصاب", width = 85.dp)
                        HeaderCell(title = "المؤهل والتخرج", width = 140.dp)
                        HeaderCell(title = "الهاتف", width = 110.dp)
                        HeaderCell(title = "إجراءات", width = 100.dp)
                    }
                }

                // Table Rows
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(teachers, key = { _, item -> item.teacher.id }) { index, item ->
                        val teacher = item.teacher
                        val rowBg = if (index % 2 == 0) MaterialTheme.colorScheme.surface else Slate100.copy(alpha = 0.5f)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(rowBg)
                                .testTag("teacher_row_${teacher.id}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Sticky First Column (Teacher Name + Code + Subject Badge)
                            Surface(
                                modifier = Modifier.width(190.dp),
                                color = rowBg
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = teacher.fullName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Surface(
                                            color = MintLight,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "كود: ${teacher.teacherCode}",
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                color = EmeraldDark
                                            )
                                        }
                                        Text(
                                            text = teacher.subject,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            color = Slate600
                                        )
                                    }
                                }
                            }

                            // Horizontally Scrollable Cells
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .horizontalScroll(horizontalScrollState),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // School Name
                                Box(
                                    modifier = Modifier
                                        .width(170.dp)
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = item.schoolName,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = EmeraldDark,
                                        maxLines = 2
                                    )
                                }

                                // School Status Badge (Deficit / Surplus / Balanced)
                                Box(
                                    modifier = Modifier
                                        .width(110.dp)
                                        .padding(horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val (badgeBg, badgeColor, badgeText) = when {
                                        item.schoolSeverity == QuotaDeficitSeverity.CRITICAL -> Triple(CriticalDeficitBg, CriticalDeficitText, "🚨 عجز حاد")
                                        item.schoolSeverity == QuotaDeficitSeverity.MODERATE -> Triple(ModerateDeficitBg, ModerateDeficitText, "⚠️ عجز متوسط")
                                        item.schoolSeverity == QuotaDeficitSeverity.MILD -> Triple(MildDeficitBg, MildDeficitText, "🟡 عجز طفيف")
                                        item.isSchoolSurplus -> Triple(SurplusGreenBg, SurplusGreenText, "زيادة بالمدرسة")
                                        else -> Triple(BalancedBlueBg, BalancedBlueText, "متوازن")
                                    }
                                    Surface(
                                        color = badgeBg,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = badgeText,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = badgeColor
                                        )
                                    }
                                }

                                // School Gap
                                Box(
                                    modifier = Modifier
                                        .width(90.dp)
                                        .padding(horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val gap = item.schoolDeficitOrSurplus
                                    val gapColor = if (item.isSchoolDeficit) DeficitRedText else if (item.isSchoolSurplus) SurplusGreenText else BalancedBlueText
                                    Text(
                                        text = if (gap > 0) "+$gap حصة" else "$gap حصة",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = gapColor
                                    )
                                }

                                // Subject
                                DataCell(text = teacher.subject, width = 90.dp)

                                // Cadre
                                DataCell(text = teacher.cadreDegree, width = 120.dp)

                                // Legal Quota
                                DataCell(text = "${item.legalQuota} حصة", width = 90.dp, color = Slate600)

                                // Assigned Weekly Periods
                                DataCell(text = "${teacher.weeklyPeriods} حصة", width = 90.dp, isBold = true, color = EmeraldPrimary)

                                // Quota Difference (weeklyPeriods - legalQuota)
                                Box(
                                    modifier = Modifier
                                        .width(85.dp)
                                        .padding(horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val diff = item.quotaDifference
                                    val diffColor = if (diff >= 0) SurplusGreenText else DeficitRedText
                                    Text(
                                        text = if (diff > 0) "+$diff" else "$diff",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = diffColor
                                    )
                                }

                                // Qualification
                                DataCell(text = "${teacher.qualification} (${teacher.qualificationDate.take(4)})", width = 140.dp)

                                // Phone
                                DataCell(text = teacher.phone, width = 110.dp)

                                // Actions (Transfer & Delete)
                                Row(
                                    modifier = Modifier
                                        .width(100.dp)
                                        .padding(horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { onTransferTeacher(teacher) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.CompareArrows,
                                            contentDescription = "نقل المعلم",
                                            tint = EmeraldPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteTeacher(teacher) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "حذف المعلم",
                                            tint = DeficitRedText,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Divider(color = Slate200.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

// ----------------- Export Reports Dialog (تقارير التوجيه الفني والتصدير) -----------------
@Composable
fun ExportReportsDialog(
    onDismiss: () -> Unit,
    onExportDashboardPdf: () -> Unit,
    onExportTeachersPdf: () -> Unit,
    onExportSchoolsCsv: () -> Unit,
    onExportTeachersCsv: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Print, contentDescription = null, tint = EmeraldPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تقارير التوجيه الفني والتصدير", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "اختر نوع التقرير المطلوب لتسهيل المتابعة الفنية والإدارية لإدارة العريش التعليمية:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )

                // Option 1: PDF Teachers Quotas & Deficit/Surplus Report
                Card(
                    onClick = {
                        onExportTeachersPdf()
                        onDismiss()
                    },
                    colors = CardDefaults.cardColors(containerColor = MintLight),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = EmeraldPrimary,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("كشف أنصبة المعلمين والتوجيه الفني (PDF)", fontWeight = FontWeight.Bold, color = EmeraldDark, style = MaterialTheme.typography.bodyMedium)
                            Text("تقرير جدول المعلمين وموقف العجز والزيادة لكل مدرسة مع اعتمادات التوجيه", style = MaterialTheme.typography.bodySmall, color = Slate600)
                        }
                    }
                }

                // Option 2: PDF Schools Dashboard & Statistics Report
                Card(
                    onClick = {
                        onExportDashboardPdf()
                        onDismiss()
                    },
                    colors = CardDefaults.cardColors(containerColor = MintLight),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = EmeraldPrimary,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("تقرير إحصائيات وأنصبة المدارس (PDF)", fontWeight = FontWeight.Bold, color = EmeraldDark, style = MaterialTheme.typography.bodyMedium)
                            Text("إحصائية الـ 65 مدرسة، الفصول، الحصص المطلوبة والمتوفرة ومعدلات العجز/الزيادة", style = MaterialTheme.typography.bodySmall, color = Slate600)
                        }
                    }
                }

                // Option 3: CSV / Excel Teachers Data
                Card(
                    onClick = {
                        onExportTeachersCsv()
                        onDismiss()
                    },
                    colors = CardDefaults.cardColors(containerColor = Slate100),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = EmeraldDark,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.TableChart, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("جدول بيانات المعلمين والأنصبة (CSV / Excel)", fontWeight = FontWeight.Bold, color = Slate800, style = MaterialTheme.typography.bodyMedium)
                            Text("ملف إكسل كامل ببيانات المعلمين، الأكواد، وموقف العجز والزيادة لكل مدرسة", style = MaterialTheme.typography.bodySmall, color = Slate600)
                        }
                    }
                }

                // Option 4: CSV / Excel Schools Data
                Card(
                    onClick = {
                        onExportSchoolsCsv()
                        onDismiss()
                    },
                    colors = CardDefaults.cardColors(containerColor = Slate100),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = EmeraldDark,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("جدول أنصبة مدارس العريش الشامل (CSV / Excel)", fontWeight = FontWeight.Bold, color = Slate800, style = MaterialTheme.typography.bodyMedium)
                            Text("ملف إكسل يتضمن جميع الـ 65 مدرسة وبيانات الفصول والمدرسين والعجز والزيادة", style = MaterialTheme.typography.bodySmall, color = Slate600)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
            }
        }
    )
}

// ----------------- Automatic Classification Guide Dialog -----------------
@Composable
fun ClassificationGuideDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "دليل التصنيف التلقائي للأنصبة والعجز",
                    fontWeight = FontWeight.Bold,
                    color = EmeraldDark,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "نظام التصنيف التلقائي المعتمد لإدارة العريش التعليمية وفق القرارات الوزارية المنظمة للأنصبة:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )

                // Critical Deficit
                Surface(
                    color = CriticalDeficitBg,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CriticalDeficitBorder.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🚨 عجز حاد (Critical Deficit)", fontWeight = FontWeight.Bold, color = CriticalDeficitText, fontSize = 13.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            Text("نقص ≥ 20 حصة", fontWeight = FontWeight.Bold, color = CriticalDeficitText, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "يمثل عجز معلم كامل فأكثر. يتطلب تدخلاً عاجلاً ولجنة فورية لندب معلمين من المدارس ذات الزيادة.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CriticalDeficitText.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                }

                // Moderate Deficit
                Surface(
                    color = ModerateDeficitBg,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, ModerateDeficitBorder.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚠️ عجز متوسط (Moderate Deficit)", fontWeight = FontWeight.Bold, color = ModerateDeficitText, fontSize = 13.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            Text("نقص 10 - 19 حصة", fontWeight = FontWeight.Bold, color = ModerateDeficitText, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "نقص يقارب نصف نصاب معلم. تتم معالجته بالندب الجزئي المشترك أو استكمال النصاب مع مدرسة مجاورة.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ModerateDeficitText.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                }

                // Mild Deficit
                Surface(
                    color = MildDeficitBg,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MildDeficitBorder.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🟡 عجز طفيف (Mild Deficit)", fontWeight = FontWeight.Bold, color = MildDeficitText, fontSize = 13.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            Text("نقص 1 - 9 حصص", fontWeight = FontWeight.Bold, color = MildDeficitText, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "نقص محدود يمكن تغطيته داخلياً بزيادة أنصبة بعض المعلمين بنظام الحصة الزائدة بمقابل.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MildDeficitText.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                }

                // Balanced
                Surface(
                    color = BalancedBlueBg,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✓ متوازن (Balanced)", fontWeight = FontWeight.Bold, color = BalancedBlueText, fontSize = 13.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            Text("فجوة = 0", fontWeight = FontWeight.Bold, color = BalancedBlueText, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "النصاب المتوفر يطابق تماماً الحصص المطلوبة وفق خطة الدراسة والخريطة الزمنية.",
                            style = MaterialTheme.typography.bodySmall,
                            color = BalancedBlueText.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                }

                // Surplus
                Surface(
                    color = SurplusGreenBg,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🟢 زيادة (Surplus)", fontWeight = FontWeight.Bold, color = SurplusGreenText, fontSize = 13.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            Text("فائض حصص > 0", fontWeight = FontWeight.Bold, color = SurplusGreenText, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "وفرة في أنصبة المعلمين مقارنة بالفصول، وتعد مصدراً لانتداب المعلمين للمدارس ذات العجز.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SurplusGreenText.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("فهمت ذلك")
            }
        }
    )
}
