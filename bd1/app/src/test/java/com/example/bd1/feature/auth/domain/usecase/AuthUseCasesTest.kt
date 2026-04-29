package com.example.bd1.feature.auth.domain.usecase

import org.junit.Test
import kotlin.test.assertNotNull

class AuthUseCasesTest {
    
    @Test
    fun `RegisterUseCase should exist`() {
        assertNotNull(RegisterUseCase::class)
    }
    
    @Test
    fun `LoginUseCase should exist`() {
        assertNotNull(LoginUseCase::class)
    }
    
    @Test
    fun `LogoutUseCase should exist`() {
        assertNotNull(LogoutUseCase::class)
    }
}
