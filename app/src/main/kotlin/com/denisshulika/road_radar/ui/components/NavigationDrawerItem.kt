package com.denisshulika.road_radar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denisshulika.road_radar.model.NavigationItem
import com.denisshulika.road_radar.pages.RubikFont

@Composable
fun NavigationDrawerItem(
    navigationItem : NavigationItem,
    selected : Boolean,
    onClick : () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30))
            .clickable {
                onClick()
            }
            .background(
                if (selected) Color(0xFF474EFF) else Color(0xFF6369FF),
                RoundedCornerShape(30)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(navigationItem.icon),
            contentDescription = "",
            tint = Color.White
        )
        Spacer(modifier = Modifier.size(12.dp))
        Text(
            text = navigationItem.title,
            fontSize = 20.sp,
            color = Color.White,
            fontFamily = RubikFont,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            lineHeight = 20.sp
        )
    }
}