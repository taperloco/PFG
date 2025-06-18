package com.example.recado.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/**
 * Represent the screens in the app
 */
enum class Routes() {
    Welcome,
    Login,
    Register,
    MainScreen
}

/**
 * Orquestrate screen swapping
 */
@Preview(showBackground = true)
@Composable
fun Navigator(viewModel: ViewModel = viewModel()) {
    // Collect UI state (see UiState dataclass)
    val uiState by viewModel.uiState.collectAsState()
    val navController: NavHostController = rememberNavController()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NavHost(
            navController = navController,
            startDestination = Routes.Welcome.name,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            composable(route = Routes.Welcome.name) {
                Welcome(
                    onLoginButtonClicked = {navController.navigate(Routes.Login.name)},
                    onRegisterButtonClicked = { navController.navigate(Routes.Register.name)})
            }
            composable(route = Routes.Login.name) {
                viewModel.checkToken()
                // If token active go directly to main screen
                LaunchedEffect(uiState.auth_status) {
                    if (uiState.auth_status == AuthStatus.Token_active) {
                        navController.navigate(Routes.MainScreen.name)
                    }
                }
                Login(navController)
            }
            composable(route = Routes.Register.name) {
                Register(navController)
            }
            composable(route = Routes.MainScreen.name) {
                MainScreen(
                    onExitButtonClicked = { navController.navigate(Routes.Welcome.name) }
                )
            }
        }
    }
}