package com.example.bd1.navigation

enum class Screen {
    SPLASH,
    LOGIN,
    REGISTER,
    RESET_PASSWORD,
    HOME,
    PROFILE,
    PRODUCTS,
    PRODUCT_FORM
}

sealed class NavigationEvent {
    object NavigateToHome : NavigationEvent()
    object NavigateToLogin : NavigationEvent()
    object NavigateToRegister : NavigationEvent()
    object NavigateToProfile : NavigationEvent()
    object NavigateToProducts : NavigationEvent()
    object Logout : NavigationEvent()
    object GoBack : NavigationEvent()
}
