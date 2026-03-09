package com.diyou.games.game

/**
 * Sprite animation state machine for pixel characters.
 */
enum class AnimState {
    IDLE, RUN, JUMP, FALL,
    ATTACK1, ATTACK2, ATTACK3, ATTACK4,
    SPECIAL, BLOCK,
    HIT, KO, WIN
}

/**
 * Manages frame-based animation for a character.
 */
class Animation(
    val state: AnimState,
    val totalFrames: Int,
    val frameDuration: Int,   // ticks per frame
    val loop: Boolean = true
) {
    var currentFrame: Int = 0
        private set
    var tickCount: Int = 0
        private set
    var finished: Boolean = false
        private set

    fun update() {
        if (finished) return
        tickCount++
        if (tickCount >= frameDuration) {
            tickCount = 0
            currentFrame++
            if (currentFrame >= totalFrames) {
                if (loop) currentFrame = 0
                else {
                    currentFrame = totalFrames - 1
                    finished = true
                }
            }
        }
    }

    fun reset() {
        currentFrame = 0
        tickCount = 0
        finished = false
    }

    /** Normalized progress 0..1 */
    val progress: Float get() = currentFrame.toFloat() / totalFrames.coerceAtLeast(1)

    companion object {
        // Standard animations used by all characters
        val IDLE_ANIM    = Animation(AnimState.IDLE,    4,  12, loop = true)
        val RUN_ANIM     = Animation(AnimState.RUN,     6,  6,  loop = true)
        val JUMP_ANIM    = Animation(AnimState.JUMP,    3,  5,  loop = false)
        val FALL_ANIM    = Animation(AnimState.FALL,    2,  8,  loop = true)
        val ATTACK1_ANIM = Animation(AnimState.ATTACK1, 4,  5,  loop = false)
        val ATTACK2_ANIM = Animation(AnimState.ATTACK2, 4,  4,  loop = false)
        val ATTACK3_ANIM = Animation(AnimState.ATTACK3, 5,  4,  loop = false)
        val SPECIAL_ANIM = Animation(AnimState.SPECIAL, 6,  6,  loop = false)
        val BLOCK_ANIM   = Animation(AnimState.BLOCK,   2,  8,  loop = true)
        val HIT_ANIM     = Animation(AnimState.HIT,     3,  4,  loop = false)
        val KO_ANIM      = Animation(AnimState.KO,      5,  8,  loop = false)
        val WIN_ANIM     = Animation(AnimState.WIN,     8,  8,  loop = true)

        fun forState(state: AnimState): Animation = when (state) {
            AnimState.IDLE    -> IDLE_ANIM.copy()
            AnimState.RUN     -> RUN_ANIM.copy()
            AnimState.JUMP    -> JUMP_ANIM.copy()
            AnimState.FALL    -> FALL_ANIM.copy()
            AnimState.ATTACK1 -> ATTACK1_ANIM.copy()
            AnimState.ATTACK2 -> ATTACK2_ANIM.copy()
            AnimState.ATTACK3 -> ATTACK3_ANIM.copy()
            AnimState.ATTACK4 -> ATTACK3_ANIM.copy()
            AnimState.SPECIAL -> SPECIAL_ANIM.copy()
            AnimState.BLOCK   -> BLOCK_ANIM.copy()
            AnimState.HIT     -> HIT_ANIM.copy()
            AnimState.KO      -> KO_ANIM.copy()
            AnimState.WIN     -> WIN_ANIM.copy()
        }

        private fun Animation.copy() = Animation(state, totalFrames, frameDuration, loop)
    }
}
