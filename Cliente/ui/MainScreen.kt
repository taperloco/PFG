package com.example.recado.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recado.network.Recado

/**
 *  Contains the main menu, overlaid on top of the Open Street Map.
 */
@Preview(showBackground = true)
@Composable
fun MainScreen(viewModel: ViewModel = viewModel(), onExitButtonClicked: () -> Unit = {}) {
    // Collect UI state (see UiState dataclass)
    val uiState by viewModel.uiState.collectAsState()

    // Screen size
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp

    // When this screen is active, viewModel periodically updates the data
    DisposableEffect(Unit) {
        viewModel.startPeriodicUpdates()
        onDispose {
            viewModel.continueUpdating = false
        }
    }

    Box(
        modifier = Modifier.size(width = screenWidthDp.dp, height = screenHeightDp.dp)
    ) {
        // Check access to location
        if (uiState.latitude == 0.0) {
            Card(modifier = Modifier
                .wrapContentSize()
                .align(Alignment.Center)
                .offset(y = (+100).dp)
            ) {Text(text = "Cargando ubicación.") }
        } else{
                // MAP SECTION. It needs access to the context and the state data
                val context = LocalContext.current
                MapSection(context, uiState)

                // Check access to server
                if (!uiState.server_available) {
                    Card(modifier = Modifier
                        .wrapContentSize()
                        .align(Alignment.Center)
                        .offset(y = (+100).dp)
                    ) {Text(text = "Conectando con el servidor.") }
                }
        }

        // USER INTERFACE. The user interface is overlapped on the map
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Fixed buttons in the bottom of the screen
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding((screenHeightDp * 0.05f).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row {
                    ElevatedButton(
                        onClick = {
                            if (uiState.current_dialog == DialogType.NewRecado) {
                                viewModel.updateCurrentDialog(DialogType.None)
                            } else {
                                viewModel.updateCurrentDialog(DialogType.NewRecado)
                            }
                        },
                        modifier = Modifier
                            .padding(10.dp)
                    ) {
                        Text("Nuevo Recado")
                    }
                }
                Row {
                    Button(
                        onClick = {
                            if (uiState.show_settings) {
                                viewModel.showSettings(false)
                            } else {
                                viewModel.showSettings(true)
                            }
                        },
                        modifier = Modifier
                            .width((screenWidthDp * 0.2f).dp),
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Text(
                            "Settings",
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Button(
                        onClick = { viewModel.centerMap(true) },
                        modifier = Modifier
                            .padding(1.dp)
                            .width((screenWidthDp * 0.2f).dp),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "Centrar",
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Button(
                        onClick = onExitButtonClicked,
                        modifier = Modifier
                            .padding(1.dp)
                            .width((screenWidthDp * 0.2f).dp),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "Salir",
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 2. Filter section on the right of the screen
            if (uiState.show_settings) {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width((screenWidthDp * 0.15f).dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SectionCard(title = "Settings:") {
                        Text(
                            "Cliente",
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Switch(
                            checked = uiState.show_client,
                            onCheckedChange = { isChecked ->
                                viewModel.filterIcons("Client", isChecked)
                            }
                        )
                        Text(
                            "Usuarios",
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Switch(
                            checked = uiState.show_users,
                            onCheckedChange = { isChecked ->
                                viewModel.filterIcons("Users", isChecked)
                            }
                        )
                        Text(
                            "Recados",
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Switch(
                            checked = uiState.show_recados,
                            onCheckedChange = { isChecked ->
                                viewModel.filterIcons("Recados", isChecked)
                            }
                        )
                        Text(
                            "Radio",
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "${uiState.map_radio.toInt()} m",
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Box(
                            modifier = Modifier
                                .rotate(-90f),
                            contentAlignment = Alignment.Center
                        ) {
                            Slider(
                                value = uiState.map_radio.toFloat(),
                                onValueChange = { viewModel.updateRadio(it) },
                                valueRange = 25f..75f,
                                steps = 1
                            )
                        }

                    }

                }
            }

            // 3. Dialogs that depend on user interactions on the top
            if (uiState.current_dialog == DialogType.NewRecado) {
                NewRecado { text ->
                    viewModel.newRecado(text)
                }
            } else if (uiState.current_dialog == DialogType.Chat) {
                Chat(uiState.chat, uiState.selected_user_name) { text ->
                    viewModel.newChatMessage(text, uiState.selected_user)
                }
            } else if (uiState.current_dialog == DialogType.ShowRecado) {
                uiState.recados.find { it.recado_id == uiState.selected_recado }?.let { recado ->
                    ShowRecado(recado)
                }
            }
        }
    }
}

/**
 *  Form to create a new Recado in the current position of the user
 */
@Composable
fun NewRecado(onSubmit: (String) -> Unit) {
    var inputText by remember { mutableStateOf("") }

    SectionCard(title = "Dejar Recado en tu ubicación:") {
        TextField(
            value = inputText,
            onValueChange = { inputText = it },
            //modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Escribe tu Recado aquí...") }
        )
        Button(
            onClick = { onSubmit(inputText) },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("Fijar recado")
        }
    }
}

/**
 *  Shows the current selected Recado
 */
@Composable
fun ShowRecado (recado: Recado) {
    SectionCard(title = "Recado: ") {
        Text(
            text = recado.text,
            modifier = Modifier
                .background(Color(0xFFFDFDFD))
                .wrapContentWidth()
                .padding(8.dp)
                .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                .verticalScroll(rememberScrollState())
        )
        Text(
            text = "Creador: " + recado.creator_name,
        )
        Text(
            text = "Fecha: " + recado.timestamp,
        )
    }
}

/**
 *  Shows the chat with the current selected user.
 */
@Composable
fun Chat (chat: String, userName: String, onSubmit: (String) -> Unit) {
    var inputText by remember { mutableStateOf("") }

    SectionCard(title = "Chat con $userName:") {
        Text(
            text = chat,
            modifier = Modifier
                .background(Color(0xFFFDFDFD))
                .wrapContentWidth()
                .verticalScroll(rememberScrollState())
                .padding(8.dp)
                .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))

        )
        TextField(
            value = inputText,
            onValueChange = { inputText = it },
            placeholder = { Text("Escribe tu mensaje aquí...") }
        )
        Button(
            onClick = { onSubmit(inputText) },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("Enviar mensaje al chat")
        }
    }
}

/**
 *  Helper composable to create responsive dialogs
 */
@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    // Screen size
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp
    var maxWidth = screenWidthDp
    var maxHeight = (screenHeightDp * 0.6f).toInt()
    if(screenWidthDp>600){
        maxWidth =  (screenWidthDp * 0.6f).toInt()
    }
    if(screenHeightDp>480){
        maxHeight =  (screenHeightDp * 0.4f).toInt()
    }
    Box(
        modifier = Modifier
            .padding(top = (screenHeightDp * 0.01f).dp, start = (screenWidthDp * 0.01f).dp)
            .background(Color("#FDFDFD".toColorInt()), RoundedCornerShape(8.dp))
            .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
            .padding((screenHeightDp * 0.01f).dp)
            .widthIn(max = maxWidth.dp)
            .heightIn(max = maxHeight.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = title)
            content()
        }
    }
}

