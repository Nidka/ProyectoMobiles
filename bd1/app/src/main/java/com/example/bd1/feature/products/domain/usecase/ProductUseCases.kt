package com.example.bd1.feature.products.domain.usecase

import com.example.bd1.feature.products.domain.model.Product
import com.example.bd1.feature.products.domain.repository.ProductRepository

class GetAllProductsUseCase(private val repository: ProductRepository) {
    suspend operator fun invoke(): List<Product> = repository.getAllProducts()
}

class GetProductByIdUseCase(private val repository: ProductRepository) {
    suspend operator fun invoke(id: String): Product? = repository.getProductById(id)
}

class CreateProductUseCase(private val repository: ProductRepository) {
    suspend operator fun invoke(product: Product): Boolean = repository.createProduct(product)
}

class UpdateProductUseCase(private val repository: ProductRepository) {
    suspend operator fun invoke(product: Product): Boolean = repository.updateProduct(product)
}

class DeleteProductUseCase(private val repository: ProductRepository) {
    suspend operator fun invoke(id: String): Boolean = repository.deleteProduct(id)
}

class SearchProductsUseCase(private val repository: ProductRepository) {
    suspend operator fun invoke(query: String): List<Product> = repository.searchProducts(query)
}
