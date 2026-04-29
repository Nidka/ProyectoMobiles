package com.example.bd1.feature.auth.domain.usecase

import com.example.bd1.feature.auth.domain.model.AuthResponse
import com.example.bd1.feature.auth.domain.model.RegisterRequest
import com.example.bd1.feature.auth.domain.repository.AuthRepository

class RegisterUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(request: RegisterRequest): AuthResponse {
        return authRepository.register(request)
    }
}
