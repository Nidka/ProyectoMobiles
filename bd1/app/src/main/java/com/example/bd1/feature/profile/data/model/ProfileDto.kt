package com.example.bd1.feature.profile.data.model

data class ProfileDto(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String = "",
    val photoUri: String = ""
)
