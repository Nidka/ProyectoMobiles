package com.example.bd1.feature.profile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bd1.feature.profile.domain.model.Profile
import com.example.bd1.feature.profile.domain.usecase.GetProfileUseCase
import com.example.bd1.feature.profile.domain.usecase.UpdateProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: Profile? = null,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class ProfileViewModel(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase
) : ViewModel() {

    private val _profileState = MutableStateFlow(ProfileUiState())
    val profileState = _profileState.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileUiState(isLoading = true)
            try {
                val profile = getProfileUseCase()
                _profileState.value = ProfileUiState(profile = profile)
            } catch (e: Exception) {
                _profileState.value = ProfileUiState(errorMessage = e.message ?: "Error al cargar perfil")
            }
        }
    }

    fun updateProfile(profile: Profile) {
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(isLoading = true)
            try {
                val success = updateProfileUseCase(profile)
                if (success) {
                    _profileState.value = ProfileUiState(profile = profile, isSuccess = true)
                } else {
                    _profileState.value = _profileState.value.copy(errorMessage = "Error al actualizar perfil")
                }
            } catch (e: Exception) {
                _profileState.value = _profileState.value.copy(errorMessage = e.message ?: "Error")
            }
        }
    }

    fun clearState() {
        _profileState.value = ProfileUiState()
    }
}
