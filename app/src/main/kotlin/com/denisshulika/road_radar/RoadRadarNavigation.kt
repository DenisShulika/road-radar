package com.denisshulika.road_radar

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.denisshulika.road_radar.pages.AboutPage
import com.denisshulika.road_radar.pages.AddNewIncidentPage
import com.denisshulika.road_radar.pages.CommentsPage
import com.denisshulika.road_radar.pages.EmailResetPage
import com.denisshulika.road_radar.pages.GoogleRegistratingPage
import com.denisshulika.road_radar.pages.IncidentPage
import com.denisshulika.road_radar.pages.IncidentsPage
import com.denisshulika.road_radar.pages.LoginPage
import com.denisshulika.road_radar.pages.MapRadarPage
import com.denisshulika.road_radar.pages.OtherProfilePage
import com.denisshulika.road_radar.pages.PasswordResetPage
import com.denisshulika.road_radar.pages.SelfProfilePage
import com.denisshulika.road_radar.pages.SettingsPage
import com.denisshulika.road_radar.pages.SignUpPage

@ExperimentalMaterial3Api
@Composable
fun RoadRadarNavigation(
    authViewModel: AuthViewModel,
    incidentsManager: IncidentsManager,
    settingsViewModel: SettingsViewModel,
    locationHandler: LocationHandler,
    commentManager: CommentManager
) {
    val navController = rememberNavController()

    LaunchedEffect(authViewModel.authState.value) {
        when (authViewModel.authState.value) {
            is AuthState.Authenticated -> navController.navigate(Routes.INCIDENTS) { popUpTo(Routes.LOGIN) { inclusive = true } }
            is AuthState.GoogleRegistrating -> navController.navigate(Routes.GOOGLE_REGISTRATING) {
                popUpTo(
                    Routes.LOGIN
                ) { inclusive = true }
            }
            is AuthState.Unauthenticated -> navController.navigate(Routes.LOGIN) { popUpTo(Routes.LOGIN) { inclusive = true } }
            is AuthState.Loading -> navController.navigate(Routes.LOGIN) { popUpTo(Routes.LOGIN) { inclusive = true } }
            else -> navController.navigate(Routes.LOGIN) { popUpTo(Routes.LOGIN) { inclusive = true } }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        builder = {
            composable(Routes.LOGIN) {
                LoginPage(navController, authViewModel, settingsViewModel)
            }
            composable(Routes.SIGNUP) {
                SignUpPage(navController, authViewModel, settingsViewModel)
            }
            composable(Routes.PASSWORD_RESET) {
                PasswordResetPage(
                    navController,
                    authViewModel,
                    settingsViewModel,
                    incidentsManager
                )
            }
            composable(Routes.EMAIL_RESET) {
                EmailResetPage(
                    navController,
                    authViewModel,
                    settingsViewModel,
                    incidentsManager
                )
            }
            composable(Routes.GOOGLE_REGISTRATING) {
                GoogleRegistratingPage(
                    navController,
                    authViewModel,
                    settingsViewModel,
                    incidentsManager
                )
            }
            composable(Routes.INCIDENTS) {
                IncidentsPage(
                    navController,
                    authViewModel,
                    settingsViewModel,
                    incidentsManager,
                    commentManager,
                    locationHandler
                )
            }
            composable(Routes.INCIDENT) {
                IncidentPage(
                    authViewModel,
                    navController,
                    settingsViewModel,
                    incidentsManager
                )
            }
            composable(Routes.COMMENTS) {
                CommentsPage(
                    navController,
                    authViewModel,
                    settingsViewModel,
                    incidentsManager,
                    commentManager
                )
            }
            composable(Routes.OTHER_PROFILE) {
                OtherProfilePage(
                    authViewModel,
                    navController,
                    settingsViewModel,
                    commentManager
                )
            }
            composable(Routes.ADD_NEW_INCIDENT) {
                AddNewIncidentPage(
                    navController,
                    authViewModel,
                    settingsViewModel,
                    incidentsManager,
                    locationHandler,
                    commentManager
                )
            }
            composable(Routes.MAP_RADAR) {
                MapRadarPage(
                    navController,
                    authViewModel,
                    settingsViewModel,
                    incidentsManager,
                    locationHandler
                )
            }
            composable(Routes.SELF_PROFILE) {
                SelfProfilePage(
                    navController,
                    authViewModel,
                    settingsViewModel,
                    incidentsManager
                )
            }
            composable(Routes.SETTINGS) {
                SettingsPage(
                    navController,
                    authViewModel,
                    settingsViewModel,
                    incidentsManager
                )
            }
            composable(Routes.ABOUT) {
                AboutPage(
                    navController,
                    authViewModel,
                    settingsViewModel,
                    incidentsManager
                )
            }
        }
    )
}

object Routes {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val PASSWORD_RESET = "password_reset"
    const val EMAIL_RESET = "email_reset"
    const val GOOGLE_REGISTRATING = "google_registrating"
    const val INCIDENTS = "incidents"
    const val INCIDENT = "incident"
    const val COMMENTS = "comments"
    const val OTHER_PROFILE = "other_profile"
    const val ADD_NEW_INCIDENT = "add_new_incident"
    const val MAP_RADAR = "map_radar"
    const val SELF_PROFILE = "self_profile"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
}