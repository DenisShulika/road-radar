package com.denisshulika.road_radar.pages

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.RichTooltipColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.denisshulika.road_radar.AuthState
import com.denisshulika.road_radar.AuthViewModel
import com.denisshulika.road_radar.IncidentManager
import com.denisshulika.road_radar.R
import com.denisshulika.road_radar.Routes
import com.denisshulika.road_radar.SettingsViewModel
import com.denisshulika.road_radar.isValidPhoneNumber
import com.denisshulika.road_radar.local.UserLocalStorage
import com.denisshulika.road_radar.model.CustomDrawerState
import com.denisshulika.road_radar.model.NavigationItem
import com.denisshulika.road_radar.model.ThemeState
import com.denisshulika.road_radar.model.UserData
import com.denisshulika.road_radar.model.isOpened
import com.denisshulika.road_radar.model.opposite
import com.denisshulika.road_radar.ui.components.AutocompleteTextFieldForRegion
import com.denisshulika.road_radar.ui.components.CustomDrawer
import com.denisshulika.road_radar.ui.components.StyledBasicTextField
import com.denisshulika.road_radar.util.coloredShadow
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@ExperimentalMaterial3Api
@Composable
fun ProfilePage(
    @Suppress("UNUSED_PARAMETER") modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    placesClient: PlacesClient,
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
    var selectedNavigationItem by remember { mutableStateOf(NavigationItem.Profile) }

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

    val localization = settingsViewModel.localization.observeAsState().value!!
    val theme = settingsViewModel.themeColors.observeAsState().value!!

    val systemUiController = rememberSystemUiController()

    systemUiController.setStatusBarColor(
        color = if (drawerState == CustomDrawerState.Closed) theme["top_bar_background"]!! else theme["drawer_background"]!!,
        darkIcons = settingsViewModel.getTheme() != ThemeState.DARK || !isSystemInDarkTheme()
    )
    systemUiController.setNavigationBarColor(
        color = if (drawerState == CustomDrawerState.Closed) theme["background"]!! else theme["drawer_background"]!!,
        darkIcons = settingsViewModel.getTheme() != ThemeState.DARK || !isSystemInDarkTheme()
    )

    var isEditingState by remember { mutableStateOf(false) }


    var userName by remember { mutableStateOf("") }
    var isNameEmpty by remember { mutableStateOf(false) }


    var userEmail by remember { mutableStateOf("") }


    var userPhoneNumber by remember { mutableStateOf("") }
    var phoneNumberError by remember { mutableStateOf(false) }
    var isPhoneNumberEmpty by remember { mutableStateOf(false) }


    var selectedRegion by remember { mutableStateOf("") }
    var isRegionSelected by remember { mutableStateOf(true) }
    var isSelectedRegionEmpty by remember { mutableStateOf(false) }

    var userPhoto by remember { mutableStateOf("") }

    val currentUser = authViewModel.getCurrentUser()
    val userLocalStorage = UserLocalStorage(context)
    LaunchedEffect(Unit) {
        userName = userLocalStorage.getUserName().toString()
        userEmail = userLocalStorage.getUserEmail().toString()
        userPhoneNumber = userLocalStorage.getUserPhoneNumber().toString()
        selectedRegion = userLocalStorage.getUserRegion().toString()
        userPhoto = userLocalStorage.getUserPhotoUrl().toString()
    }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var croppedImageUri by remember { mutableStateOf<Uri?>(null) }
    val getContent = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it

            val croppedBitmap = cropImageToSquare(it, context.contentResolver)
            croppedImageUri = if (croppedBitmap != null) bitmapToUri(context, croppedBitmap) else null

            userPhoto = croppedImageUri.toString()
        }
    }

    val tooltipState = rememberTooltipState()
    val scope = rememberCoroutineScope()

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
                            text = if (isEditingState) localization["edit_profile_title"]!! else selectedNavigationItem.getTitle(localization),
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
                    .padding(innerPadding)
                    .background(theme["background"]!!)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    if (!isEditingState) {
                        Column {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.height(32.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(theme["drawer_background"]!!)
                                            .size(125.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Image(
                                            painter = rememberAsyncImagePainter(userPhoto),
                                            contentDescription = "",
                                            modifier = Modifier
                                                .size(100.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.size(24.dp))
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(32.dp)
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = localization["name_title"]!!,
                                        fontSize = 24.sp,
                                        fontFamily = RubikFont,
                                        fontWeight = FontWeight.Normal,
                                        color = theme["placeholder"]!!
                                    )
                                    Text(
                                        text = userName,
                                        fontSize = 22.sp,
                                        fontFamily = RubikFont,
                                        fontWeight = FontWeight.Normal,
                                        color = theme["text"]!!
                                    )
                                }
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = localization["region_title"]!!,
                                        fontSize = 24.sp,
                                        fontFamily = RubikFont,
                                        fontWeight = FontWeight.Normal,
                                        color = theme["placeholder"]!!
                                    )
                                    Text(
                                        text = selectedRegion,
                                        fontSize = 22.sp,
                                        fontFamily = RubikFont,
                                        fontWeight = FontWeight.Normal,
                                        color = theme["text"]!!
                                    )
                                }
                                if (authViewModel.isUserLoggedInWithEmailPassword()) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = localization["email_title"]!!,
                                            fontSize = 24.sp,
                                            fontFamily = RubikFont,
                                            fontWeight = FontWeight.Normal,
                                            color = theme["placeholder"]!!
                                        )
                                        Text(
                                            text = userEmail,
                                            fontSize = 22.sp,
                                            fontFamily = RubikFont,
                                            fontWeight = FontWeight.Normal,
                                            color = theme["text"]!!
                                        )
                                    }
                                }
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = localization["phone_title"]!!,
                                        fontSize = 24.sp,
                                        fontFamily = RubikFont,
                                        fontWeight = FontWeight.Normal,
                                        color = theme["placeholder"]!!
                                    )
                                    Text(
                                        text = userPhoneNumber,
                                        fontSize = 22.sp,
                                        fontFamily = RubikFont,
                                        fontWeight = FontWeight.Normal,
                                        color = theme["text"]!!
                                    )
                                }
                            }
                        }
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    modifier = Modifier
                                        .height(44.dp),
                                    onClick = {
                                        isEditingState = !isEditingState
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = theme["primary"]!!)
                                ) {
                                    Text(
                                        text = localization["edit_profile_button"]!!,
                                        fontSize = 20.sp,
                                        color = theme["text"]!!,
                                        fontFamily = RubikFont,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    } else {
                        Column {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.size(32.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(theme["drawer_background"]!!)
                                            .size(125.dp)
                                            .clickable {
                                                getContent.launch("image/*")
                                            },
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Image(
                                            painter = rememberAsyncImagePainter(userPhoto),
                                            contentDescription = "",
                                            modifier = Modifier
                                                .size(100.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.size(24.dp))
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(32.dp)
                            ) {
                                Column {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = localization["name_title"]!!,
                                            fontSize = 24.sp,
                                            fontFamily = RubikFont,
                                            fontWeight = FontWeight.Normal,
                                            color = theme["placeholder"]!!
                                        )
                                        StyledBasicTextField(
                                            value = userName,
                                            onValueChange = {
                                                userName = it
                                                isNameEmpty = userName.isEmpty()
                                            },
                                            placeholder = localization["name_placeholder"]!!,
                                            theme = theme
                                        )
                                    }
                                    if (isNameEmpty) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            localization["name_empty"]!!,
                                            color = theme["error"]!!,
                                            fontSize = 12.sp,
                                            fontFamily = RubikFont,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                }
                                Column {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = localization["region_title"]!!,
                                                fontSize = 24.sp,
                                                fontFamily = RubikFont,
                                                fontWeight = FontWeight.Normal,
                                                color = theme["placeholder"]!!
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))

                                            TooltipBox(
                                                positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
                                                tooltip = {
                                                    RichTooltip(
                                                        modifier = Modifier.padding(20.dp),
                                                        title = {
                                                            Text(
                                                                text = localization["region_tip_title"]!!,
                                                                fontSize = 20.sp,
                                                                fontFamily = RubikFont,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                        },
                                                        text = {
                                                            Text(
                                                                text = localization["region_tip_text"]!!,
                                                                fontSize = 16.sp,
                                                                fontFamily = RubikFont,
                                                                fontWeight = FontWeight.Normal
                                                            )
                                                        },
                                                        colors = RichTooltipColors(
                                                            containerColor = theme["primary"]!!,
                                                            contentColor = theme["text"]!!,
                                                            titleContentColor = theme["text"]!!,
                                                            actionContentColor = theme["text"]!!
                                                        )
                                                    )
                                                },
                                                state = tooltipState
                                            ) {
                                                IconButton(
                                                    onClick = { scope.launch { tooltipState.show() } },
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = ImageVector.vectorResource(R.drawable.info),
                                                        contentDescription = "",
                                                        tint = Color(0xFFADADAD)
                                                    )
                                                }
                                            }
                                        }
                                        AutocompleteTextFieldForRegion(
                                            modifier = Modifier.heightIn(min = 0.dp, max = 300.dp),
                                            value = selectedRegion,
                                            placesClient = placesClient,
                                            onPlaceSelected = { value ->
                                                selectedRegion = value
                                                isRegionSelected = true
                                            },
                                            onValueChange = { value ->
                                                selectedRegion = value
                                                isRegionSelected = false
                                                isSelectedRegionEmpty = selectedRegion.isEmpty()
                                            },
                                            placeholder = localization["region_placeholder"]!!,
                                            settingsViewModel = settingsViewModel
                                        )
                                    }
                                    if (isSelectedRegionEmpty) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            localization["region_empty"]!!,
                                            color = theme["error"]!!,
                                            fontSize = 12.sp,
                                            fontFamily = RubikFont,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                }
                                if (authViewModel.isUserLoggedInWithEmailPassword()) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = localization["email_title"]!!,
                                            fontSize = 24.sp,
                                            fontFamily = RubikFont,
                                            fontWeight = FontWeight.Normal,
                                            color = theme["placeholder"]!!
                                        )
                                        Text(
                                            text = userEmail,
                                            fontSize = 22.sp,
                                            fontFamily = RubikFont,
                                            fontWeight = FontWeight.Normal,
                                            color = theme["text"]!!
                                        )
                                    }
                                }
                                Column {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = localization["phone_title"]!!,
                                            fontSize = 24.sp,
                                            fontFamily = RubikFont,
                                            fontWeight = FontWeight.Normal,
                                            color = theme["placeholder"]!!
                                        )
                                        StyledBasicTextField(
                                            value = userPhoneNumber,
                                            onValueChange = {
                                                userPhoneNumber = it
                                                isPhoneNumberEmpty = userPhoneNumber.isEmpty()
                                                phoneNumberError = !isValidPhoneNumber(it)
                                            },
                                            placeholder = localization["phone_placeholder"]!!,
                                            theme = theme
                                        )
                                    }
                                    if (isPhoneNumberEmpty) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            localization["phone_empty"]!!,
                                            color = theme["error"]!!,
                                            fontSize = 12.sp,
                                            fontFamily = RubikFont,
                                            fontWeight = FontWeight.Normal
                                        )
                                    } else if (phoneNumberError) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            localization["phone_invalid"]!!,
                                            color = theme["error"]!!,
                                            fontSize = 12.sp,
                                            fontFamily = RubikFont,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                        Column(
                            modifier = Modifier.padding(top = 32.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TextButton(
                                    modifier = Modifier
                                        .height(44.dp),
                                    onClick = {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            userName = userLocalStorage.getUserName().toString()
                                            userEmail = userLocalStorage.getUserEmail().toString()
                                            userPhoneNumber = userLocalStorage.getUserPhoneNumber().toString()
                                            selectedRegion = userLocalStorage.getUserRegion().toString()
                                            userPhoto = userLocalStorage.getUserPhotoUrl().toString()
                                        }
                                        isNameEmpty = false
                                        isPhoneNumberEmpty = false
                                        phoneNumberError = false
                                        isSelectedRegionEmpty = false
                                        isRegionSelected = true
                                        isEditingState = !isEditingState
                                    }
                                ) {
                                    Text(
                                        text = localization["discard_changes_button"]!!,
                                        fontSize = 20.sp,
                                        color = theme["primary"]!!,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = RubikFont
                                    )
                                }
                                Button(
                                    modifier = Modifier
                                        .height(44.dp),
                                    onClick = {
                                        isNameEmpty = userName.isEmpty()
                                        if(isNameEmpty) {
                                            Toast.makeText(context, localization["name_empty_error"]!!, Toast.LENGTH_LONG).show()
                                            return@Button
                                        }
                                        isPhoneNumberEmpty = userPhoneNumber.isEmpty()
                                        if(isPhoneNumberEmpty) {
                                            Toast.makeText(context, localization["phone_empty_error"]!!, Toast.LENGTH_LONG).show()
                                            return@Button
                                        }
                                        phoneNumberError = !isValidPhoneNumber(userPhoneNumber)
                                        if(phoneNumberError) {
                                            Toast.makeText(context, localization["phone_invalid_error"]!!, Toast.LENGTH_LONG).show()
                                            return@Button
                                        }
                                        isSelectedRegionEmpty = selectedRegion.isEmpty()
                                        if(isSelectedRegionEmpty) {
                                            Toast.makeText(context, localization["region_enter_error"]!!, Toast.LENGTH_LONG).show()
                                            return@Button
                                        }
                                        if(!isRegionSelected) {
                                            Toast.makeText(context, localization["region_select_error"]!!, Toast.LENGTH_LONG).show()
                                            return@Button
                                        }

                                        val userData = hashMapOf(
                                            "phoneNumber" to userPhoneNumber,
                                            "region" to selectedRegion
                                        )

                                        val uid = currentUser!!.uid
                                        val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
                                        firestore.collection("users")
                                            .document(uid)
                                            .set(userData)
                                            .addOnSuccessListener {
                                                authViewModel.updateUserProfile(
                                                    name = userName,
                                                    photo = userPhoto,
                                                    context = context,
                                                    localization = localization
                                                )
                                                var userPassword = ""
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    userPassword = userLocalStorage.getUserPassword().toString()
                                                }
                                                val userLocalData = UserData(
                                                    uid = uid,
                                                    email = userEmail,
                                                    password = userPassword,
                                                    name = userName,
                                                    phoneNumber = userPhoneNumber,
                                                    region = selectedRegion,
                                                    photoUrl = userPhoto
                                                )
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    incidentManager.setUserRegion(selectedRegion)
                                                    userLocalStorage.saveUser(userLocalData)
                                                    navController.navigate(Routes.PROFILE)
                                                }
                                            }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = theme["primary"]!!)
                                ) {
                                    Text(
                                        text = localization["save_profile_button"]!!,
                                        fontSize = 20.sp,
                                        color = theme["text"]!!,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = RubikFont
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