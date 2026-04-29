package com.example.bd1.feature.profile.ui.activity

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bd1.AvatarImageLoader
import com.example.bd1.ImageStorageManager
import com.example.bd1.R
import com.example.bd1.di.AppContainer
import com.example.bd1.feature.profile.domain.model.Profile
import com.example.bd1.feature.profile.ui.viewmodel.ProfileViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var etFirstName: TextInputEditText
    private lateinit var etLastName: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var ivAvatar: ImageView
    private lateinit var btnSaveProfile: MaterialButton
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

        profileViewModel = AppContainer.profileViewModel
        imageStorageManager = ImageStorageManager(this)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar_profile)
        toolbar.setNavigationOnClickListener { finish() }

        etFirstName = findViewById(R.id.et_profile_first_name)
        etLastName = findViewById(R.id.et_profile_last_name)
        etPhone = findViewById(R.id.et_profile_phone)
        etEmail = findViewById(R.id.et_profile_email)
        ivAvatar = findViewById(R.id.iv_profile_avatar)

        btnSaveProfile = findViewById(R.id.btn_profile_save)
        val btnChangePhoto: MaterialButton = findViewById(R.id.btn_profile_photo)
        val btnRemovePhoto: MaterialButton = findViewById(R.id.btn_profile_remove_photo)

        // Cargar perfil
        profileViewModel.loadProfile()

        // Observar cambios de estado del perfil
        lifecycleScope.launch {
            profileViewModel.profileState.collectLatest { state ->
                when {
                    state.isLoading -> {
                        btnSaveProfile.isEnabled = false
                        btnSaveProfile.alpha = 0.5f
                    }
                    state.profile != null -> {
                        val profile = state.profile
                        etFirstName.setText(profile.firstName)
                        etLastName.setText(profile.lastName)
                        etPhone.setText(profile.phone)
                        etEmail.setText(profile.email)
                        selectedPhotoPath = profile.photoUri.ifBlank { null }
                        renderAvatar(selectedPhotoPath, false)
                        btnSaveProfile.isEnabled = true
                        btnSaveProfile.alpha = 1f
                    }
                    state.errorMessage != null -> {
                        btnSaveProfile.isEnabled = true
                        btnSaveProfile.alpha = 1f
                        Toast.makeText(this@ProfileActivity, state.errorMessage, Toast.LENGTH_SHORT).show()
                        profileViewModel.clearState()
                    }
                }
            }
        }

        btnChangePhoto.setOnClickListener {
            photoPicker.launch("image/*")
        }

        btnRemovePhoto.setOnClickListener {
            selectedPhotoPath = ""
            renderAvatar(selectedPhotoPath, true)
            Toast.makeText(this, "Foto eliminada. Guarda perfil para confirmar", Toast.LENGTH_SHORT).show()
        }

        btnSaveProfile.setOnClickListener {
            val profile = Profile(
                firstName = etFirstName.text?.toString().orEmpty(),
                lastName = etLastName.text?.toString().orEmpty(),
                email = etEmail.text?.toString().orEmpty(),
                phone = etPhone.text?.toString().orEmpty(),
                photoUri = selectedPhotoPath.orEmpty()
            )
            profileViewModel.updateProfile(profile)
        }
    }

    private fun renderAvatar(photoUri: String?, success: Boolean) {
        val avatar = AvatarImageLoader.loadCircular(this, photoUri, R.drawable.ic_avatar_placeholder)
        
        if (avatar == null) {
            ivAvatar.setImageResource(R.drawable.ic_avatar_placeholder)
        } else {
            ivAvatar.setImageBitmap(avatar)
        }
        
        if (success) {
            Toast.makeText(this, "Foto actualizada", Toast.LENGTH_SHORT).show()
        }
    }
}
