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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denisshulika.road_radar.SettingsViewModel
import com.denisshulika.road_radar.model.NavigationItem
import com.denisshulika.road_radar.pages.RubikFont

@Composable
fun NavigationDrawerItem(
    settingsViewModel : SettingsViewModel,
    navigationItem : NavigationItem,
    selected : Boolean,
    onClick : () -> Unit,
    theme: Map<String, Color>
) {
    val localization = settingsViewModel.localization.observeAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30))
            .clickable {
                onClick()
            }
            .background(
                if (selected) theme["primary"]!! else theme["secondary"]!!,
                RoundedCornerShape(30)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(navigationItem.icon),
            contentDescription = "",
            tint = theme["icon"]!!
        )
        Spacer(modifier = Modifier.size(12.dp))
        Text(
            text = navigationItem.getTitle(localization.value!!),
            fontSize = 20.sp,
            color = theme["text"]!!,
            fontFamily = RubikFont,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            lineHeight = 20.sp
        )
    }
}