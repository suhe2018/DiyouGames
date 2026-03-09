package com.diyou.games.game

import com.diyou.games.data.CharacterType
import com.diyou.games.data.WeaponType

/**
 * Manages game mode and round flow.
 */
enum class GameMode {
    VS_AI,      // Single player vs computer
    VS_PLAYER   // Local 2-player on same device
}

enum class RoundResult { PLAYER1_WIN, PLAYER2_WIN, DRAW, ONGOING }

data class HitEffect(
    var x: Float,
    var y: Float,
    var frames: Int,
    val isSpecial: Boolean,
    val damage: Float
) {
    val alive: Boolean get() = frames > 0
}

/**
 * Core battle engine: owns the two fighters, resolves combat, tracks rounds.
 */
class BattleEngine(
    p1Char: CharacterType, p1Weapon: WeaponType,
    p2Char: CharacterType, p2Weapon: WeaponType,
    val gameMode: GameMode,
    val aiDifficulty: AIDifficulty = AIDifficulty.MEDIUM,
    screenWidth: Int, screenHeight: Int
) {
    val groundY: Float = screenHeight * GameConfig.GROUND_Y_RATIO

    val player1 = Fighter(p1Char, p1Weapon, isPlayerOne = true).also { it.y = groundY }
    val player2 = Fighter(p2Char, p2Weapon, isPlayerOne = false).also { it.y = groundY }

    private val ai: FighterAI? = if (gameMode == GameMode.VS_AI)
        FighterAI(player2, aiDifficulty) else null

    // Scale factor from virtual to actual screen
    val scaleX: Float = screenWidth.toFloat()  / GameConfig.VIRTUAL_WIDTH
    val scaleY: Float = screenHeight.toFloat() / GameConfig.VIRTUAL_HEIGHT

    // Round tracking
    var p1RoundWins = 0
    var p2RoundWins = 0
    var currentRound = 1
    var roundTimeFrames = GameConfig.ROUND_TIME_SECONDS * GameConfig.TARGET_FPS
    var roundResult: RoundResult = RoundResult.ONGOING
    var roundEndDelayFrames = 0

    // Pending hitboxes (attack frames that need to be checked)
    private var p1Hitbox: AttackHitbox? = null
    private var p2Hitbox: AttackHitbox? = null

    // Visual hit effects
    val hitEffects = mutableListOf<HitEffect>()

    // Input state (set externally)
    var p1Input = InputState()
    var p2Input = InputState()

    var isPaused = false
    var isGameOver = false

    fun update() {
        if (isPaused || isGameOver) return

        if (roundResult != RoundResult.ONGOING) {
            roundEndDelayFrames--
            if (roundEndDelayFrames <= 0) startNextRound()
            return
        }

        // Timer
        if (roundTimeFrames > 0) roundTimeFrames--
        if (roundTimeFrames <= 0) resolveTimeUp()

        // Process player 1 input
        processInput(player1, p1Input) { box -> p1Hitbox = box }

        // AI or player 2 input
        if (ai != null) {
            val aiAction = ai.update(player1, groundY)
            val aiInput = InputState(
                moveLeft  = aiAction.moveDirection < 0,
                moveRight = aiAction.moveDirection > 0,
                jump      = aiAction.jump,
                attack    = aiAction.attack,
                block     = aiAction.block,
                special   = aiAction.special
            )
            processInput(player2, aiInput) { box -> p2Hitbox = box }
        } else {
            processInput(player2, p2Input) { box -> p2Hitbox = box }
        }

        // Collision / hit detection
        p1Hitbox?.let { hb ->
            if (player2.overlaps(hb)) {
                player2.receiveDamage(hb)
                player1.damageDealt += hb.damage
                spawnHitEffect(player2.x, player2.y - GameConfig.CHAR_HEIGHT / 2f, hb)
            }
            p1Hitbox = null
        }
        p2Hitbox?.let { hb ->
            if (player1.overlaps(hb)) {
                player1.receiveDamage(hb)
                player2.damageDealt += hb.damage
                spawnHitEffect(player1.x, player1.y - GameConfig.CHAR_HEIGHT / 2f, hb)
            }
            p2Hitbox = null
        }

        // Face each other
        if (!player1.isAttacking) player1.facingRight = player2.x > player1.x
        if (!player2.isAttacking) player2.facingRight = player1.x > player2.x

        // Update physics & animation
        player1.updatePhysics(groundY)
        player2.updatePhysics(groundY)
        player1.updateAnimation()
        player2.updateAnimation()

        // Hit effects
        hitEffects.forEach { it.frames-- }
        hitEffects.removeAll { !it.alive }

        // Check KO
        when {
            !player1.isAlive && !player2.isAlive -> endRound(RoundResult.DRAW)
            !player1.isAlive -> endRound(RoundResult.PLAYER2_WIN)
            !player2.isAlive -> endRound(RoundResult.PLAYER1_WIN)
        }
    }

    private fun processInput(fighter: Fighter, input: InputState, onAttack: (AttackHitbox) -> Unit) {
        if (!fighter.isAlive) return

        fighter.isBlocking = input.block && fighter.onGround && !fighter.isAttacking
        if (fighter.isBlocking) fighter.setAnimation(AnimState.BLOCK)

        if (!fighter.isAttacking && !fighter.isBlocking && fighter.stunFrames <= 0) {
            when {
                input.moveLeft  -> fighter.move(-1)
                input.moveRight -> fighter.move(1)
                fighter.onGround -> fighter.setAnimation(AnimState.IDLE)
            }
        }

        if (input.jump) fighter.jump()

        if (input.special) {
            fighter.special()?.let(onAttack)
        } else if (input.attack) {
            fighter.attack()?.let(onAttack)
        }
    }

    private fun spawnHitEffect(x: Float, y: Float, hb: AttackHitbox) {
        hitEffects.add(HitEffect(x, y, frames = if (hb.isSpecial) 20 else 12, hb.isSpecial, hb.damage))
    }

    private fun endRound(result: RoundResult) {
        roundResult = result
        roundEndDelayFrames = GameConfig.TARGET_FPS * 2  // 2 second delay

        when (result) {
            RoundResult.PLAYER1_WIN -> {
                p1RoundWins++
                player1.setAnimation(AnimState.WIN)
                player2.setAnimation(AnimState.KO)
            }
            RoundResult.PLAYER2_WIN -> {
                p2RoundWins++
                player2.setAnimation(AnimState.WIN)
                player1.setAnimation(AnimState.KO)
            }
            RoundResult.DRAW -> {}
            else -> {}
        }

        checkGameOver()
    }

    private fun resolveTimeUp() {
        val result = when {
            player1.hp > player2.hp -> RoundResult.PLAYER1_WIN
            player2.hp > player1.hp -> RoundResult.PLAYER2_WIN
            else -> RoundResult.DRAW
        }
        endRound(result)
    }

    private fun checkGameOver() {
        val winsNeeded = (GameConfig.MAX_ROUNDS / 2) + 1
        if (p1RoundWins >= winsNeeded || p2RoundWins >= winsNeeded) {
            isGameOver = true
        }
    }

    private fun startNextRound() {
        if (isGameOver) return
        currentRound++
        roundResult = RoundResult.ONGOING
        roundTimeFrames = GameConfig.ROUND_TIME_SECONDS * GameConfig.TARGET_FPS
        player1.reset(groundY)
        player2.reset(groundY)
        hitEffects.clear()
    }

    val roundTimeSeconds: Int get() = (roundTimeFrames / GameConfig.TARGET_FPS).coerceAtLeast(0)

    val winner: Int get() = when {
        p1RoundWins > p2RoundWins -> 1
        p2RoundWins > p1RoundWins -> 2
        else -> 0  // draw
    }
}

data class InputState(
    var moveLeft:  Boolean = false,
    var moveRight: Boolean = false,
    var jump:      Boolean = false,
    var attack:    Boolean = false,
    var block:     Boolean = false,
    var special:   Boolean = false
)
