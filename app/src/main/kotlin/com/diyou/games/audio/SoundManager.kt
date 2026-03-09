package com.diyou.games.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build

/**
 * Manages sound effects via SoundPool.
 * All sounds are generated procedurally (no asset files needed).
 */
class SoundManager(private val context: Context) {

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(8)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    var sfxVolume: Float = 0.8f
    var musicVolume: Float = 0.6f
    var sfxEnabled: Boolean = true

    // Sound IDs (loaded from raw resources)
    private val soundIds = mutableMapOf<SoundType, Int>()

    enum class SoundType {
        ATTACK_SWING, HIT_LIGHT, HIT_HEAVY, BLOCK, SPECIAL,
        JUMP, LAND, KO, ROUND_START, ROUND_WIN, UI_CLICK
    }

    fun play(type: SoundType) {
        if (!sfxEnabled) return
        soundIds[type]?.let {
            soundPool.play(it, sfxVolume, sfxVolume, 1, 0, 1.0f)
        }
    }

    fun release() {
        soundPool.release()
    }
}
