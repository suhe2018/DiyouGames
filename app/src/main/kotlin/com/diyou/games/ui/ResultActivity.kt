package com.diyou.games.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.diyou.games.data.CharacterType
import com.diyou.games.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_WINNER  = "winner"
        const val EXTRA_P1_WINS = "p1_wins"
        const val EXTRA_P2_WINS = "p2_wins"
        const val EXTRA_P1_DMG  = "p1_dmg"
        const val EXTRA_P2_DMG  = "p2_dmg"
        const val EXTRA_P1_CHAR = "p1_char"
        const val EXTRA_P2_CHAR = "p2_char"
    }

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableImmersiveMode()
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val winner  = intent.getIntExtra(EXTRA_WINNER, 0)
        val p1Wins  = intent.getIntExtra(EXTRA_P1_WINS, 0)
        val p2Wins  = intent.getIntExtra(EXTRA_P2_WINS, 0)
        val p1Dmg   = intent.getFloatExtra(EXTRA_P1_DMG, 0f)
        val p2Dmg   = intent.getFloatExtra(EXTRA_P2_DMG, 0f)
        val p1Char  = CharacterType.values()[intent.getIntExtra(EXTRA_P1_CHAR, 0)]
        val p2Char  = CharacterType.values()[intent.getIntExtra(EXTRA_P2_CHAR, 2)]

        // Winner headline
        binding.tvResultTitle.text = when (winner) {
            1    -> "${p1Char.displayName} Wins!"
            2    -> "${p2Char.displayName} Wins!"
            else -> "Draw!"
        }
        binding.tvResultSubtitle.text = when (winner) {
            1    -> "P1 Victory"
            2    -> "P2 / AI Victory"
            else -> "Even Match"
        }

        // Round score
        binding.tvRoundScore.text = "$p1Wins  :  $p2Wins"

        // Stats
        binding.tvP1Stats.text = buildString {
            appendLine(p1Char.displayName)
            appendLine("Rounds Won: $p1Wins")
            appendLine("Total Dmg: ${p1Dmg.toInt()}")
        }
        binding.tvP2Stats.text = buildString {
            appendLine(p2Char.displayName)
            appendLine("Rounds Won: $p2Wins")
            appendLine("Total Dmg: ${p2Dmg.toInt()}")
        }

        // Animate result in
        animateResult()

        binding.btnRematch.setOnClickListener { finish() }
        binding.btnMainMenu.setOnClickListener {
            val intent = Intent(this, MainMenuActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
            finish()
        }
    }

    private fun animateResult() {
        binding.tvResultTitle.apply {
            alpha = 0f
            scaleX = 0.5f
            scaleY = 0.5f
            animate()
                .alpha(1f).scaleX(1f).scaleY(1f)
                .setDuration(500)
                .start()
        }
        binding.statsCard.apply {
            alpha = 0f
            translationY = 40f
            animate()
                .alpha(1f).translationY(0f)
                .setStartDelay(350)
                .setDuration(400)
                .start()
        }
        binding.btnRematch.apply {
            alpha = 0f
            animate().alpha(1f).setStartDelay(600).setDuration(300).start()
        }
        binding.btnMainMenu.apply {
            alpha = 0f
            animate().alpha(1f).setStartDelay(700).setDuration(300).start()
        }
    }

    private fun enableImmersiveMode() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        )
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) enableImmersiveMode()
    }
}
