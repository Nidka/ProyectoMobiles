package com.example.bd1

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.text.InputType

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var layoutLoginPreview: LinearLayout
    private lateinit var ivLoginAvatar: ImageView
    private lateinit var tvPreviewName: TextView
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        authManager = AuthManager(this)
        if (authManager.isLoggedIn()) {
            openHome()
            return
        }

        etEmail = findViewById(R.id.et_login_email)
        etPassword = findViewById(R.id.et_login_password)
        layoutLoginPreview = findViewById(R.id.layout_login_preview)
        ivLoginAvatar = findViewById(R.id.iv_login_avatar)
        tvPreviewName = findViewById(R.id.tv_login_preview_name)

        val btnLogin: Button = findViewById(R.id.btn_login)
        val tvGoRegister: TextView = findViewById(R.id.tv_go_register)

        val slideUp = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left)
        btnLogin.startAnimation(slideUp)

        etEmail.addTextChangedListener(SimpleTextWatcher {
            renderLoginPreview(it)
        })

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            
            authManager.login(email, password) { result ->
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                if (result.success) {
                    openHome()
                }
            }
        }

        tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        val tvForgotPassword: TextView = findViewById(R.id.tv_forgot_password)
        tvForgotPassword.setOnClickListener {
            showForgotPasswordEmailDialog()
        }
    }

    private fun showForgotPasswordEmailDialog() {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            hint = "correo@ejemplo.com"
        }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
            addView(input)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Recuperar Contraseña")
            .setMessage("Ingresa el correo asociado a tu cuenta.")
            .setView(layout)
            .setPositiveButton("Siguiente") { dialog, _ ->
                val email = input.text.toString().trim()
                if (authManager.getUserPreviewByEmail(email) != null) {
                    sendResetEmail(email)
                } else {
                    Toast.makeText(this, "No se encontró cuenta con este correo.", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun sendResetEmail(email: String) {
        val loadingDialog = MaterialAlertDialogBuilder(this)
            .setTitle("Enviando correo...")
            .setMessage("Por favor espera...")
            .setCancelable(false)
            .show()

        authManager.resetPassword(email) { result ->
            loadingDialog.dismiss()
            Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
        }
    }


    override fun onResume() {
        super.onResume()
        etEmail.setText("")
        etPassword.setText("")
        layoutLoginPreview.visibility = View.GONE
    }

    private fun renderLoginPreview(email: String) {
        val preview = authManager.getUserPreviewByEmail(email)
        if (preview == null) {
            layoutLoginPreview.visibility = View.GONE
            return
        }

        val avatar = AvatarImageLoader.loadCircular(this, preview.photoUri, R.drawable.ic_avatar_placeholder)
        if (avatar == null) {
            ivLoginAvatar.setImageResource(R.drawable.ic_avatar_placeholder)
        } else {
            ivLoginAvatar.setImageBitmap(avatar)
        }
        tvPreviewName.text = if (preview.fullName.isBlank()) "Cuenta detectada" else preview.fullName
        layoutLoginPreview.visibility = View.VISIBLE
    }

    private fun openHome() {
        startActivity(Intent(this, MainActivity::class.java))
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
