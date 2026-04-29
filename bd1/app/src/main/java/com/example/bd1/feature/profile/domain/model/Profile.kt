package com.example.bd1.feature.profile.domain.model

data class Profile(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String = "",
    val photoUri: String = ""
)
