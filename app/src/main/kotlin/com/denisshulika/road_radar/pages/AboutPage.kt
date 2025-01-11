package com.denisshulika.road_radar.pages

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import com.denisshulika.road_radar.model.CustomDrawerState
import com.denisshulika.road_radar.model.NavigationItem
import com.denisshulika.road_radar.model.isOpened
import com.denisshulika.road_radar.model.opposite
import com.denisshulika.road_radar.ui.components.CustomDrawer
import com.denisshulika.road_radar.util.coloredShadow
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.common.io.Files.append
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutPage(
    @Suppress("UNUSED_PARAMETER") modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
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
        color = if (drawerState == CustomDrawerState.Closed) Color(0xFFFEF9FE) else  Color(0xFFECE7EB),
        darkIcons = true
    )
    systemUiController.setNavigationBarColor(
        color = if (drawerState == CustomDrawerState.Closed) Color(0xFFFEF9FE) else  Color(0xFFECE7EB),
        darkIcons = true
    )
    //TODO()

    Box(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
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
            navController = navController,
            incidentManager = incidentManager
        )

        Scaffold(
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToPx(), 0) }
                .scale(scale = animatedScale)
                .coloredShadow(
                    color = Color.Black,
                    alpha = 0.1f,
                    shadowRadius = 50.dp
                )
                .clickable(enabled = drawerState == CustomDrawerState.Opened) {
                    drawerState = CustomDrawerState.Closed
                },
            topBar = {
                CenterAlignedTopAppBar(
                    modifier = Modifier,
                    title = {
                        Text(
                            text = selectedNavigationItem.title,
                            textAlign = TextAlign.Center,
                            fontFamily = RubikFont
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
                        .fillMaxSize()
                        .padding(start = 20.dp, end = 20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = Color(0xFF474EFF))) {
                                append("Road Radar")
                            }
                            append(" — a mobile application for drivers and other users")
                        },
                        fontSize = 24.sp,
                        fontFamily = RubikFont,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "that provides information about current road conditions and possible hazards along your route. With it, you can receive and exchange up-to-date information about accidents, traffic jams, dangerous sections, and other situations that may affect your trip.",
                        fontSize = 18.sp,
                        fontFamily = RubikFont,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                    val faqItems = listOf(
                        "How do I register in the app?" to "To register, you can use your email or sign in through Google. Afterward, you need to enter additional details to complete the registration.",
                        "How do I log into the app?" to "You can log in using your email and password or through Google if you already have an account.",
                        "How does the app help on the road?" to "Road Radar allows you to exchange information about accidents, traffic jams, dangerous areas, and weather conditions that could impact your journey.",
                        "How can I view current incidents in my area?" to "Incidents are displayed on the 'Incidents' page, sorted by date (from the newest) and filtered by your area as indicated in your profile.",
                        "How do I report a new incident?" to "To report a new incident, click the appropriate button in the app and fill out the form with a detailed description of the situation.",
                        "How do I view and edit my profile?" to "Profile information can be viewed on the 'Profile' page. There is also a button to edit your details.",
                        "How do I change the app settings and manage my account?" to "You can change the app's theme and language in the settings, as well as manage your account settings.",
                        "How do I view incidents on the map?" to "Incidents can be viewed on the map, where they are marked with pins. Clicking on a pin will show detailed information about the incident.",
                        "How does the app store my data?" to "Some data is stored locally on the device for faster access and to minimize internet usage, while others are synced with the server to save progress and settings.",
                        "What should I do in case of technical problems?" to "Try restarting the app or contact the developer through the contact details below."
                    )
                    faqItems.forEach { (question, answer) ->
                        FaqItem(question = question, answer = answer)
                    }
                    Text(
                        text = "Developer Contact Information:",
                        fontSize = 20.sp,
                        fontFamily = RubikFont,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                    )

                    val contactInfo = listOf(
                        "Denis Shulika Hennadiyovych",
                        "Email: denisshulika31@gmail.com",
                        "Phone: +380 67 880 50 16",
                        "Telegram: t.me/denisshulika",
                        "GitHub: github.com/DenisShulika"
                    )

                    Column {
                        contactInfo.forEach { contact ->
                            when {
                                contact.contains("Email:") -> {
                                    val email = contact.substringAfter("Email: ").trim()
                                    Text(
                                        text = buildAnnotatedString {
                                            append("● Email: ")
                                            withStyle(
                                                style = SpanStyle(
                                                    color = Color(0xFF474EFF),
                                                    textDecoration = TextDecoration.Underline
                                                )
                                            ) {
                                                append(email)
                                            }
                                        },
                                        fontSize = 18.sp,
                                        fontFamily = RubikFont,
                                        fontWeight = FontWeight.Normal,
                                        modifier = Modifier
                                            .clickable {
                                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
                                                context.startActivity(intent)
                                            }
                                            .padding(bottom = 4.dp)
                                    )
                                }
                                contact.contains("Telegram:") -> {
                                    val telegramLink = contact.substringAfter("Telegram: ").trim()
                                    Text(
                                        text = buildAnnotatedString {
                                            append("● Telegram: ")
                                            withStyle(
                                                style = SpanStyle(
                                                    color = Color(0xFF474EFF),
                                                    textDecoration = TextDecoration.Underline
                                                )
                                            ) {
                                                append(telegramLink)
                                            }
                                        },
                                        fontSize = 18.sp,
                                        fontFamily = RubikFont,
                                        fontWeight = FontWeight.Normal,
                                        modifier = Modifier
                                            .clickable {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://$telegramLink"))
                                                context.startActivity(intent)
                                            }
                                            .padding(bottom = 4.dp)
                                    )
                                }
                                contact.contains("GitHub:") -> {
                                    val githubLink = contact.substringAfter("GitHub: ").trim()
                                    Text(
                                        text = buildAnnotatedString {
                                            append("● GitHub: ")
                                            withStyle(
                                                style = SpanStyle(
                                                    color = Color(0xFF474EFF),
                                                    textDecoration = TextDecoration.Underline
                                                )
                                            ) {
                                                append(githubLink)
                                            }
                                        },
                                        fontSize = 18.sp,
                                        fontFamily = RubikFont,
                                        fontWeight = FontWeight.Normal,
                                        modifier = Modifier
                                            .clickable {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://$githubLink"))
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
fun FaqItem(question: String, answer: String) {
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
            text = "● $question",
            fontSize = 18.sp,
            fontFamily = RubikFont,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 4.dp)
                .clickable { isExpanded = !isExpanded }
        )

        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.heightIn(min = 0.dp, max = height)) {
                Text(
                    text = answer,
                    fontSize = 16.sp,
                    fontFamily = RubikFont,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}