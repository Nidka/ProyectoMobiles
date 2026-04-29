package com.example.bd1.feature.profile.data.repository

import com.example.bd1.feature.auth.data.datasource.AuthLocalDataSourceImpl
import com.example.bd1.feature.profile.domain.model.Profile
import com.example.bd1.feature.profile.domain.repository.ProfileRepository

class ProfileRepositoryImpl(
    private val authLocalDataSource: AuthLocalDataSourceImpl
) : ProfileRepository {

    override suspend fun getProfile(): Profile? {
        val userDto = authLocalDataSource.getCurrentUser() ?: return null
        return Profile(
            firstName = userDto.firstName,
            lastName = userDto.lastName,
            email = userDto.email,
            phone = userDto.phone,
            photoUri = userDto.photoUri
        )
    }

    override suspend fun updateProfile(profile: Profile): Boolean {
        return try {
            val currentUser = authLocalDataSource.getCurrentUser() ?: return false
            val updatedUser = currentUser.copy(
                firstName = profile.firstName,
                lastName = profile.lastName,
                phone = profile.phone,
                photoUri = profile.photoUri
            )
            authLocalDataSource.saveUser(updatedUser)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteProfile(): Boolean {
        return try {
            val currentEmail = authLocalDataSource.getCurrentUserEmail() ?: return false
            authLocalDataSource.deleteUser(currentEmail)
            authLocalDataSource.clearCurrentUser()
            true
        } catch (e: Exception) {
            false
        }
    }
}
