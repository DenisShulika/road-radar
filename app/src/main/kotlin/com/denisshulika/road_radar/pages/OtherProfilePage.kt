package com.denisshulika.road_radar.pages

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.denisshulika.road_radar.CommentManager
import com.denisshulika.road_radar.SettingsViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherProfilePage(
    navController: NavController,
    settingsViewModel: SettingsViewModel,
    commentManager: CommentManager
) {

    val localization = settingsViewModel.localization.observeAsState().value!!
    val theme = settingsViewModel.themeColors.observeAsState().value!!

    val accountAgeDateFormat = SimpleDateFormat(
        "d MMMM yyyy",
        Locale(localization["date_format_language"]!!, localization["date_format_country"]!!)
    )

    var name by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf("") }

    var accountAge by remember { mutableStateOf(Timestamp(Date())) }

    var experience by remember { mutableIntStateOf(0) }
    val rank = when(experience) {
        in 0..10 -> localization["rank_1"]!!
        in 11..30 -> localization["rank_2"]!!
        in 31..60 -> localization["rank_3"]!!
        in 61..100 -> localization["rank_4"]!!
        in 101..150 -> localization["rank_5"]!!
        in 151..220 -> localization["rank_6"]!!
        in 221..300 -> localization["rank_7"]!!
        else -> localization["rank_8"]!!
    }
    val (startExp, endExp) = when (experience) {
        in 0..10 -> 0 to 10
        in 11..30 -> 11 to 30
        in 31..60 -> 31 to 60
        in 61..100 -> 61 to 100
        in 101..150 -> 101 to 150
        in 151..220 -> 151 to 220
        in 221..300 -> 221 to 300
        else -> 301 to Int.MAX_VALUE
    }

    val experienceGainedInRank = experience - startExp
    val experienceNeededForRank = endExp - startExp

    val progress = (experienceGainedInRank.toFloat() / experienceNeededForRank.toFloat())

    var reportsCount by remember { mutableIntStateOf(0) }
    var thanksCount by remember { mutableStateOf(0) }
    var thanksGivenCount by remember { mutableStateOf(0) }

    var isDataLoaded by remember { mutableStateOf(false) }

    val selectedProfileID = commentManager.selectedProfileID.value!!
    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(selectedProfileID)
            .get()
            .addOnSuccessListener { doc ->
                name = doc.getString("name")!!
                avatarUrl = doc.getString("photoUrl")!!
                accountAge = doc.getTimestamp("accountAge")!!
                experience = doc.getLong("experience")!!.toInt()
                reportsCount = doc.getLong("reportsCount")!!.toInt()
                thanksCount = doc.getLong("thanksCount")!!.toInt()
                thanksGivenCount = doc.getLong("thanksGivenCount")!!.toInt()

                isDataLoaded = true
            }
    }


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
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = theme["top_bar_background"]!!,
                        titleContentColor = theme["text"]!!,
                        navigationIconContentColor = theme["icon"]!!
                    ),
                    title = {
                        Text(
                            text = localization["other_profile_page_title"]!!,
                            fontFamily = RubikFont,
                            fontWeight = FontWeight.Bold
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(theme["background"]!!)
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 20.dp, end = 20.dp, top = 36.dp),
                ) {
                    if (isDataLoaded) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Column(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(theme["drawer_background"]!!)
                                        .size(130.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(avatarUrl),
                                        contentDescription = "",
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    )
                                }
                                Spacer(modifier = Modifier.size(12.dp))
                                Text(
                                    text = name,
                                    fontSize = 24.sp,
                                    fontFamily = RubikFont,
                                    fontWeight = FontWeight.Normal,
                                    color = theme["text"]!!
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(
                                    text = "${localization["joined_subtext"]!!} ${accountAgeDateFormat.format(accountAge.toDate())}",
                                    fontSize = 16.sp,
                                    fontFamily = RubikFont,
                                    fontWeight = FontWeight.Normal,
                                    color = theme["placeholder"]!!
                                )
                                Spacer(modifier = Modifier.size(24.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = 1.dp,
                                            color = theme["placeholder"]!!,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .background(theme["drawer_background"]!!)
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = localization["experience_title"]!!,
                                        fontSize = 24.sp,
                                        fontFamily = RubikFont,
                                        fontWeight = FontWeight.Normal,
                                        color = theme["placeholder"]!!
                                    )
                                    Spacer(modifier = Modifier.size(12.dp))
                                    Text(
                                        text = "${localization["rank_subtext"]!!} $rank (${experience}/${endExp})",
                                        fontSize = 22.sp,
                                        fontFamily = RubikFont,
                                        fontWeight = FontWeight.Normal,
                                        color = theme["text"]!!
                                    )
                                    if(endExp != Int.MAX_VALUE) {
                                        Spacer(modifier = Modifier.size(8.dp))
                                        LinearProgressIndicator(
                                            progress = { progress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(50)),
                                            color = theme["primary"]!!,
                                            trackColor = theme["placeholder"]!!,
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.size(16.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = 1.dp,
                                            color = theme["placeholder"]!!,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .background(theme["drawer_background"]!!)
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = localization["user_statistics_title"]!!,
                                        fontSize = 24.sp,
                                        fontFamily = RubikFont,
                                        fontWeight = FontWeight.Normal,
                                        color = theme["placeholder"]!!
                                    )
                                    Spacer(modifier = Modifier.size(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = localization["thanks_count_title"]!!,
                                            fontSize = 20.sp,
                                            fontFamily = RubikFont,
                                            fontWeight = FontWeight.Bold,
                                            color = theme["text"]!!
                                        )
                                        Text(
                                            text = thanksCount.toString(),
                                            fontSize = 18.sp,
                                            fontFamily = RubikFont,
                                            fontWeight = FontWeight.Normal,
                                            color = theme["text"]!!
                                        )
                                    }
                                    Spacer(modifier = Modifier.size(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = localization["reports_count_title"]!!,
                                            fontSize = 20.sp,
                                            fontFamily = RubikFont,
                                            fontWeight = FontWeight.Bold,
                                            color = theme["text"]!!
                                        )
                                        Text(
                                            text = reportsCount.toString(),
                                            fontSize = 18.sp,
                                            fontFamily = RubikFont,
                                            fontWeight = FontWeight.Normal,
                                            color = theme["text"]!!
                                        )
                                    }
                                    Spacer(modifier = Modifier.size(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = localization["thanks_given_count_title"]!!,
                                            fontSize = 20.sp,
                                            fontFamily = RubikFont,
                                            fontWeight = FontWeight.Bold,
                                            color = theme["text"]!!
                                        )
                                        Text(
                                            text = thanksGivenCount.toString(),
                                            fontSize = 18.sp,
                                            fontFamily = RubikFont,
                                            fontWeight = FontWeight.Normal,
                                            color = theme["text"]!!
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = theme["primary"]!!
                            )
                        }
                    }
                }
            }
        }
    }
}
