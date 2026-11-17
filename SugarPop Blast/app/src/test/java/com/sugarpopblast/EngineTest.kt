package com.sugarpopblast

import com.sugarpopblast.game.Levels
import com.sugarpopblast.game.MatchEngine
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Smoke tests for the merged engine: every level must build, expose a legal
 * move, and survive a full game without throwing.
 */
class EngineTest {

    @Test
    fun everyLevelBuildsWithALegalMove() {
        for (level in 1..Levels.COUNT) {
            val config = Levels.get(level)
            val engine = MatchEngine(config, seed = level.toLong())
            assertTrue("level $level has no goals", config.goals.isNotEmpty())
            assertNotNull("level $level is deadlocked at start", engine.findHint())
            val frame = engine.frame()
            assertTrue("level $level produced no tiles", frame.tiles.isNotEmpty())
            frame.tiles.forEach { tile ->
                assertTrue(tile.row in 0 until config.rows)
                assertTrue(tile.col in 0 until config.cols)
            }
        }
    }

    @Test
    fun fullPlaythroughNeverThrows() {
        for (level in 1..Levels.COUNT) {
            val engine = MatchEngine(Levels.get(level), seed = level * 97L)
            var guard = 0
            while (engine.movesLeft > 0 && guard++ < 300) {
                val hint = engine.findHint() ?: break
                val frames = engine.trySwap(hint.first, hint.second)
                assertNotNull("legal hint was rejected on level $level", frames)
                assertTrue(frames!!.isNotEmpty())
                frames.forEach { frame ->
                    frame.tiles.forEach { tile ->
                        assertTrue(tile.row >= 0)
                        assertTrue(tile.col >= 0)
                    }
                }
            }
            assertTrue("moves went negative on level $level", engine.movesLeft >= 0)
        }
    }

    @Test
    fun hammerClearsTiles() {
        val engine = MatchEngine(Levels.get(1), seed = 5L)
        val rng = Random(3)
        repeat(20) {
            val target = engine.frame().tiles.filter { it.blocker == com.sugarpopblast.game.BlockerType.NONE }
            if (target.isEmpty()) return@repeat
            val tile = target[rng.nextInt(target.size)]
            val frames = engine.hammer(com.sugarpopblast.game.Pos(tile.row, tile.col))
            assertTrue(frames.isNotEmpty())
        }
    }
}

