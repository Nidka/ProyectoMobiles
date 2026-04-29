package com.example.bd1.feature.products.data.datasource

import android.content.Context
import com.example.bd1.feature.products.data.model.ProductDto
import org.json.JSONArray
import org.json.JSONObject

interface ProductLocalDataSource {
    suspend fun getAllProducts(): List<ProductDto>
    suspend fun getProductById(id: String): ProductDto?
    suspend fun saveProduct(product: ProductDto)
    suspend fun deleteProduct(id: String)
    suspend fun deleteAll()
}

class ProductLocalDataSourceImpl(context: Context) : ProductLocalDataSource {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override suspend fun getAllProducts(): List<ProductDto> {
        val raw = prefs.getString(KEY_PRODUCTS_JSON, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(raw)
            mutableListOf<ProductDto>().apply {
                for (i in 0 until jsonArray.length()) {
                    val json = jsonArray.getJSONObject(i)
                    add(ProductDto(
                        id = json.optString("id", ""),
                        name = json.optString("name", ""),
                        description = json.optString("description", ""),
                        price = json.optDouble("price", 0.0),
                        quantity = json.optInt("quantity", 0),
                        imageUri = json.optString("imageUri", ""),
                        category = json.optString("category", "")
                    ))
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getProductById(id: String): ProductDto? {
        return getAllProducts().find { it.id == id }
    }

    override suspend fun saveProduct(product: ProductDto) {
        val products = getAllProducts().toMutableList()
        products.removeAll { it.id == product.id }
        products.add(product)
        saveProductsToPrefs(products)
    }

    override suspend fun deleteProduct(id: String) {
        val products = getAllProducts().filter { it.id != id }
        saveProductsToPrefs(products)
    }

    override suspend fun deleteAll() {
        prefs.edit().remove(KEY_PRODUCTS_JSON).apply()
    }

    private fun saveProductsToPrefs(products: List<ProductDto>) {
        val jsonArray = JSONArray()
        products.forEach { product ->
            jsonArray.put(JSONObject().apply {
                put("id", product.id)
                put("name", product.name)
                put("description", product.description)
                put("price", product.price)
                put("quantity", product.quantity)
                put("imageUri", product.imageUri)
                put("category", product.category)
            })
        }
        prefs.edit().putString(KEY_PRODUCTS_JSON, jsonArray.toString()).apply()
    }

    companion object {
        private const val PREFS_NAME = "products_prefs"
        private const val KEY_PRODUCTS_JSON = "products_json"
    }
}
