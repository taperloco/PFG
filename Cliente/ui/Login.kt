package com.example.recado.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

/**
 *  Contains the login menu.
 */
@Composable
fun Login(navController: NavController, viewModel: ViewModel = viewModel()) {
    // Collect UI state (see UiState dataclass)
    val uiState by viewModel.uiState.collectAsState()

    var email by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }

    // If logged in go to main screen
    LaunchedEffect(uiState.auth_status) {
        if (uiState.auth_status == AuthStatus.Token_active) {
            navController.navigate(Routes.MainScreen.name)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SectionCard(title = "Inicia sesión en Recado") {
            TextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Correo electrónico") }
            )

            TextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                visualTransformation = PasswordVisualTransformation(),
                placeholder = { Text("Contraseña") }
            )

            Button(
                onClick = {
                    viewModel.login(email.text, password.text)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("Enviar")
            }

        }
    }
}