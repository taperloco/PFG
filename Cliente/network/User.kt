package com.example.recado.network

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val user_id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double
)