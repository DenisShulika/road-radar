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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.denisshulika.road_radar.AuthState
import com.denisshulika.road_radar.AuthViewModel
import com.denisshulika.road_radar.Routes
import com.denisshulika.road_radar.isValidPhoneNumber
import com.denisshulika.road_radar.local.UserLocalStorage
import com.denisshulika.road_radar.model.CustomDrawerState
import com.denisshulika.road_radar.model.NavigationItem
import com.denisshulika.road_radar.model.UserData
import com.denisshulika.road_radar.model.isOpened
import com.denisshulika.road_radar.model.opposite
import com.denisshulika.road_radar.ui.components.CustomDrawer
import com.denisshulika.road_radar.ui.components.StyledBasicTextField
import com.denisshulika.road_radar.util.coloredShadow
import com.google.accompanist.systemuicontroller.rememberSystemUiController
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
    authViewModel: AuthViewModel
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
    var selectedNavigationItem by remember { mutableStateOf(NavigationItem.Profile) } //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

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


    var userArea by remember { mutableStateOf("") }
    val isAreaDropdownExpanded = remember { mutableStateOf(false) }
    val areaItemPosition = remember { mutableIntStateOf(0) }

    var userRegion by remember { mutableStateOf<String?>("") }
    val isRegionDropdownExpanded = remember { mutableStateOf(false) }
    val regionItemPosition = remember { mutableIntStateOf(0) }

    val regionsByArea = loadRegionsFromJson(context)
    val areas = regionsByArea.keys.toList()

    var userPhoto by remember { mutableStateOf("") }

    val currentUser = authViewModel.getCurrentUser()
    val userLocalStorage = UserLocalStorage(context)
    LaunchedEffect(Unit) {
        userName = userLocalStorage.getUserName().toString()
        userEmail = userLocalStorage.getUserEmail().toString()
        userPhoneNumber = userLocalStorage.getUserPhoneNumber().toString()
        userArea = userLocalStorage.getUserArea().toString()
        userRegion = userLocalStorage.getUserRegion().toString()
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
            navController = navController
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
                            Text(
                                text = "Your profile information",
                                fontSize = 26.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.size(32.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Column(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFEFF1F3)),
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
                            verticalArrangement = Arrangement.spacedBy(30.dp)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            if (authViewModel.isUserLoggedInWithEmailPassword()) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Area",
                                    fontSize = 24.sp,
                                    fontFamily = RubikFont,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFF808080)
                                )
                                Text(
                                    text = userArea,
                                    fontSize = 22.sp,
                                    fontFamily = RubikFont,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Region",
                                    fontSize = 24.sp,
                                    fontFamily = RubikFont,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFF808080)
                                )
                                Text(
                                    text = userRegion.toString(),
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
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(
                                text = "Edit your profile",
                                fontSize = 26.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.size(32.dp))
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
                            verticalArrangement = Arrangement.spacedBy(29.dp)
                        ) {
                            Column {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                        placeholder = "Your Name, e.g: John Doe",
                                        fontSize = 22.sp
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
                            if (authViewModel.isUserLoggedInWithEmailPassword()) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            Column {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                        placeholder = "Your phone, e.g: +380.. or 0..",
                                        fontSize = 22.sp
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
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Area",
                                    fontSize = 24.sp,
                                    fontFamily = RubikFont,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFF808080)
                                )
                                Column(
                                    modifier = Modifier
                                        .drawBehind {
                                            val strokeWidth = 1.dp.toPx()
                                            val y = size.height - strokeWidth / 2
                                            drawLine(
                                                color = Color(0xFFD3D3D3),
                                                start = Offset(0f, 0.75f * y),
                                                end = Offset(size.width, 0.75f * y),
                                                strokeWidth = strokeWidth
                                            )
                                        },
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .height(36.dp)
                                            .fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clickable {
                                                    isAreaDropdownExpanded.value = true
                                                }
                                                .fillMaxWidth()
                                        ) {
                                            Text(
                                                text = userArea,
                                                style = TextStyle(
                                                    fontSize = 22.sp,
                                                    lineHeight = 22.sp
                                                ),
                                                fontFamily = RubikFont,
                                                fontWeight = FontWeight.Normal
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = ""
                                            )
                                        }
                                        DropdownMenu(
                                            modifier = Modifier
                                                .background(Color(0xFF474EFF)),
                                            expanded = isAreaDropdownExpanded.value,
                                            onDismissRequest = {
                                                isAreaDropdownExpanded.value = false
                                            }) {
                                            areas.forEachIndexed { index, area ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = area,
                                                            style = TextStyle(
                                                                color = Color(0xFFFFFFFF),
                                                                fontSize = 22.sp,
                                                                lineHeight = 22.sp
                                                            ),
                                                            fontFamily = RubikFont,
                                                            fontWeight = FontWeight.Normal
                                                        )
                                                    },
                                                    onClick = {
                                                        isAreaDropdownExpanded.value = false
                                                        areaItemPosition.intValue = index
                                                        userArea = area
                                                        userRegion = null
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Region",
                                    fontSize = 24.sp,
                                    fontFamily = RubikFont,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFF808080)
                                )
                                Column(
                                    modifier = Modifier
                                        .drawBehind {
                                            val strokeWidth = 1.dp.toPx()
                                            val y = size.height - strokeWidth / 2
                                            drawLine(
                                                color = Color(0xFFD3D3D3),
                                                start = Offset(0f, 0.75f * y),
                                                end = Offset(size.width, 0.75f * y),
                                                strokeWidth = strokeWidth
                                            )
                                        },
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .height(36.dp)
                                            .fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clickable {
                                                    isRegionDropdownExpanded.value = true
                                                }
                                                .fillMaxWidth()
                                        ) {
                                            Text(
                                                text = userRegion?.takeIf { it.isNotBlank() }
                                                    ?: "Choose your district",
                                                style = TextStyle(
                                                    color = if (userRegion != null) Color(0xFF000000) else Color(
                                                        0xFFADADAD
                                                    ),
                                                    fontSize = 22.sp,
                                                    lineHeight = 22.sp
                                                ),
                                                fontFamily = RubikFont,
                                                fontWeight = FontWeight.Normal
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = ""
                                            )
                                        }
                                        DropdownMenu(
                                            modifier = Modifier
                                                .background(Color(0xFF474EFF)),
                                            expanded = isRegionDropdownExpanded.value,
                                            onDismissRequest = {
                                                isRegionDropdownExpanded.value = false
                                            }) {
                                            regionsByArea[userArea]?.forEachIndexed { index, region ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = region,
                                                            style = TextStyle(
                                                                color = Color(0xFFFFFFFF),
                                                                fontSize = 22.sp,
                                                                lineHeight = 22.sp
                                                            ),
                                                            fontFamily = RubikFont,
                                                            fontWeight = FontWeight.Normal
                                                        )
                                                    },
                                                    onClick = {
                                                        isRegionDropdownExpanded.value = false
                                                        regionItemPosition.intValue = index
                                                        userRegion = region
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Column {
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
                                        userArea = userLocalStorage.getUserArea().toString()
                                        userRegion = userLocalStorage.getUserRegion().toString()
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
                                    if(userRegion == null) {
                                        Toast.makeText(context, "Please, select your region", Toast.LENGTH_LONG).show()
                                        return@Button
                                    }

                                    val userData = hashMapOf(
                                        "phoneNumber" to userPhoneNumber,
                                        "area" to userArea,
                                        "region" to userRegion
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
                                                area = userArea,
                                                region = userRegion!!,
                                                photoUrl = userPhoto
                                            )
                                            CoroutineScope(Dispatchers.Main).launch {
                                                userLocalStorage.saveUser(userLocalData)
                                            }
                                            navController.navigate(Routes.PROFILE)
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