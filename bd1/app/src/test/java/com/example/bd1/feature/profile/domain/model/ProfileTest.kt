package com.example.bd1.feature.profile.domain.model

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ProfileTest {
    
    @Test
    fun `Profile should be created correctly`() {
        val profile = Profile(
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com",
            phone = "123456789",
            photoUri = ""
        )
        
        assertEquals("John", profile.firstName)
        assertEquals("Doe", profile.lastName)
        assertEquals("john@example.com", profile.email)
    }
    
    @Test
    fun `Profile fields should not be null`() {
        val profile = Profile("Jane", "Smith", "jane@example.com", "987654321", "")
        
        assertNotNull(profile.firstName)
        assertNotNull(profile.lastName)
        assertNotNull(profile.email)
    }
}
