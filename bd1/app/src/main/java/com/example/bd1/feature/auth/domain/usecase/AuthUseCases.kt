package com.example.bd1.feature.auth.domain.usecase

import com.example.bd1.feature.auth.domain.model.AuthResponse
import com.example.bd1.feature.auth.domain.model.User
import com.example.bd1.feature.auth.domain.repository.AuthRepository

class LogoutUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(): AuthResponse {
        return authRepository.logout()
    }
}

class ResetPasswordUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String): AuthResponse {
        return authRepository.resetPassword(email)
    }
}

class UpdateProfileUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(
        firstName: String,
        lastName: String,
        phone: String,
        photoUri: String?
    ): AuthResponse {
        return authRepository.updateProfile(firstName, lastName, phone, photoUri)
    }
}

class GetCurrentUserUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(): User? {
        return authRepository.getCurrentUser()
    }
}
