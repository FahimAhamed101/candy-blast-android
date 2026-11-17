package com.sugarpopblast.game

import kotlin.random.Random

/**
 * Self-contained match-3 engine.
 *
 * Everything the UI needs is produced as a list of [Frame]s: pop frame (tiles
 * flagged `clearing`), then settle frame (tiles in their new positions). The UI
 * animates between frames, so the engine never touches Compose.
 */
class MatchEngine(val level: LevelConfig, seed: Long = level.number * 7919L + 13L) {

    private val rows = level.rows
    private val cols = level.cols
    private val rng = Random(seed)
    private val grid = Array(rows) { arrayOfNulls<Tile>(cols) }
    private val jelly = Array(rows) { IntArray(cols) }
    private var nextId = 1L
    private var combo = 0

    var score = 0
        private set
    var movesLeft = level.moves
        private set
    var jellyCleared = 0
        private set
    var chocolateBroken = 0
        private set

    private val collected = mutableMapOf<CandyType, Int>()
    private val palette = CandyType.palette(level.colorCount)

    init {
        level.jelly.forEach { (p, layers) ->
            if (inBounds(p)) jelly[p.row][p.col] = layers
        }
        level.blockers.forEach { (p, hp) ->
            if (inBounds(p) && !isHole(p)) {
                grid[p.row][p.col] = Tile(
                    id = nextId++,
                    candy = palette.first(),
                    blocker = BlockerType.CHOCOLATE,
                    hp = hp
                )
            }
        }
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val p = Pos(r, c)
                if (isHole(p) || grid[r][c] != null) continue
                grid[r][c] = Tile(nextId++, safeCandyAt(r, c))
            }
        }
        // Remove any accidental starting matches.
        var guard = 0
        while (guard++ < 60) {
            val runs = findRuns()
            if (runs.isEmpty()) break
            runs.flatMap { it.cells }.distinct().forEach { p ->
                grid[p.row][p.col]?.let { if (!it.isBlocker) it.candy = safeCandyAt(p.row, p.col) }
            }
        }
        if (findHint() == null) shuffle()
    }

    // ---------------------------------------------------------------- queries

    fun goalsComplete(): Boolean = goalStates().all { it.done }

    fun goalStates(): List<GoalState> = level.goals.map { goal ->
        val current = when (goal.type) {
            GoalType.SCORE -> score
            GoalType.COLLECT -> collected[goal.candy] ?: 0
            GoalType.JELLY -> jellyCleared
            GoalType.CHOCOLATE -> chocolateBroken
        }
        GoalState(goal.type, goal.target, current, goal.candy)
    }

    fun stars(): Int = level.starScores.count { score >= it }

    fun frame(clearing: Set<Pos> = emptySet(), created: List<Pos> = emptyList()): Frame {
        val tiles = ArrayList<TileSnap>(rows * cols)
        val jellyList = ArrayList<JellySnap>()
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (jelly[r][c] > 0) jellyList.add(JellySnap(r, c, jelly[r][c]))
                val t = grid[r][c] ?: continue
                tiles.add(
                    TileSnap(
                        id = t.id,
                        row = r,
                        col = c,
                        candy = t.candy,
                        special = t.special,
                        blocker = t.blocker,
                        hp = t.hp,
                        clearing = Pos(r, c) in clearing
                    )
                )
            }
        }
        return Frame(
            tiles = tiles,
            jelly = jellyList,
            score = score,
            combo = combo,
            goals = goalStates(),
            cleared = clearing.toList(),
            created = created
        )
    }

    fun findHint(): Pair<Pos, Pos>? {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val p = Pos(r, c)
                if (!isPlayable(p)) continue
                if (grid[r][c]?.special == SpecialType.RAINBOW) {
                    val partner = neighbors4(p).firstOrNull { isPlayable(it) }
                    if (partner != null) return p to partner
                }
                for (q in listOf(Pos(r, c + 1), Pos(r + 1, c))) {
                    if (!isPlayable(q)) continue
                    swapCells(p, q)
                    val works = findRuns().any { it.cells.size >= 3 }
                    swapCells(p, q)
                    if (works) return p to q
                }
            }
        }
        return null
    }

    // ----------------------------------------------------------------- moves

    fun trySwap(a: Pos, b: Pos): List<Frame>? {
        if (movesLeft <= 0) return null
        if (!a.isAdjacentTo(b)) return null
        if (!isPlayable(a) || !isPlayable(b)) return null

        swapCells(a, b)
        val directMatch = findRuns().any { it.cells.size >= 3 }
        val rainbowA = grid[a.row][a.col]?.special == SpecialType.RAINBOW
        val rainbowB = grid[b.row][b.col]?.special == SpecialType.RAINBOW
        if (!directMatch && !rainbowA && !rainbowB) {
            swapCells(a, b)
            return null
        }

        movesLeft--
        combo = 0
        val frames = mutableListOf<Frame>()
        val seedCells = mutableSetOf<Pos>()
        if (rainbowA || rainbowB) {
            val rainbowPos = if (rainbowA) a else b
            val other = if (rainbowA) b else a
            val target = grid[other.row][other.col]?.candy ?: palette.first()
            allPlayable().filter { grid[it.row][it.col]?.candy == target }.forEach { seedCells.add(it) }
            seedCells.add(rainbowPos)
            grid[rainbowPos.row][rainbowPos.col]?.special = SpecialType.NONE
        }

        resolve(frames, preferred = listOf(b, a), initialSeed = seedCells)
        afterCascade(frames)
        return frames
    }

    fun hammer(p: Pos): List<Frame> {
        if (!isPlayable(p)) return emptyList()
        combo = 1
        val frames = mutableListOf<Frame>()
        val clear = expand(setOf(p))
        if (clear.isEmpty()) return emptyList()
        frames.add(frame(clearing = clear))
        applyClear(clear)
        settle()
        frames.add(frame())
        resolve(frames, preferred = emptyList())
        afterCascade(frames)
        return frames
    }

    fun addMoves(amount: Int) {
        movesLeft += amount
    }

    fun addBonus(points: Int) {
        score += points
    }

    // ------------------------------------------------------------ resolution

    private fun resolve(
        frames: MutableList<Frame>,
        preferred: List<Pos>,
        initialSeed: Set<Pos> = emptySet()
    ) {
        var seed = initialSeed
        var guard = 0
        while (guard++ < 40) {
            val runs = findRuns().filter { it.cells.size >= 3 }
            if (runs.isEmpty() && seed.isEmpty()) break
            combo++

            val creations = planSpecials(runs, preferred)
            val seeds = LinkedHashSet<Pos>()
            runs.forEach { seeds.addAll(it.cells) }
            seeds.addAll(seed)
            val clear = expand(seeds)
            creations.keys.forEach { clear.remove(it) }
            seed = emptySet()

            if (clear.isEmpty()) break

            frames.add(frame(clearing = clear, created = creations.keys.toList()))
            applyClear(clear, creations)
            settle()
            frames.add(frame())
        }
    }

    private fun afterCascade(frames: MutableList<Frame>) {
        if (findHint() == null) {
            if (shuffle()) {
                frames.add(frame())
            }
        }
    }

    private fun planSpecials(runs: List<Run>, preferred: List<Pos>): Map<Pos, SpecialType> {
        val result = LinkedHashMap<Pos, SpecialType>()
        val horizontalCells = runs.filter { it.horizontal }.flatMap { it.cells }.toSet()
        val verticalCells = runs.filter { !it.horizontal }.flatMap { it.cells }.toSet()
        val crossing = horizontalCells intersect verticalCells
        crossing.firstOrNull()?.let { result[it] = SpecialType.BOMB }

        runs.forEach { run ->
            if (result.keys.any { it in run.cells }) return@forEach
            val type = when {
                run.cells.size >= 5 -> SpecialType.RAINBOW
                run.cells.size == 4 -> if (run.horizontal) SpecialType.ROW else SpecialType.COL
                else -> return@forEach
            }
            val spot = run.cells.firstOrNull { it in preferred } ?: run.cells[run.cells.size / 2]
            if (isPlayable(spot)) result[spot] = type
        }
        return result
    }

    private fun expand(seed: Set<Pos>): LinkedHashSet<Pos> {
        val result = LinkedHashSet<Pos>()
        val queue = ArrayDeque<Pos>()
        seed.forEach { p ->
            if (isPlayable(p) && result.add(p)) queue.add(p)
        }
        while (queue.isNotEmpty()) {
            val p = queue.removeFirst()
            val tile = grid[p.row][p.col] ?: continue
            val extra: List<Pos> = when (tile.special) {
                SpecialType.ROW -> (0 until cols).map { Pos(p.row, it) }
                SpecialType.COL -> (0 until rows).map { Pos(it, p.col) }
                SpecialType.BOMB -> neighbors3x3(p)
                SpecialType.RAINBOW -> {
                    val target = dominantCandy() ?: tile.candy
                    allPlayable().filter { grid[it.row][it.col]?.candy == target }
                }
                SpecialType.NONE -> emptyList()
            }
            extra.forEach { q ->
                if (isPlayable(q) && result.add(q)) queue.add(q)
            }
        }
        return result
    }

    private fun applyClear(clear: Set<Pos>, creations: Map<Pos, SpecialType> = emptyMap()) {
        val tilePoints = 60 * combo.coerceAtLeast(1)
        for (p in clear) {
            val tile = grid[p.row][p.col] ?: continue
            if (tile.isBlocker) continue
            grid[p.row][p.col] = null
            score += tilePoints
            collected[tile.candy] = (collected[tile.candy] ?: 0) + 1
            if (jelly[p.row][p.col] > 0) {
                jelly[p.row][p.col]--
                jellyCleared++
                score += 25
            }
            neighbors4(p).forEach { n ->
                val blocker = grid[n.row][n.col]
                if (blocker != null && blocker.isBlocker) {
                    blocker.hp--
                    score += 30
                    if (blocker.hp <= 0) {
                        grid[n.row][n.col] = null
                        chocolateBroken++
                        score += 100
                    }
                }
            }
        }
        creations.forEach { (p, special) ->
            grid[p.row][p.col]?.let { it.special = special }
        }
    }

    private fun settle() {
        for (c in 0 until cols) {
            val validRows = (0 until rows).filter { !isHole(Pos(it, c)) }
            var target = validRows.lastIndex
            for (i in validRows.indices.reversed()) {
                val r = validRows[i]
                val tile = grid[r][c]
                if (tile != null && tile.isBlocker) {
                    target = i - 1
                    continue
                }
                if (tile != null) {
                    if (target > i) {
                        val tr = validRows[target]
                        grid[tr][c] = tile
                        grid[r][c] = null
                    }
                    target--
                }
            }
            validRows.forEach { r ->
                if (grid[r][c] == null) grid[r][c] = Tile(nextId++, palette.random(rng))
            }
        }
    }

    /** Manual reshuffle from the booster bar. */
    fun shuffleBoard(): Boolean = shuffle()

    private fun shuffle(): Boolean {
        val cells = allPlayable().filter { grid[it.row][it.col]?.special == SpecialType.NONE }
        if (cells.size < 4) return false
        repeat(80) {
            val candys = cells.map { grid[it.row][it.col]!!.candy }.shuffled(rng)
            cells.forEachIndexed { index, p -> grid[p.row][p.col]!!.candy = candys[index] }
            if (findRuns().isEmpty() && findHint() != null) return true
        }
        return false
    }

    // --------------------------------------------------------------- helpers

    private data class Run(val cells: List<Pos>, val horizontal: Boolean)

    private fun findRuns(): List<Run> {
        val runs = mutableListOf<Run>()
        for (r in 0 until rows) {
            var c = 0
            while (c < cols) {
                val candy = grid[r][c]?.takeIf { !it.isBlocker }?.candy
                if (candy == null) {
                    c++
                    continue
                }
                var end = c + 1
                while (end < cols) {
                    val next = grid[r][end]
                    if (next == null || next.isBlocker || next.candy != candy) break
                    end++
                }
                if (end - c >= 3) runs.add(Run((c until end).map { Pos(r, it) }, true))
                c = end
            }
        }
        for (c in 0 until cols) {
            var r = 0
            while (r < rows) {
                val candy = grid[r][c]?.takeIf { !it.isBlocker }?.candy
                if (candy == null) {
                    r++
                    continue
                }
                var end = r + 1
                while (end < rows) {
                    val next = grid[end][c]
                    if (next == null || next.isBlocker || next.candy != candy) break
                    end++
                }
                if (end - r >= 3) runs.add(Run((r until end).map { Pos(it, c) }, false))
                r = end
            }
        }
        return runs
    }

    private fun safeCandyAt(r: Int, c: Int): CandyType {
        repeat(24) {
            val candidate = palette.random(rng)
            val horizontal = c >= 2 &&
                grid[r][c - 1]?.candy == candidate &&
                grid[r][c - 2]?.candy == candidate
            val vertical = r >= 2 &&
                grid[r - 1][c]?.candy == candidate &&
                grid[r - 2][c]?.candy == candidate
            if (!horizontal && !vertical) return candidate
        }
        return palette.random(rng)
    }

    private fun dominantCandy(): CandyType? {
        val counts = mutableMapOf<CandyType, Int>()
        allPlayable().forEach { p ->
            grid[p.row][p.col]?.let { counts[it.candy] = (counts[it.candy] ?: 0) + 1 }
        }
        return counts.maxByOrNull { it.value }?.key
    }

    private fun allPlayable(): List<Pos> {
        val list = ArrayList<Pos>(rows * cols)
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (isPlayable(Pos(r, c))) list.add(Pos(r, c))
            }
        }
        return list
    }

    private fun isPlayable(p: Pos): Boolean {
        if (!inBounds(p) || isHole(p)) return false
        val tile = grid[p.row][p.col] ?: return false
        return !tile.isBlocker
    }

    private fun inBounds(p: Pos): Boolean = p.row in 0 until rows && p.col in 0 until cols

    private fun isHole(p: Pos): Boolean = p in level.holes

    private fun swapCells(a: Pos, b: Pos) {
        val tmp = grid[a.row][a.col]
        grid[a.row][a.col] = grid[b.row][b.col]
        grid[b.row][b.col] = tmp
    }

    private fun neighbors4(p: Pos): List<Pos> = listOf(
        Pos(p.row - 1, p.col),
        Pos(p.row + 1, p.col),
        Pos(p.row, p.col - 1),
        Pos(p.row, p.col + 1)
    ).filter { inBounds(it) }

    private fun neighbors3x3(p: Pos): List<Pos> {
        val list = mutableListOf<Pos>()
        for (r in p.row - 1..p.row + 1) {
            for (c in p.col - 1..p.col + 1) {
                val q = Pos(r, c)
                if (inBounds(q)) list.add(q)
            }
        }
        return list
    }
}

