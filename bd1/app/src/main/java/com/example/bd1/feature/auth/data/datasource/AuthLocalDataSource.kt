package com.example.bd1.feature.auth.data.datasource

import android.content.Context
import com.example.bd1.feature.auth.data.model.UserDto
import org.json.JSONArray
import org.json.JSONObject

interface AuthLocalDataSource {
    suspend fun saveUser(user: UserDto)
    suspend fun getCurrentUser(): UserDto?
    suspend fun getAllUsers(): List<UserDto>
    suspend fun getUserByEmail(email: String): UserDto?
    suspend fun deleteUser(email: String)
    suspend fun clearAll()
}

class AuthLocalDataSourceImpl(context: Context) : AuthLocalDataSource {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override suspend fun saveUser(user: UserDto) {
        val users = getAllUsers().toMutableList()
        users.removeAll { it.email == user.email }
        users.add(user)
        saveUsersToPrefs(users)
    }

    override suspend fun getCurrentUser(): UserDto? {
        val email = prefs.getString(KEY_CURRENT_USER_EMAIL, null) ?: return null
        return getUserByEmail(email)
    }

    override suspend fun getAllUsers(): List<UserDto> {
        val raw = prefs.getString(KEY_USERS_JSON, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(raw)
            mutableListOf<UserDto>().apply {
                for (i in 0 until jsonArray.length()) {
                    val json = jsonArray.getJSONObject(i)
                    add(UserDto(
                        id = json.optString("id", ""),
                        firstName = json.optString("firstName", ""),
                        lastName = json.optString("lastName", ""),
                        email = json.optString("email", ""),
                        phone = json.optString("phone", ""),
                        photoUri = json.optString("photoUri", ""),
                        role = json.optString("role", "estudiante"),
                        isEmailVerified = json.optBoolean("isEmailVerified", false)
                    ))
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getUserByEmail(email: String): UserDto? {
        return getAllUsers().find { it.email == email.lowercase() }
    }

    override suspend fun deleteUser(email: String) {
        val users = getAllUsers().filter { it.email != email.lowercase() }
        saveUsersToPrefs(users)
    }

    override suspend fun clearAll() {
        prefs.edit().clear().apply()
    }

    private fun saveUsersToPrefs(users: List<UserDto>) {
        val jsonArray = JSONArray()
        users.forEach { user ->
            jsonArray.put(JSONObject().apply {
                put("id", user.id)
                put("firstName", user.firstName)
                put("lastName", user.lastName)
                put("email", user.email)
                put("phone", user.phone)
                put("photoUri", user.photoUri)
                put("role", user.role)
                put("isEmailVerified", user.isEmailVerified)
            })
        }
        prefs.edit().putString(KEY_USERS_JSON, jsonArray.toString()).apply()
    }

    fun setCurrentUserEmail(email: String) {
        prefs.edit().putString(KEY_CURRENT_USER_EMAIL, email).apply()
    }

    fun getCurrentUserEmail(): String? {
        return prefs.getString(KEY_CURRENT_USER_EMAIL, null)
    }

    fun clearCurrentUser() {
        prefs.edit().remove(KEY_CURRENT_USER_EMAIL).apply()
    }

    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_USERS_JSON = "users_json"
        private const val KEY_CURRENT_USER_EMAIL = "current_user_email"
    }
}
