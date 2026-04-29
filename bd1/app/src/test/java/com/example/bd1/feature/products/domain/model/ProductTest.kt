package com.example.bd1.feature.products.domain.model

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ProductTest {
    
    @Test
    fun `Product should be created correctly`() {
        val product = Product(
            id = "1",
            name = "Test Product",
            description = "Test Description",
            price = 19.99,
            quantity = 5,
            imageUri = "uri",
            category = "test"
        )
        
        assertEquals("1", product.id)
        assertEquals("Test Product", product.name)
        assertEquals(19.99, product.price)
    }
    
    @Test
    fun `Product fields should not be null`() {
        val product = Product("1", "Test", "Desc", 10.0, 1, "", "cat")
        
        assertNotNull(product.id)
        assertNotNull(product.name)
        assertNotNull(product.price)
    }
}
