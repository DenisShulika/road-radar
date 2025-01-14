package com.denisshulika.road_radar.pages

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.denisshulika.road_radar.AuthState
import com.denisshulika.road_radar.AuthViewModel
import com.denisshulika.road_radar.IncidentManager
import com.denisshulika.road_radar.Routes
import com.denisshulika.road_radar.SettingsViewModel
import com.denisshulika.road_radar.model.CustomDrawerState
import com.denisshulika.road_radar.model.NavigationItem
import com.denisshulika.road_radar.model.ThemeState
import com.denisshulika.road_radar.model.isOpened
import com.denisshulika.road_radar.model.opposite
import com.denisshulika.road_radar.ui.components.CustomDrawer
import com.denisshulika.road_radar.util.coloredShadow
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutPage(
    @Suppress("UNUSED_PARAMETER") modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    incidentManager: IncidentManager
) {
    val context = LocalContext.current

    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        authViewModel.checkAuthStatus()
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate(Routes.LOGIN)
            else -> Unit
        }
    }

    val localization = settingsViewModel.localization.observeAsState().value!!
    val theme = settingsViewModel.themeColors.observeAsState().value!!

    var drawerState by remember { mutableStateOf(CustomDrawerState.Closed) }
    var selectedNavigationItem by remember { mutableStateOf(NavigationItem.About) }

    val configuration = LocalConfiguration.current
    val destiny = LocalDensity.current.density

    val screenWidth = remember {
        derivedStateOf { (configuration.screenWidthDp * destiny).roundToInt() }
    }
    val offsetValue by remember { derivedStateOf { (screenWidth.value / 4.5).dp } }
    val animatedOffset by animateDpAsState(
        targetValue = if (drawerState.isOpened()) offsetValue else 0.dp,
        label = "Animated Offset"
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (drawerState.isOpened()) 0.9f else 1f,
        label = "Animated Scale"
    )

    BackHandler(enabled = drawerState.isOpened()) {
        drawerState = CustomDrawerState.Closed
    }

    val systemUiController = rememberSystemUiController()

    systemUiController.setStatusBarColor(
        color = if (drawerState == CustomDrawerState.Closed) theme["top_bar_background"]!! else theme["drawer_background"]!!,
        darkIcons = settingsViewModel.getTheme() != ThemeState.DARK || !isSystemInDarkTheme()
    )
    systemUiController.setNavigationBarColor(
        color = if (drawerState == CustomDrawerState.Closed) theme["background"]!! else theme["drawer_background"]!!,
        darkIcons = settingsViewModel.getTheme() != ThemeState.DARK || !isSystemInDarkTheme()
    )

    Box(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(theme["drawer_background"]!!)
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    drawerState =
                        if (dragAmount > 0) CustomDrawerState.Opened else CustomDrawerState.Closed
                }
            }
    ) {
        CustomDrawer(
            selectedNavigationItem = selectedNavigationItem,
            onNavigationItemClick = {
                selectedNavigationItem = it
            },
            onCloseClick = { drawerState = CustomDrawerState.Closed },
            authViewModel = authViewModel,
            settingsViewModel = settingsViewModel,
            navController = navController,
            incidentManager = incidentManager
        )

        Scaffold(
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToPx(), 0) }
                .scale(scale = animatedScale)
                .coloredShadow(
                    color = theme["shadow"]!!,
                    alpha = 0.1f,
                    shadowRadius = 30.dp
                )
                .clickable(enabled = drawerState == CustomDrawerState.Opened) {
                    drawerState = CustomDrawerState.Closed
                },
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = theme["top_bar_background"]!!,
                        titleContentColor = theme["text"]!!,
                        navigationIconContentColor = theme["icon"]!!
                    ),
                    title = {
                        Text(
                            text = selectedNavigationItem.getTitle(localization),
                            textAlign = TextAlign.Center,
                            fontFamily = RubikFont,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                drawerState = drawerState.opposite()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = ""
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column (
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .background(theme["background"]!!)
                        .fillMaxSize()
                        .padding(start = 20.dp, end = 20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = theme["secondary"]!!)) {
                                append(localization["app_title"])
                            }
                            append(localization["app_description_title"])
                        },
                        fontSize = 24.sp,
                        fontFamily = RubikFont,
                        fontWeight = FontWeight.Bold,
                        color = theme["text"]!!
                    )
                    Text(
                        modifier = Modifier
                            .padding(bottom = 20.dp),
                        text = localization["app_description_text"]!!,
                        fontSize = 18.sp,
                        fontFamily = RubikFont,
                        fontWeight = FontWeight.Normal,
                        color = theme["placeholder"]!!
                    )
                    Text(
                        modifier = Modifier
                            .padding(bottom = 8.dp, top = 8.dp),
                        text = localization["faq_title"]!!,
                        fontSize = 20.sp,
                        fontFamily = RubikFont,
                        fontWeight = FontWeight.Bold,
                        color = theme["text"]!!
                    )
                    val faqItems = listOf(
                        localization["faq_register_question"]!! to localization["faq_register_answer"]!!,
                        localization["faq_login_question"]!! to localization["faq_login_answer"]!!,
                        localization["faq_help_question"]!! to localization["faq_help_answer"]!!,
                        localization["faq_view_incidents_question"]!! to localization["faq_view_incidents_answer"]!!,
                        localization["faq_report_incident_question"]!! to localization["faq_report_incident_answer"]!!,
                        localization["faq_profile_question"]!! to localization["faq_profile_answer"]!!,
                        localization["faq_settings_question"]!! to localization["faq_settings_answer"]!!,
                        localization["faq_map_incidents_question"]!! to localization["faq_map_incidents_answer"]!!,
                        localization["faq_data_storage_question"]!! to localization["faq_data_storage_answer"]!!,
                        localization["faq_technical_problems_question"]!! to localization["faq_technical_problems_answer"]!!
                    )
                    faqItems.forEach { (question, answer) ->
                        FaqItem(question = question, answer = answer, theme = theme)
                    }
                    Text(
                        modifier = Modifier
                            .padding(bottom = 8.dp, top = 8.dp),
                        text = localization["developer_contact_title"]!!,
                        fontSize = 20.sp,
                        fontFamily = RubikFont,
                        fontWeight = FontWeight.Bold,
                        color = theme["text"]!!
                    )

                    val contactInfo = listOf(
                        localization["developer_name"]!!,
                        localization["developer_email"]!!,
                        localization["developer_phone"]!!,
                        localization["developer_telegram"]!!,
                        localization["developer_github"]!!
                    )

                    Column {
                        contactInfo.forEach { contact ->
                            when {
                                contact.contains(localization["developer_email_title"].toString()) -> {
                                    val email = "denisshulika31@gmail.com"
                                    Text(
                                        text = buildAnnotatedString {
                                            append("● ${localization["developer_email_title"]}")
                                            withStyle(
                                                style = SpanStyle(
                                                    color = theme["primary"]!!,
                                                    textDecoration = TextDecoration.Underline
                                                )
                                            ) {
                                                append(email)
                                            }
                                        },
                                        fontSize = 18.sp,
                                        fontFamily = RubikFont,
                                        fontWeight = FontWeight.Normal,
                                        color = theme["placeholder"]!!,
                                        modifier = Modifier
                                            .clickable {
                                                val intent = Intent(
                                                    Intent.ACTION_SENDTO,
                                                    Uri.parse("mailto:$email")
                                                )
                                                context.startActivity(intent)
                                            }
                                            .padding(bottom = 4.dp)
                                    )
                                }
                                contact.contains(localization["developer_telegram_title"].toString()) -> {
                                    val telegramLink = "t.me/denisshulika"
                                    Text(
                                        text = buildAnnotatedString {
                                            append("● ${localization["developer_telegram_title"]}")
                                            withStyle(
                                                style = SpanStyle(
                                                    color = theme["primary"]!!,
                                                    textDecoration = TextDecoration.Underline
                                                )
                                            ) {
                                                append(telegramLink)
                                            }
                                        },
                                        fontSize = 18.sp,
                                        fontFamily = RubikFont,
                                        fontWeight = FontWeight.Normal,
                                        color = theme["placeholder"]!!,
                                        modifier = Modifier
                                            .clickable {
                                                val intent = Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse("https://$telegramLink")
                                                )
                                                context.startActivity(intent)
                                            }
                                            .padding(bottom = 4.dp)
                                    )
                                }
                                contact.contains(localization["developer_github_title"].toString()) -> {
                                    val githubLink = "github.com/DenisShulika"
                                    Text(
                                        text = buildAnnotatedString {
                                            append("● ${localization["developer_github_title"]}")
                                            withStyle(
                                                style = SpanStyle(
                                                    color = theme["primary"]!!,
                                                    textDecoration = TextDecoration.Underline
                                                )
                                            ) {
                                                append(githubLink)
                                            }
                                        },
                                        fontSize = 18.sp,
                                        fontFamily = RubikFont,
                                        fontWeight = FontWeight.Normal,
                                        color = theme["placeholder"]!!,
                                        modifier = Modifier
                                            .clickable {
                                                val intent = Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse("https://$githubLink")
                                                )
                                                context.startActivity(intent)
                                            }
                                            .padding(bottom = 4.dp)
                                    )
                                }
                                else -> {
                                    Text(
                                        text = "● $contact",
                                        fontSize = 18.sp,
                                        fontFamily = RubikFont,
                                        fontWeight = FontWeight.Normal,
                                        color = theme["placeholder"]!!,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun FaqItem(
    question: String,
    answer: String,
    theme: Map<String, Color>
) {
    var isExpanded by remember { mutableStateOf(false) }

    val height by animateDpAsState(
        targetValue = if (isExpanded) 200.dp else 0.dp,
        label = "Height Animation"
    )

    Column(
        modifier = Modifier
            .padding(bottom = 12.dp)
    ) {
        Text(
            modifier = Modifier
                .padding(bottom = 4.dp)
                .clickable { isExpanded = !isExpanded },
            text = "● $question",
            fontSize = 18.sp,
            fontFamily = RubikFont,
            fontWeight = FontWeight.Bold,
            color = theme["text"]!!
        )

        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.heightIn(min = 0.dp, max = height)) {
                Text(
                    text = answer,
                    fontSize = 16.sp,
                    fontFamily = RubikFont,
                    fontWeight = FontWeight.Normal,
                    color = theme["placeholder"]!!
                )
            }
        }
    }
}