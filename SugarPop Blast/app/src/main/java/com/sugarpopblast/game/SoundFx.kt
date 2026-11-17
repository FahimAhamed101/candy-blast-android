package com.sugarpopblast.game

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.sugarpopblast.R

/** Tiny SoundPool wrapper around the audio shipped in res/raw. */
class SoundFx(context: Context) {

    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val matchId = pool.load(context, R.raw.match, 1)
    private val swapId = pool.load(context, R.raw.swap, 1)
    private val winId = pool.load(context, R.raw.levelup, 1)

    var enabled: Boolean = true

    fun swap() = play(swapId, 0.9f)

    /** Quiet tick when a candy is first touched. */
    fun select() = play(swapId, 0.28f, 1.45f)

    fun match(combo: Int) {
        val rate = (1f + (combo - 1).coerceIn(0, 4) * 0.08f)
        play(matchId, 1f, rate)
    }

    fun invalid() = play(swapId, 0.5f, 0.7f)

    fun explode() = play(matchId, 1f, 0.65f)

    fun win() = play(winId, 1f)

    fun lose() = play(swapId, 0.7f, 0.55f)

    private fun play(id: Int, volume: Float, rate: Float = 1f) {
        if (!enabled || id == 0) return
        runCatching { pool.play(id, volume, volume, 1, 0, rate) }
    }

    fun release() {
        runCatching { pool.release() }
    }
}

