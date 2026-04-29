package com.example.bd1.feature.products.data.model

data class ProductDto(
    val id: String = "",
    val name: String,
    val description: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val imageUri: String = "",
    val category: String = ""
)
