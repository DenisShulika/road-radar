package com.denisshulika.road_radar.pages

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import com.denisshulika.road_radar.IncidentManager
import com.denisshulika.road_radar.Routes
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
    @Suppress("UNUSED_PARAMETER") modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    incidentManager: IncidentManager
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
        color = if (drawerState == CustomDrawerState.Closed) Color(0xFFFEF9FE) else  Color(0xFFECE7EB),
        darkIcons = true
    )
    systemUiController.setNavigationBarColor(
        color = if (drawerState == CustomDrawerState.Closed) Color(0xFFFEF9FE) else  Color(0xFFECE7EB),
        darkIcons = true
    )
    //TODO()

    var themeState by remember { mutableStateOf(ThemeState.SYSTEM) }
    var languageState by remember { mutableStateOf(LanguageState.UKRAINIAN) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val userLocalStorage = UserLocalStorage(context)
    val settingsLocalStorage = SettingsLocalStorage(context)
    LaunchedEffect(Unit) {
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
            incidentManager = incidentManager
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .background(Color(0x10000000))
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "Theme",
                                fontSize = 24.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Medium
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
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                onClick = {
                                    themeState = ThemeState.SYSTEM
                                    CoroutineScope(Dispatchers.Main).launch {
                                        settingsLocalStorage.saveTheme(themeState)
                                    }
                                },
                                selected = themeState == ThemeState.SYSTEM
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "System",
                                fontSize = 20.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Normal
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
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                onClick = {
                                    themeState = ThemeState.LIGHT
                                    CoroutineScope(Dispatchers.Main).launch {
                                        settingsLocalStorage.saveTheme(themeState)
                                    }
                                },
                                selected = themeState == ThemeState.LIGHT
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Light",
                                fontSize = 20.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Normal
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
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                onClick = {
                                    themeState = ThemeState.DARK
                                    CoroutineScope(Dispatchers.Main).launch {
                                        settingsLocalStorage.saveTheme(themeState)
                                    }
                                },
                                selected = themeState == ThemeState.DARK
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Dark",
                                fontSize = 20.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Normal
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
                            .background(Color(0x10000000))
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "Language",
                                fontSize = 24.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Medium
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
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                onClick = {
                                    languageState = LanguageState.UKRAINIAN
                                    CoroutineScope(Dispatchers.Main).launch {
                                        settingsLocalStorage.saveLanguage(languageState)
                                    }
                                },
                                selected = languageState == LanguageState.UKRAINIAN
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ukrainian",
                                fontSize = 20.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Normal
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
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                onClick = {
                                    languageState = LanguageState.ENGLISH
                                    CoroutineScope(Dispatchers.Main).launch {
                                        settingsLocalStorage.saveLanguage(languageState)
                                    }
                                },
                                selected = languageState == LanguageState.ENGLISH
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "English",
                                fontSize = 20.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Normal
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
                            .background(Color(0x10000000))
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "Actions With Account",
                                fontSize = 24.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Medium
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
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Change email",
                                        fontSize = 20.sp,
                                        fontFamily = RubikFont,
                                        fontWeight = FontWeight.Normal
                                    )
                                    IconButton(
                                        onClick = {
                                            navController.navigate(Routes.EMAIL_RESET)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = ""
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
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Change password",
                                        fontSize = 20.sp,
                                        fontFamily = RubikFont,
                                        fontWeight = FontWeight.Normal
                                    )
                                    IconButton(
                                        onClick = {
                                            navController.navigate(Routes.PASSWORD_RESET)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = ""
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
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Delete account",
                                    fontSize = 20.sp,
                                    fontFamily = RubikFont,
                                    fontWeight = FontWeight.Normal
                                )
                                IconButton(
                                    onClick = {
                                        showDeleteDialog()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = ""
                                    )
                                }
                            }
                            DeleteAccountDialog(
                                isDialogVisible = isDialogVisible,
                                onConfirm = { confirmDelete() },
                                onCancel = { cancelDelete() }
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
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Sign out",
                                    fontSize = 20.sp,
                                    fontFamily = RubikFont,
                                    fontWeight = FontWeight.Normal
                                )
                                IconButton(
                                    onClick = {
                                        authViewModel.signout(
                                            context = context,
                                            coroutineScope = coroutineScope,
                                            incidentManager = incidentManager
                                        )
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = ""
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
    onCancel: () -> Unit
) {
    if (isDialogVisible) {
        AlertDialog(
            onDismissRequest = {
                onCancel()
            },
            title = {
                Text(
                    text = "Confirmation",
                    fontFamily = RubikFont,
                    fontWeight = FontWeight.Normal
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete your account? This action cannot be undone.",
                    fontFamily = RubikFont,
                    fontWeight = FontWeight.Normal
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF474EFF))
                ) {
                    Text(
                        text = "Yes, delete",
                        fontFamily = RubikFont,
                        fontWeight = FontWeight.Normal
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        onCancel()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF474EFF))
                ) {
                    Text(
                        text = "Cancel",
                        fontFamily = RubikFont,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        )
    }
}