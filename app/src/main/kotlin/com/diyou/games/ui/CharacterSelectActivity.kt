package com.diyou.games.ui

import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.diyou.games.R
import com.diyou.games.data.CharacterType
import com.diyou.games.data.WeaponType
import com.diyou.games.databinding.ActivityCharacterSelectBinding
import com.diyou.games.game.AIDifficulty
import com.diyou.games.game.GameMode

class CharacterSelectActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MODE = "game_mode"
    }

    private lateinit var binding: ActivityCharacterSelectBinding

    private val gameMode: GameMode by lazy {
        when (intent.getStringExtra(EXTRA_MODE)) {
            "VS_PLAYER" -> GameMode.VS_PLAYER
            else        -> GameMode.VS_AI
        }
    }

    private var p1CharIndex  = 0
    private var p1WeaponIndex = 0
    private var p2CharIndex  = 2
    private var p2WeaponIndex = 0
    private var aiDifficulty = AIDifficulty.MEDIUM

    private val characters = CharacterType.values()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableImmersiveMode()
        binding = ActivityCharacterSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupP1Controls()
        setupP2Controls()
        setupDifficultySelector()
        setupFightButton()
        updateP1UI()
        updateP2UI()

        // Show/hide P2 title based on mode
        binding.labelP2.text = if (gameMode == GameMode.VS_AI) "AI Opponent" else "Player 2"
        binding.difficultyGroup.visibility =
            if (gameMode == GameMode.VS_AI) View.VISIBLE else View.GONE
    }

    // ─── P1 Controls ─────────────────────────────────────────────────────────

    private fun setupP1Controls() {
        binding.p1PrevChar.setOnClickListener {
            p1CharIndex = (p1CharIndex - 1 + characters.size) % characters.size
            p1WeaponIndex = 0
            updateP1UI()
        }
        binding.p1NextChar.setOnClickListener {
            p1CharIndex = (p1CharIndex + 1) % characters.size
            p1WeaponIndex = 0
            updateP1UI()
        }
        binding.p1PrevWeapon.setOnClickListener {
            val weapons = WeaponType.forCharacterClass(characters[p1CharIndex])
            p1WeaponIndex = (p1WeaponIndex - 1 + weapons.size) % weapons.size
            updateP1UI()
        }
        binding.p1NextWeapon.setOnClickListener {
            val weapons = WeaponType.forCharacterClass(characters[p1CharIndex])
            p1WeaponIndex = (p1WeaponIndex + 1) % weapons.size
            updateP1UI()
        }
    }

    // ─── P2 Controls ─────────────────────────────────────────────────────────

    private fun setupP2Controls() {
        binding.p2PrevChar.setOnClickListener {
            p2CharIndex = (p2CharIndex - 1 + characters.size) % characters.size
            p2WeaponIndex = 0
            updateP2UI()
        }
        binding.p2NextChar.setOnClickListener {
            p2CharIndex = (p2CharIndex + 1) % characters.size
            p2WeaponIndex = 0
            updateP2UI()
        }
        binding.p2PrevWeapon.setOnClickListener {
            val weapons = WeaponType.forCharacterClass(characters[p2CharIndex])
            p2WeaponIndex = (p2WeaponIndex - 1 + weapons.size) % weapons.size
            updateP2UI()
        }
        binding.p2NextWeapon.setOnClickListener {
            val weapons = WeaponType.forCharacterClass(characters[p2CharIndex])
            p2WeaponIndex = (p2WeaponIndex + 1) % weapons.size
            updateP2UI()
        }
    }

    // ─── Difficulty ───────────────────────────────────────────────────────────

    private fun setupDifficultySelector() {
        val diffButtons = listOf(
            binding.btnDiffEasy   to AIDifficulty.EASY,
            binding.btnDiffMedium to AIDifficulty.MEDIUM,
            binding.btnDiffHard   to AIDifficulty.HARD,
            binding.btnDiffExpert to AIDifficulty.EXPERT
        )
        diffButtons.forEach { (btn, diff) ->
            btn.text = diff.displayName
            btn.setOnClickListener {
                aiDifficulty = diff
                diffButtons.forEach { (b, _) ->
                    b.isSelected = b == btn
                }
            }
        }
        binding.btnDiffMedium.isSelected = true
    }

    // ─── UI Updates ───────────────────────────────────────────────────────────

    private fun updateP1UI() {
        val char = characters[p1CharIndex]
        val weapons = WeaponType.forCharacterClass(char)
        val weapon = weapons[p1WeaponIndex]

        binding.p1CharName.text   = "${char.displayName}  ${char.displayNameEn}"
        binding.p1CharDesc.text   = char.description
        binding.p1WeaponName.text = "${weapon.displayName}  ${weapon.displayNameEn}"
        binding.p1CharSprite.setImageBitmap(generateCharSprite(char, weapon))
        binding.p1SpecialName.text = "Special: ${char.specialName}"
        binding.p1SpecialDesc.text = char.specialDesc
        updateStatBars(
            binding.p1Stats.statHp,
            binding.p1Stats.statSpd,
            binding.p1Stats.statDef,
            char
        )
    }

    private fun updateP2UI() {
        val char = characters[p2CharIndex]
        val weapons = WeaponType.forCharacterClass(char)
        val weapon = weapons[p2WeaponIndex]

        binding.p2CharName.text   = "${char.displayName}  ${char.displayNameEn}"
        binding.p2CharDesc.text   = char.description
        binding.p2WeaponName.text = "${weapon.displayName}  ${weapon.displayNameEn}"
        binding.p2CharSprite.setImageBitmap(generateCharSprite(char, weapon, flipped = true))
        binding.p2SpecialName.text = "Special: ${char.specialName}"
        binding.p2SpecialDesc.text = char.specialDesc
        updateStatBars(
            binding.p2Stats.statHp,
            binding.p2Stats.statSpd,
            binding.p2Stats.statDef,
            char
        )
    }

    private fun updateStatBars(
        hpView: View, spdView: View, defView: View,
        char: CharacterType
    ) {
        // Scale width based on stars (1-5 → 20%-100%)
        fun scaleBar(view: View, stars: Int) {
            val parent = (view.parent as View)
            view.post {
                val maxWidth = parent.width
                view.layoutParams = view.layoutParams.also {
                    it.width = (maxWidth * stars / 5f).toInt()
                }
                view.requestLayout()
            }
        }
        scaleBar(hpView,  char.hpStars)
        scaleBar(spdView, char.speedStars)
        scaleBar(defView, char.defenseStars)
    }

    // ─── Character Sprite (pixel art generated) ───────────────────────────────

    private fun generateCharSprite(
        char: CharacterType,
        weapon: WeaponType,
        flipped: Boolean = false,
        size: Int = 96
    ): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint().apply { isAntiAlias = false }
        val s = size / 24f  // pixel scale

        fun rect(x: Float, y: Float, w: Float, h: Float, color: Int) {
            paint.color = color
            canvas.drawRect(
                (x * s), ((y + 4) * s),
                ((x + w) * s), ((y + h + 4) * s),
                paint
            )
        }

        // Body parts
        rect(7f,  0f, 10f, 3f,  char.hairColor)   // hair
        rect(8f,  3f, 8f,  8f,  char.skinColor)   // head
        rect(9f,  5f, 2f,  1f,  char.eyeColor)    // left eye
        rect(13f, 5f, 2f,  1f,  char.eyeColor)    // right eye
        rect(6f,  11f, 12f, 10f, char.armorColor) // torso
        rect(8f,  11f, 8f,  1f,  char.accentColor) // armor stripe
        rect(5f,  21f, 5f,  9f,  char.bodyColor)  // left leg
        rect(14f, 21f, 5f,  9f,  char.bodyColor)  // right leg
        rect(3f,  11f, 3f,  8f,  char.skinColor)  // left arm
        rect(18f, 11f, 3f,  8f,  char.skinColor)  // right arm

        // Weapon
        rect(19f, 9f, 2f, 12f, weapon.color)

        if (flipped) {
            val matrix = Matrix()
            matrix.postScale(-1f, 1f, size / 2f, size / 2f)
            return Bitmap.createBitmap(bmp, 0, 0, size, size, matrix, false)
        }
        return bmp
    }

    // ─── Fight Button ─────────────────────────────────────────────────────────

    private fun setupFightButton() {
        binding.btnFight.setOnClickListener {
            val p1Char   = characters[p1CharIndex]
            val p1Weapon = WeaponType.forCharacterClass(p1Char)[p1WeaponIndex]
            val p2Char   = characters[p2CharIndex]
            val p2Weapon = WeaponType.forCharacterClass(p2Char)[p2WeaponIndex]

            val intent = Intent(this, GameActivity::class.java).apply {
                putExtra(GameActivity.EXTRA_P1_CHAR,   p1Char.ordinal)
                putExtra(GameActivity.EXTRA_P1_WEAPON, p1Weapon.ordinal)
                putExtra(GameActivity.EXTRA_P2_CHAR,   p2Char.ordinal)
                putExtra(GameActivity.EXTRA_P2_WEAPON, p2Weapon.ordinal)
                putExtra(GameActivity.EXTRA_GAME_MODE, gameMode.ordinal)
                putExtra(GameActivity.EXTRA_AI_DIFF,   aiDifficulty.ordinal)
            }
            startActivity(intent)
        }

        binding.btnBack.setOnClickListener { finish() }
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
