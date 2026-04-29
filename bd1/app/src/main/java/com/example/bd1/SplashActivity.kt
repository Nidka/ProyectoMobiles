package com.example.bd1

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.example.bd1.feature.auth.ui.activity.LoginActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val ivSplashLogo = findViewById<ImageView>(R.id.iv_splash_logo)
        val progressBar = findViewById<ProgressBar>(R.id.pb_splash)
        progressBar.progress = 0

        // Apply pulse animation to logo
        val pulseAnim = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.pulse)
        ivSplashLogo.startAnimation(pulseAnim)

        // Animate progress bar over 2.5 seconds
        val handler = Handler(Looper.getMainLooper())
        val totalDuration = 2500L
        val steps = 100
        val stepDelay = totalDuration / steps

        var currentStep = 0
        val runnable = object : Runnable {
            override fun run() {
                if (currentStep <= steps) {
                    progressBar.progress = currentStep
                    currentStep++
                    handler.postDelayed(this, stepDelay)
                } else {
                    // Navigate to Login
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }
            }
        }
        handler.post(runnable)
    }
}
