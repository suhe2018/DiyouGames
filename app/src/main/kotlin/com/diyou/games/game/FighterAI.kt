package com.diyou.games.game

import kotlin.random.Random

/**
 * AI difficulty levels.
 */
enum class AIDifficulty(val displayName: String, val reactionDelay: Int, val aggressiveness: Float) {
    EASY(   "简单", reactionDelay = 30, aggressiveness = 0.3f),
    MEDIUM( "普通", reactionDelay = 18, aggressiveness = 0.55f),
    HARD(   "困难", reactionDelay = 8,  aggressiveness = 0.80f),
    EXPERT( "专家", reactionDelay = 3,  aggressiveness = 0.95f)
}

/**
 * Simple state-machine AI controller for the opponent fighter.
 */
class FighterAI(
    private val fighter: Fighter,
    private val difficulty: AIDifficulty = AIDifficulty.MEDIUM
) {
    private enum class AIState { APPROACH, RETREAT, ATTACK, BLOCK, SPECIAL, IDLE }

    private var state: AIState = AIState.IDLE
    private var stateTimer: Int = 0
    private var reactionTimer: Int = 0
    private var jumpCooldown: Int = 0

    fun update(player: Fighter, groundY: Float): AIAction {
        reactionTimer++
        if (jumpCooldown > 0) jumpCooldown--
        stateTimer++

        if (reactionTimer < difficulty.reactionDelay) return AIAction()
        reactionTimer = 0

        val dx = player.x - fighter.x
        val absDx = kotlin.math.abs(dx)
        val weaponRange = fighter.weaponType.attackRange

        // Decide state
        state = when {
            !fighter.isAlive -> AIState.IDLE
            fighter.stunFrames > 0 -> AIState.IDLE
            fighter.canUseSpecial && Random.nextFloat() < 0.35f -> AIState.SPECIAL
            absDx < weaponRange * 0.85f && Random.nextFloat() < difficulty.aggressiveness -> AIState.ATTACK
            absDx < weaponRange * 0.5f && player.isAttacking && Random.nextFloat() < 0.5f -> AIState.BLOCK
            absDx > weaponRange * 1.2f -> AIState.APPROACH
            fighter.hpPercent < 0.25f && Random.nextFloat() < 0.4f -> AIState.RETREAT
            else -> if (Random.nextFloat() < difficulty.aggressiveness) AIState.ATTACK else AIState.APPROACH
        }

        val action = AIAction()

        when (state) {
            AIState.APPROACH -> {
                action.moveDirection = if (dx > 0) 1 else -1
                // Occasionally jump over opponent
                if (absDx < 40f && fighter.onGround && jumpCooldown <= 0 && Random.nextFloat() < 0.15f) {
                    action.jump = true
                    jumpCooldown = 60
                }
            }
            AIState.RETREAT -> {
                action.moveDirection = if (dx > 0) -1 else 1
                if (fighter.onGround && jumpCooldown <= 0 && Random.nextFloat() < 0.2f) {
                    action.jump = true
                    jumpCooldown = 45
                }
            }
            AIState.ATTACK -> {
                action.attack = true
            }
            AIState.BLOCK -> {
                action.block = true
            }
            AIState.SPECIAL -> {
                action.special = true
            }
            AIState.IDLE -> {}
        }

        return action
    }
}

data class AIAction(
    var moveDirection: Int = 0,   // -1, 0, 1
    var jump: Boolean = false,
    var attack: Boolean = false,
    var block: Boolean = false,
    var special: Boolean = false
)
