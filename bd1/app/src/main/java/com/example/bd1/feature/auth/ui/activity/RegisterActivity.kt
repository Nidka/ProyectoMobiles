package com.example.bd1.feature.auth.ui.activity

import android.graphics.Typeface
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bd1.R
import com.example.bd1.di.AppContainer
import com.example.bd1.feature.auth.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: TextView

    private var selectedRole = "estudiante"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        authViewModel = AppContainer.authViewModel
        authViewModel.clearState()

        // Back button
        val ivBack: ImageView = findViewById(R.id.iv_back)
        ivBack.setOnClickListener { onBackPressed() }

        // Tabs
        val tabStudent: LinearLayout = findViewById(R.id.tab_student)
        val tabDriver: LinearLayout = findViewById(R.id.tab_driver)
        val ivTabStudent: ImageView = findViewById(R.id.iv_tab_student)
        val tvTabStudent: TextView = findViewById(R.id.tv_tab_student)
        val ivTabDriver: ImageView = findViewById(R.id.iv_tab_driver)
        val tvTabDriver: TextView = findViewById(R.id.tv_tab_driver)

        val colorActive = ContextCompat.getColor(this, R.color.text_on_yellow)
        val colorInactive = ContextCompat.getColor(this, R.color.text_heading)

        tabStudent.setOnClickListener {
            selectedRole = "estudiante"
            tabStudent.setBackgroundResource(R.drawable.bg_tab_active)
            tabDriver.setBackgroundResource(R.drawable.bg_tab_inactive)

            ivTabStudent.setColorFilter(colorActive)
            tvTabStudent.setTextColor(colorActive)
            tvTabStudent.setTypeface(tvTabStudent.typeface, Typeface.BOLD)

            ivTabDriver.setColorFilter(colorInactive)
            tvTabDriver.setTextColor(colorInactive)
            tvTabDriver.setTypeface(tvTabDriver.typeface, Typeface.NORMAL)
        }

        tabDriver.setOnClickListener {
            selectedRole = "conductor"
            tabDriver.setBackgroundResource(R.drawable.bg_tab_active)
            tabStudent.setBackgroundResource(R.drawable.bg_tab_inactive)

            ivTabDriver.setColorFilter(colorActive)
            tvTabDriver.setTextColor(colorActive)
            tvTabDriver.setTypeface(tvTabDriver.typeface, Typeface.BOLD)

            ivTabStudent.setColorFilter(colorInactive)
            tvTabStudent.setTextColor(colorInactive)
            tvTabStudent.setTypeface(tvTabStudent.typeface, Typeface.NORMAL)
        }

        // Input fields
        etFirstName = findViewById(R.id.et_register_first_name)
        etLastName = findViewById(R.id.et_register_last_name)
        etPhone = findViewById(R.id.et_register_phone)
        etEmail = findViewById(R.id.et_register_email)
        etPassword = findViewById(R.id.et_register_password)
        etConfirmPassword = findViewById(R.id.et_register_password_confirm)
        btnRegister = findViewById(R.id.btn_register)

        // Animaciones de entrada
        val slideUp1 = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in)
        val slideUp2 = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in).apply { startOffset = 50 }
        val slideUp3 = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in).apply { startOffset = 100 }
        val slideUp4 = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in).apply { startOffset = 150 }
        val slideUp5 = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in).apply { startOffset = 200 }
        val slideUp6 = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in).apply { startOffset = 250 }

        etFirstName.startAnimation(slideUp1)
        etLastName.startAnimation(slideUp2)
        etPhone.startAnimation(slideUp3)
        etEmail.startAnimation(slideUp4)
        etPassword.startAnimation(slideUp5)
        etConfirmPassword.startAnimation(slideUp6)

        // Observar cambios de estado del registro
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.authState.collectLatest { state ->
                    when {
                        state.isLoading -> {
                            btnRegister.isEnabled = false
                            btnRegister.alpha = 0.5f
                        }
                        state.isSuccess -> {
                            Toast.makeText(this@RegisterActivity, state.authResponse?.message ?: "Registro exitoso", Toast.LENGTH_SHORT).show()
                            // No ir a home, sino volver a login para verificar correo
                            authViewModel.clearState()
                            finish()
                        }
                        state.errorMessage != null -> {
                            btnRegister.isEnabled = true
                            btnRegister.alpha = 1f
                            Toast.makeText(this@RegisterActivity, state.errorMessage, Toast.LENGTH_SHORT).show()
                            authViewModel.clearState()
                        }
                    }
                }
            }
        }

        btnRegister.setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            // Validación básica
            var isValid = true

            if (firstName.isBlank()) {
                etFirstName.startAnimation(AnimationUtils.loadAnimation(this@RegisterActivity, R.anim.shake))
                isValid = false
            }
            if (lastName.isBlank()) {
                etLastName.startAnimation(AnimationUtils.loadAnimation(this@RegisterActivity, R.anim.shake))
                isValid = false
            }
            if (phone.isBlank()) {
                etPhone.startAnimation(AnimationUtils.loadAnimation(this@RegisterActivity, R.anim.shake))
                isValid = false
            }
            if (email.isBlank()) {
                etEmail.startAnimation(AnimationUtils.loadAnimation(this@RegisterActivity, R.anim.shake))
                isValid = false
            }
            if (password.isBlank()) {
                etPassword.startAnimation(AnimationUtils.loadAnimation(this@RegisterActivity, R.anim.shake))
                isValid = false
            }
            if (confirmPassword.isBlank()) {
                etConfirmPassword.startAnimation(AnimationUtils.loadAnimation(this@RegisterActivity, R.anim.shake))
                isValid = false
            }
            if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword) {
                etPassword.startAnimation(AnimationUtils.loadAnimation(this@RegisterActivity, R.anim.shake))
                etConfirmPassword.startAnimation(AnimationUtils.loadAnimation(this@RegisterActivity, R.anim.shake))
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                isValid = false
            }

            if (!isValid) return@setOnClickListener

            authViewModel.register(firstName, lastName, email, password, phone, selectedRole)
        }
    }
}
