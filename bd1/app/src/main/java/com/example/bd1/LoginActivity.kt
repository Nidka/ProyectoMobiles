package com.example.bd1

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.text.InputType

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var layoutLoginPreview: LinearLayout
    private lateinit var ivLoginAvatar: ImageView
    private lateinit var tvPreviewName: TextView
    private lateinit var authManager: AuthManager
    private var isPasswordVisible = false

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

        val btnLogin: TextView = findViewById(R.id.btn_login)
        val tvGoRegister: TextView = findViewById(R.id.tv_go_register)
        val ivTogglePassword: ImageView = findViewById(R.id.iv_toggle_password)
        val tvForgotPassword: TextView = findViewById(R.id.tv_forgot_password)

        // Staggered Entrance Animations for form elements
        val slideUp1 = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in)
        val slideUp2 = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in).apply { startOffset = 100 }
        val slideUp3 = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in).apply { startOffset = 200 }
        val slideUp4 = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in).apply { startOffset = 300 }
        val slideUp5 = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in).apply { startOffset = 400 }

        etEmail.startAnimation(slideUp1)
        etPassword.startAnimation(slideUp2)
        tvForgotPassword.startAnimation(slideUp3)
        btnLogin.startAnimation(slideUp4)
        tvGoRegister.startAnimation(slideUp5)
        layoutLoginPreview.startAnimation(slideUp1)

        etEmail.addTextChangedListener(SimpleTextWatcher {
            renderLoginPreview(it)
        })

        // Password visibility toggle
        ivTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivTogglePassword.setImageResource(R.drawable.ic_eye_off)
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivTogglePassword.setImageResource(R.drawable.ic_eye)
            }
            etPassword.setSelection(etPassword.text.length)
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validate fields are not empty
            if (email.isEmpty()) {
                Toast.makeText(this, "Ingresa tu correo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                Toast.makeText(this, "Ingresa tu contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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

        tvForgotPassword.setOnClickListener {
            openResetPasswordScreen()
        }
    }

    private fun openResetPasswordScreen() {
        val email = etEmail.text.toString().trim()
        val intent = Intent(this, ResetPasswordActivity::class.java).apply {
            putExtra(ResetPasswordActivity.EXTRA_EMAIL, email)
        }
        startActivity(intent)
        overridePendingTransition(R.anim.reset_screen_enter, R.anim.fade_scale_in)
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
