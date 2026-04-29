package com.example.bd1.feature.auth.ui.activity

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bd1.R
import com.example.bd1.di.AppContainer
import com.example.bd1.feature.auth.ui.viewmodel.AuthViewModel
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var etEmail: TextInputEditText
    private lateinit var btnSendReset: TextView
    private lateinit var layoutRoot: android.view.View
    private lateinit var ivLogo: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var tvHint: TextView
    private lateinit var ivBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        authViewModel = AppContainer.authViewModel
        
        etEmail = findViewById(R.id.et_reset_email)
        btnSendReset = findViewById(R.id.btn_send_reset)
        layoutRoot = findViewById(R.id.layout_reset_root)
        ivLogo = findViewById(R.id.iv_reset_logo)
        tvTitle = findViewById(R.id.tv_reset_title)
        tvSubtitle = findViewById(R.id.tv_reset_subtitle)
        tvHint = findViewById(R.id.tv_reset_hint)
        ivBack = findViewById(R.id.iv_reset_back)

        etEmail.setText(intent.getStringExtra(EXTRA_EMAIL).orEmpty())
        if (etEmail.text?.isNotBlank() == true) {
            etEmail.setSelection(etEmail.text?.length ?: 0)
        }

        playIntroAnimations()

        ivBack.setOnClickListener {
            onBackPressed()
        }

        // Observar cambios de estado de autenticación
        lifecycleScope.launch {
            authViewModel.authState.collectLatest { state ->
                when {
                    state.isLoading -> {
                        btnSendReset.isEnabled = false
                        btnSendReset.text = "Enviando..."
                        btnSendReset.alpha = 0.5f
                    }
                    state.isSuccess -> {
                        btnSendReset.isEnabled = true
                        btnSendReset.text = "Enviar enlace"
                        btnSendReset.alpha = 1f
                        Toast.makeText(this@ResetPasswordActivity, "Enlace de recuperación enviado", Toast.LENGTH_LONG).show()
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            finish()
                            overridePendingTransition(R.anim.fade_scale_in, R.anim.reset_screen_exit)
                        }, 900)
                    }
                    state.errorMessage != null -> {
                        btnSendReset.isEnabled = true
                        btnSendReset.text = "Enviar enlace"
                        btnSendReset.alpha = 1f
                        Toast.makeText(this@ResetPasswordActivity, state.errorMessage, Toast.LENGTH_LONG).show()
                        authViewModel.clearState()
                    }
                }
            }
        }

        btnSendReset.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isEmpty()) {
                etEmail.startAnimation(AnimationUtils.loadAnimation(this@ResetPasswordActivity, R.anim.shake))
                return@setOnClickListener
            }

            authViewModel.resetPassword(email)
        }
    }

    private fun playIntroAnimations() {
        layoutRoot.alpha = 0f
        ivLogo.alpha = 0f
        ivLogo.scaleX = 0.88f
        ivLogo.scaleY = 0.88f
        tvTitle.alpha = 0f
        tvTitle.translationY = 24f
        tvSubtitle.alpha = 0f
        tvSubtitle.translationY = 24f
        tvHint.alpha = 0f
        btnSendReset.alpha = 0f
        btnSendReset.translationY = 20f
        ivBack.alpha = 0f

        layoutRoot.animate().alpha(1f).setDuration(180).start()
        ivBack.animate().alpha(1f).setStartDelay(60).setDuration(220).start()
        ivLogo.animate().alpha(1f).scaleX(1f).scaleY(1f).setStartDelay(110).setDuration(320).start()
        tvTitle.animate().alpha(1f).translationY(0f).setStartDelay(180).setDuration(260).start()
        tvSubtitle.animate().alpha(1f).translationY(0f).setStartDelay(230).setDuration(260).start()
        btnSendReset.animate().alpha(1f).translationY(0f).setStartDelay(320).setDuration(260).start()
        tvHint.animate().alpha(1f).setStartDelay(380).setDuration(220).start()
    }

    companion object {
        const val EXTRA_EMAIL = "extra_email"
    }
}
