package com.example.bd1.feature.products.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bd1.feature.products.domain.model.Product
import com.example.bd1.feature.products.domain.usecase.CreateProductUseCase
import com.example.bd1.feature.products.domain.usecase.DeleteProductUseCase
import com.example.bd1.feature.products.domain.usecase.GetAllProductsUseCase
import com.example.bd1.feature.products.domain.usecase.SearchProductsUseCase
import com.example.bd1.feature.products.domain.usecase.UpdateProductUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductUiState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class ProductsViewModel(
    private val getAllProductsUseCase: GetAllProductsUseCase,
    private val createProductUseCase: CreateProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val searchProductsUseCase: SearchProductsUseCase
) : ViewModel() {

    private val _productState = MutableStateFlow(ProductUiState())
    val productState = _productState.asStateFlow()

    init {
        loadAllProducts()
    }

    fun loadAllProducts() {
        viewModelScope.launch {
            _productState.value = ProductUiState(isLoading = true)
            try {
                val products = getAllProductsUseCase()
                _productState.value = ProductUiState(products = products)
            } catch (e: Exception) {
                _productState.value = ProductUiState(errorMessage = e.message ?: "Error al cargar productos")
            }
        }
    }

    fun createProduct(product: Product) {
        viewModelScope.launch {
            _productState.value = _productState.value.copy(isLoading = true)
            try {
                val success = createProductUseCase(product)
                if (success) {
                    loadAllProducts()
                    _productState.value = _productState.value.copy(isSuccess = true)
                } else {
                    _productState.value = _productState.value.copy(errorMessage = "Error al crear producto")
                }
            } catch (e: Exception) {
                _productState.value = _productState.value.copy(errorMessage = e.message ?: "Error")
            }
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            _productState.value = _productState.value.copy(isLoading = true)
            try {
                val success = updateProductUseCase(product)
                if (success) {
                    loadAllProducts()
                    _productState.value = _productState.value.copy(isSuccess = true)
                } else {
                    _productState.value = _productState.value.copy(errorMessage = "Error al actualizar producto")
                }
            } catch (e: Exception) {
                _productState.value = _productState.value.copy(errorMessage = e.message ?: "Error")
            }
        }
    }

    fun deleteProduct(id: String) {
        viewModelScope.launch {
            _productState.value = _productState.value.copy(isLoading = true)
            try {
                val success = deleteProductUseCase(id)
                if (success) {
                    loadAllProducts()
                    _productState.value = _productState.value.copy(isSuccess = true)
                } else {
                    _productState.value = _productState.value.copy(errorMessage = "Error al eliminar producto")
                }
            } catch (e: Exception) {
                _productState.value = _productState.value.copy(errorMessage = e.message ?: "Error")
            }
        }
    }

    fun searchProducts(query: String) {
        viewModelScope.launch {
            _productState.value = _productState.value.copy(isLoading = true)
            try {
                if (query.isBlank()) {
                    loadAllProducts()
                } else {
                    val results = searchProductsUseCase(query)
                    _productState.value = _productState.value.copy(products = results)
                }
            } catch (e: Exception) {
                _productState.value = _productState.value.copy(errorMessage = e.message ?: "Error en búsqueda")
            }
        }
    }

    fun clearState() {
        _productState.value = ProductUiState()
    }
}
