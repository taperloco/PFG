package com.example.recado.network

import kotlinx.serialization.Serializable

@Serializable
data class Recado(
    val recado_id: String,
    val text: String,
    val creator_id: String,
    val creator_name: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: String
)

@Serializable
data class Message(
    val Name: String,
    val Text: String,
    val Date: String
)

