package com.example.bd1.feature.profile.domain.repository

import com.example.bd1.feature.profile.domain.model.Profile

interface ProfileRepository {
    suspend fun getProfile(): Profile?
    suspend fun updateProfile(profile: Profile): Boolean
    suspend fun deleteProfile(): Boolean
}
