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
import com.denisshulika.road_radar.isValidPhoneNumber
import com.denisshulika.road_radar.local.UserLocalStorage
import com.denisshulika.road_radar.model.CustomDrawerState
import com.denisshulika.road_radar.model.NavigationItem
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
                            text = if (isEditingState) "Edit profile" else selectedNavigationItem.title,
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
                    .fillMaxHeight()
                    .padding(innerPadding)
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
                            Spacer(modifier = Modifier.size(4.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Column(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFEFF1F3))
                                        .clickable {
                                            getContent.launch("image/*")
                                        },
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(userPhoto),
                                        contentDescription = "Cropped Image",
                                        modifier = Modifier.size(100.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.size(4.dp))
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
                                    text = "Name",
                                    fontSize = 24.sp,
                                    fontFamily = RubikFont,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFF808080)
                                )
                                Text(
                                    text = userName,
                                    fontSize = 22.sp,
                                    fontFamily = RubikFont,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Region",
                                    fontSize = 24.sp,
                                    fontFamily = RubikFont,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFF808080)
                                )
                                Text(
                                    text = selectedRegion,
                                    fontSize = 22.sp,
                                    fontFamily = RubikFont,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                            if (authViewModel.isUserLoggedInWithEmailPassword()) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "Email",
                                        fontSize = 24.sp,
                                        fontFamily = RubikFont,
                                        fontWeight = FontWeight.Normal,
                                        color = Color(0xFF808080)
                                    )
                                    Text(
                                        text = userEmail,
                                        fontSize = 22.sp,
                                        fontFamily = RubikFont,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                            }
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Phone Number",
                                    fontSize = 24.sp,
                                    fontFamily = RubikFont,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFF808080)
                                )
                                Text(
                                    text = userPhoneNumber,
                                    fontSize = 22.sp,
                                    fontFamily = RubikFont,
                                    fontWeight = FontWeight.Normal
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
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFF474EFF
                                    )
                                )
                            ) {
                                Text(
                                    text = "Edit profile",
                                    fontSize = 20.sp,
                                    color = Color.White,
                                    fontFamily = RubikFont
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
                            Spacer(modifier = Modifier.size(16.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Column(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFEFF1F3))
                                        .clickable {
                                            getContent.launch("image/*")
                                        },
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(userPhoto),
                                        contentDescription = "Cropped Image",
                                        modifier = Modifier.size(100.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.size(4.dp))
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
                                        text = "Name",
                                        fontSize = 24.sp,
                                        fontFamily = RubikFont,
                                        fontWeight = FontWeight.Normal,
                                        color = Color(0xFF808080)
                                    )
                                    StyledBasicTextField(
                                        value = userName,
                                        onValueChange = {
                                            userName = it
                                            isNameEmpty = userName.isEmpty()
                                        },
                                        placeholder = "Your Name, e.g: John Doe"
                                    )
                                }
                                if (isNameEmpty) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Name cant be empty",
                                        color = Color(0xFFB71C1C),
                                        style = MaterialTheme.typography.bodySmall
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
                                            text = "Region",
                                            fontSize = 24.sp,
                                            fontFamily = RubikFont,
                                            fontWeight = FontWeight.Normal
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))

                                        TooltipBox(
                                            positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
                                            tooltip = {
                                                RichTooltip(
                                                    modifier = Modifier.padding(20.dp),
                                                    title = {
                                                        Text(
                                                            text = "Region",
                                                            fontSize = 20.sp,
                                                            fontFamily = RubikFont,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    },
                                                    text = {
                                                        Text(
                                                            text = "Incidents are filtered by region, so enter the one you live in",
                                                            fontSize = 16.sp,
                                                            fontFamily = RubikFont,
                                                            fontWeight = FontWeight.Normal
                                                        )
                                                    },
                                                    colors = RichTooltipColors(
                                                        containerColor = Color(0xFF474EFF),
                                                        contentColor = Color(0xFFFFFFFF),
                                                        titleContentColor = Color(0xFFFFFFFF),
                                                        actionContentColor = Color(0xFFFFFFFF)
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
                                        placeholder = "Enter your region"
                                    )
                                }
                                if (isSelectedRegionEmpty) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Region cant be empty",
                                        color = Color(0xFFB71C1C),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            Column {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "Phone Number",
                                        fontSize = 24.sp,
                                        fontFamily = RubikFont,
                                        fontWeight = FontWeight.Normal,
                                        color = Color(0xFF808080)
                                    )
                                    StyledBasicTextField(
                                        value = userPhoneNumber,
                                        onValueChange = {
                                            userPhoneNumber = it
                                            isPhoneNumberEmpty = userPhoneNumber.isEmpty()
                                            phoneNumberError = !isValidPhoneNumber(it)
                                        },
                                        placeholder = "Your phone, e.g: +380.. or 0.."
                                    )
                                }
                                if (isPhoneNumberEmpty) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Phone number cant be empty",
                                        color = Color(0xFFB71C1C),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                } else if (phoneNumberError) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Invalid phone number",
                                        color = Color(0xFFB71C1C),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            if (authViewModel.isUserLoggedInWithEmailPassword()) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "Email",
                                        fontSize = 24.sp,
                                        fontFamily = RubikFont,
                                        fontWeight = FontWeight.Normal,
                                        color = Color(0xFF808080)
                                    )
                                    Text(
                                        text = userEmail,
                                        fontSize = 20.sp,
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
                                    isEditingState = !isEditingState
                                }
                            ) {
                                Text(
                                    text = "Discard changes",
                                    fontSize = 20.sp,
                                    color = Color(0xFF474EFF),
                                    fontFamily = RubikFont
                                )
                            }
                            Button(
                                modifier = Modifier
                                    .height(44.dp),
                                onClick = {
                                    isNameEmpty = userName.isEmpty()
                                    if(isNameEmpty) {
                                        Toast.makeText(context, "Please, enter your name", Toast.LENGTH_LONG).show()
                                        return@Button
                                    }
                                    isPhoneNumberEmpty = userPhoneNumber.isEmpty()
                                    if(isPhoneNumberEmpty) {
                                        Toast.makeText(context, "Please, enter your phone", Toast.LENGTH_LONG).show()
                                        return@Button
                                    }
                                    phoneNumberError = !isValidPhoneNumber(userPhoneNumber)
                                    if(phoneNumberError) {
                                        Toast.makeText(context, "Please, enter correct phone number", Toast.LENGTH_LONG).show()
                                        return@Button
                                    }
                                    isSelectedRegionEmpty = selectedRegion.isEmpty()
                                    if(isSelectedRegionEmpty) {
                                        Toast.makeText(context, "Please, enter your region", Toast.LENGTH_LONG).show()
                                        return@Button
                                    }
                                    if(!isRegionSelected) {
                                        Toast.makeText(context, "Please, select your region", Toast.LENGTH_LONG).show()
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
                                                context = context
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
                                                userLocalStorage.saveUser(userLocalData)
                                                navController.navigate(Routes.PROFILE)
                                            }
                                        }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF474EFF))
                            ) {
                                Text(
                                    text = "Save",
                                    fontSize = 20.sp,
                                    color = Color.White,
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