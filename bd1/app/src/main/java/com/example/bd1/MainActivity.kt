package com.example.bd1

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var ivHomeAvatar: ImageView
    private lateinit var etSearch: TextInputEditText
    private lateinit var rvProducts: RecyclerView
    private lateinit var tvEmptyProducts: TextView

    private lateinit var authManager: AuthManager
    private lateinit var productRepository: ProductRepository
    private lateinit var adapter: ProductAdapter

    private val allProducts = mutableListOf<ProductItem>()

    private val productFormLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            loadProducts()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        authManager = AuthManager(this)
        productRepository = ProductRepository(this)

        if (!authManager.isLoggedIn()) {
            goToLogin()
            return
        }

        tvWelcome = findViewById(R.id.tv_home_welcome)
        ivHomeAvatar = findViewById(R.id.iv_home_avatar)
        etSearch = findViewById(R.id.et_home_search)
        rvProducts = findViewById(R.id.rv_products)
        tvEmptyProducts = findViewById(R.id.tv_empty_products)

        val btnLogoutLarge: MaterialButton = findViewById(R.id.btn_logout_large)
        val btnRefresh: MaterialButton = findViewById(R.id.btn_refresh)
        val btnProfile: MaterialButton = findViewById(R.id.btn_profile)
        val fabCreate: FloatingActionButton = findViewById(R.id.fab_create_product)

        tvWelcome.text = getString(R.string.home_welcome, authManager.getCurrentUserName())
        renderHomeAvatar()

        adapter = ProductAdapter(
            onItemClick = { item ->
                openProductForm(item.codigo)
            },
            onItemLongClick = { item ->
                confirmDelete(item)
            }
        )

        rvProducts.layoutManager = GridLayoutManager(this, 2)
        rvProducts.adapter = adapter

        etSearch.addTextChangedListener(SimpleTextWatcher { text ->
            filterProducts(text)
        })

        btnRefresh.setOnClickListener {
            loadProducts()
            Toast.makeText(this, "Catálogo actualizado", Toast.LENGTH_SHORT).show()
        }

        btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        btnLogoutLarge.setOnClickListener {
            authManager.logout()
            goToLogin()
        }

        fabCreate.setOnClickListener {
            openProductForm(null)
        }

        loadProducts()
    }

    override fun onStart() {
        super.onStart()
        if (!authManager.isLoggedIn()) {
            goToLogin()
            return
        }
        tvWelcome.text = getString(R.string.home_welcome, authManager.getCurrentUserName())
        renderHomeAvatar()
    }

    private fun renderHomeAvatar() {
        val photoPath = authManager.getCurrentUserPhotoUri()
        val avatar = AvatarImageLoader.loadCircular(this, photoPath, R.drawable.ic_avatar_placeholder)
        if (avatar == null) {
            ivHomeAvatar.setImageResource(R.drawable.ic_avatar_placeholder)
            return
        }
        ivHomeAvatar.setImageBitmap(avatar)
    }

    private fun loadProducts() {
        allProducts.clear()
        allProducts.addAll(productRepository.getAllProducts())
        filterProducts(etSearch.text?.toString().orEmpty())
    }

    private fun filterProducts(query: String) {
        val clean = query.trim().lowercase(Locale.getDefault())
        val filtered = if (clean.isBlank()) {
            allProducts
        } else {
            allProducts.filter {
                it.descripcion.lowercase(Locale.getDefault()).contains(clean) ||
                    it.codigo.toString().contains(clean)
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

    private fun openProductForm(code: Int?) {
        val intent = Intent(this, ProductFormActivity::class.java)
        if (code != null) {
            intent.putExtra(ProductFormActivity.EXTRA_PRODUCT_CODE, code)
        }
        productFormLauncher.launch(intent)
    }

    private fun confirmDelete(item: ProductItem) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_title))
            .setMessage(getString(R.string.delete_message, item.descripcion))
            .setNegativeButton(getString(R.string.action_cancel), null)
            .setPositiveButton(getString(R.string.action_delete)) { _, _ ->
                val ok = productRepository.deleteProduct(item.codigo)
                if (ok) {
                    Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show()
                    loadProducts()
                } else {
                    Toast.makeText(this, "No se pudo eliminar", Toast.LENGTH_SHORT).show()
                }
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
