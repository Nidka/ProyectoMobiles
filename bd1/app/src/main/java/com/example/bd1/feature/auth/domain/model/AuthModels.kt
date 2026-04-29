package com.example.bd1.feature.auth.domain.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val phone: String = "",
    val role: String = "estudiante"
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val user: User? = null
)
