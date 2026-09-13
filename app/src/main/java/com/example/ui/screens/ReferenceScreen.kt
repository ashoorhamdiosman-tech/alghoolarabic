package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel
import com.example.util.QuotaCalculations

@Composable
fun ReferenceScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    var simPrimary by remember { mutableStateOf("12") }
    var simUpper by remember { mutableStateOf("12") }
    var simQuota by remember { mutableStateOf("24") }

    val p = simPrimary.toIntOrNull() ?: 0
    val u = simUpper.toIntOrNull() ?: 0
    val q = (simQuota.toIntOrNull() ?: 24).coerceAtLeast(1)

    val simArabic = (p * 9) + (u * 9)
    val simReligion = (p * 5) + (u * 4)
    val simTotal = simArabic + simReligion
    val simTeachersNeeded = simTotal.toDouble() / q

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("reference_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        // 1. Header Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldPrimary)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "المرجعية القانونية واللوائح الوزارية",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "القانون رقم ١٥٦ لسنة ٢٠٠٧ ومنشور الحافز رقم (٨) لسنة ٢٠٢٤",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MintContainer
                    )
                }
            }
        }

        // 2. Law 156 Cadre Quotas Table
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Gavel, contentDescription = null, tint = EmeraldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "أنصبة المعلمين وفق القانون ١٥٦ لسنة ٢٠٠٧ (كادر المعلمين)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val cadreData = listOf(
                        Triple("معلم مساعد / معلم", "24 حصة أسبوعياً", "المرحلة الابتدائية"),
                        Triple("معلم أول", "22 حصة أسبوعياً", "المرحلة الابتدائية"),
                        Triple("معلم أول أ", "20 حصة أسبوعياً", "المرحلة الابتدائية"),
                        Triple("معلم خبير", "18 حصة أسبوعياً", "المرحلة الابتدائية"),
                        Triple("كبير معلمين", "16 حصة أسبوعياً", "المرحلة الابتدائية")
                    )

                    Surface(
                        color = EmeraldPrimary,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("المسمى الوظيفي على الكادر", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(2f))
                            Text("النصاب القانوني", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.5f))
                            Text("المرحلة", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        }
                    }

                    cadreData.forEachIndexed { idx, item ->
                        val bg = if (idx % 2 == 0) MintLight else Color.Transparent
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bg)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.first, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(2f))
                            Text(item.second, fontWeight = FontWeight.Bold, color = EmeraldPrimary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.5f))
                            Text(item.third, style = MaterialTheme.typography.bodySmall, color = Slate600, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // 3. Circular No. 8 (منشور رقم 8 الخاص بالحوافز والحصص الزائدة)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFD97706))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "منشور رقم (8): ضوابط صرف الحافز والحصص الزائدة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val rules = listOf(
                        "استحقاق المعلم لحافز تطوير التعليم مشروط بالالتزام بتدريس النصاب القانوني كاملاً دون تخفيض غير قانوني.",
                        "أي حصص يتم إسنادها للمعلم زيادة عن نصابه القانوني المعتمد وفق كادر المعلمين تُعامل كحصص زائدة مدفوعة الأجر وفق الكتاب الدوري المعمول به.",
                        "يُشترط التوقيع في دفتر الحضور والانصراف بحد أدنى عدد الأيام القانونية المقررة شهرياً (لا تقل عن 18 توقيعاً فعلياً باستثناء العطلات الرسمية).",
                        "تتولى إدارة التعليم الابتدائي وتوجيه اللغة العربية بالإدارة المتابعة الدورية ومطابقة الجداول مع الواقع الفعلي بالمدارس."
                    )

                    rules.forEach { rule ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = rule,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }

        // 4. Mathematical Calculation Formulas Box
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MintLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Calculate, contentDescription = null, tint = EmeraldDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "المعادلات الرياضية لحساب الأنصبة والعجز والزيادة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    FormulaItem(
                        label = "حصص اللغة العربية المطلوبة",
                        formula = "(فصول ١-٣ × ٩) + (فصول ٤-٦ × ٩)"
                    )
                    FormulaItem(
                        label = "حصص التربية الدينية المطلوبة",
                        formula = "(فصول ١-٣ × ٥) + (فصول ٤-٦ × ٤)"
                    )
                    FormulaItem(
                        label = "إجمالي الحصص المطلوبة للمدرسة",
                        formula = "حصص اللغة العربية + حصص التربية الدينية"
                    )
                    FormulaItem(
                        label = "الحصص المتوفرة الفعلية",
                        formula = "مجموع (أعداد المعلمين بكل رتبة × نصاب الرتبة القانوني)"
                    )
                    FormulaItem(
                        label = "فجوة الحصص (العجز / الزيادة)",
                        formula = "الحصص المتوفرة - الحصص المطلوبة (السالب عجز والموجب زيادة)"
                    )
                    FormulaItem(
                        label = "المكافئ بالمعلمين",
                        formula = "فجوة الحصص ÷ النصاب المرجعي (افتراضي 24 حصة)"
                    )
                }
            }
        }

        // 5. Interactive Simulation Calculator
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "محاكي احتساب أنصبة مدرسة افتراضية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                    Text(
                        text = "جرب إدخال أعداد الفصول لمعاينة الحسابات الفورية",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = simPrimary,
                            onValueChange = { simPrimary = it },
                            label = { Text("فصول (1-3)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = simUpper,
                            onValueChange = { simUpper = it },
                            label = { Text("فصول (4-6)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = simQuota,
                            onValueChange = { simQuota = it },
                            label = { Text("النصاب المرجعي") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = MintContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("مطلوب لغة عربية: $simArabic حصة", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                                Text("مطلوب دين: $simReligion حصة", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "إجمالي الحصص المطلوبة: $simTotal حصة",
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDark
                                )
                                Text(
                                    text = "الاحتياج من المعلمين: ${QuotaCalculations.formatDouble(simTeachersNeeded)} معلم",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Creator Credits
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MintLight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "جمهورية مصر العربية - محافظة شمال سيناء",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )
                    Text(
                        text = "مديرية التربية والتعليم - إدارة العريش التعليمية",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "تصميم وبرمجة: دكتور/ أحمد حمدي عاشور الغول",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                    Text(
                        text = "جميع الحقوق محفوظة - العام الدراسي ٢٠٢٦ / ٢٠٢٧ م",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate500
                    )
                }
            }
        }
    }
}

@Composable
private fun FormulaItem(label: String, formula: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = EmeraldDark
        )
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = formula,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = Slate800
            )
        }
    }
}
