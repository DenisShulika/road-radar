package com.denisshulika.road_radar.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoPickerDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onPickFromGallery: () -> Unit,
    onTakePhoto: () -> Unit,
    localization: Map<String, String>,
    theme: Map<String, Color>
) {
    if (showDialog) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { onDismiss() },
            containerColor = theme["background"]!!,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            contentColor = theme["text"]!!,
            tonalElevation = 8.dp,
            scrimColor = Color.Black.copy(alpha = 0.32f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = localization["choose_option_title"]!!,
                        color = theme["text"]!!,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp
                    )
                }
                Spacer(modifier = Modifier.size(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    TextButton(
                        onClick = {
                            onTakePhoto()
                            onDismiss()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            tint = theme["icon"]!!,
                            contentDescription = "",
                        )
                        Spacer(modifier = Modifier.size(12.dp))
                        Text(
                            text = localization["camera_option"]!!,
                            color = theme["text"]!!,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.size(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    TextButton(
                        onClick = {
                            onPickFromGallery()
                            onDismiss()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            tint = theme["icon"]!!,
                            contentDescription = "",
                        )
                        Spacer(modifier = Modifier.size(12.dp))
                        Text(
                            text = localization["photo_library_option"]!!,
                            color = theme["text"]!!,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }
    }
}