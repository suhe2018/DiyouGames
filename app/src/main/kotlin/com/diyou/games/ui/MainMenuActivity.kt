package com.diyou.games.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.diyou.games.databinding.ActivityMainMenuBinding

class MainMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableImmersiveMode()
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButtons()
        startMenuAnimation()
    }

    private fun setupButtons() {
        binding.btnVsAi.setOnClickListener {
            animateButtonPress(it) {
                startActivity(
                    Intent(this, CharacterSelectActivity::class.java)
                        .putExtra(CharacterSelectActivity.EXTRA_MODE, "VS_AI")
                )
            }
        }

        binding.btnVsPlayer.setOnClickListener {
            animateButtonPress(it) {
                startActivity(
                    Intent(this, CharacterSelectActivity::class.java)
                        .putExtra(CharacterSelectActivity.EXTRA_MODE, "VS_PLAYER")
                )
            }
        }

        binding.btnSettings.setOnClickListener {
            animateButtonPress(it) {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }
    }

    private fun animateButtonPress(view: View, action: () -> Unit) {
        view.animate()
            .scaleX(0.92f).scaleY(0.92f)
            .setDuration(80)
            .withEndAction {
                view.animate()
                    .scaleX(1f).scaleY(1f)
                    .setDuration(80)
                    .withEndAction(action)
                    .start()
            }.start()
    }

    private fun startMenuAnimation() {
        // Animate logo in
        binding.logoText.apply {
            alpha = 0f
            translationY = -60f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .start()
        }

        // Animate buttons in with stagger
        val buttons = listOf(binding.btnVsAi, binding.btnVsPlayer, binding.btnSettings)
        buttons.forEachIndexed { i, btn ->
            btn.apply {
                alpha = 0f
                translationX = -80f
                animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setStartDelay(300L + i * 120L)
                    .setDuration(350)
                    .start()
            }
        }
    }

    private fun enableImmersiveMode() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        )
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) enableImmersiveMode()
    }
}
