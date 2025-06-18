package com.example.recado.data

import com.example.recado.network.NetworkService
import com.example.recado.network.Recado
import com.example.recado.network.User

/**
 *  Exposes data to the UI layer and fetches it from network and local services.
 */
class DataRepository (private val locationProvider: LocationProvider, private val tokenManager: TokenManager) {
    private val networkService = NetworkService()
    var recados: List<Recado> = emptyList()
    var users: List<User> = emptyList()

    // 1. Network calls
    suspend fun updateData(latitude: Double, longitude: Double, map_radio: Double): Boolean {
            val data = networkService.updateData(tokenManager.getToken()!!,latitude,longitude, map_radio)
            if (data != null) {
                recados = data.recados
                users = data.users
                return true
            } else {
                // Refresh token
                refreshToken(tokenManager.getToken()!!)
                return false
            }
    }

    suspend fun refreshToken(old_token: String) {
        val token = networkService.refreshToken(old_token)
        if(token != null){
            tokenManager.saveToken(token)
        }
    }

    fun eraseToken() {
        tokenManager.saveToken(null)
    }

    suspend fun login(email: String, password: String): Boolean{
        val token = networkService.login(email, password)
        if(token == null){
            return false
        } else{
            tokenManager.saveToken(token)
            return true
        }
    }

    suspend fun registerUser(name: String, email: String, password: String): Boolean {
        val token = networkService.registerUser(name, email, password)
        if(token == null){
            return false
        } else{
            tokenManager.saveToken(token)
            return true
        }
    }

    fun putRecado(
        mensaje: String,
        latitude: Double,
        longitude: Double
    ) {
        if(tokenManager.getToken()!=null) {
            networkService.putRecado(tokenManager.getToken()!!, mensaje, latitude, longitude)
        }
    }

    fun putChatMessage(
        text: String,
        send_to: String
    ) {
        if(tokenManager.getToken()!=null) {
            networkService.putChatMessage(tokenManager.getToken()!!, text, send_to)
        }
    }

    suspend fun getChat(send_to: String): String {
        if(tokenManager.getToken()!=null) {
            return networkService.getChat(tokenManager.getToken()!!, send_to)
        }
        else{
            return ""
        }
    }

    // 2. Local calls
    fun checkToken(): Boolean {
        val token = tokenManager.getToken()
        // Token not found, user not identified
        if(token == null){
            return false
        }
        return true
    }

    suspend fun getLocation(): Pair<Double?, Double?> {
        val location = locationProvider.getLocation()
        if (location != null) {
            return Pair(location.latitude, location.longitude)
        } else {
            return Pair(null, null)
        }
    }
}
