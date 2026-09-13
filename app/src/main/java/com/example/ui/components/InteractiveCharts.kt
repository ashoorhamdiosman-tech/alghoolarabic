package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class CadreBarData(
    val title: String,
    val count: Int,
    val quotaPerTeacher: Int,
    val color: Color
)

@Composable
fun TeacherCadreBarChart(
    assistantCount: Int,
    firstCount: Int,
    firstACount: Int,
    expertCount: Int,
    seniorCount: Int,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        CadreBarData("معلم / مساعد", assistantCount, 24, EmeraldPrimary),
        CadreBarData("معلم أول", firstCount, 22, Color(0xFF0D9488)),
        CadreBarData("معلم أول أ", firstACount, 20, Color(0xFF0284C7)),
        CadreBarData("معلم خبير", expertCount, 18, Color(0xFF7C3AED)),
        CadreBarData("كبير معلمين", seniorCount, 16, Color(0xFFD97706))
    )

    val maxCount = (items.maxOfOrNull { it.count } ?: 1).coerceAtLeast(1)
    var selectedItem by remember { mutableStateOf<CadreBarData?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cadre_barchart_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "الرسم البياني لتوزيع المعلمين على الكادر",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "اضغط على أي عمود لمعرفة إجمالي الحصص المغطاة",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )
                }
                Surface(
                    color = MintContainer,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "إجمالي: ${items.sumOf { it.count }} معلم",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = EmeraldDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bars representation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                items.forEach { item ->
                    val heightFraction = (item.count.toFloat() / maxCount).coerceIn(0.08f, 1f)
                    val animatedHeight by animateFloatAsState(
                        targetValue = heightFraction,
                        animationSpec = tween(durationMillis = 600),
                        label = "barHeight"
                    )

                    val isSelected = selectedItem?.title == item.title

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable {
                                selectedItem = if (isSelected) null else item
                            }
                            .padding(horizontal = 4.dp)
                    ) {
                        // Count above bar
                        Text(
                            text = "${item.count}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isSelected) item.color else MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Animated Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.65f)
                                .height((110f * animatedHeight).dp)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(
                                    if (isSelected) item.color else item.color.copy(alpha = 0.85f)
                                )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Title below bar
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                            color = if (isSelected) item.color else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 2
                        )
                    }
                }
            }

            // Interactive Tooltip Card
            selectedItem?.let { item ->
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MintLight
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "مسمى: ${item.title}",
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDark,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "نصاب المعلم الواحد: ${item.quotaPerTeacher} حصة أسبوعية",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate700
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "إجمالي الحصص المغطاة",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate600
                            )
                            Text(
                                text = "${item.count * item.quotaPerTeacher} حصة",
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldPrimary,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SupervisorMonthlyProgressSection(
    supervisorRates: List<Pair<String, Float>>, // Name to completion rate (0f..1f)
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("supervisors_progress_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "نسب إنجاز خطط المتابعة للموجهين (التقدم الشهري)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "معدل تنفيذ الزيارات الميدانية المعتمدة لكل موجه متابع",
                style = MaterialTheme.typography.bodySmall,
                color = Slate600
            )

            Spacer(modifier = Modifier.height(14.dp))

            supervisorRates.forEach { (name, progress) ->
                val animatedProgress by animateFloatAsState(
                    targetValue = progress.coerceIn(0f, 1f),
                    animationSpec = tween(600),
                    label = "supProgress"
                )
                val percent = (progress.coerceIn(0f, 1f) * 100).toInt()

                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$percent%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (percent >= 80) EmeraldPrimary else if (percent >= 50) AlertAmber else DeficitRed
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        progress = { animatedProgress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (percent >= 80) EmeraldPrimary else if (percent >= 50) AlertAmber else DeficitRed,
                        trackColor = Slate100
                    )
                }
            }
        }
    }
}

@Composable
fun DeficitSurplusDistributionGauge(
    deficitCount: Int,
    balancedCount: Int,
    surplusCount: Int,
    totalCount: Int,
    criticalDeficitCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("deficit_surplus_gauge"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "مؤشر التوازن العام (العجز والزيادة)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (criticalDeficitCount > 0) {
                    Surface(
                        color = CriticalDeficitBg,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CriticalDeficitBorder)
                    ) {
                        Text(
                            text = "🚨 $criticalDeficitCount عجز حاد",
                            color = CriticalDeficitText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            val safeTotal = if (totalCount > 0) totalCount.toFloat() else 1f
            val hasData = totalCount > 0 && (deficitCount > 0 || balancedCount > 0 || surplusCount > 0)

            // Proportional Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
            ) {
                if (!hasData) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Slate200)
                    )
                } else {
                    if (criticalDeficitCount > 0) {
                        Box(
                            modifier = Modifier
                                .weight((criticalDeficitCount.toFloat() / safeTotal).coerceAtLeast(0.05f))
                                .fillMaxHeight()
                                .background(CriticalDeficitRed)
                        )
                    }
                    val regularDeficit = (deficitCount - criticalDeficitCount).coerceAtLeast(0)
                    if (regularDeficit > 0) {
                        Box(
                            modifier = Modifier
                                .weight((regularDeficit.toFloat() / safeTotal).coerceAtLeast(0.05f))
                                .fillMaxHeight()
                                .background(DeficitRed)
                        )
                    }
                    if (balancedCount > 0) {
                        Box(
                            modifier = Modifier
                                .weight((balancedCount.toFloat() / safeTotal).coerceAtLeast(0.05f))
                                .fillMaxHeight()
                                .background(BalancedBlue)
                        )
                    }
                    if (surplusCount > 0) {
                        Box(
                            modifier = Modifier
                                .weight((surplusCount.toFloat() / safeTotal).coerceAtLeast(0.05f))
                                .fillMaxHeight()
                                .background(SurplusGreen)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                BadgeLegend(
                    title = if (criticalDeficitCount > 0) "عجز ($criticalDeficitCount حاد)" else "مدارس بها عجز",
                    count = deficitCount,
                    color = if (criticalDeficitCount > 0) CriticalDeficitText else DeficitRed,
                    bgColor = if (criticalDeficitCount > 0) CriticalDeficitBg else DeficitRedBg
                )
                BadgeLegend(
                    title = "مدارس متوازنة",
                    count = balancedCount,
                    color = BalancedBlueText,
                    bgColor = BalancedBlueBg
                )
                BadgeLegend(
                    title = "مدارس بها زيادة",
                    count = surplusCount,
                    color = SurplusGreenText,
                    bgColor = SurplusGreenBg
                )
            }
        }
    }
}

@Composable
private fun BadgeLegend(title: String, count: Int, color: Color, bgColor: Color) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = color
                )
                Text(
                    text = "$count مدرسة",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}
