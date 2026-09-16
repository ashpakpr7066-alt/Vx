package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.TextClipConfig
import com.example.ui.theme.*

@Composable
fun TextEditorDialog(
    config: TextClipConfig,
    onSave: (TextClipConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(config.text) }
    var fontSize by remember { mutableFloatStateOf(config.fontSizeSp) }
    var textColorHex by remember { mutableLongStateOf(config.textColorHex) }
    var hasBgBox by remember { mutableStateOf(config.hasBackgroundBox) }
    var isBold by remember { mutableStateOf(config.isBold) }
    var isItalic by remember { mutableStateOf(config.isItalic) }

    val presetColors = listOf(
        0xFFFFFFFF, 0xFF00E5FF, 0xFFFFD166, 0xFFEC4899,
        0xFF10B981, 0xFFA855F7, 0xFFEF4444, 0xFF000000
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = VNSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, VNBorder),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Edit Text & Typography",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = VNTextPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Text Content") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VNPurple,
                        unfocusedBorderColor = VNBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Font Size
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Font Size", fontSize = 12.sp, color = VNTextSecondary)
                    Text("${fontSize.toInt()} sp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VNPurple)
                }
                Slider(
                    value = fontSize,
                    onValueChange = { fontSize = it },
                    valueRange = 14f..64f,
                    colors = SliderDefaults.colors(
                        thumbColor = VNPurple,
                        activeTrackColor = VNPurple,
                        inactiveTrackColor = VNBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Color Chips
                Text("Text Color", fontSize = 12.sp, color = VNTextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    presetColors.forEach { cHex ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(cHex))
                                .border(
                                    width = if (textColorHex == cHex) 2.5.dp else 1.dp,
                                    color = if (textColorHex == cHex) VNCyan else Color.Gray,
                                    shape = CircleShape
                                )
                                .clickable { textColorHex = cHex }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Options: Background Box, Bold, Italic
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = hasBgBox,
                            onCheckedChange = { hasBgBox = it },
                            colors = CheckboxDefaults.colors(checkedColor = VNPurple)
                        )
                        Text("Background Box", fontSize = 12.sp, color = VNTextPrimary)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilterChip(
                            selected = isBold,
                            onClick = { isBold = !isBold },
                            label = { Text("B", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = VNPurple)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        FilterChip(
                            selected = isItalic,
                            onClick = { isItalic = !isItalic },
                            label = { Text("I", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = VNPurple)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = VNTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(
                                config.copy(
                                    text = text,
                                    fontSizeSp = fontSize,
                                    textColorHex = textColorHex,
                                    hasBackgroundBox = hasBgBox,
                                    isBold = isBold,
                                    isItalic = isItalic
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VNPurple)
                    ) {
                        Text("Apply", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
