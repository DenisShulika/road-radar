package com.denisshulika.road_radar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.denisshulika.road_radar.pages.LoginPage
import com.denisshulika.road_radar.pages.NewsPage
import com.denisshulika.road_radar.pages.PasswordResetPage
import com.denisshulika.road_radar.pages.SignUpPage

@Composable
fun RoadRadarNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination =
            if (authViewModel.authState.value is AuthState.Authenticated) {
                Routes.NEWS
            } else {
                Routes.LOGIN
            },
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
            composable(Routes.NEWS) {
                NewsPage(modifier, navController, authViewModel)
            }
        }
    )
}

object Routes {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val PASSWORD_RESET = "password_reset"
    const val NEWS = "news"
}