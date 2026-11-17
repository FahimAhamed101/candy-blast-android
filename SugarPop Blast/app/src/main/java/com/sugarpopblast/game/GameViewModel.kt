package com.sugarpopblast.game

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameViewModel(app: Application) : AndroidViewModel(app) {

    enum class Status { PLAYING, WON, LOST }

    data class UiState(
        val level: LevelConfig? = null,
        val frame: Frame? = null,
        val movesLeft: Int = 0,
        val selected: Pos? = null,
        val status: Status = Status.PLAYING,
        val stars: Int = 0,
        val hammers: Int = 3,
        val hammerMode: Boolean = false,
        val hint: Pair<Pos, Pos>? = null,
        val comboFlash: Int = 0,
        val shakeTick: Int = 0,
        val coins: Int = 0,
        val busy: Boolean = false
    )

    private val store = ProgressStore(app)
    private val sound = SoundFx(app).apply { enabled = store.soundEnabled }
    private var engine: MatchEngine? = null
    private var animJob: Job? = null
    private var comboJob: Job? = null

    var state by mutableStateOf(UiState(coins = store.coins))
        private set

    // ------------------------------------------------------------- lifecycle

    fun startLevel(level: Int) {
        animJob?.cancel()
        comboJob?.cancel()
        val config = Levels.get(level)
        val fresh = MatchEngine(config)
        engine = fresh
        sound.enabled = store.soundEnabled
        state = UiState(
            level = config,
            frame = fresh.frame(),
            movesLeft = fresh.movesLeft,
            coins = store.coins
        )
    }

    fun retry() {
        state.level?.let { startLevel(it.number) }
    }

    fun nextLevel() {
        val next = ((state.level?.number ?: 1) + 1).coerceAtMost(Levels.COUNT)
        startLevel(next)
    }

    fun toggleSound(): Boolean {
        store.soundEnabled = !store.soundEnabled
        sound.enabled = store.soundEnabled
        return store.soundEnabled
    }

    fun toggleMusic(): Boolean {
        store.musicEnabled = !store.musicEnabled
        return store.musicEnabled
    }

    fun toggleVibration(): Boolean {
        store.vibrationEnabled = !store.vibrationEnabled
        return store.vibrationEnabled
    }

    fun soundEnabled(): Boolean = store.soundEnabled
    fun musicEnabled(): Boolean = store.musicEnabled
    fun vibrationEnabled(): Boolean = store.vibrationEnabled
    fun coins(): Int = store.coins
    fun totalStars(): Int = store.totalStars()
    fun starsFor(level: Int): Int = store.stars(level)
    fun isUnlocked(level: Int): Boolean = store.isUnlocked(level)
    fun currentLevel(): Int = store.currentLevel()

    // ---------------------------------------------------------------- input

    fun onCellTapped(p: Pos) {
        val snapshot = state
        if (snapshot.busy || snapshot.status != Status.PLAYING) return
        if (snapshot.hammerMode) {
            useHammerAt(p)
            return
        }
        val selected = snapshot.selected
        when {
            selected == null -> state = snapshot.copy(selected = p, hint = null)
            selected == p -> state = snapshot.copy(selected = null)
            selected.isAdjacentTo(p) -> attemptSwap(selected, p)
            else -> state = snapshot.copy(selected = p)
        }
    }

    fun onSwipe(from: Pos, to: Pos) {
        if (state.busy || state.status != Status.PLAYING || state.hammerMode) return
        if (!from.isAdjacentTo(to)) return
        attemptSwap(from, to)
    }

    /** Soft tick when a finger lands on a tile. */
    fun onPressFeedback() {
        if (state.busy || state.status != Status.PLAYING) return
        sound.select()
        vibrate(8)
    }

    private fun attemptSwap(a: Pos, b: Pos) {
        val active = engine ?: return
        val frames = active.trySwap(a, b)
        if (frames == null) {
            sound.invalid()
            vibrate(28)
            state = state.copy(selected = null, shakeTick = state.shakeTick + 1)
            return
        }
        sound.swap()
        vibrate(18)
        playFrames(frames)
    }

    fun toggleHammer() {
        if (state.hammers <= 0 || state.status != Status.PLAYING) return
        state = state.copy(hammerMode = !state.hammerMode, selected = null)
    }

    private fun useHammerAt(p: Pos) {
        val active = engine ?: return
        if (state.hammers <= 0) return
        val frames = active.hammer(p)
        if (frames.isEmpty()) return
        state = state.copy(hammers = state.hammers - 1, hammerMode = false)
        sound.explode()
        vibrate(30)
        playFrames(frames)
    }

    fun showHint() {
        if (state.busy || state.status != Status.PLAYING) return
        val hint = engine?.findHint() ?: return
        state = state.copy(hint = hint, selected = null)
        viewModelScope.launch {
            delay(2200)
            if (state.hint == hint) state = state.copy(hint = null)
        }
    }

    /** Reshuffles the board from the booster bar. */
    fun shuffleBoard() {
        if (state.busy || state.status != Status.PLAYING) return
        val active = engine ?: return
        if (!active.shuffleBoard()) return
        sound.swap()
        vibrate(22)
        state = state.copy(
            frame = active.frame(),
            selected = null,
            hint = null,
            shakeTick = state.shakeTick + 1
        )
    }

    fun continueWithMoves() {
        val active = engine ?: return
        if (state.status != Status.LOST) return
        if (!store.spendCoins(50)) return
        active.addMoves(5)
        state = state.copy(
            status = Status.PLAYING,
            movesLeft = active.movesLeft,
            frame = active.frame(),
            coins = store.coins
        )
    }

    // ------------------------------------------------------------ animation

    private fun playFrames(frames: List<Frame>) {
        val active = engine ?: return
        animJob?.cancel()
        // flip `busy` synchronously: doing it inside the coroutine left a window
        // where a second tap could start another swap mid-animation
        state = state.copy(busy = true, selected = null, hint = null)
        animJob = viewModelScope.launch {
            for (frame in frames) {
                state = state.copy(
                    frame = frame,
                    movesLeft = active.movesLeft
                )
                if (frame.cleared.isNotEmpty()) {
                    // the banner runs on its own timer, otherwise the next
                    // frame wipes it after ~190 ms and it is unreadable
                    if (frame.combo > 1) showCombo(frame.combo)
                    sound.match(frame.combo)
                    vibrate(14)
                    delay(if (frame.combo > 1) 190 else 160)
                } else {
                    delay(230)
                }
            }
            state = state.copy(frame = active.frame(), busy = false)
            evaluate()
        }
    }

    /** Shows "COMBO xN" and keeps it up long enough to read. */
    private fun showCombo(value: Int) {
        comboJob?.cancel()
        state = state.copy(comboFlash = value)
        comboJob = viewModelScope.launch {
            delay(1400)
            state = state.copy(comboFlash = 0)
        }
    }

    private fun evaluate() {
        val active = engine ?: return
        if (active.goalsComplete()) {
            active.addBonus(active.movesLeft * 150)
            val stars = maxOf(1, active.stars())
            store.recordWin(active.level.number, stars, active.score)
            state = state.copy(
                status = Status.WON,
                stars = stars,
                frame = active.frame(),
                coins = store.coins
            )
            sound.win()
            vibrate(120)
        } else if (active.movesLeft <= 0) {
            state = state.copy(status = Status.LOST, frame = active.frame())
            sound.lose()
        }
    }

    // -------------------------------------------------------------- helpers

    private fun vibrate(ms: Long) {
        if (!store.vibrationEnabled) return
        runCatching {
            val context = getApplication<Application>()
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager)
                    .defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            if (!vibrator.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(ms)
            }
        }
    }

    override fun onCleared() {
        animJob?.cancel()
        comboJob?.cancel()
        sound.release()
        super.onCleared()
    }
}

