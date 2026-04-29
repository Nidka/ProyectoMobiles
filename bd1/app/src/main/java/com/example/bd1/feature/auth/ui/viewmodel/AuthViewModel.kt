package com.example.bd1.feature.auth.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bd1.feature.auth.domain.model.AuthResponse
import com.example.bd1.feature.auth.domain.model.LoginRequest
import com.example.bd1.feature.auth.domain.model.RegisterRequest
import com.example.bd1.feature.auth.domain.usecase.LoginUseCase
import com.example.bd1.feature.auth.domain.usecase.LogoutUseCase
import com.example.bd1.feature.auth.domain.usecase.RegisterUseCase
import com.example.bd1.feature.auth.domain.usecase.ResetPasswordUseCase
import com.example.bd1.feature.auth.domain.usecase.UpdateProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val authResponse: AuthResponse? = null
)

class AuthViewModel(
    private val registerUseCase: RegisterUseCase,
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthUiState())
    val authState = _authState.asStateFlow()

    fun register(firstName: String, lastName: String, email: String, password: String, role: String = "estudiante") {
        viewModelScope.launch {
            _authState.value = AuthUiState(isLoading = true)
            val request = RegisterRequest(firstName, lastName, email, password, role)
            val result = registerUseCase(request)
            _authState.value = if (result.success) {
                AuthUiState(isSuccess = true, authResponse = result)
            } else {
                AuthUiState(errorMessage = result.message)
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState(isLoading = true)
            val request = LoginRequest(email, password)
            val result = loginUseCase(request)
            _authState.value = if (result.success) {
                AuthUiState(isSuccess = true, authResponse = result)
            } else {
                AuthUiState(errorMessage = result.message)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _authState.value = AuthUiState(isLoading = true)
            val result = logoutUseCase()
            _authState.value = if (result.success) {
                AuthUiState(isSuccess = true, authResponse = result)
            } else {
                AuthUiState(errorMessage = result.message)
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState(isLoading = true)
            val result = resetPasswordUseCase(email)
            _authState.value = if (result.success) {
                AuthUiState(isSuccess = true, authResponse = result)
            } else {
                AuthUiState(errorMessage = result.message)
            }
        }
    }

    fun updateProfile(firstName: String, lastName: String, phone: String, photoUri: String? = null) {
        viewModelScope.launch {
            _authState.value = AuthUiState(isLoading = true)
            val result = updateProfileUseCase(firstName, lastName, phone, photoUri)
            _authState.value = if (result.success) {
                AuthUiState(isSuccess = true, authResponse = result)
            } else {
                AuthUiState(errorMessage = result.message)
            }
        }
    }

    fun clearState() {
        _authState.value = AuthUiState()
    }
}
