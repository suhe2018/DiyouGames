package com.diyou.games.ui

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.diyou.games.data.CharacterType
import com.diyou.games.data.WeaponType
import com.diyou.games.databinding.ActivityGameBinding
import com.diyou.games.game.*

/**
 * Full-screen game activity. Hosts the GameView (SurfaceView) and
 * on-screen touch controls.
 */
class GameActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_P1_CHAR   = "p1_char"
        const val EXTRA_P1_WEAPON = "p1_weapon"
        const val EXTRA_P2_CHAR   = "p2_char"
        const val EXTRA_P2_WEAPON = "p2_weapon"
        const val EXTRA_GAME_MODE = "game_mode"
        const val EXTRA_AI_DIFF   = "ai_diff"
    }

    private lateinit var binding: ActivityGameBinding
    private lateinit var gameView: GameView

    // Track per-finger touch areas for the control pads
    private val p1Input = InputState()
    private val p2Input = InputState()

    // Active pointer IDs for each button group
    private val p1ActivePointers = mutableSetOf<Int>()
    private val p2ActivePointers = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableImmersiveMode()
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val p1Char   = CharacterType.values()[intent.getIntExtra(EXTRA_P1_CHAR, 0)]
        val p1Weapon = WeaponType.values()[intent.getIntExtra(EXTRA_P1_WEAPON, 0)]
        val p2Char   = CharacterType.values()[intent.getIntExtra(EXTRA_P2_CHAR, 2)]
        val p2Weapon = WeaponType.values()[intent.getIntExtra(EXTRA_P2_WEAPON, 0)]
        val gameMode = GameMode.values()[intent.getIntExtra(EXTRA_GAME_MODE, 0)]
        val aiDiff   = AIDifficulty.values()[intent.getIntExtra(EXTRA_AI_DIFF, AIDifficulty.MEDIUM.ordinal)]

        gameView = GameView(
            context      = this,
            p1Char       = p1Char,
            p1Weapon     = p1Weapon,
            p2Char       = p2Char,
            p2Weapon     = p2Weapon,
            gameMode     = gameMode,
            aiDifficulty = aiDiff
        )

        // Insert game view behind HUD controls
        binding.gameContainer.addView(gameView, 0)

        setupControls(gameMode)
        setupGameCallbacks()

        binding.btnPause.setOnClickListener { gameView.togglePause() }
        binding.btnBack.setOnClickListener { confirmExit() }
    }

    // ─── Touch Controls ───────────────────────────────────────────────────────

    private fun setupControls(mode: GameMode) {
        // Show P2 controls only for local vs-player mode
        binding.p2Controls.visibility = if (mode == GameMode.VS_PLAYER) View.VISIBLE else View.GONE

        setupP1Controls()
        if (mode == GameMode.VS_PLAYER) setupP2Controls()
    }

    private fun setupP1Controls() {
        // D-pad left
        binding.p1BtnLeft.setOnTouchListener { _, e ->
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    p1Input.moveLeft = true; p1Input.moveRight = false
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                    p1Input.moveLeft = false
                }
            }
            gameView.setP1Input(p1Input); true
        }
        // D-pad right
        binding.p1BtnRight.setOnTouchListener { _, e ->
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    p1Input.moveRight = true; p1Input.moveLeft = false
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                    p1Input.moveRight = false
                }
            }
            gameView.setP1Input(p1Input); true
        }
        // Jump
        binding.p1BtnJump.setOnTouchListener { _, e ->
            p1Input.jump = e.actionMasked in listOf(
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN)
            gameView.setP1Input(p1Input); true
        }
        // Attack
        binding.p1BtnAttack.setOnTouchListener { _, e ->
            p1Input.attack = e.actionMasked in listOf(
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN)
            if (p1Input.attack) p1Input.special = false
            gameView.setP1Input(p1Input); true
        }
        // Block
        binding.p1BtnBlock.setOnTouchListener { _, e ->
            p1Input.block = e.actionMasked in listOf(
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN)
            gameView.setP1Input(p1Input); true
        }
        // Special
        binding.p1BtnSpecial.setOnTouchListener { _, e ->
            p1Input.special = e.actionMasked in listOf(
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN)
            if (p1Input.special) p1Input.attack = false
            gameView.setP1Input(p1Input); true
        }
    }

    private fun setupP2Controls() {
        binding.p2BtnLeft.setOnTouchListener { _, e ->
            p2Input.moveLeft = e.actionMasked in listOf(
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN)
            gameView.setP2Input(p2Input); true
        }
        binding.p2BtnRight.setOnTouchListener { _, e ->
            p2Input.moveRight = e.actionMasked in listOf(
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN)
            gameView.setP2Input(p2Input); true
        }
        binding.p2BtnJump.setOnTouchListener { _, e ->
            p2Input.jump = e.actionMasked in listOf(
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN)
            gameView.setP2Input(p2Input); true
        }
        binding.p2BtnAttack.setOnTouchListener { _, e ->
            p2Input.attack = e.actionMasked in listOf(
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN)
            gameView.setP2Input(p2Input); true
        }
        binding.p2BtnBlock.setOnTouchListener { _, e ->
            p2Input.block = e.actionMasked in listOf(
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN)
            gameView.setP2Input(p2Input); true
        }
        binding.p2BtnSpecial.setOnTouchListener { _, e ->
            p2Input.special = e.actionMasked in listOf(
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN)
            gameView.setP2Input(p2Input); true
        }
    }

    // ─── Game Callbacks ───────────────────────────────────────────────────────

    private fun setupGameCallbacks() {
        gameView.onGameOver = { winner ->
            val engine = gameView.getBattleEngine()
            if (engine != null) {
                runOnUiThread {
                    val intent = Intent(this, ResultActivity::class.java).apply {
                        putExtra(ResultActivity.EXTRA_WINNER, winner)
                        putExtra(ResultActivity.EXTRA_P1_WINS, engine.p1RoundWins)
                        putExtra(ResultActivity.EXTRA_P2_WINS, engine.p2RoundWins)
                        putExtra(ResultActivity.EXTRA_P1_DMG,  engine.player1.damageDealt)
                        putExtra(ResultActivity.EXTRA_P2_DMG,  engine.player2.damageDealt)
                        putExtra(ResultActivity.EXTRA_P1_CHAR, engine.player1.characterType.ordinal)
                        putExtra(ResultActivity.EXTRA_P2_CHAR, engine.player2.characterType.ordinal)
                    }
                    startActivity(intent)
                    finish()
                }
            }
        }

        gameView.onPauseToggle = { paused ->
            runOnUiThread {
                binding.btnPause.text = if (paused) "▶" else "⏸"
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (!gameView.isPaused) gameView.togglePause()
    }

    override fun onResume() {
        super.onResume()
        enableImmersiveMode()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) enableImmersiveMode()
    }

    private fun confirmExit() {
        val wasPaused = gameView.isPaused
        if (!wasPaused) gameView.togglePause()
        AlertDialog.Builder(this)
            .setTitle("Quit Match")
            .setMessage("Abandon this match and return to the main menu?")
            .setPositiveButton("Main Menu") { _, _ -> finish() }
            .setNegativeButton("Keep Playing") { _, _ ->
                if (!wasPaused) gameView.togglePause()
            }
            .setOnCancelListener {
                if (!wasPaused) gameView.togglePause()
            }
            .show()
    }

    private fun enableImmersiveMode() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        )
    }
}
