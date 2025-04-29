package com.denisshulika.road_radar.pages

import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import com.denisshulika.road_radar.AuthViewModel
import com.denisshulika.road_radar.Author
import com.denisshulika.road_radar.Comment
import com.denisshulika.road_radar.CommentAdditionState
import com.denisshulika.road_radar.CommentManager
import com.denisshulika.road_radar.IncidentsManager
import com.denisshulika.road_radar.Routes
import com.denisshulika.road_radar.SettingsViewModel
import com.denisshulika.road_radar.ui.components.CommentInputTextField
import com.denisshulika.road_radar.ui.components.PhotoPickerDialog
import com.github.chrisbanes.photoview.PhotoView
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsPage(
    navController: NavController,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    incidentsManager: IncidentsManager,
    commentManager: CommentManager
) {
    val context = LocalContext.current

    val incidentInfo by incidentsManager.selectedDocumentInfo.observeAsState()
    val localization = settingsViewModel.localization.observeAsState().value!!
    val theme = settingsViewModel.themeColors.observeAsState().value!!

    val commentAdditionState = commentManager.commentAdditionState.observeAsState().value

    var showDialog by remember { mutableStateOf(false) }

    val selectedImages = remember { mutableStateListOf<Uri>() }
    val cameraImageUri = remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uriList ->
        val remaining = 3 - selectedImages.size
        selectedImages.addAll(uriList.take(remaining))
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && selectedImages.size < 3) {
            selectedImages.add(cameraImageUri.value!!)
        }
    }

    val newCommentText = remember { mutableStateOf("") }

    LaunchedEffect(incidentInfo?.id) {
        incidentInfo?.id?.let { commentManager.startListeningComments(it) }
    }

    DisposableEffect(Unit) {
        onDispose {
            commentManager.stopListeningComments()
        }
    }

    val comments = commentManager.comments.observeAsState(emptyList()).value
    val authors = commentManager.authors.observeAsState(emptyMap()).value

    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(commentAdditionState) {
        if (commentAdditionState == CommentAdditionState.Success) {
            scope.launch {
                scrollState.animateScrollToItem(comments.size + 1)
            }
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
                            text = localization["comments_title"]!!,
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
                        .weight(1f)
                ) {
                    LazyColumn(
                        state = scrollState,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {}
                        items(comments) { comment ->
                            CommentItem(
                                comment = comment,
                                author = authors[comment.authorId]!!,
                                theme = theme,
                                localization = localization,
                                navController = navController,
                                commentManager = commentManager
                            )
                        }
                        item {}
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(theme["input_background"]!!)
                ) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        itemsIndexed(selectedImages) { index, uri ->
                            Box(modifier = Modifier.padding(4.dp)) {
                                Image(
                                    painter = rememberAsyncImagePainter(uri),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                IconButton(
                                    onClick = { selectedImages.removeAt(index) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .background(
                                            Color.Black.copy(alpha = 0.5f),
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "",
                                        tint = theme["icon"]!!
                                    )
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        PhotoPickerDialog(
                            showDialog = showDialog,
                            onDismiss = { showDialog = false },
                            onPickFromGallery = {
                                galleryLauncher.launch("image/*")
                            },
                            onTakePhoto = {
                                val photoFile = File(context.cacheDir, "${UUID.randomUUID()}.jpg")
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    photoFile
                                )
                                cameraImageUri.value = uri
                                cameraLauncher.launch(uri)
                            },
                            localization = localization,
                            theme = theme
                        )


                        IconButton(
                            enabled = selectedImages.size < 3,
                            onClick = {
                                showDialog = true
                            },
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Attachment,
                                contentDescription = "",
                                tint = theme["icon"]!!
                            )
                        }

                        CommentInputTextField(
                            value = newCommentText.value,
                            onValueChange = { newCommentText.value = it },
                            placeholder = localization["comment_placeholder"]!!,
                            theme = theme,
                            modifier = Modifier.weight(1f)
                        )
                        if (commentAdditionState == CommentAdditionState.Success || commentAdditionState == CommentAdditionState.Idle) {
                            IconButton(
                                modifier = Modifier.padding(
                                    end = 4.dp,
                                    bottom = 4.dp
                                ),
                                onClick = {
                                    if (newCommentText.value.isNotEmpty() || selectedImages.isNotEmpty()) {
                                        val user = authViewModel.getCurrentUser()!!
                                        incidentsManager.incrementCommentCount(incidentInfo!!.id, 1)
                                        commentManager.addComment(
                                            comment = Comment(
                                                incidentId = incidentInfo!!.id,
                                                authorId = authViewModel.getCurrentUser()!!.uid,
                                                text = newCommentText.value
                                            ),
                                            photoUris = selectedImages,
                                            localization = localization
                                        )
                                        newCommentText.value = ""
                                        selectedImages.clear()
                                    }
                                },
                                enabled = newCommentText.value.isNotEmpty() || selectedImages.isNotEmpty()
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "",
                                    tint = theme["icon"]!!
                                )
                            }
                        } else {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(end = 12.dp, bottom = 12.dp)
                                    .size(32.dp),
                                strokeWidth = 4.dp,
                                color = theme["icon"]!!
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ZoomableImage(
    imageUrl: String,
    theme: Map<String, Color>
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = theme["icon"]!!
            )
        }

        AndroidView(
            factory = {
                PhotoView(it).apply {
                    scaleType = ImageView.ScaleType.FIT_CENTER
                }
            },
            update = { photoView ->
                val imageLoader = context.imageLoader
                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .listener(
                        onSuccess = { _, _ -> isLoading = false },
                        onError = { _, _ -> isLoading = false }
                    )
                    .target { drawable ->
                        photoView.setImageDrawable(drawable)
                    }
                    .build()
                imageLoader.enqueue(request)
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentItem(
    comment: Comment,
    author: Author,
    theme: Map<String, Color>,
    localization: Map<String, String>,
    navController: NavController,
    commentManager: CommentManager
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedImageUrl by remember { mutableStateOf("") }

    val text = comment.text
    val photos = comment.photos

    val commentDateFormat = SimpleDateFormat(
        "d MMMM yyyy, HH:mm",
        Locale(localization["date_format_language"]!!, localization["date_format_country"]!!)
    )

    val timestamp = comment.timestamp
    val date = commentDateFormat.format(timestamp)

    val userName = author.name
    val userAvatarUrl = author.photoUrl

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = bottomSheetState,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            dragHandle = {},
            shape = RectangleShape,
            containerColor = if(comment.systemComment) Color.Transparent else theme["background"]!!
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ZoomableImage(selectedImageUrl, theme)

                IconButton(
                    onClick = {
                        showBottomSheet = false
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .wrapContentSize()
                        .padding(12.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(50)
                        ),
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.Default.Close,
                        contentDescription = "",
                        tint = theme["icon"]!!
                    )
                }
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 8.dp),
        color = theme["drawer_background"]!!,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                if (userAvatarUrl.isNotBlank()) {
                    SubcomposeAsyncImage(

                        model = userAvatarUrl,
                        contentDescription = "",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                commentManager.setSelectedProfileID(author.id)
                                navController.navigate(Routes.OTHER_PROFILE)
                            },
                        loading = {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userName.firstOrNull()?.toString() ?: "?",
                                    color = Color.White,
                                    fontSize = 20.sp
                                )
                            }
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                commentManager.setSelectedProfileID(author.id)
                                navController.navigate(Routes.OTHER_PROFILE)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.firstOrNull()?.toString() ?: "?",
                            color = Color.White,
                            fontSize = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))


                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        modifier = Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            commentManager.setSelectedProfileID(author.id)
                            navController.navigate(Routes.OTHER_PROFILE)
                        },
                        text = "$userName · $date",
                        style = TextStyle(
                            color = theme["text"]!!,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (comment.systemComment) {"${localization["user"]} $userName ${localization[text]!!}"} else text,
                        style = TextStyle(
                            color = theme["text"]!!,
                            fontSize = 16.sp
                        )
                    )
                }
            }

            photos.takeIf { it.isNotEmpty() }?.let { images ->
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(images.size) { index ->
                        val imageUrl = images[index]
                        val painter = rememberAsyncImagePainter(imageUrl)
                        val painterState = painter.state
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Gray.copy(alpha = 0.2f))
                                .clickable { }
                        ) {
                            Image(
                                painter = painter,
                                contentDescription = "",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                                    .matchParentSize()
                                    .clickable {
                                        selectedImageUrl = imageUrl
                                        showBottomSheet = true
                                    },
                                contentScale = ContentScale.Crop
                            )
                            if (painterState is AsyncImagePainter.State.Loading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(48.dp),
                                        color = theme["icon"]!!
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