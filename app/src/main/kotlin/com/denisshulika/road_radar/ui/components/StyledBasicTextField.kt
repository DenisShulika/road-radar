package com.denisshulika.road_radar.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denisshulika.road_radar.pages.RubikFont

@Composable
fun StyledBasicTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isVisible: Boolean = true,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    theme: Map<String, Color>
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                val y = size.height - strokeWidth / 2
                drawLine(
                    color = theme["placeholder"] ?: Color.Gray,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth
                )
            }
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                color = theme["text"] ?: Color.Black,
                fontSize = 22.sp,
                fontFamily = RubikFont,
                fontWeight = FontWeight.Normal
            ),
            cursorBrush = SolidColor(theme["text"]!!),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = theme["placeholder"]!!,
                            fontSize = 22.sp,
                            lineHeight = 24.sp,
                            fontFamily = RubikFont,
                            fontWeight = FontWeight.Normal
                        )
                    }
                    innerTextField()
                }
            },
            singleLine = singleLine,
            maxLines = if (singleLine) 1 else 4,
            keyboardOptions = keyboardOptions,
            visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation()
        )
    }
}

@Composable
fun CommentInputTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    theme: Map<String, Color>
) {
    Surface(
        color = theme["input_background"]!!,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    color = theme["text"]!!,
                    fontSize = 20.sp,
                    fontFamily = RubikFont,
                    fontWeight = FontWeight.Normal
                ),
                placeholder = {
                    Text(
                        text = placeholder,
                        color = theme["placeholder"]!!,
                        fontSize = 20.sp,
                        fontFamily = RubikFont,
                        fontWeight = FontWeight.Normal
                    )
                },
                colors = TextFieldDefaults.colors().copy(
                    focusedContainerColor = theme["input_background"]!!,
                    unfocusedContainerColor = theme["input_background"]!!,
                    focusedTextColor = theme["text"]!!,
                    unfocusedTextColor = theme["text"]!!,
                    focusedPlaceholderColor = theme["placeholder"]!!,
                    unfocusedPlaceholderColor = theme["placeholder"]!!,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    cursorColor = theme["text"]!!,
                ),
                keyboardOptions = KeyboardOptions.Default,
                maxLines = 4
            )
        }
    }
}