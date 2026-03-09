package com.diyou.games.data

import android.graphics.Color

/**
 * Weapon types available in the game.
 * All weapons have fantasy/stylized design, no realistic violence.
 */
enum class WeaponType(
    val displayName: String,
    val displayNameEn: String,
    val baseDamage: Int,
    val attackSpeed: Float,    // attacks per second
    val attackRange: Float,    // in pixels (game units)
    val comboHits: Int,        // number of combo strikes
    val specialCooldown: Int,  // frames between special attacks
    val color: Int,
    val glowColor: Int
) {
    SWORD(
        displayName = "长剑",
        displayNameEn = "Sword",
        baseDamage = 25,
        attackSpeed = 1.5f,
        attackRange = 120f,
        comboHits = 2,
        specialCooldown = 180,
        color = Color.rgb(200, 200, 220),
        glowColor = Color.rgb(150, 200, 255)
    ),
    DAGGER(
        displayName = "双刃",
        displayNameEn = "Dual Daggers",
        baseDamage = 15,
        attackSpeed = 2.8f,
        attackRange = 80f,
        comboHits = 4,
        specialCooldown = 120,
        color = Color.rgb(180, 180, 60),
        glowColor = Color.rgb(255, 240, 80)
    ),
    STAFF(
        displayName = "法杖",
        displayNameEn = "Magic Staff",
        baseDamage = 35,
        attackSpeed = 0.9f,
        attackRange = 150f,
        comboHits = 1,
        specialCooldown = 240,
        color = Color.rgb(160, 100, 220),
        glowColor = Color.rgb(220, 100, 255)
    ),
    CLUB(
        displayName = "铁棍",
        displayNameEn = "Iron Club",
        baseDamage = 40,
        attackSpeed = 0.7f,
        attackRange = 100f,
        comboHits = 1,
        specialCooldown = 200,
        color = Color.rgb(140, 100, 60),
        glowColor = Color.rgb(255, 160, 60)
    ),
    SPEAR(
        displayName = "长矛",
        displayNameEn = "Spear",
        baseDamage = 30,
        attackSpeed = 1.2f,
        attackRange = 180f,
        comboHits = 2,
        specialCooldown = 160,
        color = Color.rgb(200, 160, 80),
        glowColor = Color.rgb(255, 200, 100)
    ),
    AXE(
        displayName = "战斧",
        displayNameEn = "Battle Axe",
        baseDamage = 50,
        attackSpeed = 0.6f,
        attackRange = 110f,
        comboHits = 1,
        specialCooldown = 300,
        color = Color.rgb(180, 60, 60),
        glowColor = Color.rgb(255, 80, 80)
    );

    /** Damage per second rating (for UI display) */
    val dps: Float get() = baseDamage * attackSpeed

    companion object {
        fun forCharacterClass(type: CharacterType): List<WeaponType> = when (type) {
            CharacterType.WARRIOR -> listOf(SWORD, AXE, CLUB)
            CharacterType.NINJA   -> listOf(DAGGER, SPEAR, SWORD)
            CharacterType.MONK    -> listOf(STAFF, CLUB, SPEAR)
            CharacterType.KNIGHT  -> listOf(SWORD, SPEAR, AXE)
        }
    }
}
