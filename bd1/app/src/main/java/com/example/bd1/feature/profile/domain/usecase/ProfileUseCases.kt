package com.example.bd1.feature.profile.domain.usecase

import com.example.bd1.feature.profile.domain.model.Profile
import com.example.bd1.feature.profile.domain.repository.ProfileRepository

class GetProfileUseCase(private val repository: ProfileRepository) {
    suspend operator fun invoke(): Profile? = repository.getProfile()
}

class UpdateProfileUseCase(private val repository: ProfileRepository) {
    suspend operator fun invoke(profile: Profile): Boolean = repository.updateProfile(profile)
}

class DeleteProfileUseCase(private val repository: ProfileRepository) {
    suspend operator fun invoke(): Boolean = repository.deleteProfile()
}
