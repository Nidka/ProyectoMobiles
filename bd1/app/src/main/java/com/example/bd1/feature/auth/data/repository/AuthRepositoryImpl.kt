package com.example.bd1.feature.auth.data.repository

import com.example.bd1.core.common.ValidationUtils
import com.example.bd1.core.utils.await
import com.example.bd1.feature.auth.data.datasource.AuthLocalDataSourceImpl
import com.example.bd1.feature.auth.data.model.UserDto
import com.example.bd1.feature.auth.domain.model.AuthResponse
import com.example.bd1.feature.auth.domain.model.LoginRequest
import com.example.bd1.feature.auth.domain.model.RegisterRequest
import com.example.bd1.feature.auth.domain.model.User
import com.example.bd1.feature.auth.domain.repository.AuthRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val localDataSource: AuthLocalDataSourceImpl,
    private val validationUtils: ValidationUtils
) : AuthRepository {

    override suspend fun register(request: RegisterRequest): AuthResponse {
        // Validar datos
        val validation = validationUtils.validateRegisterData(
            firstName = request.firstName,
            lastName = request.lastName,
            email = request.email,
            phone = "",
            password = request.password
        )

        if (!validation.success) {
            return AuthResponse(false, validation.message)
        }

        return try {
            firebaseAuth.createUserWithEmailAndPassword(
                request.email.lowercase(),
                request.password
            ).await()

            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                firebaseUser.sendEmailVerification().await()
            }

            val userDto = UserDto(
                id = firebaseAuth.currentUser?.uid ?: "",
                firstName = request.firstName,
                lastName = request.lastName,
                email = request.email.lowercase(),
                phone = request.phone,
                photoUri = "",
                role = request.role,
                isEmailVerified = false
            )
            localDataSource.saveUser(userDto)
            // No establecer como usuario actual hasta verificar correo
            AuthResponse(true, "Registro exitoso. Por favor verifica tu correo para continuar.", userDto.toModel())
        } catch (e: Exception) {
            AuthResponse(false, e.message ?: "Error en el registro")
        }
    }

    override suspend fun login(request: LoginRequest): AuthResponse {
        val validation = validationUtils.validateLoginData(
            email = request.email,
            password = request.password
        )

        if (!validation.success) {
            return AuthResponse(false, validation.message)
        }

        return try {
            firebaseAuth.signInWithEmailAndPassword(
                request.email.lowercase(),
                request.password
            ).await()

            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                // Verificar si el correo está verificado
                firebaseUser.reload().await()
                if (!firebaseUser.isEmailVerified) {
                    return AuthResponse(false, "Por favor verifica tu correo antes de continuar. Revisa tu bandeja de entrada.")
                }

                val userEmail = firebaseUser.email?.lowercase() ?: ""
                var userDto = localDataSource.getUserByEmail(userEmail)
                
                // Si no existe localmente, pero existe en Firebase, crear registro local
                if (userDto == null) {
                    userDto = UserDto(
                        id = firebaseUser.uid,
                        firstName = firebaseUser.displayName?.split(" ")?.firstOrNull() ?: "Usuario",
                        lastName = firebaseUser.displayName?.split(" ")?.drop(1)?.joinToString(" ") ?: "",
                        email = userEmail,
                        phone = "",
                        photoUri = "",
                        role = "estudiante",
                        isEmailVerified = true
                    )
                    localDataSource.saveUser(userDto)
                } else {
                    // Actualizar estado de verificación
                    val updatedUserDto = userDto.copy(isEmailVerified = true)
                    localDataSource.saveUser(updatedUserDto)
                    userDto = updatedUserDto
                }
                
                localDataSource.setCurrentUserEmail(userEmail)
                AuthResponse(true, "Bienvenido", userDto.toModel())
            } else {
                AuthResponse(false, "Usuario no encontrado")
            }
        } catch (e: Exception) {
            // Si falla en Firebase, intenta login local (para testing offline)
            val localUser = localDataSource.getUserByEmail(request.email.lowercase())
            return if (localUser != null && localUser.isEmailVerified) {
                localDataSource.setCurrentUserEmail(localUser.email)
                AuthResponse(true, "Bienvenido (modo offline)", localUser.toModel())
            } else if (localUser != null) {
                AuthResponse(false, "Por favor verifica tu correo antes de continuar.")
            } else {
                AuthResponse(false, "Credenciales incorrectas")
            }
        }
    }

    override suspend fun logout(): AuthResponse {
        return try {
            firebaseAuth.signOut()
            localDataSource.clearCurrentUser()
            AuthResponse(true, "Sesión cerrada")
        } catch (e: Exception) {
            AuthResponse(false, e.message ?: "Error al cerrar sesión")
        }
    }

    override suspend fun getCurrentUser(): User? {
        val email = localDataSource.getCurrentUserEmail() ?: return null
        return localDataSource.getUserByEmail(email)?.toModel()
    }

    override suspend fun resetPassword(email: String): AuthResponse {
        return try {
            firebaseAuth.sendPasswordResetEmail(email.lowercase()).await()
            AuthResponse(true, "Correo de recuperación enviado.")
        } catch (e: Exception) {
            AuthResponse(false, e.message ?: "Error al enviar correo")
        }
    }

    override suspend fun updateProfile(
        firstName: String,
        lastName: String,
        phone: String,
        photoUri: String?
    ): AuthResponse {
        return try {
            val currentEmail = localDataSource.getCurrentUserEmail() ?: return AuthResponse(false, "No hay sesión")
            val user = localDataSource.getUserByEmail(currentEmail)?.copy(
                firstName = firstName,
                lastName = lastName,
                phone = phone,
                photoUri = photoUri ?: ""
            ) ?: return AuthResponse(false, "Usuario no encontrado")
            
            localDataSource.saveUser(user)
            AuthResponse(true, "Perfil actualizado", user.toModel())
        } catch (e: Exception) {
            AuthResponse(false, e.message ?: "Error al actualizar perfil")
        }
    }

    private fun UserDto.toModel() = User(
        id = id,
        firstName = firstName,
        lastName = lastName,
        email = email,
        phone = phone,
        photoUri = photoUri,
        role = role
    )
}
