package com.example.bd1.feature.products.data.repository

import com.example.bd1.feature.products.data.datasource.ProductLocalDataSourceImpl
import com.example.bd1.feature.products.data.model.ProductDto
import com.example.bd1.feature.products.domain.model.Product
import com.example.bd1.feature.products.domain.repository.ProductRepository

class ProductRepositoryImpl(
    private val localDataSource: ProductLocalDataSourceImpl
) : ProductRepository {

    override suspend fun getAllProducts(): List<Product> {
        return localDataSource.getAllProducts().map { it.toModel() }
    }

    override suspend fun getProductById(id: String): Product? {
        return localDataSource.getProductById(id)?.toModel()
    }

    override suspend fun createProduct(product: Product): Boolean {
        return try {
            val dto = ProductDto(
                id = product.id.ifEmpty { System.currentTimeMillis().toString() },
                name = product.name,
                description = product.description,
                price = product.price,
                quantity = product.quantity,
                imageUri = product.imageUri,
                category = product.category
            )
            localDataSource.saveProduct(dto)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateProduct(product: Product): Boolean {
        return try {
            val dto = ProductDto(
                id = product.id,
                name = product.name,
                description = product.description,
                price = product.price,
                quantity = product.quantity,
                imageUri = product.imageUri,
                category = product.category
            )
            localDataSource.saveProduct(dto)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteProduct(id: String): Boolean {
        return try {
            localDataSource.deleteProduct(id)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun searchProducts(query: String): List<Product> {
        val products = localDataSource.getAllProducts()
        return products
            .filter { 
                it.name.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true)
            }
            .map { it.toModel() }
    }

    private fun ProductDto.toModel() = Product(
        id = id,
        name = name,
        description = description,
        price = price,
        quantity = quantity,
        imageUri = imageUri,
        category = category
    )
}
