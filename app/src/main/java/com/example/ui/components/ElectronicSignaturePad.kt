package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate700

@Composable
fun ElectronicSignaturePad(
    title: String,
    signerRole: String,
    onSignatureCaptured: (signatureSummary: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val points = remember { mutableStateListOf<Offset?>() }
    var hasSigned by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("signature_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = EmeraldPrimary
                    )
                    Text(
                        text = signerRole,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate700
                    )
                }
                if (hasSigned) {
                    SuggestionChip(
                        onClick = { },
                        label = { Text("تم التوقيع الإلكتروني") },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = Color(0xFFDCFCE7),
                            labelColor = Color(0xFF15803D)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Signature drawing area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFCFDFD))
                    .border(1.dp, if (hasSigned) EmeraldPrimary else Slate200, RoundedCornerShape(8.dp))
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                points.add(offset)
                                hasSigned = true
                            },
                            onDrag = { change, _ ->
                                points.add(change.position)
                            },
                            onDragEnd = {
                                points.add(null)
                            }
                        )
                    }
                    .testTag("signature_canvas_box")
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val path = Path()
                    var isFirst = true

                    for (point in points) {
                        if (point == null) {
                            isFirst = true
                        } else if (isFirst) {
                            path.moveTo(point.x, point.y)
                            isFirst = false
                        } else {
                            path.lineTo(point.x, point.y)
                        }
                    }

                    drawPath(
                        path = path,
                        color = EmeraldPrimary,
                        style = Stroke(
                            width = 4.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                if (points.isEmpty()) {
                    Text(
                        text = "وقع هنا بإصبعك أو القلم الإلكتروني...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Control Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        points.clear()
                        hasSigned = false
                    },
                    modifier = Modifier.testTag("clear_signature_btn"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "مسح التوقيع", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("مسح التوقيع")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (hasSigned) {
                            onSignatureCaptured("توقيع إلكتروني معتمد - $signerRole (${System.currentTimeMillis()})")
                        }
                    },
                    enabled = hasSigned,
                    modifier = Modifier.testTag("confirm_signature_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Icon(Icons.Default.Done, contentDescription = "تأكيد التوقيع", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("اعتماد التوقيع")
                }
            }
        }
    }
}
