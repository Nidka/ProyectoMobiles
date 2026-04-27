package com.example.bd1

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class ProductFormActivity : AppCompatActivity() {

    private lateinit var etCodigo: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var etPrecio: EditText
    private lateinit var etImagenUri: EditText
    private lateinit var ivPreview: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var btnGuardar: Button

    private lateinit var repo: ProductRepository
    private lateinit var imageStorageManager: ImageStorageManager

    private var editCode: Int? = null
    private var selectedImageUri: Uri? = null
    private var selectedImagePath: String? = null

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@registerForActivityResult

        selectedImageUri = uri
        val savedPath = imageStorageManager.persistImage(uri)
        if (savedPath.isNullOrBlank()) {
            Toast.makeText(this, "No se pudo guardar la imagen", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }

        selectedImagePath = savedPath
        etImagenUri.setText(savedPath)
        renderPreview(savedPath)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_form)

        repo = ProductRepository(this)
        imageStorageManager = ImageStorageManager(this)

        etCodigo = findViewById(R.id.et_form_codigo)
        etDescripcion = findViewById(R.id.et_form_descripcion)
        etPrecio = findViewById(R.id.et_form_precio)
        etImagenUri = findViewById(R.id.et_form_image_uri)
        ivPreview = findViewById(R.id.iv_form_producto)
        tvTitle = findViewById(R.id.tv_form_title)
        btnGuardar = findViewById(R.id.btn_form_save)
        val btnClear: Button = findViewById(R.id.btn_form_clear)
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar_form)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val btnSelectImage: Button = findViewById(R.id.btn_form_select_image)

        editCode = intent.getIntExtra(EXTRA_PRODUCT_CODE, -1).takeIf { it > 0 }

        if (editCode != null) {
            loadEditData(editCode!!)
        } else {
            tvTitle.text = getString(R.string.product_form_create_title)
            etCodigo.setText(repo.getNextCode().toString())
            renderPreview(null)
        }

        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        btnGuardar.startAnimation(fadeIn)

        btnSelectImage.setOnClickListener {
            imagePicker.launch("image/*")
        }

        btnGuardar.setOnClickListener {
            saveProduct()
        }

        btnClear.setOnClickListener {
            clearForm()
        }
    }

    private fun loadEditData(code: Int) {
        val item = repo.getByCode(code)
        if (item == null) {
            Toast.makeText(this, "No se encontró el producto", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvTitle.text = getString(R.string.product_form_edit_title)
        btnGuardar.text = getString(R.string.product_form_update_button)
        etCodigo.setText(item.codigo.toString())
        etDescripcion.setText(item.descripcion)
        etPrecio.setText(item.precio.toString())

        if (item.imagenUri.isNullOrBlank()) {
            selectedImageUri = null
            selectedImagePath = null
            etImagenUri.setText("")
            renderPreview(null)
        } else {
            selectedImageUri = Uri.parse(item.imagenUri)
            selectedImagePath = item.imagenUri
            etImagenUri.setText(item.imagenUri)
            renderPreview(item.imagenUri)
        }
    }

    private fun saveProduct() {
        val code = etCodigo.text.toString().toIntOrNull()
        val description = etDescripcion.text.toString().trim()
        val price = etPrecio.text.toString().toDoubleOrNull()

        if (code == null || code <= 0) {
            Toast.makeText(this, "Código inválido", Toast.LENGTH_SHORT).show()
            return
        }
        if (description.isBlank()) {
            Toast.makeText(this, "La descripción es obligatoria", Toast.LENGTH_SHORT).show()
            return
        }
        if (price == null || price <= 0.0) {
            Toast.makeText(this, "El precio debe ser mayor a 0", Toast.LENGTH_SHORT).show()
            return
        }

        val success = if (editCode == null) {
            repo.saveProduct(code, description, price, selectedImagePath)
        } else {
            repo.updateProduct(code, description, price, selectedImagePath)
        }

        if (!success) {
            Toast.makeText(this, "No se pudo guardar el producto", Toast.LENGTH_SHORT).show()
            return
        }

        setResult(RESULT_OK)
        finish()
    }

    private fun clearForm() {
        etDescripcion.setText("")
        etPrecio.setText("")
        etImagenUri.setText("")
        selectedImageUri = null
        selectedImagePath = null
        renderPreview(null)
    }

    private fun renderPreview(uriText: String?) {
        if (uriText.isNullOrBlank()) {
            ivPreview.setImageResource(R.drawable.ic_image_placeholder)
            return
        }

        val result = runCatching {
            val uri = Uri.parse(uriText)
            contentResolver.openInputStream(uri)?.use {
                val bitmap = BitmapFactory.decodeStream(it)
                ivPreview.setImageBitmap(bitmap)
            } ?: throw IllegalArgumentException("No se pudo abrir la imagen")
        }

        if (result.isFailure) {
            selectedImageUri = null
            selectedImagePath = null
            etImagenUri.setText("")
            ivPreview.setImageResource(R.drawable.ic_image_placeholder)
        }
    }

    companion object {
        const val EXTRA_PRODUCT_CODE = "extra_product_code"
    }
}
