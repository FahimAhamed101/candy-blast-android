package com.sugarpopblast.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sugarpopblast.game.BlockerType
import com.sugarpopblast.game.Frame
import com.sugarpopblast.game.LevelConfig
import com.sugarpopblast.game.Pos
import com.sugarpopblast.game.TileSnap
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

private val BoardDeep = Color(0xFF1D2760)
private val BoardMid = Color(0xFF2B3A8C)
private val CellBase = Color(0xFF3A4DAE)
private val CellAlt = Color(0xFF4A61CC)
private val JellyOne = Color(0xFF8B6BE8)
private val JellyTwo = Color(0xFFC56BFF)

@Composable
fun GameBoard(
    level: LevelConfig,
    frame: Frame,
    selected: Pos?,
    hint: Pair<Pos, Pos>?,
    shakeTick: Int,
    onTap: (Pos) -> Unit,
    onSwipe: (Pos, Pos) -> Unit,
    onPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val shape = RoundedCornerShape(18.dp)

    // cell the finger is currently on, for the press-down squash
    var pressed by remember { mutableStateOf<Pos?>(null) }

    // horizontal wobble when a swap is rejected
    val shake = remember { Animatable(0f) }
    LaunchedEffect(shakeTick) {
        if (shakeTick == 0) return@LaunchedEffect
        shake.snapTo(0f)
        shake.animateTo(1f, tween(320, easing = LinearEasing))
    }
    val shakeDp = (sin(shake.value * PI.toFloat() * 3f) * 9f * (1f - shake.value)).dp

    BoxWithConstraints(
        modifier = modifier
            .clip(shape)
            .background(Brush.verticalGradient(listOf(BoardMid, BoardDeep)))
            .border(2.dp, Color(0xFF6E86E8).copy(alpha = 0.55f), shape)
            .padding(4.dp)
    ) {
        val cell = minOf(maxWidth / level.cols, maxHeight / level.rows)
        val originX = (maxWidth - cell * level.cols) / 2
        val originY = (maxHeight - cell * level.rows) / 2

        val cellPx = with(density) { cell.toPx() }
        val originXPx = with(density) { originX.toPx() }
        val originYPx = with(density) { originY.toPx() }

        // Board cells (with jelly tint underneath)
        for (r in 0 until level.rows) {
            for (c in 0 until level.cols) {
                val p = Pos(r, c)
                if (p in level.holes) continue
                val layers = frame.jelly.firstOrNull { it.row == r && it.col == c }?.layers ?: 0
                val base = if ((r + c) % 2 == 0) CellBase else CellAlt
                Box(
                    Modifier
                        .offset(originX + cell * c, originY + cell * r)
                        .size(cell)
                        .padding(cell * 0.018f)
                        .clip(RoundedCornerShape(cell * 0.18f))
                        .background(
                            when (layers) {
                                0 -> Brush.linearGradient(listOf(base, base))
                                1 -> Brush.verticalGradient(listOf(JellyOne, Color(0xFF6C4FD0)))
                                else -> Brush.verticalGradient(listOf(JellyTwo, Color(0xFF9B3BE0)))
                            }
                        )
                ) {
                    if (layers > 0) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .padding(cell * 0.07f)
                                .clip(RoundedCornerShape(cell * 0.14f))
                                .border(
                                    cell * 0.035f,
                                    Color.White.copy(alpha = if (layers > 1) 0.55f else 0.32f),
                                    RoundedCornerShape(cell * 0.14f)
                                )
                        )
                    }
                }
            }
        }

        // Candies â€” shifted as a layer so a rejected swap wobbles the whole pile
        Box(Modifier.matchParentSize().offset(x = shakeDp)) {
            frame.tiles.forEach { tile ->
                key(tile.id) {
                    val x by animateDpAsState(
                        targetValue = originX + cell * tile.col,
                        animationSpec = spring(dampingRatio = 0.78f, stiffness = 430f),
                        label = "x"
                    )
                    val y by animateDpAsState(
                        targetValue = originY + cell * tile.row,
                        animationSpec = spring(dampingRatio = 0.72f, stiffness = 430f),
                        label = "y"
                    )
                    Box(Modifier.offset(x, y).size(cell)) {
                        CandyPiece(
                            tile = tile,
                            cell = cell,
                            selected = selected == Pos(tile.row, tile.col),
                            pressed = pressed == Pos(tile.row, tile.col),
                            hinted = hint != null &&
                                (hint.first == Pos(tile.row, tile.col) || hint.second == Pos(tile.row, tile.col))
                        )
                    }
                }
            }
        }

        // One gesture detector for both tap and drag. A swipe commits as soon as
        // the finger passes the touch slop, so the board answers without waiting
        // for a lift-off.
        Box(
            Modifier
                .matchParentSize()
                .pointerInput(cellPx, originXPx, originYPx, level.rows, level.cols) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val col = ((down.position.x - originXPx) / cellPx).toInt()
                        val row = ((down.position.y - originYPx) / cellPx).toInt()
                        if (row !in 0 until level.rows || col !in 0 until level.cols) {
                            return@awaitEachGesture
                        }
                        val start = Pos(row, col)
                        pressed = start
                        onPress()

                        val slop = viewConfiguration.touchSlop
                        var total = Offset.Zero
                        var swiped = false
                        var cancelled = false

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id }
                            if (change == null) {
                                // pointer vanished (gesture taken over / cancelled)
                                cancelled = true
                                break
                            }
                            if (!change.pressed) break
                            total += change.positionChange()

                            if (!swiped && total.getDistance() > slop) {
                                swiped = true
                                pressed = null
                                val to = if (abs(total.x) > abs(total.y)) {
                                    Pos(start.row, start.col + if (total.x > 0) 1 else -1)
                                } else {
                                    Pos(start.row + if (total.y > 0) 1 else -1, start.col)
                                }
                                if (to.row in 0 until level.rows && to.col in 0 until level.cols) {
                                    change.consume()
                                    onSwipe(start, to)
                                }
                            }
                            if (swiped) change.consume()
                        }

                        pressed = null
                        if (!swiped && !cancelled) onTap(start)
                    }
                }
        )
    }
}

@Composable
private fun CandyPiece(tile: TileSnap, cell: Dp, selected: Boolean, pressed: Boolean, hinted: Boolean) {
    var appeared by remember(tile.id) { mutableStateOf(false) }
    LaunchedEffect(tile.id) { appeared = true }

    val appear by animateFloatAsState(if (appeared) 1f else 0.3f, tween(170), label = "appear")
    val targetScale = when {
        tile.clearing -> 0.05f
        selected -> 1.12f
        pressed -> 0.9f
        hinted -> 1.08f
        else -> 1f
    }
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(
            when {
                tile.clearing -> 175
                pressed -> 70
                else -> 130
            },
            easing = FastOutSlowInEasing
        ),
        label = "scale"
    )
    val fade by animateFloatAsState(if (tile.clearing) 0f else 1f, tween(175), label = "fade")

    Box(
        Modifier
            .fillMaxSize()
            .padding(cell * 0.03f)
            .scale(scale * appear)
            .alpha(fade),
        contentAlignment = Alignment.Center
    ) {
        if (tile.blocker != BlockerType.NONE) {
            Canvas(Modifier.fillMaxSize()) { drawBlocker(tile.blocker, tile.hp) }
        } else {
            CandyView(tile.candy, Modifier.fillMaxSize(), special = tile.special)
        }

        if (tile.blocker != BlockerType.NONE && tile.hp > 1) {
            Text(
                "${tile.hp}",
                color = Color.White,
                fontSize = (cell.value * 0.17f).sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.align(Alignment.BottomEnd).padding(cell * 0.04f)
            )
        }

        if (selected) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(cell * 0.02f)
                    .border(2.5.dp, Color.White, RoundedCornerShape(cell * 0.26f))
            )
        }
        if (hinted) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(cell * 0.02f)
                    .border(2.dp, Color(0xFFFFF176), CircleShape)
            )
        }
    }
}

