package com.denisshulika.road_radar.pages

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.denisshulika.road_radar.AuthState
import com.denisshulika.road_radar.AuthViewModel
import com.denisshulika.road_radar.IncidentsManager
import com.denisshulika.road_radar.Routes
import com.denisshulika.road_radar.SettingsViewModel
import com.denisshulika.road_radar.local.SettingsLocalStorage
import com.denisshulika.road_radar.local.UserLocalStorage
import com.denisshulika.road_radar.model.CustomDrawerState
import com.denisshulika.road_radar.model.LanguageState
import com.denisshulika.road_radar.model.NavigationItem
import com.denisshulika.road_radar.model.ThemeState
import com.denisshulika.road_radar.model.isOpened
import com.denisshulika.road_radar.model.opposite
import com.denisshulika.road_radar.ui.components.CustomDrawer
import com.denisshulika.road_radar.util.coloredShadow
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(
    navController: NavController,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    incidentsManager: IncidentsManager
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

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
    var selectedNavigationItem by remember { mutableStateOf(NavigationItem.Settings) }

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

    var radius by remember { mutableFloatStateOf(1000f) }
    var themeState by remember { mutableStateOf(ThemeState.SYSTEM) }
    var languageState by remember { mutableStateOf(LanguageState.UKRAINIAN) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val userLocalStorage = UserLocalStorage(context)
    val settingsLocalStorage = SettingsLocalStorage(context, settingsViewModel)
    LaunchedEffect(Unit) {
        radius = settingsLocalStorage.getRadius()
        themeState = settingsLocalStorage.getTheme()
        languageState = settingsLocalStorage.getLanguage()

        email = userLocalStorage.getUserEmail().toString()
        password = userLocalStorage.getUserPassword().toString()
    }

    var isDialogVisible by remember { mutableStateOf(false) }

    fun showDeleteDialog() {
        isDialogVisible = true
    }

    fun confirmDelete() {
        authViewModel.deleteAccount(
            email = email,
            password = password,
            context = context,
            coroutineScope = coroutineScope,
            incidentsManager = incidentsManager,
            localization = localization
        )
        isDialogVisible = false
    }

    fun cancelDelete() {
        isDialogVisible = false
    }

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
        val isSystemInDarkTheme = isSystemInDarkTheme()
        CustomDrawer(
            selectedNavigationItem = selectedNavigationItem,
            onNavigationItemClick = {
                selectedNavigationItem = it
            },
            onCloseClick = { drawerState = CustomDrawerState.Closed },
            authViewModel = authViewModel,
            settingsViewModel = settingsViewModel,
            navController = navController,
            incidentsManager = incidentsManager
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(theme["background"]!!)
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .background(theme["drawer_background"]!!)
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Text(
                                text = localization["radius_title"]!!,
                                fontSize = 24.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Medium,
                                color = theme["text"]!!
                            )
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text(
                                modifier = Modifier
                                    .padding(vertical = 4.dp),
                                text = (radius / 1000f).let {
                                    if (it < 1) "$radius m" else "${
                                        "%.1f".format(
                                            it
                                        )
                                    } km"
                                },
                                fontSize = 20.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Normal,
                                color = theme["text"]!!
                            )

                            Slider(
                                value = radius,
                                onValueChange = {
                                    radius = it
                                    CoroutineScope(Dispatchers.Main).launch {
                                        settingsLocalStorage.saveRadius(radius, context)
                                    }
                                },
                                valueRange = 1000f..100000f,
                                steps = 98,
                                colors = SliderDefaults.colors(
                                    thumbColor = theme["text"]!!,
                                    activeTrackColor = theme["primary"]!!,
                                    inactiveTrackColor = theme["placeholder"]!!,
                                    activeTickColor = theme["secondary"]!!,
                                    inactiveTickColor = theme["background"]!!
                                ),
                                modifier = Modifier
                                    .padding(top = 8.dp)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .background(theme["drawer_background"]!!)
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Text(
                                text = localization["theme_title"]!!,
                                fontSize = 24.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Medium,
                                color = theme["text"]!!
                            )
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    themeState = ThemeState.DARK
                                    CoroutineScope(Dispatchers.Main).launch {
                                        settingsLocalStorage.saveTheme(themeState, isSystemInDarkTheme)
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                onClick = {
                                    themeState = ThemeState.DARK
                                    CoroutineScope(Dispatchers.Main).launch {
                                        settingsLocalStorage.saveTheme(themeState, isSystemInDarkTheme)
                                    }
                                },
                                selected = themeState == ThemeState.DARK,
                                colors = RadioButtonColors(
                                    selectedColor = theme["primary"]!!,
                                    unselectedColor = theme["placeholder"]!!,
                                    disabledSelectedColor = theme["primary"]!!,
                                    disabledUnselectedColor = theme["placeholder"]!!,
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = localization["theme_dark"]!!,
                                fontSize = 20.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Normal,
                                color = theme["text"]!!
                            )
                        }
                        HorizontalDivider(
                            modifier = Modifier
                                .padding(top = 4.dp, bottom = 4.dp, start = 12.dp, end = 12.dp),
                            thickness = 1.dp,
                            color = Color(0xFFADADAD)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    themeState = ThemeState.LIGHT
                                    CoroutineScope(Dispatchers.Main).launch {
                                        settingsLocalStorage.saveTheme(themeState, isSystemInDarkTheme)
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                onClick = {
                                    themeState = ThemeState.LIGHT
                                    CoroutineScope(Dispatchers.Main).launch {
                                        settingsLocalStorage.saveTheme(themeState, isSystemInDarkTheme)
                                    }
                                },
                                selected = themeState == ThemeState.LIGHT,
                                colors = RadioButtonColors(
                                    selectedColor = theme["primary"]!!,
                                    unselectedColor = theme["placeholder"]!!,
                                    disabledSelectedColor = theme["primary"]!!,
                                    disabledUnselectedColor = theme["placeholder"]!!,
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = localization["theme_light"]!!,
                                fontSize = 20.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Normal,
                                color = theme["text"]!!
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .background(theme["drawer_background"]!!)
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Text(
                                text = localization["language_title"]!!,
                                fontSize = 24.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Medium,
                                color = theme["text"]!!
                            )
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    languageState = LanguageState.UKRAINIAN
                                    CoroutineScope(Dispatchers.Main).launch {
                                        settingsLocalStorage.saveLanguage(languageState, context)
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                onClick = {
                                    languageState = LanguageState.UKRAINIAN
                                    CoroutineScope(Dispatchers.Main).launch {
                                        settingsLocalStorage.saveLanguage(languageState, context)
                                    }
                                },
                                selected = languageState == LanguageState.UKRAINIAN,
                                colors = RadioButtonColors(
                                    selectedColor = theme["primary"]!!,
                                    unselectedColor = theme["placeholder"]!!,
                                    disabledSelectedColor = theme["primary"]!!,
                                    disabledUnselectedColor = theme["placeholder"]!!,
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = localization["language_uk"]!!,
                                fontSize = 20.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Normal,
                                color = theme["text"]!!
                            )
                        }
                        HorizontalDivider(
                            modifier = Modifier
                                .padding(top = 4.dp, bottom = 4.dp, start = 12.dp, end = 12.dp),
                            thickness = 1.dp,
                            color = Color(0xFFADADAD)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    languageState = LanguageState.ENGLISH
                                    CoroutineScope(Dispatchers.Main).launch {
                                        settingsLocalStorage.saveLanguage(languageState, context)
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                onClick = {
                                    languageState = LanguageState.ENGLISH
                                    CoroutineScope(Dispatchers.Main).launch {
                                        settingsLocalStorage.saveLanguage(languageState, context)
                                    }
                                },
                                selected = languageState == LanguageState.ENGLISH,
                                colors = RadioButtonColors(
                                    selectedColor = theme["primary"]!!,
                                    unselectedColor = theme["placeholder"]!!,
                                    disabledSelectedColor = theme["primary"]!!,
                                    disabledUnselectedColor = theme["placeholder"]!!,
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = localization["language_en"]!!,
                                fontSize = 20.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Normal,
                                color = theme["text"]!!
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .background(theme["drawer_background"]!!)
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Text(
                                text = localization["account_actions_title"]!!,
                                fontSize = 24.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Medium,
                                color = theme["text"]!!
                            )
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        if (authViewModel.isUserLoggedInWithEmailPassword()) {
                            Column(
                                modifier = Modifier
                                    .padding(
                                        start = 14.dp,
                                        end = 14.dp
                                    )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) {
                                            navController.navigate(Routes.EMAIL_RESET)
                                        },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = localization["account_action_change_email"]!!,
                                        fontSize = 20.sp,
                                        fontFamily = RubikFont,
                                        fontWeight = FontWeight.Normal,
                                        color = theme["text"]!!
                                    )
                                    IconButton(
                                        onClick = {
                                            navController.navigate(Routes.EMAIL_RESET)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = "",
                                            tint = theme["icon"]!!
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(
                                modifier = Modifier
                                    .padding(top = 4.dp, bottom = 4.dp, start = 12.dp, end = 12.dp),
                                thickness = 1.dp,
                                color = Color(0xFFADADAD)
                            )
                            Column(
                                modifier = Modifier
                                    .padding(
                                        start = 14.dp,
                                        end = 14.dp
                                    )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) {
                                            navController.navigate(Routes.PASSWORD_RESET)
                                        },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = localization["account_action_change_password"]!!,
                                        fontSize = 20.sp,
                                        fontFamily = RubikFont,
                                        fontWeight = FontWeight.Normal,
                                        color = theme["text"]!!
                                    )
                                    IconButton(
                                        onClick = {
                                            navController.navigate(Routes.PASSWORD_RESET)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = "",
                                            tint = theme["icon"]!!
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(
                                modifier = Modifier
                                    .padding(top = 4.dp, bottom = 4.dp, start = 12.dp, end = 12.dp),
                                thickness = 1.dp,
                                color = Color(0xFFADADAD)
                            )
                        }
                        Column(
                            modifier = Modifier
                                .padding(
                                    start = 14.dp,
                                    end = 14.dp
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
                                        showDeleteDialog()
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = localization["account_action_delete_account"]!!,
                                    fontSize = 20.sp,
                                    fontFamily = RubikFont,
                                    fontWeight = FontWeight.Normal,
                                    color = theme["text"]!!
                                )
                                IconButton(
                                    onClick = {
                                        showDeleteDialog()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "",
                                        tint = theme["icon"]!!
                                    )
                                }
                            }
                            DeleteAccountDialog(
                                isDialogVisible = isDialogVisible,
                                onConfirm = { confirmDelete() },
                                onCancel = { cancelDelete() },
                                localization = localization,
                                theme = theme
                            )
                        }
                        HorizontalDivider(
                            modifier = Modifier
                                .padding(top = 4.dp, bottom = 4.dp, start = 12.dp, end = 12.dp),
                            thickness = 1.dp,
                            color = Color(0xFFADADAD)
                        )
                        Column(
                            modifier = Modifier
                                .padding(
                                    start = 14.dp,
                                    end = 14.dp
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
                                        authViewModel.signout(
                                            context = context,
                                            coroutineScope = coroutineScope,
                                            incidentsManager = incidentsManager
                                        )
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = localization["account_action_sign_out"]!!,
                                    fontSize = 20.sp,
                                    fontFamily = RubikFont,
                                    fontWeight = FontWeight.Normal,
                                    color = theme["text"]!!
                                )
                                IconButton(
                                    onClick = {
                                        authViewModel.signout(
                                            context = context,
                                            coroutineScope = coroutineScope,
                                            incidentsManager = incidentsManager
                                        )
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "",
                                        tint = theme["icon"]!!
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteAccountDialog(
    isDialogVisible: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    localization: Map<String, String>,
    theme: Map<String, Color>
) {
    if (isDialogVisible) {
        AlertDialog(
            onDismissRequest = {
                onCancel()
            },
            title = {
                Text(
                    text = localization["account_delete_title"]!!,
                    fontFamily = RubikFont,
                    fontWeight = FontWeight.Medium,
                    color = theme["text"]!!
                )
            },
            text = {
                Text(
                    text = localization["account_delete_info"]!!,
                    fontFamily = RubikFont,
                    fontWeight = FontWeight.Normal,
                    color = theme["placeholder"]!!
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme["primary"]!!)
                ) {
                    Text(
                        text = localization["account_delete_confirm"]!!,
                        fontFamily = RubikFont,
                        fontWeight = FontWeight.Medium,
                        color = theme["text"]!!
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        onCancel()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme["primary"]!!)
                ) {
                    Text(
                        text = localization["account_delete_cancel"]!!,
                        fontFamily = RubikFont,
                        fontWeight = FontWeight.Medium,
                        color = theme["text"]!!
                    )
                }
            },
            containerColor = theme["background"]!!
        )
    }
}