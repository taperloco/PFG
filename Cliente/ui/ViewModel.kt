package com.example.recado.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recado.data.LocationProvider
import com.example.recado.data.DataRepository
import com.example.recado.data.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 *  Handles the data for UI. Manages UI state and coordinates data operations and business logic.
 */
class ViewModel (application: Application) : AndroidViewModel(application) {
    // Create location and token helpers
    private val locationProvider = LocationProvider(application.applicationContext)
    private val tokenManager = TokenManager(application.applicationContext)
    // Create the repository and pass the helpers
    private val repository = DataRepository(locationProvider, tokenManager)

    // Ui state
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // 1. REPOSITORY CALLS
    // Variable to track when to stop requiring info from the server
    var continueUpdating: Boolean = true
    fun startPeriodicUpdates() {
        // Launch local location updates
        viewModelScope.launch {
            while (continueUpdating) {
                val (lat, lon) = repository.getLocation()
                _uiState.update { currentState ->
                    currentState.copy(
                        latitude = lat ?: 0.0,
                        longitude = lon ?: 0.0
                    )
                }
                kotlinx.coroutines.delay(2000)
            }
        }
        // Launch data updating from server
        viewModelScope.launch {
            while (continueUpdating) {
                // The location of the user must be known to be passed to the server
                if(_uiState.value.latitude != 0.0){
                    // Updata repository data
                    val success = repository.updateData(
                        _uiState.value.latitude,
                        _uiState.value.longitude,
                        _uiState.value.map_radio)
                    if(success){
                        // Update view state
                        val updateRecados = repository.recados
                        val updateUsers = repository.users
                        _uiState.update { currentState ->
                            currentState.copy(
                                recados = updateRecados,
                                users = updateUsers,
                                server_available = true
                            )
                        }
                    } else {
                        _uiState.update { currentState ->
                            UiState.DEFAULT.copy(
                                latitude = currentState.latitude,
                                longitude = currentState.longitude,
                            )
                        }
                    }
                }
                kotlinx.coroutines.delay(5000)
            }
        }
    }

    fun registerUser(name: String, email: String, password: String){
        viewModelScope.launch {
            val success = repository.registerUser(name, email, password)
            Log.e("TOKEN", "Success $success")
            if (success) {
                Log.e("TOKEN", "ACTIVE")
                _uiState.update { currentState ->
                    currentState.copy(auth_status = AuthStatus.Token_active)
                }
            } else {
                Log.e("TOKEN", "MISSING")
                _uiState.update { currentState ->
                    currentState.copy(auth_status = AuthStatus.Token_missing)
                }
            }
        }
    }

    fun login(email: String, password: String){
        viewModelScope.launch {
            val success = repository.login(email, password)
            Log.e("TOKEN", "Success $success")
            if (success) {
                Log.e("TOKEN", "ACTIVE")
                _uiState.update { currentState ->
                    currentState.copy(auth_status = AuthStatus.Token_active)
                }
            } else {
                Log.e("TOKEN", "MISSING")
                _uiState.update { currentState ->
                    currentState.copy(auth_status = AuthStatus.Token_missing)
                }
            }
        }
    }

    fun logout(){
        repository.eraseToken()
        viewModelScope.launch {
            _uiState.update {  currentState ->
                UiState.DEFAULT.copy()
            }
        }
    }

    fun checkToken():Boolean {
        if(repository.checkToken()){
            _uiState.update { currentState ->
                currentState.copy(
                    auth_status = AuthStatus.Token_active)
            }
            return true
        } else{
            _uiState.update { currentState ->
            currentState.copy(
                auth_status = AuthStatus.Token_missing)
            }
            return false
        }
    }

    fun newRecado(text: String) {
        viewModelScope.launch {
            repository.putRecado(
                text,
                _uiState.value.latitude,
                _uiState.value.longitude
            )
        }
    }

    fun getChat(send_to: String) {
        viewModelScope.launch {
            val currentChat = repository.getChat(send_to)
            _uiState.update { currentState ->
                currentState.copy(chat = currentChat)
            }
        }
    }

    fun newChatMessage(text: String, send_to: String) {
        viewModelScope.launch {
            repository.putChatMessage(
                text,
                send_to
            )
        }
    }

    // 2. UI STATUS UPDATES
    fun updateSelectedUser(user_id: String, name: String) {
        _uiState.update { currentState ->
            if (!_uiState.value.clicked_users.isEmpty() &&
                _uiState.value.clicked_users.contains(user_id)){
                currentState.copy(selected_user = user_id, selected_user_name = name)
            }else{
                currentState.copy(selected_user = user_id,
                    selected_user_name = name,
                    clicked_users = currentState.clicked_users + user_id)
            }
        }
    }

    fun updateSelectedRecado(recado_id: String) {
        _uiState.update { currentState ->
            if (!_uiState.value.clicked_recados.isEmpty() &&
                _uiState.value.clicked_recados.contains(recado_id)){
                currentState.copy(selected_recado = recado_id)
            }else{
                currentState.copy(selected_recado = recado_id,
                    clicked_recados = currentState.clicked_recados + recado_id)
            }
        }
    }

    fun centerMap(value: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(center_map = value)
        }
    }

    fun filterIcons(show_type: String, set_to: Boolean) {
        if(show_type=="Client"){
            _uiState.update { currentState ->
                currentState.copy(show_client = set_to)
            }
        }else if(show_type=="Recados"){
            _uiState.update { currentState ->
                currentState.copy(show_recados = set_to)
            }
        }else if(show_type=="Users") {
            _uiState.update { currentState ->
                currentState.copy(show_users = set_to)
            }
        }
    }

    fun updateRadio(new_radio: Float) {
        _uiState.update { currentState ->
            currentState.copy(map_radio = new_radio.toDouble())
        }
    }

    fun showSettings(value: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(show_settings = value)
        }
    }

    fun updateCurrentDialog(dialog: DialogType) {
        _uiState.update { currentState ->
            currentState.copy(current_dialog = dialog)
        }
    }
}
