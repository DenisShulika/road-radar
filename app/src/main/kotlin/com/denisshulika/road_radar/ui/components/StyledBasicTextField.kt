package com.denisshulika.road_radar.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denisshulika.road_radar.pages.RubikFont

@Composable
fun StyledBasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardOptions : KeyboardOptions = KeyboardOptions.Default,
    isVisible : Boolean = true
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                val y = size.height - strokeWidth / 2
                drawLine(
                    color = Color.LightGray,
                    start = Offset(0f, 0.75f * y),
                    end = Offset(size.width, 0.75f * y),
                    strokeWidth = strokeWidth
                )
            }
    ) {
        BasicTextField(
            modifier = Modifier.fillMaxSize(),
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 20.sp
            ),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = TextStyle(
                            color = Color(0xFFADADAD),
                            fontSize = 20.sp,
                            lineHeight = 20.sp
                        ),
                        fontFamily = RubikFont,
                        fontWeight = FontWeight.Normal
                    )
                }
                innerTextField()
            },
            singleLine = true,
            keyboardOptions = keyboardOptions,
            visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation()
        )
    }
}