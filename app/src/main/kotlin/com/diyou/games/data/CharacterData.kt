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
        displayName = "战士",
        displayNameEn = "Warrior",
        description = "厚甲重剑，冲锋在前",
        maxHp = 150,
        speed = 3.5f,
        defense = 20,
        jumpForce = 14f,
        specialName = "猛力斩",
        specialDesc = "蓄力一击，造成双倍伤害",
        bodyColor = Color.rgb(80, 120, 160),
        armorColor = Color.rgb(160, 160, 180),
        skinColor = Color.rgb(220, 180, 140),
        hairColor = Color.rgb(80, 50, 30),
        eyeColor = Color.rgb(50, 100, 200),
        accentColor = Color.rgb(255, 200, 50)
    ),
    NINJA(
        displayName = "忍者",
        displayNameEn = "Ninja",
        description = "身法迅捷，出刀如风",
        maxHp = 100,
        speed = 5.5f,
        defense = 5,
        jumpForce = 17f,
        specialName = "分身术",
        specialDesc = "瞬移至背后，连击三次",
        bodyColor = Color.rgb(30, 30, 40),
        armorColor = Color.rgb(50, 50, 60),
        skinColor = Color.rgb(200, 160, 120),
        hairColor = Color.rgb(20, 20, 20),
        eyeColor = Color.rgb(220, 30, 30),
        accentColor = Color.rgb(200, 50, 50)
    ),
    MONK(
        displayName = "武僧",
        displayNameEn = "Monk",
        description = "禅武合一，棍扫千军",
        maxHp = 120,
        speed = 4.0f,
        defense = 10,
        jumpForce = 15f,
        specialName = "金刚波",
        specialDesc = "发出冲击波，击退敌人并造成伤害",
        bodyColor = Color.rgb(200, 150, 80),
        armorColor = Color.rgb(230, 180, 80),
        skinColor = Color.rgb(210, 170, 130),
        hairColor = Color.rgb(30, 20, 10),
        eyeColor = Color.rgb(80, 160, 80),
        accentColor = Color.rgb(255, 220, 100)
    ),
    KNIGHT(
        displayName = "骑士",
        displayNameEn = "Knight",
        description = "盾盔全覆，坚不可摧",
        maxHp = 180,
        speed = 2.8f,
        defense = 35,
        jumpForce = 12f,
        specialName = "神圣冲锋",
        specialDesc = "举盾冲锋，格挡一切伤害并击飞敌人",
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
