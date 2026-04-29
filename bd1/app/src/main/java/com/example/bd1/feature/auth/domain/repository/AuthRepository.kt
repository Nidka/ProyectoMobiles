package com.example.bd1.feature.auth.domain.repository

import com.example.bd1.feature.auth.domain.model.AuthResponse
import com.example.bd1.feature.auth.domain.model.LoginRequest
import com.example.bd1.feature.auth.domain.model.RegisterRequest
import com.example.bd1.feature.auth.domain.model.User

interface AuthRepository {
    suspend fun register(request: RegisterRequest): AuthResponse
    suspend fun login(request: LoginRequest): AuthResponse
    suspend fun logout(): AuthResponse
    suspend fun getCurrentUser(): User?
    suspend fun resetPassword(email: String): AuthResponse
    suspend fun updateProfile(
        firstName: String,
        lastName: String,
        phone: String,
        photoUri: String?
    ): AuthResponse
}
