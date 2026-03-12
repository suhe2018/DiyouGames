package com.diyou.games.game

import com.diyou.games.data.CharacterType
import com.diyou.games.data.WeaponType
import kotlin.math.abs

/**
 * Represents a fighter (player-controlled or AI) in battle.
 */
class Fighter(
    val characterType: CharacterType,
    val weaponType: WeaponType,
    val isPlayerOne: Boolean
) {
    // --- Position & Physics ---
    var x: Float = if (isPlayerOne) 80f else 380f
    var y: Float = 0f  // set to groundY on init
    var velX: Float = 0f
    var velY: Float = 0f
    var facingRight: Boolean = isPlayerOne
    var onGround: Boolean = true

    // --- Stats ---
    var hp: Float = characterType.maxHp.toFloat()
    val maxHp: Float = characterType.maxHp.toFloat()
    var energy: Float = 0f  // fills up with attacks, used for special
    val maxEnergy: Float = 100f

    // --- Combat state ---
    var isBlocking: Boolean = false
    var isAttacking: Boolean = false
    var stunFrames: Int = 0
    var invincibleFrames: Int = 0
    var knockbackFrames: Int = 0

    // --- Combo system ---
    var comboCount: Int = 0
    var comboTimer: Int = 0
    private val COMBO_WINDOW = 45  // frames to continue combo

    // --- Animation ---
    var anim: Animation = Animation.forState(AnimState.IDLE)
    var currentAnimState: AnimState = AnimState.IDLE

    // --- Round stats ---
    var damageDealt: Float = 0f
    var hitsTaken: Int = 0
    var specialsUsed: Int = 0

    val isAlive: Boolean get() = hp > 0f
    val hpPercent: Float get() = (hp / maxHp).coerceIn(0f, 1f)
    val energyPercent: Float get() = (energy / maxEnergy).coerceIn(0f, 1f)
    val canUseSpecial: Boolean get() = energy >= maxEnergy

    fun setAnimation(state: AnimState) {
        if (currentAnimState == state && !anim.finished) return
        currentAnimState = state
        anim = Animation.forState(state)
    }

    /**
     * Apply gravity and movement physics.
     */
    fun updatePhysics(groundY: Float) {
        if (!onGround) {
            velY += GameConfig.GRAVITY
        }
        y += velY
        x += velX

        // Ground collision
        if (y >= groundY) {
            y = groundY
            velY = 0f
            onGround = true
            if (currentAnimState == AnimState.FALL || currentAnimState == AnimState.JUMP) {
                setAnimation(AnimState.IDLE)
            }
        }

        // Ceiling collision — prevent character from jumping off top of screen
        if (y < GameConfig.ARENA_TOP_BOUND) {
            y = GameConfig.ARENA_TOP_BOUND
            if (velY < 0f) velY = 0f
        }

        // Arena horizontal bounds (account for half-sprite width ~7 vp)
        x = x.coerceIn(GameConfig.ARENA_LEFT_BOUND, GameConfig.ARENA_RIGHT_BOUND)

        // Friction
        if (onGround && !isAttacking && knockbackFrames <= 0) {
            velX *= 0.75f
        }

        // Counters
        if (stunFrames > 0) stunFrames--
        if (invincibleFrames > 0) invincibleFrames--
        if (knockbackFrames > 0) knockbackFrames--
        if (comboTimer > 0) {
            comboTimer--
            if (comboTimer == 0) comboCount = 0
        }
    }

    /**
     * Update animation state.
     */
    fun updateAnimation() {
        anim.update()

        // Auto-transition finished non-looping animations
        if (anim.finished) {
            when (currentAnimState) {
                AnimState.ATTACK1, AnimState.ATTACK2,
                AnimState.ATTACK3, AnimState.ATTACK4 -> {
                    isAttacking = false
                    setAnimation(AnimState.IDLE)
                }
                AnimState.SPECIAL -> {
                    isAttacking = false
                    specialsUsed++
                    setAnimation(AnimState.IDLE)
                }
                AnimState.HIT -> setAnimation(AnimState.IDLE)
                else -> {}
            }
        }

        // In-flight animation
        if (!onGround && currentAnimState !in setOf(AnimState.JUMP, AnimState.FALL, AnimState.SPECIAL)) {
            if (velY < 0) setAnimation(AnimState.JUMP)
            else setAnimation(AnimState.FALL)
        }
    }

    /**
     * Jump action.
     */
    fun jump() {
        if (onGround && stunFrames <= 0) {
            velY = -characterType.jumpForce
            onGround = false
            setAnimation(AnimState.JUMP)
        }
    }

    /**
     * Move horizontally.
     * @param direction -1 = left, 1 = right
     */
    fun move(direction: Int) {
        if (stunFrames > 0 || knockbackFrames > 0 || isAttacking) return
        velX = direction * characterType.speed
        facingRight = direction > 0
        if (onGround) setAnimation(AnimState.RUN)
    }

    /**
     * Start an attack. Returns the hitbox for this attack, or null if can't attack.
     */
    fun attack(): AttackHitbox? {
        if (stunFrames > 0 || !isAlive) return null

        val attackAnimState = when (comboCount % weaponType.comboHits) {
            0    -> AnimState.ATTACK1
            1    -> AnimState.ATTACK2
            2    -> AnimState.ATTACK3
            else -> AnimState.ATTACK4
        }

        isAttacking = true
        comboCount++
        comboTimer = COMBO_WINDOW
        setAnimation(attackAnimState)

        val damage = weaponType.baseDamage.toFloat() * (1f + comboCount * 0.1f)
        energy = (energy + 15f).coerceAtMost(maxEnergy)

        val hitX = if (facingRight) x + GameConfig.CHAR_WIDTH else x - weaponType.attackRange
        return AttackHitbox(
            x = hitX,
            width = weaponType.attackRange,
            y = y - GameConfig.CHAR_HEIGHT * 0.6f,
            height = GameConfig.CHAR_HEIGHT.toFloat(),
            damage = damage,
            knockback = GameConfig.KNOCKBACK_FORCE * (if (comboCount == 1) 1f else 0.5f),
            isSpecial = false
        )
    }

    /**
     * Use special ability. Returns hitbox or null if not enough energy.
     */
    fun special(): AttackHitbox? {
        if (!canUseSpecial || stunFrames > 0 || !isAlive) return null
        energy = 0f
        isAttacking = true
        setAnimation(AnimState.SPECIAL)
        comboCount = 0

        val specialMult = 2.0f
        val hitX = if (facingRight) x else x - weaponType.attackRange * 1.5f
        return AttackHitbox(
            x = hitX,
            width = weaponType.attackRange * 1.5f,
            y = y - GameConfig.CHAR_HEIGHT.toFloat(),
            height = GameConfig.CHAR_HEIGHT * 1.5f,
            damage = weaponType.baseDamage * specialMult,
            knockback = GameConfig.KNOCKBACK_FORCE * 2f,
            isSpecial = true
        )
    }

    /**
     * Receive damage from an attack.
     */
    fun receiveDamage(hitbox: AttackHitbox) {
        if (invincibleFrames > 0 || !isAlive) return

        val reduction = if (isBlocking) GameConfig.BLOCK_DAMAGE_REDUCTION else 0f
        val defenseReduction = characterType.defense / 100f
        val finalDamage = hitbox.damage * (1f - reduction) * (1f - defenseReduction)

        hp = (hp - finalDamage).coerceAtLeast(0f)
        hitsTaken++

        val kbDir = if (hitbox.x < x) 1f else -1f
        velX = hitbox.knockback * kbDir * (1f - reduction * 0.5f)
        velY = -3f

        stunFrames = if (isBlocking) GameConfig.STUN_FRAMES / 3 else GameConfig.STUN_FRAMES
        invincibleFrames = GameConfig.INVINCIBLE_FRAMES
        knockbackFrames = 12

        if (!isBlocking) setAnimation(if (hp <= 0f) AnimState.KO else AnimState.HIT)
    }

    /**
     * Check if this fighter's current attack hitbox overlaps with another fighter.
     */
    fun overlaps(hitbox: AttackHitbox): Boolean {
        return hitbox.x < x + GameConfig.CHAR_WIDTH &&
               hitbox.x + hitbox.width > x &&
               hitbox.y < y &&
               hitbox.y + hitbox.height > y - GameConfig.CHAR_HEIGHT
    }

    fun reset(groundY: Float) {
        x = if (isPlayerOne) 80f else 380f
        y = groundY
        velX = 0f
        velY = 0f
        hp = maxHp
        energy = 0f
        stunFrames = 0
        invincibleFrames = 0
        knockbackFrames = 0
        isBlocking = false
        isAttacking = false
        comboCount = 0
        comboTimer = 0
        damageDealt = 0f
        hitsTaken = 0
        facingRight = isPlayerOne
        setAnimation(AnimState.IDLE)
    }
}

data class AttackHitbox(
    val x: Float,
    val width: Float,
    val y: Float,
    val height: Float,
    val damage: Float,
    val knockback: Float,
    val isSpecial: Boolean
)
