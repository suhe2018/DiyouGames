package com.diyou.games.data

import android.graphics.Color

/**
 * Playable character classes with unique stats and pixel art color palettes.
 */
enum class CharacterType(
    val displayName: String,
    val displayNameEn: String,
    val description: String,
    val maxHp: Int,
    val speed: Float,         // movement speed (pixels/frame)
    val defense: Int,         // damage reduction (%)
    val jumpForce: Float,     // upward velocity on jump
    val specialName: String,
    val specialDesc: String,
    // Pixel art color palette
    val bodyColor: Int,
    val armorColor: Int,
    val skinColor: Int,
    val hairColor: Int,
    val eyeColor: Int,
    val accentColor: Int
) {
    WARRIOR(
        displayName = "Warrior",
        displayNameEn = "Warrior",
        description = "Heavy armour, heavy sword — charge first.",
        maxHp = 150,
        speed = 3.5f,
        defense = 20,
        jumpForce = 14f,
        specialName = "Power Slash",
        specialDesc = "Charge up and deal double damage.",
        bodyColor = Color.rgb(80, 120, 160),
        armorColor = Color.rgb(160, 160, 180),
        skinColor = Color.rgb(220, 180, 140),
        hairColor = Color.rgb(80, 50, 30),
        eyeColor = Color.rgb(50, 100, 200),
        accentColor = Color.rgb(255, 200, 50)
    ),
    NINJA(
        displayName = "Ninja",
        displayNameEn = "Ninja",
        description = "Lightning fast — blade strikes like the wind.",
        maxHp = 100,
        speed = 5.5f,
        defense = 5,
        jumpForce = 17f,
        specialName = "Shadow Clone",
        specialDesc = "Teleport behind the enemy and combo 3 times.",
        bodyColor = Color.rgb(30, 30, 40),
        armorColor = Color.rgb(50, 50, 60),
        skinColor = Color.rgb(200, 160, 120),
        hairColor = Color.rgb(20, 20, 20),
        eyeColor = Color.rgb(220, 30, 30),
        accentColor = Color.rgb(200, 50, 50)
    ),
    MONK(
        displayName = "Monk",
        displayNameEn = "Monk",
        description = "Zen and combat as one — staff sweeps all.",
        maxHp = 120,
        speed = 4.0f,
        defense = 10,
        jumpForce = 15f,
        specialName = "Iron Wave",
        specialDesc = "Release a shockwave that knocks back enemies.",
        bodyColor = Color.rgb(200, 150, 80),
        armorColor = Color.rgb(230, 180, 80),
        skinColor = Color.rgb(210, 170, 130),
        hairColor = Color.rgb(30, 20, 10),
        eyeColor = Color.rgb(80, 160, 80),
        accentColor = Color.rgb(255, 220, 100)
    ),
    KNIGHT(
        displayName = "Knight",
        displayNameEn = "Knight",
        description = "Shield and helmet covering all — unbreakable.",
        maxHp = 180,
        speed = 2.8f,
        defense = 35,
        jumpForce = 12f,
        specialName = "Holy Charge",
        specialDesc = "Shield-bash forward, blocking all damage and launching enemies.",
        bodyColor = Color.rgb(200, 200, 210),
        armorColor = Color.rgb(220, 220, 230),
        skinColor = Color.rgb(225, 190, 155),
        hairColor = Color.rgb(180, 140, 60),
        eyeColor = Color.rgb(100, 180, 255),
        accentColor = Color.rgb(255, 215, 0)
    );

    /** Stars rating 1-5 for UI display */
    val hpStars: Int get() = when {
        maxHp >= 160 -> 5
        maxHp >= 130 -> 4
        maxHp >= 110 -> 3
        maxHp >= 90  -> 2
        else         -> 1
    }
    val speedStars: Int get() = when {
        speed >= 5.0f -> 5
        speed >= 4.5f -> 4
        speed >= 3.5f -> 3
        speed >= 2.5f -> 2
        else          -> 1
    }
    val defenseStars: Int get() = when {
        defense >= 30 -> 5
        defense >= 20 -> 4
        defense >= 12 -> 3
        defense >= 6  -> 2
        else          -> 1
    }
}
