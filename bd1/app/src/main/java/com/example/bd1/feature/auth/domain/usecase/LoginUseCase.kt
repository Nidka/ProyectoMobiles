package com.example.bd1.feature.auth.domain.usecase

import com.example.bd1.feature.auth.domain.model.AuthResponse
import com.example.bd1.feature.auth.domain.model.LoginRequest
import com.example.bd1.feature.auth.domain.repository.AuthRepository

class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(request: LoginRequest): AuthResponse {
        return authRepository.login(request)
    }
}
