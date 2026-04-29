package com.example.bd1.feature.auth.domain.model

data class User(
    val id: String = "",
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String = "",
    val photoUri: String = "",
    val role: String = "estudiante"
)
