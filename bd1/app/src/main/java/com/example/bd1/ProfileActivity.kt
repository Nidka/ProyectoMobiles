package com.example.bd1

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class ProfileActivity : AppCompatActivity() {

    private lateinit var etFirstName: TextInputEditText
    private lateinit var etLastName: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etCurrentPassword: TextInputEditText
    private lateinit var etNewPassword: TextInputEditText
    private lateinit var ivAvatar: ImageView
    private lateinit var authManager: AuthManager
    private lateinit var imageStorageManager: ImageStorageManager

    private var selectedPhotoPath: String? = null

    private val photoPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@registerForActivityResult
        val savedPath = imageStorageManager.persistImage(uri)
        if (savedPath.isNullOrBlank()) {
            Toast.makeText(this, "No se pudo guardar la foto", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }
        selectedPhotoPath = savedPath
        renderAvatar(savedPath, true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        authManager = AuthManager(this)
        imageStorageManager = ImageStorageManager(this)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar_profile)
        toolbar.setNavigationOnClickListener { finish() }

        etFirstName = findViewById(R.id.et_profile_first_name)
        etLastName = findViewById(R.id.et_profile_last_name)
        etPhone = findViewById(R.id.et_profile_phone)
        etEmail = findViewById(R.id.et_profile_email)
        etCurrentPassword = findViewById(R.id.et_profile_password_current)
        etNewPassword = findViewById(R.id.et_profile_password_new)
        ivAvatar = findViewById(R.id.iv_profile_avatar)

        val btnSaveProfile: MaterialButton = findViewById(R.id.btn_profile_save)
        val btnSavePassword: MaterialButton = findViewById(R.id.btn_profile_password)
        val btnChangePhoto: MaterialButton = findViewById(R.id.btn_profile_photo)
        val btnRemovePhoto: MaterialButton = findViewById(R.id.btn_profile_remove_photo)

        etFirstName.setText(authManager.getCurrentUserFirstName())
        etLastName.setText(authManager.getCurrentUserLastName())
        etPhone.setText(authManager.getCurrentUserPhone())
        etEmail.setText(authManager.getCurrentUserEmail())
        selectedPhotoPath = authManager.getCurrentUserPhotoUri().ifBlank { null }
        renderAvatar(selectedPhotoPath, false)

        btnChangePhoto.setOnClickListener {
            photoPicker.launch("image/*")
        }

        btnRemovePhoto.setOnClickListener {
            selectedPhotoPath = ""
            renderAvatar(selectedPhotoPath, true)
            Toast.makeText(this, "Foto eliminada. Guarda perfil para confirmar", Toast.LENGTH_SHORT)
                .show()
        }

        btnSaveProfile.setOnClickListener {
            val result = authManager.updateProfile(
                firstName = etFirstName.text?.toString().orEmpty(),
                lastName = etLastName.text?.toString().orEmpty(),
                phone = etPhone.text?.toString().orEmpty(),
                photoUri = selectedPhotoPath
            )
            Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
        }

        btnSavePassword.setOnClickListener {
            val currentPassword = etCurrentPassword.text?.toString().orEmpty()
            val newPassword = etNewPassword.text?.toString().orEmpty()
            
            authManager.updatePassword(currentPassword, newPassword) { result ->
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                if (result.success) {
                    etCurrentPassword.setText("")
                    etNewPassword.setText("")
                }
            }
        }
    }

    private fun renderAvatar(photoPath: String?, animate: Boolean) {
        val avatar = AvatarImageLoader.loadCircular(this, photoPath, R.drawable.ic_avatar_placeholder)
        
        if (animate) {
            ivAvatar.animate()
                .alpha(0f)
                .setDuration(150)
                .withEndAction {
                    if (avatar == null) {
                        ivAvatar.setImageResource(R.drawable.ic_avatar_placeholder)
                    } else {
                        ivAvatar.setImageBitmap(avatar)
                    }
                    ivAvatar.animate()
                        .alpha(1f)
                        .setDuration(150)
                        .start()
                }.start()
        } else {
            if (avatar == null) {
                ivAvatar.setImageResource(R.drawable.ic_avatar_placeholder)
            } else {
                ivAvatar.setImageBitmap(avatar)
            }
        }
    }
}
