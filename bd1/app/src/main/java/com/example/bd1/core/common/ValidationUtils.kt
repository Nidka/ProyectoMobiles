package com.example.bd1.core.common

data class ValidationResult(
    val success: Boolean,
    val message: String
)

class ValidationUtils {
    fun validateRegisterData(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        password: String
    ): ValidationResult {
        val cleanFirstName = firstName.trim()
        val cleanLastName = lastName.trim()
        val cleanEmail = email.trim().lowercase()
        val cleanPhone = phone.trim().replace(" ", "").replace("-", "").replace("+", "")

        if (cleanFirstName.length < 2) return ValidationResult(false, "Nombres: mínimo 2 caracteres")
        if (cleanLastName.length < 2) return ValidationResult(false, "Apellidos: mínimo 2 caracteres")
        if (!NAME_REGEX.matches(cleanFirstName) || !NAME_REGEX.matches(cleanLastName)) {
            return ValidationResult(false, "Nombres y apellidos: solo letras y espacios")
        }
        if (!EMAIL_REGEX.matches(cleanEmail)) return ValidationResult(false, "Ingresa un correo válido")
        if (cleanPhone.isNotEmpty() && !PHONE_REGEX.matches(cleanPhone)) return ValidationResult(false, "Teléfono inválido")
        if (!PASSWORD_REGEX.matches(password)) {
            return ValidationResult(false, "La contraseña debe tener 8+ caracteres, mayúscula, número y símbolo")
        }
        return ValidationResult(true, "Válido")
    }

    fun validateLoginData(email: String, password: String): ValidationResult {
        val cleanEmail = email.trim().lowercase()
        val cleanPassword = password.trim()

        if (cleanEmail.isEmpty()) return ValidationResult(false, "Ingresa tu correo electrónico")
        if (cleanPassword.isEmpty()) return ValidationResult(false, "Ingresa tu contraseña")
        if (!EMAIL_REGEX.matches(cleanEmail)) return ValidationResult(false, "Ingresa un correo válido")

        return ValidationResult(true, "Válido")
    }

    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        private val NAME_REGEX = Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$")
        private val PHONE_REGEX = Regex("^[0-9]{9,15}$")
        private val PASSWORD_REGEX = Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$")
    }
}
