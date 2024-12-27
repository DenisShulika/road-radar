package com.denisshulika.road_radar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.denisshulika.road_radar.pages.GoogleRegistratingPage
import com.denisshulika.road_radar.pages.IncidentsPage
import com.denisshulika.road_radar.pages.LoginPage
import com.denisshulika.road_radar.pages.PasswordResetPage
import com.denisshulika.road_radar.pages.SignUpPage

@Composable
fun RoadRadarNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel
) {
    val navController = rememberNavController()

    LaunchedEffect(authViewModel.authState.value) {
        when (authViewModel.authState.value) {
            is AuthState.Authenticated -> navController.navigate(Routes.INCIDENTS) { popUpTo(Routes.LOGIN) { inclusive = true } }
            is AuthState.Registrating -> navController.navigate(Routes.GOOGLE_REGISTRATING) { popUpTo(Routes.LOGIN) { inclusive = true } }
            is AuthState.Unauthenticated -> navController.navigate(Routes.LOGIN) { popUpTo(Routes.LOGIN) { inclusive = true } }
            else -> navController.navigate(Routes.LOGIN) { popUpTo(Routes.LOGIN) { inclusive = true } }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        builder = {
            composable(Routes.LOGIN) {
                LoginPage(modifier, navController, authViewModel)
            }
            composable(Routes.SIGNUP) {
                SignUpPage(modifier, navController, authViewModel)
            }
            composable(Routes.PASSWORD_RESET) {
                PasswordResetPage(modifier, navController, authViewModel)
            }
            composable(Routes.INCIDENTS) {
                IncidentsPage(modifier, navController, authViewModel)
            }
            composable(Routes.GOOGLE_REGISTRATING) {
                GoogleRegistratingPage(modifier, navController, authViewModel)
            }
        }
    )
}

object Routes {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val PASSWORD_RESET = "password_reset"
    const val INCIDENTS = "incidents"
    const val GOOGLE_REGISTRATING = "google_registrating"
}