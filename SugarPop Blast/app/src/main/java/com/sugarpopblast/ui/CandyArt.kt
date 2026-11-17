package com.sugarpopblast.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.graphics.FilterQuality
import com.sugarpopblast.R
import com.sugarpopblast.game.BlockerType
import com.sugarpopblast.game.CandyType
import com.sugarpopblast.game.SpecialType

/**
 * Candy artwork. The included images must be original or licensed for
 * redistribution in a commercial source-code item.
 * (`candy_*`, `stripe_*`, `color_bomb`) which is normalised to a common
 * 128x128 canvas by `tools/prep_sprites.py`, so every colour has the same
 * optical weight on the board.
 */
private fun baseRes(candy: CandyType): Int = when (candy) {
    CandyType.RED_BEAN -> R.drawable.candy_red
    CandyType.ORANGE_LOZENGE -> R.drawable.candy_orange
    CandyType.YELLOW_DROP -> R.drawable.candy_yellow
    CandyType.GREEN_SQUARE -> R.drawable.candy_green
    CandyType.BLUE_GUMDROP -> R.drawable.candy_blue
    CandyType.PURPLE_FLOWER -> R.drawable.candy_purple
}

private fun stripeRes(candy: CandyType): Int = when (candy) {
    CandyType.RED_BEAN -> R.drawable.stripe_red
    CandyType.ORANGE_LOZENGE -> R.drawable.stripe_orange
    CandyType.YELLOW_DROP -> R.drawable.stripe_yellow
    CandyType.GREEN_SQUARE -> R.drawable.stripe_green
    CandyType.BLUE_GUMDROP -> R.drawable.stripe_blue
    CandyType.PURPLE_FLOWER -> R.drawable.stripe_purple
}

private fun spriteFor(candy: CandyType, special: SpecialType): Int = when (special) {
    SpecialType.RAINBOW -> R.drawable.color_bomb
    SpecialType.ROW, SpecialType.COL, SpecialType.BOMB -> stripeRes(candy)
    SpecialType.NONE -> baseRes(candy)
}

/**
 * One candy piece. [special] swaps in the striped / colour-bomb artwork and
 * adds the little "wrapped" ring used for bomb matches.
 */
@Composable
fun CandyView(
    candy: CandyType,
    modifier: Modifier = Modifier,
    special: SpecialType = SpecialType.NONE,
    rotation: Float = 0f
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        // vertical stripes read as a "clear the column" candy
        val spin = if (special == SpecialType.COL) rotation + 90f else rotation
        Image(
            bitmap = ImageBitmap.imageResource(spriteFor(candy, special)),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            filterQuality = FilterQuality.Medium,
            modifier = Modifier.fillMaxSize().rotate(spin)
        )
        if (special == SpecialType.BOMB) {
            Canvas(Modifier.fillMaxSize()) { drawWrappedRing() }
        }
    }
}

/** Golden wrapper ring so a bomb reads differently from a plain stripe. */
private fun DrawScope.drawWrappedRing() {
    val r = minOf(size.width, size.height) / 2f * 0.90f
    val cx = size.width / 2f
    val cy = size.height / 2f
    drawCircle(
        brush = Brush.sweepGradient(
            listOf(
                Color(0xFFFFF3B0), Color(0xFFFFC93C), Color(0xFFFF9A1F),
                Color(0xFFFFE07A), Color(0xFFFFF3B0)
            ),
            center = Offset(cx, cy)
        ),
        radius = r,
        center = Offset(cx, cy),
        style = Stroke(width = r * 0.16f)
    )
    // four wrapper twists
    listOf(0f, 90f, 180f, 270f).forEach { angle ->
        rotate(angle, pivot = Offset(cx, cy)) {
            drawCircle(
                color = Color.White.copy(alpha = 0.92f),
                radius = r * 0.13f,
                center = Offset(cx, cy - r)
            )
        }
    }
}

/** Cookies and chocolate bars are still drawn procedurally. */
fun DrawScope.drawBlocker(blocker: BlockerType, hp: Int) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val r = minOf(size.width, size.height) / 2f * 0.96f
    if (blocker == BlockerType.COOKIE) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFF0D2A0), Color(0xFFD9A961), Color(0xFFA9762F)),
                center = Offset(cx - r * 0.3f, cy - r * 0.32f),
                radius = r * 1.5f
            ),
            radius = r,
            center = Offset(cx, cy)
        )
        listOf(
            Offset(-0.36f, -0.30f), Offset(0.28f, -0.40f), Offset(0.42f, 0.16f),
            Offset(-0.18f, 0.34f), Offset(-0.50f, 0.10f), Offset(0.06f, -0.04f)
        ).forEach { p ->
            drawCircle(
                color = Color(0xFF4A2A12),
                radius = r * 0.11f,
                center = Offset(cx + r * p.x, cy + r * p.y)
            )
        }
    } else {
        val side = r * 1.86f
        val topLeft = Offset(cx - side / 2f, cy - side / 2f)
        val corner = CornerRadius(side * 0.16f, side * 0.16f)
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF8A5533), Color(0xFF5A3319), Color(0xFF3A1E0C)),
                start = Offset(cx - side * 0.5f, cy - side * 0.5f),
                end = Offset(cx + side * 0.5f, cy + side * 0.5f)
            ),
            topLeft = topLeft,
            size = Size(side, side),
            cornerRadius = corner
        )
        val line = Color(0xFF2A1408).copy(alpha = 0.55f)
        val stroke = side * 0.05f
        drawLine(line, Offset(cx, cy - side * 0.42f), Offset(cx, cy + side * 0.42f), stroke)
        drawLine(line, Offset(cx - side * 0.42f, cy), Offset(cx + side * 0.42f, cy), stroke)
        drawRoundRect(
            color = Color.White.copy(alpha = 0.18f),
            topLeft = Offset(cx - side * 0.40f, cy - side * 0.42f),
            size = Size(side * 0.80f, side * 0.16f),
            cornerRadius = CornerRadius(side * 0.08f, side * 0.08f)
        )
    }
    if (hp > 1) {
        drawCircle(
            color = Color(0xFF1B0B18).copy(alpha = 0.72f),
            radius = r * 0.30f,
            center = Offset(cx + r * 0.58f, cy + r * 0.56f)
        )
    }
}

