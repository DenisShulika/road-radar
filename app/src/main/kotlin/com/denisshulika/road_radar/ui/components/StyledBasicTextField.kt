package com.denisshulika.road_radar.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
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
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isVisible: Boolean = true,
    singleLine: Boolean = true
) {
    val textLayoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }

    val lineCount = textLayoutResult.value?.lineCount ?: 1
    val targetHeight = if (singleLine) 28.dp else (lineCount * 24).dp

    val animatedHeight = animateDpAsState(
        targetValue = targetHeight,
        animationSpec = tween(durationMillis = 900),
        label = "TextField Anim"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                val y = size.height - strokeWidth / 2
                drawLine(
                    color = Color(0xFFD3D3D3),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth
                )
            }
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(animatedHeight.value),
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 22.sp,
                fontFamily = RubikFont,
                fontWeight = FontWeight.Normal
            ),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = TextStyle(
                            color = Color(0xFFADADAD),
                            fontSize = 22.sp,
                            lineHeight = 24.sp
                        ),
                        fontFamily = RubikFont,
                        fontWeight = FontWeight.Normal
                    )
                }
                innerTextField()
            },
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
            onTextLayout = { layoutResult ->
                textLayoutResult.value = layoutResult
            },
        )
    }
}

