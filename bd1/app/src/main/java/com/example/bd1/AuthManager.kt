package com.example.bd1

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONArray
import org.json.JSONObject

class AuthManager(context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun validateRegisterData(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        password: String
    ): AuthResult {
        val cleanFirstName = firstName.trim()
        val cleanLastName = lastName.trim()
        val cleanEmail = email.trim().lowercase()
        val cleanPhone = phone.trim().replace(" ", "").replace("-", "").replace("+", "")

        if (cleanFirstName.length < 2) return AuthResult(false, "Nombres: mínimo 2 caracteres")
        if (cleanLastName.length < 2) return AuthResult(false, "Apellidos: mínimo 2 caracteres")
        if (!NAME_REGEX.matches(cleanFirstName) || !NAME_REGEX.matches(cleanLastName)) {
            return AuthResult(false, "Nombres y apellidos: solo letras y espacios")
        }
        if (!EMAIL_REGEX.matches(cleanEmail)) return AuthResult(false, "Ingresa un correo válido")
        if (!PHONE_REGEX.matches(cleanPhone)) return AuthResult(false, "Teléfono inválido")
        if (!PASSWORD_REGEX.matches(password)) {
            return AuthResult(false, "La contraseña debe tener 8+ caracteres, mayúscula, número y símbolo")
        }
        return AuthResult(true, "Válido")
    }

    fun register(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        password: String,
        onResult: (AuthResult) -> Unit
    ) {
        val cleanEmail = email.trim().lowercase()
        auth.createUserWithEmailAndPassword(cleanEmail, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.sendEmailVerification()?.addOnCompleteListener { verifyTask ->
                        if (verifyTask.isSuccessful) {
                            saveUserMetadata(firstName, lastName, cleanEmail, phone)
                            onResult(AuthResult(true, "Registro exitoso. Revisa tu correo para confirmar."))
                        } else {
                            onResult(AuthResult(false, "Error al enviar correo de verificación."))
                        }
                    }
                } else {
                    onResult(AuthResult(false, task.exception?.message ?: "Error en el registro"))
                }
            }
    }

    fun login(email: String, password: String, onResult: (AuthResult) -> Unit) {
        val cleanEmail = email.trim().lowercase()
        
        // Verificación de bloqueo local (opcional con Firebase, pero lo mantenemos si quieres)
        val lockoutKey = KEY_LOCKOUT_UNTIL_PREFIX + cleanEmail
        val lockoutUntil = prefs.getLong(lockoutKey, 0)
        if (System.currentTimeMillis() < lockoutUntil) {
            val rem = (lockoutUntil - System.currentTimeMillis()) / 1000
            onResult(AuthResult(false, "Bloqueado. Intenta en ${rem / 60}m ${rem % 60}s"))
            return
        }

        auth.signInWithEmailAndPassword(cleanEmail, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        prefs.edit().putString(KEY_CURRENT_USER_EMAIL, cleanEmail).apply()
                        onResult(AuthResult(true, "Bienvenido"))
                    } else {
                        onResult(AuthResult(false, "Por favor, verifica tu correo electrónico antes de entrar."))
                    }
                } else {
                    handleLoginFailure(cleanEmail, onResult)
                }
            }
    }

    private fun handleLoginFailure(email: String, onResult: (AuthResult) -> Unit) {
        val attemptsKey = KEY_FAILED_ATTEMPTS_PREFIX + email
        val lockoutKey = KEY_LOCKOUT_UNTIL_PREFIX + email
        val attempts = prefs.getInt(attemptsKey, 0) + 1
        
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            prefs.edit()
                .putLong(lockoutKey, System.currentTimeMillis() + LOCKOUT_DURATION_MS)
                .putInt(attemptsKey, 0)
                .apply()
            onResult(AuthResult(false, "Demasiados intentos. Bloqueado por 3 min."))
        } else {
            prefs.edit().putInt(attemptsKey, attempts).apply()
            onResult(AuthResult(false, "Credenciales incorrectas. Intentos: ${MAX_FAILED_ATTEMPTS - attempts}"))
        }
    }

    private fun saveUserMetadata(fName: String, lName: String, email: String, phone: String) {
        val users = getUsersArray()
        val user = JSONObject().apply {
            put(KEY_USER_FIRST_NAME, fName.trim())
            put(KEY_USER_LAST_NAME, lName.trim())
            put(KEY_USER_EMAIL, email)
            put(KEY_USER_PHONE, phone.trim())
        }
        users.put(user)
        prefs.edit().putString(KEY_USERS_JSON, users.toString()).apply()
    }

    fun isLoggedIn(): Boolean {
        val user = auth.currentUser
        return user != null && user.isEmailVerified
    }

    fun logout() {
        auth.signOut()
        prefs.edit().remove(KEY_CURRENT_USER_EMAIL).apply()
    }

    fun getCurrentUserName(): String {
        val user = getCurrentUserMetadata() ?: return "Usuario"
        return "${user.optString(KEY_USER_FIRST_NAME)} ${user.optString(KEY_USER_LAST_NAME)}".trim()
    }

    fun getCurrentUserFirstName(): String = getCurrentUserMetadata()?.optString(KEY_USER_FIRST_NAME).orEmpty()
    fun getCurrentUserLastName(): String = getCurrentUserMetadata()?.optString(KEY_USER_LAST_NAME).orEmpty()
    fun getCurrentUserPhone(): String = getCurrentUserMetadata()?.optString(KEY_USER_PHONE).orEmpty()
    fun getCurrentUserPhotoUri(): String = getCurrentUserMetadata()?.optString(KEY_USER_PHOTO_URI).orEmpty()

    fun updateProfile(firstName: String, lastName: String, phone: String, photoUri: String?): AuthResult {
        val email = auth.currentUser?.email ?: return AuthResult(false, "No hay sesión")
        val users = getUsersArray()
        
        for (i in 0 until users.length()) {
            val user = users.getJSONObject(i)
            if (user.optString(KEY_USER_EMAIL) == email) {
                user.put(KEY_USER_FIRST_NAME, firstName.trim())
                user.put(KEY_USER_LAST_NAME, lastName.trim())
                user.put(KEY_USER_PHONE, phone.trim())
                if (photoUri != null) user.put(KEY_USER_PHOTO_URI, photoUri)
                
                prefs.edit().putString(KEY_USERS_JSON, users.toString()).apply()
                return AuthResult(true, "Perfil actualizado")
            }
        }
        return AuthResult(false, "Usuario no encontrado")
    }

    fun updatePassword(currentPassword: String, newPassword: String, onResult: (AuthResult) -> Unit) {
        val user = auth.currentUser ?: return onResult(AuthResult(false, "No hay sesión"))
        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(user.email!!, currentPassword)
        
        user.reauthenticate(credential).addOnCompleteListener { reAuthTask ->
            if (reAuthTask.isSuccessful) {
                user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        onResult(AuthResult(true, "Contraseña actualizada"))
                    } else {
                        onResult(AuthResult(false, updateTask.exception?.message ?: "Error al actualizar"))
                    }
                }
            } else {
                onResult(AuthResult(false, "Contraseña actual incorrecta"))
            }
        }
    }

    fun getCurrentUserEmail(): String = auth.currentUser?.email.orEmpty()

    fun getUserPreviewByEmail(email: String): UserPreview? {
        val cleanEmail = email.trim().lowercase()
        val user = findUserMetadataByEmail(cleanEmail) ?: return null
        return UserPreview(
            fullName = "${user.optString(KEY_USER_FIRST_NAME)} ${user.optString(KEY_USER_LAST_NAME)}".trim(),
            photoUri = user.optString(KEY_USER_PHOTO_URI)
        )
    }

    fun resetPassword(email: String, onResult: (AuthResult) -> Unit) {
        auth.sendPasswordResetEmail(email.trim().lowercase())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(AuthResult(true, "Correo de recuperación enviado."))
                } else {
                    onResult(AuthResult(false, task.exception?.message ?: "Error al enviar correo"))
                }
            }
    }

    // Métodos de ayuda para metadata local
    private fun getUsersArray(): JSONArray {
        val raw = prefs.getString(KEY_USERS_JSON, null)
        return if (raw.isNullOrBlank()) JSONArray() else JSONArray(raw)
    }

    private fun findUserMetadataByEmail(email: String): JSONObject? {
        val users = getUsersArray()
        for (i in 0 until users.length()) {
            val u = users.getJSONObject(i)
            if (u.optString(KEY_USER_EMAIL) == email) return u
        }
        return null
    }

    private fun getCurrentUserMetadata(): JSONObject? {
        val email = auth.currentUser?.email ?: return null
        return findUserMetadataByEmail(email)
    }

    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_USERS_JSON = "users_json"
        private const val KEY_CURRENT_USER_EMAIL = "current_user_email"
        private const val KEY_USER_FIRST_NAME = "first_name"
        private const val KEY_USER_LAST_NAME = "last_name"
        private const val KEY_USER_EMAIL = "email"
        private const val KEY_USER_PHONE = "phone"
        private const val KEY_USER_PHOTO_URI = "photo_uri"
        private const val KEY_FAILED_ATTEMPTS_PREFIX = "failed_attempts_"
        private const val KEY_LOCKOUT_UNTIL_PREFIX = "lockout_until_"
        private const val MAX_FAILED_ATTEMPTS = 3
        private const val LOCKOUT_DURATION_MS = 3 * 60 * 1000L

        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        private val NAME_REGEX = Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$")
        private val PHONE_REGEX = Regex("^[0-9]{9,15}$")
        private val PASSWORD_REGEX = Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$")
    }
}

data class AuthResult(val success: Boolean, val message: String)
data class UserPreview(val fullName: String, val photoUri: String)
