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
import androidx.core.content.ContextCompat
import android.graphics.Typeface
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class RegisterActivity : AppCompatActivity() {

    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText

    private lateinit var authManager: AuthManager

    private var selectedRole = "estudiante"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        authManager = AuthManager(this)

        // Back button
        val ivBack: ImageView = findViewById(R.id.iv_back)
        ivBack.setOnClickListener { finish() }

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
        etEmail = findViewById(R.id.et_register_email)
        etPassword = findViewById(R.id.et_register_password)
        etConfirmPassword = findViewById(R.id.et_register_password_confirm)

        val btnRegister: TextView = findViewById(R.id.btn_register)

        // Staggered Entrance Animations for form elements
        val slideUp1 = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in)
        val slideUp2 = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in).apply { startOffset = 50 }
        val slideUp3 = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in).apply { startOffset = 100 }
        val slideUp4 = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in).apply { startOffset = 150 }
        val slideUp5 = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in).apply { startOffset = 200 }
        val slideUp6 = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in).apply { startOffset = 250 }

        etFirstName.startAnimation(slideUp1)
        etLastName.startAnimation(slideUp2)
        etEmail.startAnimation(slideUp3)
        etPassword.startAnimation(slideUp4)
        etConfirmPassword.startAnimation(slideUp5)
        btnRegister.startAnimation(slideUp6)

        btnRegister.setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            // Basic validation
            var isValid = true
            
            if (firstName.isBlank()) {
                etFirstName.startAnimation(AnimationUtils.loadAnimation(this@RegisterActivity, R.anim.shake))
                isValid = false
            }
            if (lastName.isBlank()) {
                etLastName.startAnimation(AnimationUtils.loadAnimation(this@RegisterActivity, R.anim.shake))
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

            val validation = authManager.validateRegisterData(
                firstName = firstName,
                lastName = lastName,
                email = email,
                phone = "",
                password = password
            )

            if (!validation.success) {
                Toast.makeText(this, validation.message, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val loadingDialog = MaterialAlertDialogBuilder(this)
                .setTitle("Creando cuenta...")
                .setMessage("Por favor espera...")
                .setCancelable(false)
                .show()

            authManager.register(firstName, lastName, email, "", password) { result ->
                loadingDialog.dismiss()
                if (result.success) {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Verifica tu correo")
                        .setMessage(result.message)
                        .setCancelable(false)
                        .setPositiveButton("Ir al Login") { _, _ ->
                            finish()
                        }
                        .show()
                } else {
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        clearForm()
    }

    private fun clearForm() {
        etFirstName.setText("")
        etLastName.setText("")
        etEmail.setText("")
        etPassword.setText("")
        etConfirmPassword.setText("")
    }
}
