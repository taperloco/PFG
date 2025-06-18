package com.example.recado.network

import kotlinx.serialization.Serializable

@Serializable
data class UpdateResponse(
    val recados: List<Recado>,
    val users: List<User>
)
