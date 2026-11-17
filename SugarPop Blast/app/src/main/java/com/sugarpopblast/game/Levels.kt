package com.sugarpopblast.game

/**
 * The full campaign: 4 worlds x 12 levels = 48 hand-tuned levels.
 *
 * Goal targets are derived from the generated board, so a level can never ask
 * for more jelly than actually exists on the board.
 */
object Levels {

    const val COUNT = 48

    data class World(val index: Int, val name: String, val emoji: String, val tagline: String)

    val worlds = listOf(
        World(1, "Berry Grove", "ðŸ“", "Where every crush begins"),
        World(2, "Citrus Cove", "ðŸŠ", "Sunny shores of sticky jelly"),
        World(3, "Grape Gardens", "ðŸ‡", "Twisted vines and dark chocolate"),
        World(4, "Cosmic Candy", "ðŸ‘", "The final sugar frontier")
    )

    fun worldOf(level: Int): Int = ((level - 1) / 12 + 1).coerceIn(1, 4)

    fun world(level: Int): World = worlds[worldOf(level) - 1]

    fun totalStars(store: (Int) -> Int): Int = (1..COUNT).sumOf { store(it) }

    fun get(level: Int): LevelConfig {
        val n = level.coerceIn(1, COUNT)
        val worldIndex = worldOf(n)
        val moves = (26 - n / 4).coerceIn(18, 26)
        val colors = if (n <= 30) 5 else 6
        val jelly = mutableMapOf<Pos, Int>()
        val blockers = mutableMapOf<Pos, Int>()
        val holes = mutableSetOf<Pos>()
        val goals = mutableListOf<Goal>()

        fun jellyRect(r0: Int, r1: Int, c0: Int, c1: Int, layers: Int = 1) {
            for (r in r0..r1) for (c in c0..c1) jelly[Pos(r, c)] = layers
        }

        fun jellyDiamond(centerR: Int, centerC: Int, radius: Int, layers: Int = 1) {
            for (r in 0 until 8) for (c in 0 until 8) {
                if (kotlin.math.abs(r - centerR) + kotlin.math.abs(c - centerC) <= radius) {
                    jelly[Pos(r, c)] = layers
                }
            }
        }

        fun jellyChecker(layers: Int = 1) {
            for (r in 1..6) for (c in 1..6) {
                if ((r + c) % 2 == 0) jelly[Pos(r, c)] = layers
            }
        }

        fun jellyRing(layers: Int = 1) {
            for (r in 0 until 8) for (c in 0 until 8) {
                if (r == 0 || c == 0 || r == 7 || c == 7) jelly[Pos(r, c)] = layers
            }
        }

        fun choco(vararg cells: Pair<Int, Int>, hp: Int = 1) {
            cells.forEach { (r, c) ->
                val p = Pos(r, c)
                if (p !in jelly) blockers[p] = hp
            }
        }

        fun scatterChoco(count: Int, hp: Int) {
            val candidates = (0 until 8).flatMap { r -> (0 until 8).map { c -> Pos(r, c) } }
                .filter { it !in jelly && it !in blockers && !(it.row in 3..4 && it.col in 3..4) }
                .shuffled(kotlin.random.Random(n * 31L + 7))
            candidates.take(count).forEach { blockers[it] = hp }
        }

        fun addGoals() {
            val jellyLayers = jelly.values.sum()
            val chocolateCount = blockers.size
            when {
                jellyLayers > 0 && chocolateCount > 0 && n % 3 == 0 -> {
                    goals.add(Goal(GoalType.JELLY, jellyLayers))
                    goals.add(Goal(GoalType.CHOCOLATE, chocolateCount))
                }
                jellyLayers > 0 -> goals.add(Goal(GoalType.JELLY, jellyLayers))
                chocolateCount > 0 -> goals.add(Goal(GoalType.CHOCOLATE, chocolateCount))
                else -> goals.add(Goal(GoalType.SCORE, 1800 + n * 260))
            }
        }

        when (n) {
            // ---------------------------------------------------- WORLD 1
            1 -> { addGoals() }
            2 -> goals.add(Goal(GoalType.COLLECT, 18, CandyType.RED_BEAN))
            3 -> { addGoals() }
            4 -> { jellyRect(2, 5, 2, 5); addGoals() }
            5 -> {
                goals.add(Goal(GoalType.COLLECT, 16, CandyType.ORANGE_LOZENGE))
                goals.add(Goal(GoalType.COLLECT, 16, CandyType.YELLOW_DROP))
            }
            6 -> { addGoals() }
            7 -> { jellyRect(6, 7, 0, 7); addGoals() }
            8 -> { choco(0 to 0, 0 to 7, 7 to 0, 7 to 7, 3 to 3, 4 to 4); addGoals() }
            9 -> goals.add(Goal(GoalType.COLLECT, 24, CandyType.YELLOW_DROP))
            10 -> { jellyChecker(); addGoals() }
            11 -> { jellyRect(2, 5, 2, 5); choco(0 to 0, 0 to 7, 7 to 0, 7 to 7, 3 to 1, 4 to 6); addGoals() }
            12 -> { addGoals() }

            // ---------------------------------------------------- WORLD 2
            13 -> { jellyRect(1, 6, 1, 6); addGoals() }
            14 -> goals.add(Goal(GoalType.COLLECT, 26, CandyType.PURPLE_FLOWER))
            15 -> { scatterChoco(10, 1); addGoals() }
            16 -> { jellyDiamond(3, 3, 3); jellyDiamond(4, 4, 3); addGoals() }
            17 -> {
                goals.add(Goal(GoalType.COLLECT, 20, CandyType.PURPLE_FLOWER))
                goals.add(Goal(GoalType.COLLECT, 20, CandyType.GREEN_SQUARE))
            }
            18 -> { addGoals() }
            19 -> {
                jellyRect(1, 6, 1, 6)
                jellyRect(3, 4, 3, 4, 2)
                addGoals()
            }
            20 -> { scatterChoco(12, 1); goals.add(Goal(GoalType.COLLECT, 18, CandyType.ORANGE_LOZENGE)); addGoals() }
            21 -> {
                for (r in 0..1) for (c in 0..1) jelly[Pos(r, c)] = 2
                for (r in 0..1) for (c in 6..7) jelly[Pos(r, c)] = 2
                for (r in 6..7) for (c in 0..1) jelly[Pos(r, c)] = 2
                for (r in 6..7) for (c in 6..7) jelly[Pos(r, c)] = 2
                addGoals()
            }
            22 -> goals.add(Goal(GoalType.COLLECT, 30, CandyType.BLUE_GUMDROP))
            23 -> { scatterChoco(14, 1); addGoals() }
            24 -> { addGoals() }

            // ---------------------------------------------------- WORLD 3
            25 -> { jellyRect(1, 6, 1, 6); addGoals() }
            26 -> goals.add(Goal(GoalType.COLLECT, 30, CandyType.PURPLE_FLOWER))
            27 -> { scatterChoco(14, 1); addGoals() }
            28 -> {
                jellyRect(1, 6, 1, 6)
                jellyRect(3, 4, 3, 4, 2)
                addGoals()
            }
            29 -> {
                goals.add(Goal(GoalType.COLLECT, 26, CandyType.YELLOW_DROP))
                goals.add(Goal(GoalType.COLLECT, 26, CandyType.BLUE_GUMDROP))
            }
            30 -> { addGoals() }
            31 -> {
                jellyRing()
                jellyRect(3, 4, 3, 4, 2)
                addGoals()
            }
            32 -> { scatterChoco(16, 2); goals.add(Goal(GoalType.COLLECT, 22, CandyType.GREEN_SQUARE)); addGoals() }
            33 -> {
                jellyDiamond(3, 3, 4)
                jellyDiamond(4, 4, 4)
                addGoals()
            }
            34 -> goals.add(Goal(GoalType.COLLECT, 34, CandyType.BLUE_GUMDROP))
            35 -> { scatterChoco(18, 2); addGoals() }
            36 -> { addGoals() }

            // ---------------------------------------------------- WORLD 4
            37 -> {
                jellyRect(1, 6, 1, 6)
                jellyRect(3, 4, 3, 4, 2)
                addGoals()
            }
            38 -> {
                goals.add(Goal(GoalType.COLLECT, 30, CandyType.RED_BEAN))
                goals.add(Goal(GoalType.COLLECT, 30, CandyType.ORANGE_LOZENGE))
            }
            39 -> { scatterChoco(20, 2); addGoals() }
            40 -> {
                jellyRing()
                jellyRect(3, 4, 3, 4, 2)
                addGoals()
            }
            41 -> goals.add(Goal(GoalType.COLLECT, 40, CandyType.PURPLE_FLOWER))
            42 -> { addGoals() }
            43 -> {
                jellyChecker(2)
                scatterChoco(10, 2)
                addGoals()
            }
            44 -> {
                goals.add(Goal(GoalType.COLLECT, 24, CandyType.YELLOW_DROP))
                goals.add(Goal(GoalType.COLLECT, 24, CandyType.GREEN_SQUARE))
                goals.add(Goal(GoalType.COLLECT, 24, CandyType.BLUE_GUMDROP))
            }
            45 -> { scatterChoco(24, 2); addGoals() }
            46 -> {
                jellyRect(1, 6, 1, 6)
                jellyRect(2, 5, 2, 5, 2)
                addGoals()
            }
            47 -> {
                goals.add(Goal(GoalType.COLLECT, 45, CandyType.BLUE_GUMDROP))
                addGoals()
            }
            48 -> { addGoals() }
        }

        // A few carved-out corners in the last world for board variety.
        if (n >= 43 && n % 2 == 1) {
            holes.add(Pos(0, 0))
            holes.add(Pos(0, 7))
            holes.add(Pos(7, 0))
            holes.add(Pos(7, 7))
            holes.forEach { jelly.remove(it); blockers.remove(it) }
        }

        val star1 = 1800 + n * 260
        return LevelConfig(
            number = n,
            world = worldIndex,
            worldName = world(n).name,
            moves = moves,
            colorCount = colors,
            goals = goals,
            jelly = jelly.toMap(),
            blockers = blockers.toMap(),
            holes = holes.toSet(),
            starScores = listOf(star1, (star1 * 1.7).toInt(), (star1 * 2.5).toInt())
        )
    }

    fun goalLabel(goal: GoalState): String = when (goal.type) {
        GoalType.SCORE -> "${goal.current}/${goal.target}"
        GoalType.COLLECT -> "${goal.current}/${goal.target}"
        GoalType.JELLY -> "${goal.current}/${goal.target}"
        GoalType.CHOCOLATE -> "${goal.current}/${goal.target}"
    }
}

