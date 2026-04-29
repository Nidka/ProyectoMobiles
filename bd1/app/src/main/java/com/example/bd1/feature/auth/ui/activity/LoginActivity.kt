package com.example.bd1.feature.auth.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bd1.feature.products.ui.activity.MainActivity
import com.example.bd1.R
import com.example.bd1.SimpleTextWatcher
import com.example.bd1.di.AppContainer
import com.example.bd1.feature.auth.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var layoutLoginPreview: LinearLayout
    private lateinit var ivLoginAvatar: ImageView
    private lateinit var tvPreviewName: TextView
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Obtener ViewModel del contenedor de inyección de dependencias
        authViewModel = AppContainer.authViewModel
        authViewModel.clearState()

        etEmail = findViewById(R.id.et_login_email)
        etPassword = findViewById(R.id.et_login_password)
        layoutLoginPreview = findViewById(R.id.layout_login_preview)
        ivLoginAvatar = findViewById(R.id.iv_login_avatar)
        tvPreviewName = findViewById(R.id.tv_login_preview_name)

        val btnLogin: TextView = findViewById(R.id.btn_login)
        val tvGoRegister: TextView = findViewById(R.id.tv_go_register)
        val ivTogglePassword: ImageView = findViewById(R.id.iv_toggle_password)
        val tvForgotPassword: TextView = findViewById(R.id.tv_forgot_password)

        // Animaciones de entrada
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

        // Preview de usuario
        etEmail.addTextChangedListener(SimpleTextWatcher {
            renderLoginPreview(it)
        })

        // Toggle de visibilidad de contraseña
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

        // Observar cambios de estado del login
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.authState.collectLatest { state ->
                    when {
                        state.isLoading -> {
                            btnLogin.isEnabled = false
                            btnLogin.alpha = 0.5f
                        }
                        state.isSuccess -> {
                            btnLogin.isEnabled = true
                            btnLogin.alpha = 1f

                            val authenticatedUser = state.authResponse?.user
                            if (authenticatedUser != null) {
                                Toast.makeText(this@LoginActivity, state.authResponse?.message ?: "Bienvenido", Toast.LENGTH_SHORT).show()
                                openHome()
                            } else {
                                Toast.makeText(this@LoginActivity, state.authResponse?.message ?: "Operación completada", Toast.LENGTH_SHORT).show()
                                authViewModel.clearState()
                            }
                        }
                        state.errorMessage != null -> {
                            btnLogin.isEnabled = true
                            btnLogin.alpha = 1f
                            Toast.makeText(this@LoginActivity, state.errorMessage, Toast.LENGTH_SHORT).show()
                            authViewModel.clearState()
                        }
                    }
                }
            }
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Ingresa tu correo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                Toast.makeText(this, "Ingresa tu contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authViewModel.login(email, password)
        }

        tvGoRegister.setOnClickListener {
            authViewModel.clearState()
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        findViewById<ImageView>(R.id.btn_social_facebook).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/UPN/")))
        }
        findViewById<ImageView>(R.id.btn_social_instagram).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/upn/")))
        }
        findViewById<ImageView>(R.id.btn_social_youtube).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/user/UPNTVcanaloficial?themeRefresh=1")))
        }

        tvForgotPassword.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Ingresa tu correo para recuperar tu contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivity(
                Intent(this, ResetPasswordActivity::class.java)
                    .putExtra(ResetPasswordActivity.EXTRA_EMAIL, email)
            )
        }
    }

    override fun onResume() {
        super.onResume()
        authViewModel.clearState()
    }

    private fun renderLoginPreview(email: String) {
        // Buscar usuario en la base de datos local
        // Este es un mock, en producción obtendría del repositorio
    }

    private fun openHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
