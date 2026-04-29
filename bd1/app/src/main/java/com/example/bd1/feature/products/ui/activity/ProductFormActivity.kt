package com.example.bd1.feature.products.ui.activity

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bd1.AvatarImageLoader
import com.example.bd1.ImageStorageManager
import com.example.bd1.R
import com.example.bd1.di.AppContainer
import com.example.bd1.feature.products.domain.model.Product
import com.example.bd1.feature.products.ui.viewmodel.ProductsViewModel
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProductFormActivity : AppCompatActivity() {

    private lateinit var productsViewModel: ProductsViewModel
    private lateinit var etNombre: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var etPrecio: EditText
    private lateinit var etImageUri: EditText
    private lateinit var ivPreview: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var btnGuardar: Button

    private lateinit var imageStorageManager: ImageStorageManager

    private var productId: String? = null
    private var selectedImagePath: String? = null

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@registerForActivityResult

        val savedPath = imageStorageManager.persistImage(uri)
        if (savedPath.isNullOrBlank()) {
            Toast.makeText(this, "No se pudo guardar la imagen", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }

        selectedImagePath = savedPath
        etImageUri.setText(savedPath)
        renderPreview(savedPath)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_form)

        productsViewModel = AppContainer.productsViewModel
        imageStorageManager = ImageStorageManager(this)

        etNombre = findViewById(R.id.et_form_codigo)
        etDescripcion = findViewById(R.id.et_form_descripcion)
        etPrecio = findViewById(R.id.et_form_precio)
        etImageUri = findViewById(R.id.et_form_image_uri)
        ivPreview = findViewById(R.id.iv_form_producto)
        tvTitle = findViewById(R.id.tv_form_title)
        btnGuardar = findViewById(R.id.btn_form_save)
        val btnClear: Button = findViewById(R.id.btn_form_clear)
        val btnSelectImage: Button = findViewById(R.id.btn_form_select_image)
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar_form)

        toolbar.setNavigationOnClickListener { finish() }

        productId = intent.getStringExtra(EXTRA_PRODUCT_ID)

        if (productId != null) {
            tvTitle.text = getString(R.string.product_form_edit_title)
            // Cargar datos del producto para editar
        } else {
            tvTitle.text = getString(R.string.product_form_create_title)
            renderPreview(null)
        }

        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        etNombre.startAnimation(fadeIn)
        etDescripcion.startAnimation(fadeIn)
        etPrecio.startAnimation(fadeIn)
        btnGuardar.startAnimation(fadeIn)

        // Observar cambios de estado de productos
        lifecycleScope.launch {
            productsViewModel.productState.collectLatest { state ->
                when {
                    state.isLoading -> {
                        btnGuardar.isEnabled = false
                        btnGuardar.alpha = 0.5f
                    }
                    state.isSuccess -> {
                        Toast.makeText(
                            this@ProductFormActivity,
                            if (productId != null) "Producto actualizado" else "Producto creado",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                    state.errorMessage != null -> {
                        btnGuardar.isEnabled = true
                        btnGuardar.alpha = 1f
                        Toast.makeText(this@ProductFormActivity, state.errorMessage, Toast.LENGTH_SHORT).show()
                        productsViewModel.clearState()
                    }
                }
            }
        }

        btnSelectImage.setOnClickListener {
            imagePicker.launch("image/*")
        }

        btnClear.setOnClickListener {
            clearForm()
        }

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val descripcion = etDescripcion.text.toString().trim()
            val precio = etPrecio.text.toString().trim().toDoubleOrNull() ?: 0.0

            if (nombre.isBlank()) {
                Toast.makeText(this, "Ingresa el nombre del producto", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (precio <= 0.0) {
                Toast.makeText(this, "Ingresa un precio válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val product = Product(
                id = productId ?: "",
                name = nombre,
                description = descripcion,
                price = precio,
                quantity = 0,
                imageUri = selectedImagePath.orEmpty(),
                category = ""
            )

            if (productId != null) {
                productsViewModel.updateProduct(product)
            } else {
                productsViewModel.createProduct(product)
            }
        }
    }

    private fun clearForm() {
        etNombre.text.clear()
        etDescripcion.text.clear()
        etPrecio.text.clear()
        etImageUri.text.clear()
        selectedImagePath = null
        renderPreview(null)
    }

    private fun renderPreview(imagePath: String?) {
        if (imagePath.isNullOrEmpty()) {
            ivPreview.setImageResource(R.drawable.ic_image_placeholder)
        } else {
            val bitmap = AvatarImageLoader.loadCircular(this, imagePath, R.drawable.ic_image_placeholder)
            if (bitmap != null) {
                ivPreview.setImageBitmap(bitmap)
            } else {
                ivPreview.setImageResource(R.drawable.ic_image_placeholder)
            }
        }
    }

    companion object {
        const val EXTRA_PRODUCT_ID = "product_id"
    }
}
