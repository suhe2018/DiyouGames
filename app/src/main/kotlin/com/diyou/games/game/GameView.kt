package com.diyou.games.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PixelFormat
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.diyou.games.data.CharacterType
import com.diyou.games.data.WeaponType

/**
 * SurfaceView-based game view that owns the render + game loop threads.
 */
class GameView(
    context: Context,
    p1Char: CharacterType,
    p1Weapon: WeaponType,
    p2Char: CharacterType,
    p2Weapon: WeaponType,
    gameMode: GameMode,
    aiDifficulty: AIDifficulty
) : SurfaceView(context), SurfaceHolder.Callback {

    private var gameThread: GameThread? = null
    private lateinit var renderer: PixelRenderer
    private lateinit var engine: BattleEngine

    // Stored for deferred init (we need actual dimensions)
    private val p1Char   = p1Char
    private val p1Weapon = p1Weapon
    private val p2Char   = p2Char
    private val p2Weapon = p2Weapon
    private val gameMode = gameMode
    private val aiDiff   = aiDifficulty

    // Callbacks for the hosting Activity
    var onRoundEnd: ((RoundResult) -> Unit)? = null
    var onGameOver: ((Int) -> Unit)? = null   // winner player number (1/2/0=draw)
    var onPauseToggle: ((Boolean) -> Unit)? = null

    init {
        holder.addCallback(this)
        holder.setFormat(PixelFormat.RGBA_8888)
        isFocusable = true
        isFocusableInTouchMode = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        val w = holder.surfaceFrame.width()
        val h = holder.surfaceFrame.height()
        renderer = PixelRenderer(w, h)
        engine    = BattleEngine(p1Char, p1Weapon, p2Char, p2Weapon, gameMode, aiDiff, w, h)
        startThread()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // handled in surfaceCreated
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopThread()
    }

    private fun startThread() {
        gameThread = GameThread(holder, engine, renderer) { event ->
            when (event) {
                is GameEvent.RoundEnd -> onRoundEnd?.invoke(event.result)
                is GameEvent.GameOver -> onGameOver?.invoke(event.winner)
            }
        }.also { it.start() }
    }

    private fun stopThread() {
        gameThread?.running = false
        gameThread?.join(1500)
        gameThread = null
    }

    // ─── Public control API ───────────────────────────────────────────────────

    fun setP1Input(input: InputState) {
        engine.p1Input = input
    }

    fun setP2Input(input: InputState) {
        engine.p2Input = input
    }

    fun togglePause() {
        engine.isPaused = !engine.isPaused
        onPauseToggle?.invoke(engine.isPaused)
    }

    val isPaused: Boolean get() = ::engine.isInitialized && engine.isPaused
    val isGameOver: Boolean get() = ::engine.isInitialized && engine.isGameOver

    fun getBattleEngine(): BattleEngine? = if (::engine.isInitialized) engine else null
}

// ─── Game Thread ─────────────────────────────────────────────────────────────

sealed class GameEvent {
    data class RoundEnd(val result: RoundResult) : GameEvent()
    data class GameOver(val winner: Int)         : GameEvent()
}

class GameThread(
    private val holder: SurfaceHolder,
    private val engine: BattleEngine,
    private val renderer: PixelRenderer,
    private val onEvent: (GameEvent) -> Unit
) : Thread("GameThread") {

    @Volatile var running = false
    private var tick = 0
    private var roundStartTick = -1
    private var prevRoundResult = RoundResult.ONGOING
    private var prevGameOver = false

    override fun run() {
        running = true
        var nextFrameTime = System.nanoTime()

        while (running) {
            val now = System.nanoTime()
            if (now >= nextFrameTime) {
                engine.update()
                tick++

                // Fire events
                if (engine.roundResult != RoundResult.ONGOING && engine.roundResult != prevRoundResult) {
                    prevRoundResult = engine.roundResult
                    post { onEvent(GameEvent.RoundEnd(engine.roundResult)) }
                }
                if (engine.isGameOver && !prevGameOver) {
                    prevGameOver = true
                    post { onEvent(GameEvent.GameOver(engine.winner)) }
                }

                render()
                nextFrameTime += GameConfig.FRAME_TIME_MS * 1_000_000L
            } else {
                val sleepMs = (nextFrameTime - now) / 1_000_000L
                if (sleepMs > 0) sleep(sleepMs)
            }
        }
    }

    private fun render() {
        var canvas: Canvas? = null
        try {
            canvas = holder.lockHardwareCanvas()
            if (canvas != null) {
                canvas.drawColor(Color.BLACK)
                renderer.drawBattle(canvas, engine, tick)
                if (engine.isPaused) renderer.drawPause(canvas)
            }
        } finally {
            if (canvas != null) {
                try { holder.unlockCanvasAndPost(canvas) } catch (_: Exception) {}
            }
        }
    }
}
