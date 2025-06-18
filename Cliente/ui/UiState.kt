package com.example.recado.ui

import com.example.recado.network.Recado
import com.example.recado.network.User

/**
 *  Stores and represents the state of the user interface.
 */
data class UiState(
    // User data
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,

    // Current data
    val recados: List<Recado> = emptyList(),
    val users: List<User> = emptyList(),
    val chat: String = "Empty",
    val server_available: Boolean = false,

    // User interaction
    val selected_user: String = "Empty",
    val selected_user_name: String = "Empty",
    val selected_recado: String = "Empty",
    val clicked_users: List<String> = emptyList(),
    val clicked_recados: List<String> = emptyList(),
    val current_dialog: DialogType = DialogType.None,
    val center_map: Boolean = false,
    val map_radio: Double = 50.0,
    val show_settings: Boolean = true,
    val show_client: Boolean = true,
    val show_users: Boolean = true,
    val show_recados: Boolean = true,

    // Token track
    val auth_status: AuthStatus = AuthStatus.Token_missing,

    ){
    companion object {
        // For resets
        val DEFAULT = UiState()
    }
}

// Current dialog opened
enum class DialogType {
    None,
    Chat,
    NewRecado,
    ShowRecado
}

enum class AuthStatus {
    Token_missing,
    Token_active,
}

