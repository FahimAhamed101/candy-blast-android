package com.sugarpopblast.game

import androidx.compose.ui.graphics.Color

/**
 * The six classic candy pieces. Each one has its own silhouette so the board
 * reads like SugarPop rather than a grid of identical circles.
 */
enum class CandyShape { BEAN, LOZENGE, DROP, SQUARE, GUMDROP, FLOWER }

enum class CandyType(
    val label: String,
    val shape: CandyShape,
    val light: Color,
    val mid: Color,
    val deep: Color
) {
    RED_BEAN("Red Jelly", CandyShape.BEAN, Color(0xFFFF7A8C), Color(0xFFF5233F), Color(0xFFB00C24)),
    ORANGE_LOZENGE("Orange Drop", CandyShape.LOZENGE, Color(0xFFFFC46B), Color(0xFFFF8A1F), Color(0xFFD4530A)),
    YELLOW_DROP("Lemon Drop", CandyShape.DROP, Color(0xFFFFF09A), Color(0xFFFFCF1F), Color(0xFFE09A00)),
    GREEN_SQUARE("Lime Square", CandyShape.SQUARE, Color(0xFF9BF08A), Color(0xFF3BC13B), Color(0xFF14761F)),
    BLUE_GUMDROP("Blue Gum", CandyShape.GUMDROP, Color(0xFF9FE4FF), Color(0xFF27A8F5), Color(0xFF0B5FA8)),
    PURPLE_FLOWER("Grape Blossom", CandyShape.FLOWER, Color(0xFFE3A6FF), Color(0xFFA93BFF), Color(0xFF6413A8));

    companion object {
        fun palette(count: Int): List<CandyType> = entries.take(count.coerceIn(3, entries.size))
    }
}

enum class SpecialType { NONE, ROW, COL, BOMB, RAINBOW }

enum class BlockerType { NONE, CHOCOLATE, COOKIE }

data class Pos(val row: Int, val col: Int) {
    fun isAdjacentTo(other: Pos): Boolean {
        val dr = kotlin.math.abs(row - other.row)
        val dc = kotlin.math.abs(col - other.col)
        return dr + dc == 1
    }
}

/** Live tile used by the engine. */
class Tile(
    val id: Long,
    var candy: CandyType,
    var special: SpecialType = SpecialType.NONE,
    var blocker: BlockerType = BlockerType.NONE,
    var hp: Int = 0
) {
    val isBlocker: Boolean get() = blocker != BlockerType.NONE
}

/** Immutable snapshot handed to the UI. */
data class TileSnap(
    val id: Long,
    val row: Int,
    val col: Int,
    val candy: CandyType,
    val special: SpecialType = SpecialType.NONE,
    val blocker: BlockerType = BlockerType.NONE,
    val hp: Int = 0,
    val clearing: Boolean = false
)

data class JellySnap(val row: Int, val col: Int, val layers: Int)

enum class GoalType { SCORE, COLLECT, JELLY, CHOCOLATE }

data class Goal(val type: GoalType, val target: Int, val candy: CandyType? = null)

data class GoalState(
    val type: GoalType,
    val target: Int,
    val current: Int,
    val candy: CandyType? = null
) {
    val done: Boolean get() = current >= target
    val fraction: Float
        get() = if (target <= 0) 1f else (current.toFloat() / target).coerceIn(0f, 1f)
}

data class Frame(
    val tiles: List<TileSnap>,
    val jelly: List<JellySnap>,
    val score: Int,
    val combo: Int,
    val goals: List<GoalState>,
    val cleared: List<Pos> = emptyList(),
    val created: List<Pos> = emptyList()
)

data class LevelConfig(
    val number: Int,
    val world: Int,
    val worldName: String,
    val rows: Int = 8,
    val cols: Int = 8,
    val moves: Int,
    val colorCount: Int = 5,
    val goals: List<Goal>,
    val jelly: Map<Pos, Int> = emptyMap(),
    val blockers: Map<Pos, Int> = emptyMap(),
    val holes: Set<Pos> = emptySet(),
    val starScores: List<Int>
) {
    val threeStar: Int get() = starScores.last()
}

