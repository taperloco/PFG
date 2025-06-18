package com.example.recado.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

/**
 *  Screen to register a new user
 */
@Composable
fun Register(navController: NavController, viewModel: ViewModel = viewModel()) {
    // Show politica de privacidad
    var showPolitica by remember { mutableStateOf(true) }

    // Variables to save user input
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if(showPolitica) {
            AlertDialog(
                title = {
                    Text(text = "Política de Privacidad")
                },
                text = {
                    Text(
                        text = "De acuerdo con Ley Orgánica 3/2018, de 5 de diciembre, de Protección de Datos Personales y garantía de los derechos digitales (LOPD-GDD) " +
                                "le informamos de que al registrarte acepta que sus datos personales seran utilizados y almacenados según lo requerido para el servicio " +
                                "(Acceso a internet, a ubicación y tratamiento de sus datos personales). Para más detalles lea nuestra Política de Privacidad."
                    )
                },
                onDismissRequest = {
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showPolitica = false
                        }
                    ) {
                        Text("Aceptar")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            navController.navigate(Routes.Welcome.name)
                        }
                    ) {
                        Text("Rechazar")
                    }
                }
            )
        }

        SectionCard(title = "Registrar un nuevo usuario") {
            TextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Nombre") }
            )

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
                    viewModel.registerUser(name, email, password)
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