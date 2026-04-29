package com.example.bd1.feature.auth.data.model

data class UserDto(
    val id: String = "",
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String = "",
    val photoUri: String = "",
    val role: String = "estudiante"
)
