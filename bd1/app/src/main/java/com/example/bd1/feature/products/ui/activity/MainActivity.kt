package com.example.bd1.feature.products.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bd1.AvatarImageLoader
import com.example.bd1.R
import com.example.bd1.di.AppContainer
import com.example.bd1.feature.auth.ui.activity.LoginActivity
import com.example.bd1.feature.auth.domain.model.User
import com.example.bd1.feature.auth.ui.viewmodel.AuthViewModel
import com.example.bd1.feature.profile.ui.activity.ProfileActivity
import com.example.bd1.feature.products.ui.adapter.ProductAdapter
import com.example.bd1.feature.products.ui.viewmodel.ProductsViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var productsViewModel: ProductsViewModel
    private lateinit var tvWelcome: TextView
    private lateinit var ivHomeAvatar: ImageView
    private lateinit var etSearch: TextInputEditText
    private lateinit var rvProducts: RecyclerView
    private lateinit var tvEmptyProducts: TextView
    private lateinit var adapter: ProductAdapter

    private val productFormLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            productsViewModel.loadAllProducts()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        authViewModel = AppContainer.authViewModel
        productsViewModel = AppContainer.productsViewModel

        tvWelcome = findViewById(R.id.tv_home_welcome)
        ivHomeAvatar = findViewById(R.id.iv_home_avatar)
        etSearch = findViewById(R.id.et_home_search)
        rvProducts = findViewById(R.id.rv_products)
        tvEmptyProducts = findViewById(R.id.tv_empty_products)

        val btnLogoutLarge: MaterialButton = findViewById(R.id.btn_logout_large)
        val btnRefresh: MaterialButton = findViewById(R.id.btn_refresh)
        val btnProfile: MaterialButton = findViewById(R.id.btn_profile)
        val fabCreate: FloatingActionButton = findViewById(R.id.fab_create_product)

        adapter = ProductAdapter(
            onItemClick = { product ->
                openProductForm(product.id)
            },
            onItemLongClick = { product ->
                confirmDelete(product)
            }
        )

        rvProducts.layoutManager = GridLayoutManager(this, 2)
        rvProducts.adapter = adapter

        // Observar cambios de estado de productos
        lifecycleScope.launch {
            productsViewModel.productState.collectLatest { state ->
                when {
                    state.isLoading -> {}
                    state.isSuccess -> {}
                    state.errorMessage != null -> {
                        Toast.makeText(this@MainActivity, state.errorMessage, Toast.LENGTH_SHORT).show()
                        productsViewModel.clearState()
                    }
                }
            }
        }

        // Observar lista de productos
        lifecycleScope.launch {
            productsViewModel.productState.collectLatest { state ->
                val query = etSearch.text?.toString().orEmpty()
                filterAndShowProducts(state.products, query)
            }
        }

        lifecycleScope.launch {
            authViewModel.authState.collectLatest { state ->
                state.authResponse?.user?.let { updateWelcome(it) }
            }
        }

        etSearch.addTextChangedListener(SimpleTextWatcher { text ->
            productsViewModel.searchProducts(text)
        })

        btnRefresh.setOnClickListener {
            productsViewModel.loadAllProducts()
            Toast.makeText(this, "Catálogo actualizado", Toast.LENGTH_SHORT).show()
        }

        btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        btnLogoutLarge.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Deseas cerrar sesión?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Aceptar") { _, _ ->
                    authViewModel.logout()
                    goToLogin()
                }
                .show()
        }

        fabCreate.setOnClickListener {
            openProductForm(null)
        }

        refreshHomeHeader()
        productsViewModel.loadAllProducts()
    }

    override fun onStart() {
        super.onStart()
        refreshHomeHeader()
    }

    private fun refreshHomeHeader() {
        val currentUser = authViewModel.authState.value.authResponse?.user
        if (currentUser != null) {
            updateWelcome(currentUser)
            renderHomeAvatar(currentUser)
            return
        }

        val fallbackName = getString(R.string.home_welcome, "Usuario")
        tvWelcome.text = fallbackName
        renderHomeAvatar(null)
    }

    private fun updateWelcome(user: User) {
        val displayName = listOf(user.firstName, user.lastName)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { user.email.substringBefore('@').replaceFirstChar { it.uppercase() } }
        tvWelcome.text = getString(R.string.home_welcome, displayName)
    }

    private fun renderHomeAvatar(user: User?) {
        val avatar = AvatarImageLoader.loadCircular(this, user?.photoUri, R.drawable.ic_avatar_placeholder)
        if (avatar == null) {
            ivHomeAvatar.setImageResource(R.drawable.ic_avatar_placeholder)
            return
        }
        ivHomeAvatar.setImageBitmap(avatar)
    }

    private fun filterAndShowProducts(
        products: List<com.example.bd1.feature.products.domain.model.Product>,
        query: String
    ) {
        val clean = query.trim().lowercase(Locale.getDefault())
        val filtered = if (clean.isBlank()) {
            products
        } else {
            products.filter {
                it.name.lowercase(Locale.getDefault()).contains(clean) ||
                    it.id.lowercase(Locale.getDefault()).contains(clean)
            }
        }

        adapter.submitList(filtered)

        if (filtered.isEmpty()) {
            tvEmptyProducts.text = if (clean.isBlank()) {
                getString(R.string.no_products)
            } else {
                getString(R.string.no_products_for_filter)
            }
            tvEmptyProducts.visibility = View.VISIBLE
            rvProducts.visibility = View.GONE
        } else {
            tvEmptyProducts.visibility = View.GONE
            rvProducts.visibility = View.VISIBLE
        }
    }

    private fun openProductForm(productId: String?) {
        val intent = Intent(this, ProductFormActivity::class.java)
        if (productId != null) {
            intent.putExtra(ProductFormActivity.EXTRA_PRODUCT_ID, productId)
        }
        productFormLauncher.launch(intent)
    }

    private fun confirmDelete(product: com.example.bd1.feature.products.domain.model.Product) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_title))
            .setMessage(getString(R.string.delete_message, product.name))
            .setNegativeButton(getString(R.string.action_cancel), null)
            .setPositiveButton(getString(R.string.action_delete)) { _, _ ->
                productsViewModel.deleteProduct(product.id)
                Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private class SimpleTextWatcher(
        private val onChange: (String) -> Unit
    ) : android.text.TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            onChange(s?.toString().orEmpty())
        }

        override fun afterTextChanged(s: android.text.Editable?) = Unit
    }
}
