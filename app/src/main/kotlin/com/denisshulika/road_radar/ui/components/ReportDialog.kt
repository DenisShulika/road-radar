package com.denisshulika.road_radar.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.denisshulika.road_radar.pages.RubikFont

@Composable
fun ReportDialog(
    onDismiss: () -> Unit,
    onSend: (type: String, message: String) -> Unit,
    reportTargetType: String,
    localization: Map<String, String>,
    theme: Map<String, Color>
) {
    var selectedReason by remember { mutableStateOf("") }
    var customMessage by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val reasons = if (reportTargetType == "incident") listOf(
        localization["report_reason_fake"]!!,
        localization["report_reason_duplicate"]!!,
        localization["report_reason_irrelevant"]!!,
        localization["report_reason_other"]!!
    ) else listOf(
        localization["report_reason_abuse"]!!,
        localization["report_reason_spam"]!!,
        localization["report_reason_other"]!!
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (reportTargetType == "incident") localization["incident_report_title"]!! else localization["profile_report_title"]!!,
                fontFamily = RubikFont,
                fontWeight = FontWeight.Medium,
                color = theme["text"]!!
            )
        },
        text = {
            Column {
                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    OutlinedButton(
                        onClick = { dropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = theme["text"]!!)
                    ) {
                        Text(
                            text = selectedReason.ifEmpty { localization["report_reason_placeholder"]!! },
                            fontFamily = RubikFont
                        )
                    }
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.background(theme["background"]!!)
                    ) {
                        reasons.forEach { reason ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = reason,
                                        fontFamily = RubikFont,
                                        color = theme["text"]!!
                                    )
                                },
                                onClick = {
                                    selectedReason = reason
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                StyledBasicTextField(
                    value = customMessage,
                    onValueChange = { customMessage = it },
                    placeholder = localization["report_message_placeholder"]!!,
                    isVisible = true,
                    singleLine = false,
                    theme = theme
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSend(selectedReason, customMessage)
                },
                enabled = selectedReason.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = theme["primary"]!!)
            ) {
                Text(
                    text = localization["report_send"]!!,
                    fontFamily = RubikFont,
                    color = theme["text"]!!
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = theme["primary"]!!)
            ) {
                Text(
                    text = localization["report_cancel"]!!,
                    fontFamily = RubikFont,
                    color = theme["text"]!!
                )
            }
        },
        containerColor = theme["background"]!!
    )
}