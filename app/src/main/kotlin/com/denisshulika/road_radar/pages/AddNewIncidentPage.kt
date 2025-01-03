package com.denisshulika.road_radar.pages

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.denisshulika.road_radar.AuthViewModel
import com.denisshulika.road_radar.Routes
import com.denisshulika.road_radar.ui.components.StyledBasicTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewIncidentPage(
    @Suppress("UNUSED_PARAMETER") modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val incidentTypes = listOf("Car accident", "Roadblock", "Weather conditions", "Traffic jam", "Other")

    var selectedIncidentType by remember { mutableStateOf<String?>(null) }
    var isIncidentTypeDropdownExpanded by remember { mutableStateOf(false) }

    var incidentDescription by remember { mutableStateOf("") }
    var isIncidentDescriptionEmpty by remember { mutableStateOf(false) }

    var incidentPhotos by remember { mutableStateOf<List<Uri?>>(emptyList()) }

    val context = LocalContext.current

    val getContent = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uriList ->
            if (uriList.size + incidentPhotos.size <= 3) {
                incidentPhotos = incidentPhotos + uriList
            } else {
                Toast.makeText(context, "You can select up to 3 photos", Toast.LENGTH_SHORT).show()
            }
        }
    )

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
                            text = "Add new incident",
                            textAlign = TextAlign.Center,
                            fontFamily = RubikFont
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigate(Routes.INCIDENTS) }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "")
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Incident type",
                            fontSize = 24.sp,
                            fontFamily = RubikFont
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
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .height(36.dp)
                                    .fillMaxWidth()
                                    .clickable { isIncidentTypeDropdownExpanded = true }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = selectedIncidentType ?: "Choose incident type",
                                        color = if (selectedIncidentType != null) Color(0xFF000000) else Color(0xFFADADAD),
                                        fontSize = 22.sp,
                                        fontFamily = RubikFont
                                    )
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "")
                                }
                                DropdownMenu(
                                    modifier = Modifier.background(Color(0xFF474EFF)),
                                    expanded = isIncidentTypeDropdownExpanded,
                                    onDismissRequest = { isIncidentTypeDropdownExpanded = false }
                                ) {
                                    incidentTypes.forEach { type ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = type,
                                                    color = Color.White,
                                                    fontSize = 20.sp,
                                                    fontFamily = RubikFont
                                                )
                                            },
                                            onClick = {
                                                selectedIncidentType = type
                                                isIncidentTypeDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(text = "Description", fontSize = 24.sp, fontFamily = RubikFont)
                        StyledBasicTextField(
                            value = incidentDescription,
                            onValueChange = {
                                incidentDescription = it
                                isIncidentDescriptionEmpty = incidentDescription.isEmpty()
                            },
                            placeholder = "Enter a description to an accident",
                            singleLine = false
                        )
                    }
                    if (isIncidentDescriptionEmpty) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Description can't be empty",
                            color = Color(0xFFB71C1C),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Photos",
                            fontSize = 24.sp,
                            fontFamily = RubikFont
                        )
                        Column {
                            if (incidentPhotos.isEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "No photos selected",
                                        fontSize = 16.sp,
                                        fontFamily = RubikFont,
                                        color = Color(0xFFADADAD)
                                    )
                                }
                            }
                            incidentPhotos.forEachIndexed { index, uri ->
                                val fileName = getFileNameFromUri(context, uri ?: Uri.EMPTY)
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            AsyncImage(
                                                model = uri,
                                                contentDescription = "",
                                                modifier = Modifier
                                                    .size(50.dp)
                                                    .padding(end = 8.dp)
                                            )
                                            Text(
                                                text = fileName,
                                                fontSize = 18.sp,
                                                fontFamily = RubikFont,
                                                color = Color.Black
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                incidentPhotos = incidentPhotos.toMutableList().apply { removeAt(index) }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Delete,
                                                contentDescription = "Delete Photo",
                                                tint = Color.Red
                                            )
                                        }
                                    }
                                    if (index < incidentPhotos.size - 1) {
                                        HorizontalDivider(
                                            modifier = Modifier
                                                .padding(vertical = 8.dp, horizontal = 36.dp),
                                            thickness = 1.dp,
                                            color = Color(0xFFADADAD)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        TextButton(
                            onClick = {
                                getContent.launch(
                                    PickVisualMediaRequest()
                                )
                            }
                        ) {
                            Text(
                                text = "Add photos (up to 3)",
                                fontSize = 20.sp,
                                color = Color(0xFF6369FF),
                                fontFamily = RubikFont
                            )
                            Spacer(modifier = Modifier.size(4.dp))
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "",
                                tint = Color(0xFF6369FF)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        onClick = {
                            if (selectedIncidentType == null) {
                                Toast.makeText(context, "Please, choose incident type", Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            isIncidentDescriptionEmpty = incidentDescription.isEmpty()
                            if (isIncidentDescriptionEmpty) {
                                Toast.makeText(context, "Please, enter a description", Toast.LENGTH_LONG).show()
                                return@Button
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF474EFF))
                    ) {
                        Text(
                            text = "Publish the incident",
                            fontSize = 24.sp,
                            fontFamily = RubikFont
                        )
                    }
                }
            }
        }
    }
}

fun getFileNameFromUri(context: Context, uri: Uri): String {
    var fileName = "Unknown file"

    val cursor = context.contentResolver.query(uri, null, null, null, null)

    cursor?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex != -1 && it.moveToFirst()) {
            fileName = it.getString(nameIndex)
        }
    }

    return fileName
}

