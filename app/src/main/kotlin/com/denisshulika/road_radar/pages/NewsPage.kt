package com.denisshulika.road_radar.pages

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.denisshulika.road_radar.AuthState
import com.denisshulika.road_radar.AuthViewModel
import com.denisshulika.road_radar.Routes
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NewsPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current

    val auth : FirebaseAuth = FirebaseAuth.getInstance()
    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.Unauthenticated -> navController.navigate(Routes.LOGIN)
            else -> Unit
        }
    }

    Column(
        modifier = modifier
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                authViewModel.signout()
            }
        ) {
            Text(text = "SignOut")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (auth.currentUser != null) {
                    authViewModel.deleteAccount(context = context, auth.currentUser!!)
                } else {
                    Toast.makeText(
                        context,
                        "No account logged in",
                        Toast.LENGTH_LONG)
                        .show()
                }
            }
        ) {
            Text(text = "Sell your soul (delete account)")
        }
    }
}