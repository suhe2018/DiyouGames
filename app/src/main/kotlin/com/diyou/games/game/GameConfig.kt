package com.diyou.games.game

/**
 * Central configuration constants for the game.
 */
object GameConfig {
    // Target frame rate
    const val TARGET_FPS = 60
    const val FRAME_TIME_MS = (1000 / TARGET_FPS).toLong()

    // Virtual game resolution (will be scaled to actual screen)
    const val VIRTUAL_WIDTH  = 480
    const val VIRTUAL_HEIGHT = 270

    // Physics
    const val GRAVITY = 0.65f
    const val GROUND_Y_RATIO = 0.65f  // ground at 65% — leaves room for on-screen controls

    // Character dimensions (virtual pixels)
    const val CHAR_WIDTH  = 24
    const val CHAR_HEIGHT = 32

    // Arena bounds (virtual pixels; x = fighter centre, y = fighter feet)
    const val ARENA_LEFT_BOUND  = 50f
    const val ARENA_RIGHT_BOUND = 430f
    const val ARENA_TOP_BOUND   = 32f   // character is ~30 vp tall, keep head on screen

    // Combat
    const val KNOCKBACK_FORCE  = 6f
    const val STUN_FRAMES      = 20
    const val INVINCIBLE_FRAMES = 30   // after being hit
    const val BLOCK_DAMAGE_REDUCTION = 0.75f  // 75% reduction when blocking

    // HP bar UI
    const val HP_BAR_WIDTH  = 120f
    const val HP_BAR_HEIGHT = 10f

    // Round settings
    const val ROUND_TIME_SECONDS = 99
    const val MAX_ROUNDS = 3

    // Scoring
    const val WIN_SCORE  = 3
    const val DRAW_SCORE = 1

    // Pixel size (for chunky pixel look)
    const val PIXEL_SIZE = 2
}
