package com.sugarpopblast.game

import android.content.Context

class ProgressStore(context: Context) {

    private val prefs = context.getSharedPreferences("candy_crush_blast_save_v2", Context.MODE_PRIVATE)

    fun stars(level: Int): Int = prefs.getInt("stars_$level", 0)

    fun isUnlocked(level: Int): Boolean = level <= 1 || stars(level - 1) > 0

    /** First level the player has not finished yet. */
    fun currentLevel(): Int =
        (1..Levels.COUNT).firstOrNull { stars(it) == 0 } ?: Levels.COUNT

    fun totalStars(): Int = (1..Levels.COUNT).sumOf { stars(it) }

    var coins: Int
        get() = prefs.getInt("coins", 150)
        set(value) = prefs.edit().putInt("coins", value.coerceAtLeast(0)).apply()

    var bestScore: Int
        get() = prefs.getInt("best_score", 0)
        set(value) = prefs.edit().putInt("best_score", value).apply()

    var soundEnabled: Boolean
        get() = prefs.getBoolean("sound", true)
        set(value) = prefs.edit().putBoolean("sound", value).apply()

    var musicEnabled: Boolean
        get() = prefs.getBoolean("music", true)
        set(value) = prefs.edit().putBoolean("music", value).apply()

    var vibrationEnabled: Boolean
        get() = prefs.getBoolean("vibration", true)
        set(value) = prefs.edit().putBoolean("vibration", value).apply()

    fun recordWin(level: Int, stars: Int, score: Int) {
        if (stars > stars(level)) {
            prefs.edit().putInt("stars_$level", stars).apply()
        }
        coins += 30 + stars * 25
        if (score > bestScore) bestScore = score
    }

    fun spendCoins(amount: Int): Boolean {
        if (coins < amount) return false
        coins -= amount
        return true
    }

    fun reset() {
        prefs.edit().clear().apply()
    }
}

