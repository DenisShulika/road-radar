package com.denisshulika.road_radar.pages

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.denisshulika.road_radar.IncidentManager
import com.denisshulika.road_radar.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    settingsViewModel: SettingsViewModel,
    incidentManager: IncidentManager
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val incidentInfo by incidentManager.selectedDocumentInfo.observeAsState()

    val localization = settingsViewModel.localization.observeAsState().value!!

    Box(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .fillMaxSize()
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = localization["incident_info_title"]!!,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = RubikFont
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = ""
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                incidentInfo?.let { info ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${localization["incident_type_subtext"]!!} ${info.type}",
                            fontSize = 20.sp,
                            fontFamily = RubikFont
                        )
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = Color(0xFFADADAD)
                        )
                        Text(
                            text = "${localization["incident_date_subtext"]!!} ${info.date}",
                            fontSize = 20.sp,
                            fontFamily = RubikFont
                        )
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = Color(0xFFADADAD)
                        )
                        Text(
                            text = "${localization["incident_address_subtext"]!!} ${info.address}",
                            fontSize = 20.sp,
                            fontFamily = RubikFont
                        )
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = Color(0xFFADADAD)
                        )
                        Text(
                            text = "${localization["incident_description_subtext"]!!} ${info.description}",
                            fontSize = 20.sp,
                            fontFamily = RubikFont
                        )
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = Color(0xFFADADAD)
                        )

                        if (info.photos.isNotEmpty()) {
                            info.photos.forEachIndexed { _, photoUrl ->
                                Image(
                                    painter = rememberAsyncImagePainter(photoUrl),
                                    contentDescription = "",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(0.dp, 800.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.FillWidth
                                )
                            }
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = Color(0xFFADADAD)
                            )
                        }
                        Text(
                            text = "${localization["incident_created_by_subtext"]!!} ${info.createdBy}",
                            fontSize = 20.sp,
                            fontFamily = RubikFont
                        )
                        Spacer(modifier = modifier)
                    }
                }
            }
        }
    }
}
