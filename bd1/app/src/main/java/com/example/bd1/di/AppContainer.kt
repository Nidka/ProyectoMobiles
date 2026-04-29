package com.example.bd1.di

import android.content.Context
import com.example.bd1.core.common.ValidationUtils
import com.example.bd1.feature.auth.data.datasource.AuthLocalDataSourceImpl
import com.example.bd1.feature.auth.data.repository.AuthRepositoryImpl
import com.example.bd1.feature.auth.domain.usecase.LoginUseCase
import com.example.bd1.feature.auth.domain.usecase.LogoutUseCase
import com.example.bd1.feature.auth.domain.usecase.RegisterUseCase
import com.example.bd1.feature.auth.domain.usecase.ResetPasswordUseCase
import com.example.bd1.feature.auth.domain.usecase.UpdateProfileUseCase
import com.example.bd1.feature.auth.ui.viewmodel.AuthViewModel
import com.example.bd1.feature.products.data.datasource.ProductLocalDataSourceImpl
import com.example.bd1.feature.products.data.repository.ProductRepositoryImpl
import com.example.bd1.feature.products.domain.usecase.CreateProductUseCase
import com.example.bd1.feature.products.domain.usecase.DeleteProductUseCase
import com.example.bd1.feature.products.domain.usecase.GetAllProductsUseCase
import com.example.bd1.feature.products.domain.usecase.SearchProductsUseCase
import com.example.bd1.feature.products.domain.usecase.UpdateProductUseCase
import com.example.bd1.feature.products.ui.viewmodel.ProductsViewModel
import com.example.bd1.feature.profile.data.repository.ProfileRepositoryImpl
import com.example.bd1.feature.profile.domain.usecase.GetProfileUseCase
import com.example.bd1.feature.profile.ui.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth

/**
 * Simple Service Locator pattern para inyección de dependencias.
 * Esto puede ser reemplazado con Hilt en el futuro.
 */
object AppContainer {
    
    private lateinit var context: Context
    private lateinit var firebaseAuth: FirebaseAuth
    
    // Core
    private val validationUtils by lazy { ValidationUtils() }
    
    // Auth
    private val authLocalDataSource by lazy { AuthLocalDataSourceImpl(context) }
    private val authRepository by lazy { 
        AuthRepositoryImpl(firebaseAuth, authLocalDataSource, validationUtils) 
    }
    private val registerUseCase by lazy { RegisterUseCase(authRepository) }
    private val loginUseCase by lazy { LoginUseCase(authRepository) }
    private val logoutUseCase by lazy { LogoutUseCase(authRepository) }
    private val resetPasswordUseCase by lazy { ResetPasswordUseCase(authRepository) }
    private val authUpdateProfileUseCase by lazy { UpdateProfileUseCase(authRepository) }
    
    val authViewModel by lazy {
        AuthViewModel(
            registerUseCase,
            loginUseCase,
            logoutUseCase,
            resetPasswordUseCase,
            authUpdateProfileUseCase
        )
    }
    
    // Products
    private val productLocalDataSource by lazy { ProductLocalDataSourceImpl(context) }
    private val productRepository by lazy { ProductRepositoryImpl(productLocalDataSource) }
    private val getAllProductsUseCase by lazy { GetAllProductsUseCase(productRepository) }
    private val createProductUseCase by lazy { CreateProductUseCase(productRepository) }
    private val updateProductUseCase by lazy { UpdateProductUseCase(productRepository) }
    private val deleteProductUseCase by lazy { DeleteProductUseCase(productRepository) }
    private val searchProductsUseCase by lazy { SearchProductsUseCase(productRepository) }
    
    val productsViewModel by lazy {
        ProductsViewModel(
            getAllProductsUseCase,
            createProductUseCase,
            updateProductUseCase,
            deleteProductUseCase,
            searchProductsUseCase
        )
    }
    
    // Profile
    private val profileRepository by lazy { ProfileRepositoryImpl(authLocalDataSource) }
    private val getProfileUseCase by lazy { GetProfileUseCase(profileRepository) }
    private val profileUpdateProfileUseCase by lazy { com.example.bd1.feature.profile.domain.usecase.UpdateProfileUseCase(profileRepository) }
    
    val profileViewModel by lazy {
        ProfileViewModel(
            getProfileUseCase,
            profileUpdateProfileUseCase
        )
    }
    
    fun initialize(appContext: Context) {
        context = appContext
        firebaseAuth = FirebaseAuth.getInstance()
    }
}
