package com.diyou.games.game

import android.graphics.*
import com.diyou.games.data.CharacterType
import com.diyou.games.data.WeaponType
import kotlin.math.*

/**
 * Renders all game visuals using Android Canvas.
 * Uses chunky pixel-art style: all drawing is done in virtual coordinates
 * then scaled to screen via a Matrix.
 */
class PixelRenderer(private val screenWidth: Int, private val screenHeight: Int) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isFilterBitmap = false   // sharp pixel scaling
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowPaint = Paint().apply { color = Color.argb(120, 0, 0, 0) }

    // Virtual-to-screen scale matrix
    private val scaleX = screenWidth.toFloat()  / GameConfig.VIRTUAL_WIDTH
    private val scaleY = screenHeight.toFloat() / GameConfig.VIRTUAL_HEIGHT

    /** Draw the full battle scene */
    fun drawBattle(canvas: Canvas, engine: BattleEngine, tick: Int) {
        drawBackground(canvas, tick)
        drawGround(canvas, engine.groundY)
        drawFighterShadow(canvas, engine.player1)
        drawFighterShadow(canvas, engine.player2)
        drawFighter(canvas, engine.player1, tick)
        drawFighter(canvas, engine.player2, tick)
        drawHitEffects(canvas, engine.hitEffects, tick)
        drawHUD(canvas, engine, tick)
    }

    // ─── Background ──────────────────────────────────────────────────────────

    private fun drawBackground(canvas: Canvas, tick: Int) {
        // Sky gradient
        val skyPaint = Paint()
        val skyShader = LinearGradient(
            0f, 0f, 0f, screenHeight * 0.7f,
            intArrayOf(
                Color.rgb(20, 10, 40),
                Color.rgb(60, 20, 80),
                Color.rgb(100, 40, 100)
            ),
            null, Shader.TileMode.CLAMP
        )
        skyPaint.shader = skyShader
        canvas.drawRect(0f, 0f, screenWidth.toFloat(), screenHeight * 0.7f, skyPaint)

        // Parallax pixel stars
        drawStars(canvas, tick)

        // Distant pixel mountains
        drawMountains(canvas)
    }

    private fun drawStars(canvas: Canvas, tick: Int) {
        paint.color = Color.WHITE
        val starPositions = listOf(
            40f to 20f, 80f to 35f, 150f to 15f, 220f to 40f, 300f to 10f,
            370f to 30f, 420f to 50f, 100f to 55f, 250f to 60f, 460f to 20f
        )
        starPositions.forEachIndexed { i, (sx, sy) ->
            val twinkle = (sin((tick + i * 17).toDouble() * 0.08) * 0.5 + 0.5).toFloat()
            paint.alpha = (80 + twinkle * 175).toInt()
            val px = sx * scaleX
            val py = sy * scaleY
            canvas.drawRect(px, py, px + scaleX, py + scaleY, paint)
        }
        paint.alpha = 255
    }

    private fun drawMountains(canvas: Canvas) {
        // Base y scaled to match GROUND_Y_RATIO = 0.65 → virtual ground ≈ 175
        paint.color = Color.rgb(40, 15, 60)
        val pts = listOf(0f to 155f, 60f to 105f, 130f to 135f, 200f to 75f,
                         260f to 115f, 340f to 65f, 400f to 105f, 480f to 95f, 480f to 180f, 0f to 180f)
        drawPixelPolygon(canvas, pts)

        paint.color = Color.rgb(55, 25, 75)
        val pts2 = listOf(0f to 175f, 80f to 140f, 160f to 160f, 240f to 125f,
                          320f to 145f, 400f to 130f, 480f to 140f, 480f to 180f, 0f to 180f)
        drawPixelPolygon(canvas, pts2)
    }

    private fun drawPixelPolygon(canvas: Canvas, pts: List<Pair<Float, Float>>) {
        if (pts.size < 3) return
        val path = Path()
        path.moveTo(pts[0].first * scaleX, pts[0].second * scaleY)
        pts.drop(1).forEach { (x, y) -> path.lineTo(x * scaleX, y * scaleY) }
        path.close()
        canvas.drawPath(path, paint)
    }

    private fun drawGround(canvas: Canvas, groundY: Float) {
        val gy = groundY * scaleY

        // Main ground
        paint.color = Color.rgb(80, 50, 120)
        canvas.drawRect(0f, gy, screenWidth.toFloat(), screenHeight.toFloat(), paint)

        // Ground top stripe
        paint.color = Color.rgb(120, 80, 160)
        canvas.drawRect(0f, gy, screenWidth.toFloat(), gy + 4 * scaleY, paint)

        // Pixel grid lines on ground
        paint.color = Color.rgb(70, 45, 110)
        paint.alpha = 180
        val tileSize = 32f * scaleX
        var xLine = 0f
        while (xLine < screenWidth) {
            canvas.drawRect(xLine, gy + 6 * scaleY, xLine + scaleX, screenHeight.toFloat(), paint)
            xLine += tileSize
        }
        paint.alpha = 255
    }

    // ─── Fighter ─────────────────────────────────────────────────────────────

    private fun drawFighterShadow(canvas: Canvas, fighter: Fighter) {
        val cx = fighter.x * scaleX
        val gy = fighter.y * scaleY
        val shadowW = 20f * scaleX
        val shadowH = 6f * scaleY
        val alpha = (120 * (1f - ((gy - fighter.y * scaleY).absoluteValue / 100f).coerceIn(0f, 1f))).toInt()
        shadowPaint.alpha = alpha.coerceIn(20, 120)
        canvas.drawOval(
            RectF(cx - shadowW / 2, gy - shadowH / 2, cx + shadowW / 2, gy + shadowH / 2),
            shadowPaint
        )
    }

    private fun drawFighter(canvas: Canvas, fighter: Fighter, tick: Int) {
        if (!fighter.isAlive && fighter.currentAnimState == AnimState.KO) {
            drawKO(canvas, fighter, tick)
            return
        }

        val cx = fighter.x * scaleX
        val by = fighter.y * scaleY  // bottom y
        val flip = !fighter.facingRight

        // Blink when invincible
        if (fighter.invincibleFrames > 0 && (tick / 3) % 2 == 0) return

        canvas.save()
        if (flip) {
            canvas.scale(-1f, 1f, cx, by - (GameConfig.CHAR_HEIGHT / 2f) * scaleY)
        }

        val f = fighter.anim.currentFrame
        val ct = fighter.characterType
        val wt = fighter.weaponType

        when (fighter.currentAnimState) {
            AnimState.IDLE    -> drawIdle(canvas, cx, by, ct, wt, tick)
            AnimState.RUN     -> drawRun(canvas, cx, by, ct, wt, f)
            AnimState.JUMP, AnimState.FALL -> drawJump(canvas, cx, by, ct, wt, f)
            AnimState.ATTACK1 -> drawAttack(canvas, cx, by, ct, wt, f, 1)
            AnimState.ATTACK2 -> drawAttack(canvas, cx, by, ct, wt, f, 2)
            AnimState.ATTACK3, AnimState.ATTACK4 -> drawAttack(canvas, cx, by, ct, wt, f, 3)
            AnimState.SPECIAL -> drawSpecial(canvas, cx, by, ct, wt, f, tick)
            AnimState.BLOCK   -> drawBlock(canvas, cx, by, ct, wt, tick)
            AnimState.HIT     -> drawHitReaction(canvas, cx, by, ct, wt, f)
            AnimState.WIN     -> drawWin(canvas, cx, by, ct, wt, tick)
            else              -> drawIdle(canvas, cx, by, ct, wt, tick)
        }

        canvas.restore()
    }

    private fun drawIdle(canvas: Canvas, cx: Float, by: Float, ct: CharacterType, wt: WeaponType, tick: Int) {
        val bob = sin(tick * 0.1) * 1.5 * scaleY  // breathing bob
        drawCharacterBase(canvas, cx, by + bob.toFloat(), ct, wt, legOffset = 0f, armRaise = 0.1f)
    }

    private fun drawRun(canvas: Canvas, cx: Float, by: Float, ct: CharacterType, wt: WeaponType, frame: Int) {
        val legOffset = sin(frame * 1.1) * 4.0 * scaleY
        val armRaise  = cos(frame * 1.1).toFloat() * 0.3f
        drawCharacterBase(canvas, cx, by, ct, wt, legOffset.toFloat(), armRaise)
    }

    private fun drawJump(canvas: Canvas, cx: Float, by: Float, ct: CharacterType, wt: WeaponType, frame: Int) {
        drawCharacterBase(canvas, cx, by, ct, wt, legOffset = frame * 2f * scaleY, armRaise = -0.4f)
    }

    private fun drawAttack(canvas: Canvas, cx: Float, by: Float, ct: CharacterType, wt: WeaponType, frame: Int, combo: Int) {
        val armExtend = (frame.toFloat() / 4f).coerceIn(0f, 1f)
        drawCharacterBase(canvas, cx, by, ct, wt, legOffset = 0f, armRaise = -0.3f - armExtend * 0.3f)
        drawWeaponSwing(canvas, cx, by, wt, armExtend, combo)
    }

    private fun drawSpecial(canvas: Canvas, cx: Float, by: Float, ct: CharacterType, wt: WeaponType, frame: Int, tick: Int) {
        // Glow aura
        paint.color = wt.glowColor
        paint.alpha = (frame * 20).coerceIn(0, 180)
        val auraR = (12f + frame * 3f) * scaleX
        canvas.drawCircle(cx, by - GameConfig.CHAR_HEIGHT * 0.5f * scaleY, auraR, paint)
        paint.alpha = 255

        drawCharacterBase(canvas, cx, by, ct, wt, legOffset = 0f, armRaise = -0.6f)
        drawWeaponSwing(canvas, cx, by, wt, frame.toFloat() / 6f, combo = 0)
    }

    private fun drawBlock(canvas: Canvas, cx: Float, by: Float, ct: CharacterType, wt: WeaponType, tick: Int) {
        drawCharacterBase(canvas, cx, by, ct, wt, legOffset = 0f, armRaise = 0.5f)
        // Shield glow
        paint.color = Color.rgb(100, 200, 255)
        paint.alpha = (sin(tick * 0.3) * 60 + 120).toInt()
        canvas.drawRect(
            cx + 8f * scaleX, by - 24f * scaleY,
            cx + 16f * scaleX, by - 8f * scaleY, paint
        )
        paint.alpha = 255
    }

    private fun drawHitReaction(canvas: Canvas, cx: Float, by: Float, ct: CharacterType, wt: WeaponType, frame: Int) {
        paint.colorFilter = PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP)
        drawCharacterBase(canvas, cx, by, ct, wt, legOffset = frame * scaleY, armRaise = 0.2f)
        paint.colorFilter = null
    }

    private fun drawKO(canvas: Canvas, fighter: Fighter, tick: Int) {
        val cx = fighter.x * scaleX
        val by = fighter.y * scaleY
        // Draw character lying on the ground
        paint.color = fighter.characterType.bodyColor
        paint.alpha = 200
        canvas.drawRect(
            cx - 16f * scaleX, by - 6f * scaleY,
            cx + 16f * scaleX, by, paint
        )
        // Head
        paint.color = fighter.characterType.skinColor
        canvas.drawRect(
            cx + 12f * scaleX, by - 10f * scaleY,
            cx + 20f * scaleX, by - 4f * scaleY, paint
        )
        // Stars circling
        for (i in 0 until 3) {
            val angle = (tick * 0.08f + i * 2.09f).toDouble()
            val sx = cx + cos(angle).toFloat() * 10f * scaleX
            val sy = by - 14f * scaleY + sin(angle).toFloat() * 4f * scaleY
            paint.color = Color.YELLOW
            paint.alpha = 200
            canvas.drawRect(sx, sy, sx + 3f * scaleX, sy + 3f * scaleY, paint)
        }
        paint.alpha = 255
    }

    private fun drawWin(canvas: Canvas, cx: Float, by: Float, ct: CharacterType, wt: WeaponType, tick: Int) {
        drawCharacterBase(canvas, cx, by, ct, wt, legOffset = 0f, armRaise = -0.8f)
        // Victory sparkles
        for (i in 0 until 5) {
            val angle = (tick * 0.05f + i * 1.257f).toDouble()
            val dist = (12f + sin(tick * 0.1f + i) * 4f)
            val sx = cx + cos(angle).toFloat() * dist * scaleX
            val sy = by - GameConfig.CHAR_HEIGHT * scaleY + sin(angle).toFloat() * dist * scaleY
            paint.color = if (i % 2 == 0) Color.YELLOW else Color.WHITE
            paint.alpha = (sin(tick * 0.12f + i) * 127 + 128).toInt()
            canvas.drawRect(sx, sy, sx + 3f * scaleX, sy + 3f * scaleY, paint)
        }
        paint.alpha = 255
    }

    /**
     * Draws the character body: head, torso, legs, arms.
     * All in virtual units scaled up.
     */
    private fun drawCharacterBase(
        canvas: Canvas, cx: Float, by: Float,
        ct: CharacterType, wt: WeaponType,
        legOffset: Float, armRaise: Float
    ) {
        val p = GameConfig.PIXEL_SIZE.toFloat() * scaleX.coerceAtMost(scaleY)

        fun rect(vx: Float, vy: Float, vw: Float, vh: Float) {
            canvas.drawRect(
                cx + vx * p, by + vy * p,
                cx + (vx + vw) * p, by + (vy + vh) * p, paint
            )
        }

        // Legs
        paint.color = ct.bodyColor
        rect(-3f, -8f + legOffset / p, 2f, 8f)
        rect(1f, -8f - legOffset / p, 2f, 8f)

        // Torso
        paint.color = ct.armorColor
        rect(-5f, -20f, 10f, 12f)

        // Head
        paint.color = ct.skinColor
        rect(-3f, -28f, 6f, 8f)

        // Hair
        paint.color = ct.hairColor
        rect(-3f, -30f, 6f, 3f)

        // Eyes
        paint.color = ct.eyeColor
        rect(-2f, -26f, 2f, 1f)
        rect(1f, -26f, 2f, 1f)

        // Arms
        val armY = -20f + armRaise * 8f
        paint.color = ct.skinColor
        rect(-7f, armY, 2f, 6f)

        paint.color = wt.color
        rect(5f, armY - 2f, 2f, 8f)

        // Armor accents
        paint.color = ct.accentColor
        rect(-4f, -19f, 8f, 1f)
        rect(-4f, -14f, 8f, 1f)
    }

    private fun drawWeaponSwing(canvas: Canvas, cx: Float, by: Float, wt: WeaponType, progress: Float, combo: Int) {
        val p = GameConfig.PIXEL_SIZE.toFloat() * scaleX.coerceAtMost(scaleY)
        val angle = (-45f + progress * 120f + combo * 30f)
        val rad = angle.toDouble() * PI / 180.0
        val wx = cx + 6f * p
        val wy = by - 18f * p

        paint.color = wt.color
        paint.strokeWidth = 2f * p
        paint.style = Paint.Style.STROKE

        val len = when (wt) {
            WeaponType.SWORD, WeaponType.SPEAR -> 16f * p
            WeaponType.DAGGER                  -> 8f  * p
            WeaponType.STAFF                   -> 20f * p
            WeaponType.CLUB, WeaponType.AXE    -> 12f * p
        }

        canvas.drawLine(
            wx, wy,
            wx + (cos(rad) * len).toFloat(),
            wy + (sin(rad) * len).toFloat(),
            paint
        )

        // Glow trail
        paint.color = wt.glowColor
        paint.alpha = (progress * 200).toInt()
        paint.strokeWidth = p
        canvas.drawLine(
            wx, wy,
            wx + (cos(rad - 0.3) * len * 0.8f).toFloat(),
            wy + (sin(rad - 0.3) * len * 0.8f).toFloat(),
            paint
        )

        paint.style = Paint.Style.FILL
        paint.alpha = 255
    }

    // ─── HUD ─────────────────────────────────────────────────────────────────

    private fun drawHUD(canvas: Canvas, engine: BattleEngine, tick: Int) {
        drawHpBar(canvas, engine.player1, isLeft = true)
        drawHpBar(canvas, engine.player2, isLeft = false)
        drawRoundTimer(canvas, engine, tick)
        drawRoundWins(canvas, engine)
    }

    private fun drawHpBar(canvas: Canvas, fighter: Fighter, isLeft: Boolean) {
        val barW = screenWidth * 0.38f
        val barH = 14f * scaleY
        val barY = 12f * scaleY
        val barX = if (isLeft) 8f * scaleX else screenWidth - barW - 8f * scaleX

        // Background
        paint.color = Color.rgb(20, 10, 30)
        canvas.drawRect(barX, barY, barX + barW, barY + barH, paint)

        // HP fill with color transition
        val hpRatio = fighter.hpPercent
        val hpColor = when {
            hpRatio > 0.5f -> Color.rgb(50, 200, 80)
            hpRatio > 0.25f -> Color.rgb(220, 180, 20)
            else -> Color.rgb(220, 50, 50)
        }
        paint.color = hpColor
        if (isLeft) {
            canvas.drawRect(barX, barY, barX + barW * hpRatio, barY + barH, paint)
        } else {
            canvas.drawRect(barX + barW * (1f - hpRatio), barY, barX + barW, barY + barH, paint)
        }

        // HP bar border
        paint.color = Color.rgb(200, 180, 230)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f * scaleX
        canvas.drawRect(barX, barY, barX + barW, barY + barH, paint)
        paint.style = Paint.Style.FILL

        // Energy bar
        val engBarY = barY + barH + 3f * scaleY
        val engBarH = 6f * scaleY
        paint.color = Color.rgb(20, 20, 40)
        canvas.drawRect(barX, engBarY, barX + barW, engBarY + engBarH, paint)
        paint.color = if (fighter.canUseSpecial)
            Color.rgb(255, 220, 50) else Color.rgb(80, 100, 220)
        val engFill = barW * fighter.energyPercent
        if (isLeft) canvas.drawRect(barX, engBarY, barX + engFill, engBarY + engBarH, paint)
        else canvas.drawRect(barX + barW - engFill, engBarY, barX + barW, engBarY + engBarH, paint)

        // Character name
        textPaint.color = Color.WHITE
        textPaint.textSize = 9f * scaleY
        textPaint.typeface = Typeface.MONOSPACE
        val nameX = if (isLeft) barX + 3f * scaleX else barX + barW - 3f * scaleX
        textPaint.textAlign = if (isLeft) Paint.Align.LEFT else Paint.Align.RIGHT
        canvas.drawText(fighter.characterType.displayName, nameX, barY - 2f * scaleY, textPaint)

        // Round wins (as pixel dots)
        val winsNeeded = (GameConfig.MAX_ROUNDS / 2) + 1
        val wins = if (isLeft) 0 else 0   // drawn by drawRoundWins
        val dotSize = 5f * scaleX
        val dotSpacing = 8f * scaleX
        val dotsStartX = if (isLeft) barX else barX + barW - winsNeeded * dotSpacing
    }

    private fun drawRoundTimer(canvas: Canvas, engine: BattleEngine, tick: Int) {
        val time = engine.roundTimeSeconds
        val cx = screenWidth / 2f

        // Timer box
        paint.color = Color.rgb(20, 10, 40)
        val boxW = 48f * scaleX
        val boxH = 24f * scaleY
        canvas.drawRect(cx - boxW / 2, 6f * scaleY, cx + boxW / 2, 6f * scaleY + boxH, paint)
        paint.color = Color.rgb(120, 80, 160)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f * scaleX
        canvas.drawRect(cx - boxW / 2, 6f * scaleY, cx + boxW / 2, 6f * scaleY + boxH, paint)
        paint.style = Paint.Style.FILL

        // Timer text
        val pulse = if (time <= 10) sin(tick * 0.25) * 0.2 + 1.0 else 1.0
        textPaint.color = if (time <= 10) Color.rgb(255, 100, 100) else Color.WHITE
        textPaint.textSize = (18f * scaleY * pulse).toFloat()
        textPaint.typeface = Typeface.MONOSPACE
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(time.toString(), cx, 6f * scaleY + boxH * 0.75f, textPaint)

        // Round label
        textPaint.textSize = 7f * scaleY
        textPaint.color = Color.rgb(180, 150, 220)
        canvas.drawText("R${engine.currentRound}", cx, 5f * scaleY, textPaint)
    }

    private fun drawRoundWins(canvas: Canvas, engine: BattleEngine) {
        val dotSize = 6f * scaleX
        val dotGap = 4f * scaleX
        val dotY = 34f * scaleY
        val cx = screenWidth / 2f

        for (i in 0 until GameConfig.MAX_ROUNDS) {
            val isP1 = i < engine.p1RoundWins
            val isP2 = i >= GameConfig.MAX_ROUNDS - engine.p2RoundWins
            val dx = cx - (GameConfig.MAX_ROUNDS / 2f - i) * (dotSize + dotGap)
            paint.color = when {
                isP1 -> Color.rgb(100, 200, 255)
                isP2 -> Color.rgb(255, 100, 100)
                else -> Color.rgb(60, 50, 80)
            }
            canvas.drawRect(dx, dotY, dx + dotSize, dotY + dotSize, paint)
        }
    }

    // ─── Hit Effects ─────────────────────────────────────────────────────────

    private fun drawHitEffects(canvas: Canvas, effects: List<HitEffect>, tick: Int) {
        effects.forEach { eff ->
            val alpha = ((eff.frames / (if (eff.isSpecial) 20f else 12f)) * 255).toInt()
            val scale = if (eff.isSpecial) 1.5f else 1f

            // Starburst
            paint.color = if (eff.isSpecial) Color.rgb(255, 220, 50) else Color.rgb(255, 180, 50)
            paint.alpha = alpha

            val cx = eff.x * scaleX
            val cy = eff.y * scaleY
            val size = 8f * scaleX * scale

            for (i in 0 until 8) {
                val angle = i * 45.0 * PI / 180.0 + tick * 0.1
                val dist = size * 0.6f
                canvas.drawRect(
                    cx + cos(angle).toFloat() * dist - scaleX,
                    cy + sin(angle).toFloat() * dist * scaleY / scaleX - scaleY,
                    cx + cos(angle).toFloat() * dist + scaleX,
                    cy + sin(angle).toFloat() * dist * scaleY / scaleX + scaleY,
                    paint
                )
            }

            // Damage number
            if (eff.frames > 6) {
                textPaint.color = if (eff.isSpecial) Color.rgb(255, 255, 100) else Color.WHITE
                textPaint.alpha = alpha
                textPaint.textSize = (if (eff.isSpecial) 14f else 10f) * scaleY
                textPaint.typeface = Typeface.MONOSPACE
                textPaint.textAlign = Paint.Align.CENTER
                canvas.drawText(
                    "-${eff.damage.toInt()}",
                    cx,
                    cy - (eff.frames * 0.5f * scaleY),
                    textPaint
                )
            }
        }
        paint.alpha = 255
        textPaint.alpha = 255
    }

    // ─── Overlay screens ─────────────────────────────────────────────────────

    fun drawRoundStart(canvas: Canvas, roundNum: Int, tick: Int) {
        val alpha = when {
            tick < 15 -> (tick * 17).coerceAtMost(255)
            tick > 45 -> ((60 - tick) * 17).coerceAtLeast(0)
            else -> 255
        }
        drawOverlayText(canvas, "Round $roundNum", alpha)
    }

    fun drawPause(canvas: Canvas) {
        paint.color = Color.argb(180, 0, 0, 0)
        canvas.drawRect(0f, 0f, screenWidth.toFloat(), screenHeight.toFloat(), paint)
        drawOverlayText(canvas, "PAUSED", 255)
    }

    private fun drawOverlayText(canvas: Canvas, text: String, alpha: Int) {
        textPaint.color = Color.WHITE
        textPaint.alpha = alpha
        textPaint.textSize = 28f * scaleY
        textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textPaint.textAlign = Paint.Align.CENTER

        // Shadow
        textPaint.color = Color.rgb(80, 0, 120)
        textPaint.alpha = alpha
        canvas.drawText(text, screenWidth / 2f + 3f * scaleX, screenHeight / 2f + 3f * scaleY, textPaint)

        textPaint.color = Color.WHITE
        textPaint.alpha = alpha
        canvas.drawText(text, screenWidth / 2f, screenHeight / 2f, textPaint)

        textPaint.alpha = 255
    }

    companion object {
        private const val PI = Math.PI
    }
}
