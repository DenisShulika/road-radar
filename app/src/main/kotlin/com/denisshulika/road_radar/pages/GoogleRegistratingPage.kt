package com.denisshulika.road_radar.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.denisshulika.road_radar.AuthState
import com.denisshulika.road_radar.AuthViewModel
import com.denisshulika.road_radar.R
import com.denisshulika.road_radar.Routes

@Composable
fun GoogleRegistratingPage(
    @Suppress("UNUSED_PARAMETER") modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val authState = authViewModel.authState.observeAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val regionsByArea = mapOf(
        "Avtonomna Respublika Krym" to listOf("Bakhchysaraiskyi", "Bilogirskyi", "Dzhankoyskyi", "Yevpatoriiskyi", "Kerchenskyi", "Kurmanskyi", "Perekopskyi", "Simferopolskyi", "Feodosiiskyi", "Yaltynskyi"),
        "Vinnytska" to listOf("Vinnytskyi", "Haisynskyi", "Zhmerynskyi", "Mohyliv-Podilskyi", "Tulchynskyi", "Khmilnytskyi"),
        "Volynska" to listOf("Volodymyr-Volynskyi", "Kamin-Kashyrskyi", "Kovelskyi", "Lutskyi"),
        "Dnipropetrovska" to listOf("Dniprovskiyi", "Kamianchskyi", "Kryvorizkyi", "Nikopolskyi", "Novomoskovskyi", "Pavlohradskiyi", "Synelnykivskyi"),
        "Donetska" to listOf("Bakhmutskyi", "Volnovaskyi", "Horlivskyi", "Donetskyi", "Kalmiuskyi", "Kramatorskyi", "Mariupolskyi", "Pokrovskyi"),
        "Zhytomyrska" to listOf("Berdychivskyi", "Zhytomyrskyi", "Korostenskyi", "Novograd-Volynskyi"),
        "Zakarpatska" to listOf("Beregivskyi", "Mukachivskyi", "Rakhivskyi", "Tyachivskyi", "Uzhhorodskyi", "Khustskyi"),
        "Zaporizka" to listOf("Berdyanskyi", "Vasylivskyi", "Zaporizkyi", "Melitopolskyi", "Polohivskyi"),
        "Ivano-Frankivska" to listOf("Verkhovynskyi", "Ivano-Frankivskyi", "Kalushskyi", "Kolomyiskyi", "Kosivskyi", "Nadvirnianskyi"),
        "Kyivska" to listOf("Bilotserkivskyi", "Boryspilskyi", "Brovarskyi", "Buchanskyi", "Vyshhorodskyi", "Obukhivskyi", "Fastivskyi"),
        "Kirovohradska" to listOf("Holovanyivskyi", "Kropyvnytskyi", "Novoukrainskyi", "Oleksandriiskyi"),
        "Luhanska" to listOf("Alchevskyi", "Dovzhanskyi", "Luhanskyi", "Rovenkivskyi", "Svatovskyi", "Severodonetskyi", "Starobilskyi", "Shchastynskyi"),
        "Lvivska" to listOf("Drohobychskyi", "Zolochivskyi", "Lvivskyi", "Sambirskyi", "Striiskyi", "Chervonogradskyi", "Yavorivskyi"),
        "Mykolaivska" to listOf("Bashtanskyi", "Voznesenskyi", "Mykolaivskyi", "Pervomaiskyi"),
        "Odeska" to listOf("Berezivskyi", "Bilhorod-Dnistrovskyi", "Bolhradskyi", "Izmailskyi", "Odeskyi", "Podilskyi", "Rozdilnianskyi"),
        "Poltavska" to listOf("Kremenchutskyi", "Lubenskyi", "Myrohorodskyi", "Poltavskyi"),
        "Rivnenska" to listOf("Varashskyi", "Dubenskyi", "Rivnenskyi", "Sarnenskyi"),
        "Sumska" to listOf("Konotopskyi", "Okhtyrskyi", "Romenskyi", "Sumskyi", "Shostkynskyi"),
        "Ternopilska" to listOf("Kremenetskyi", "Ternopilskyi", "Chortkivskyi"),
        "Kharkivska" to listOf("Bohodukhivskyi", "Iziumskyi", "Krasnohradskyi", "Kupyanskyi", "Lozivskyi", "Kharkivskyi", "Chuhuivskyi"),
        "Khersonska" to listOf("Beryslavskyi", "Henicheskyi", "Kakhovsky", "Skadovsky", "Khersonskiy"),
        "Khmelnytska" to listOf("Kamianets-Podilskyi", "Khmelnytskyi", "Shepetivskyi"),
        "Cherkaska" to listOf("Zvenyhorodskyi", "Zolotoniskyi", "Umanskyi", "Cherkaskyi"),
        "Chernivetska" to listOf("Vyzhnytskyi", "Dnistrovskyi", "Chernivetskyi"),
        "Chernihivska" to listOf("Koriukivskyi", "Nizhynskyi", "Novhorod-Siverskyi", "Prilutskyi", "Chernihivskyi"),
        "Misto Kyiv" to listOf("Holosiivskyi", "Darnytskyi", "Desnianskyi", "Dniprovskiyi", "Obolonskyi", "Pecherskyi", "Podilskyi", "Sviatoshynskyi", "Solomianskyi", "Shevchenkivskyi"),
        "Misto Simferopol" to listOf("Leninskyi", "Balaklavskyi", "Haharinskyi", "Nakhimovskyi")
    )
    val areas = regionsByArea.keys.toList()

    var selectedArea by remember { mutableStateOf<String?>(null) }
    val isAreaDropdownExpanded = remember { mutableStateOf(false) }
    val areaItemPosition = remember { mutableIntStateOf(0) }

    var selectedRegion by remember { mutableStateOf<String?>(null) }
    val isRegionDropdownExpanded = remember { mutableStateOf(false) }
    val regionItemPosition = remember { mutableIntStateOf(0) }

    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.Authenticated ->
                navController.navigate(Routes.NEWS)
            is AuthState.Unauthenticated ->
                navController.navigate(Routes.LOGIN)
            is AuthState.Error ->
                Toast.makeText(context, (authState.value as AuthState.Error).message, Toast.LENGTH_LONG).show()
            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(painterResource(R.drawable.auth_background), contentScale = ContentScale.Crop)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                )
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Sign in",
                    fontSize = 64.sp,
                    color = Color.White,
                    fontFamily = RubikFont,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "with Google",
                    fontSize = 64.sp,
                    color = Color.White,
                    fontFamily = RubikFont,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Box (
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
                        .background(Color.White)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Top
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(modifier = Modifier.size(20.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Text(
                                text = "Your area",
                                fontSize = 24.sp,
                                fontFamily = RubikFont,
                                fontWeight = FontWeight.Normal
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .drawBehind {
                                        val strokeWidth = 1.dp.toPx()
                                        val y = size.height - strokeWidth / 2
                                        drawLine(
                                            color = Color(0xFFADADAD),
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
                                            text = selectedArea?.takeIf { it.isNotBlank() } ?: "Choose your area",
                                            style = TextStyle(
                                                color = Color(0xFFADADAD),
                                                fontSize = 20.sp,
                                                lineHeight = 20.sp
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
                                            DropdownMenuItem(text = {
                                                Text(
                                                    text = area,
                                                    style = TextStyle(
                                                        color = Color(0xFFFFFFFF),
                                                        fontSize = 20.sp,
                                                        lineHeight = 20.sp
                                                    ),
                                                    fontFamily = RubikFont,
                                                    fontWeight = FontWeight.Normal
                                                )
                                            },
                                                onClick = {
                                                    isAreaDropdownExpanded.value = false
                                                    areaItemPosition.intValue = index
                                                    selectedArea = area
                                                    selectedRegion = null
                                                })
                                        }
                                    }
                                }

                            }
                        }

                        if (selectedArea != null) {
                            Spacer(modifier = Modifier.size(32.dp))
                            Column(
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                Text(
                                    text = "Your district",
                                    fontSize = 24.sp,
                                    fontFamily = RubikFont,
                                    fontWeight = FontWeight.Normal
                                )
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .drawBehind {
                                            val strokeWidth = 1.dp.toPx()
                                            val y = size.height - strokeWidth / 2
                                            drawLine(
                                                color = Color(0xFFADADAD),
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
                                                text = selectedRegion?.takeIf { it.isNotBlank() } ?: "Choose your district",
                                                style = TextStyle(
                                                    color = Color(0xFFADADAD),
                                                    fontSize = 20.sp,
                                                    lineHeight = 20.sp
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
                                            regionsByArea[selectedArea]?.forEachIndexed { index, region ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = region,
                                                            style = TextStyle(
                                                                color = Color(0xFFFFFFFF),
                                                                fontSize = 20.sp,
                                                                lineHeight = 20.sp
                                                            ),
                                                            fontFamily = RubikFont,
                                                            fontWeight = FontWeight.Normal
                                                        )
                                                    },
                                                    onClick = {
                                                        isRegionDropdownExpanded.value = false
                                                        regionItemPosition.intValue = index
                                                        selectedRegion = region
                                                    })
                                            }
                                        }
                                    }

                                }
                            }

                        }
                        Spacer(modifier = Modifier.size(24.dp))
                        Button(
                            onClick = {
                                if(selectedArea == null) {
                                    Toast.makeText(context, "Please, select your area", Toast.LENGTH_LONG).show()
                                    return@Button
                                } else if(selectedRegion == null) {
                                    Toast.makeText(context, "Please, select your region", Toast.LENGTH_LONG).show()
                                    return@Button
                                } else {
                                    authViewModel.completeRegistrationViaGoogle(
                                        selectedArea!!,
                                        selectedRegion!!,
                                        context,
                                        coroutineScope
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF474EFF)),
                            enabled = authState.value != AuthState.Loading
                        ) {
                            if (authState.value is AuthState.Loading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .align(Alignment.Center),
                                        color = Color(0xFF474EFF)
                                    )
                                }
                            } else {
                                Text(
                                    text = "Finish Signing Up",
                                    fontSize = 24.sp,
                                    fontFamily = RubikFont,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            TextButton(
                                modifier = Modifier
                                    .padding(top = 2.dp, bottom = 2.dp),
                                onClick = {
                                    authViewModel.deleteAccount(
                                        context = context,
                                        coroutineScope = coroutineScope
                                    )
                                }
                            ) {
                                Text(
                                    modifier = Modifier,
                                    text = "Changed your mind about signing in with Google?",
                                    fontSize = 14.sp,
                                    color = Color(0xFF6369FF),
                                    fontFamily = RubikFont,
                                    fontWeight = FontWeight.Normal,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        Spacer(modifier = Modifier.size(4.dp))
                    }
                }
            }
        }
    }
}