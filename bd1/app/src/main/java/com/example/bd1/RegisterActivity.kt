package com.example.bd1

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.text.InputType
import android.view.Gravity
import android.widget.LinearLayout

class RegisterActivity : AppCompatActivity() {

    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var actvCountryCode: AutoCompleteTextView

    private lateinit var tilFirstName: TextInputLayout
    private lateinit var tilLastName: TextInputLayout
    private lateinit var tilCountryCode: TextInputLayout
    private lateinit var tilPhone: TextInputLayout
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout

    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        authManager = AuthManager(this)

        tilFirstName = findViewById(R.id.til_register_first_name)
        tilLastName = findViewById(R.id.til_register_last_name)
        tilCountryCode = findViewById(R.id.til_register_country_code)
        tilPhone = findViewById(R.id.til_register_phone)
        tilEmail = findViewById(R.id.til_register_email)
        tilPassword = findViewById(R.id.til_register_password)
        tilConfirmPassword = findViewById(R.id.til_register_password_confirm)

        etFirstName = findViewById(R.id.et_register_first_name)
        etLastName = findViewById(R.id.et_register_last_name)
        etPhone = findViewById(R.id.et_register_phone)
        etEmail = findViewById(R.id.et_register_email)
        etPassword = findViewById(R.id.et_register_password)
        etConfirmPassword = findViewById(R.id.et_register_password_confirm)
        actvCountryCode = findViewById(R.id.actv_register_country_code)

        val countries = resources.getStringArray(R.array.country_codes)
        actvCountryCode.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, countries)
        )
        actvCountryCode.setText(countries.firstOrNull().orEmpty(), false)

        val btnRegister: Button = findViewById(R.id.btn_register)
        val tvGoLogin: TextView = findViewById(R.id.tv_go_login)

        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        btnRegister.startAnimation(fadeIn)

        btnRegister.setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val countryCode = actvCountryCode.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            clearErrors()

            if (!validateFields(firstName, lastName, countryCode, phone, email, password, confirmPassword)) {
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                tilConfirmPassword.error = "Las contraseñas no coinciden"
                return@setOnClickListener
            }

            val fullPhone = "$countryCode$phone"
            val validation = authManager.validateRegisterData(
                firstName = firstName,
                lastName = lastName,
                email = email,
                phone = fullPhone,
                password = password
            )

            if (!validation.success) {
                if (validation.message.contains("Teléfono", ignoreCase = true)) {
                    tilPhone.error = validation.message
                } else {
                    Toast.makeText(this, validation.message, Toast.LENGTH_SHORT).show()
                }
                return@setOnClickListener
            }

            val loadingDialog = MaterialAlertDialogBuilder(this)
                .setTitle("Creando cuenta...")
                .setMessage("Por favor espera...")
                .setCancelable(false)
                .show()

            authManager.register(firstName, lastName, email, fullPhone, password) { result ->
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

        tvGoLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }



    private fun validateFields(
        firstName: String,
        lastName: String,
        countryCode: String,
        phone: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var valid = true

        if (firstName.isBlank()) {
            tilFirstName.error = "Ingresa tus nombres"
            valid = false
        }
        if (lastName.isBlank()) {
            tilLastName.error = "Ingresa tus apellidos"
            valid = false
        }
        if (countryCode.isBlank()) {
            tilCountryCode.error = "Selecciona código"
            valid = false
        }
        if (phone.isBlank()) {
            tilPhone.error = "Ingresa tu teléfono"
            valid = false
        } else if (!phone.matches(Regex("^\\d{7,15}$"))) {
            tilPhone.error = "Solo dígitos (7 a 15)"
            valid = false
        }
        if (email.isBlank()) {
            tilEmail.error = "Ingresa tu correo"
            valid = false
        }
        if (password.isBlank()) {
            tilPassword.error = "Ingresa una contraseña"
            valid = false
        }
        if (confirmPassword.isBlank()) {
            tilConfirmPassword.error = "Confirma tu contraseña"
            valid = false
        }

        return valid
    }

    private fun clearErrors() {
        tilFirstName.error = null
        tilLastName.error = null
        tilCountryCode.error = null
        tilPhone.error = null
        tilEmail.error = null
        tilPassword.error = null
        tilConfirmPassword.error = null
    }

    override fun onPause() {
        super.onPause()
        clearForm()
    }

    private fun clearForm() {
        etFirstName.setText("")
        etLastName.setText("")
        etPhone.setText("")
        val countries = resources.getStringArray(R.array.country_codes)
        actvCountryCode.setText(countries.firstOrNull().orEmpty(), false)
        etEmail.setText("")
        etPassword.setText("")
        etConfirmPassword.setText("")
        clearErrors()
    }
}
