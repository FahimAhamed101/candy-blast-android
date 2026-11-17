package com.sugarpopblast.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sugarpopblast.game.CandyType

@Composable
fun CandyScaffold(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.fillMaxSize().background(CandyBackground)) {
        Canvas(Modifier.fillMaxSize()) {
            // Soft clouds, kept clear of the top-left header text
            val cloud = Color.White.copy(alpha = 0.45f)
            fun puff(cx: Float, cy: Float, scale: Float) {
                drawCircle(cloud, radius = size.width * 0.085f * scale, center = Offset(cx, cy))
                drawCircle(cloud, radius = size.width * 0.065f * scale, center = Offset(cx + size.width * 0.08f * scale, cy + size.height * 0.006f))
                drawCircle(cloud, radius = size.width * 0.055f * scale, center = Offset(cx - size.width * 0.075f * scale, cy + size.height * 0.008f))
            }
            puff(size.width * 0.66f, size.height * 0.06f, 0.85f)
            puff(size.width * 0.90f, size.height * 0.19f, 0.70f)
            puff(size.width * 0.45f, size.height * 0.03f, 0.42f)

            // Rolling hills
            drawCircle(
                color = Color(0xFF8FD96E),
                radius = size.width * 1.05f,
                center = Offset(size.width * 0.05f, size.height * 1.06f)
            )
            drawCircle(
                color = Color(0xFF6FC45F),
                radius = size.width * 0.95f,
                center = Offset(size.width * 1.02f, size.height * 1.10f)
            )
            drawCircle(
                color = Color(0xFF59B44F),
                radius = size.width * 0.75f,
                center = Offset(size.width * 0.45f, size.height * 1.22f)
            )
        }
        content()
    }
}

/** Cream panel with a pink rim â€” the SugarPop HUD look. */
@Composable
fun CreamPanel(
    modifier: Modifier = Modifier,
    corner: Dp = 20.dp,
    padding: Dp = 12.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(corner), ambientColor = Color(0x33000000))
            .clip(RoundedCornerShape(corner))
            .background(Brush.verticalGradient(listOf(CandyColors.cream, CandyColors.creamShade)))
            .border(3.dp, CandyColors.pinkSoft, RoundedCornerShape(corner))
            .padding(padding),
        content = content
    )
}

@Composable
fun CandyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    brush: Brush = PinkGradient,
    lip: Color = CandyColors.pinkDeep,
    icon: ImageVector? = null,
    subtitle: String? = null,
    enabled: Boolean = true
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    // the face sinks into its lip while held, like a real candy button
    val faceOffset by animateDpAsState(
        targetValue = if (pressed && enabled) 4.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 1400f),
        label = "faceOffset"
    )
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier.height(64.dp),
        contentAlignment = Alignment.Center
    ) {
        // 3D lip
        Box(
            Modifier
                .fillMaxWidth()
                .height(64.dp)
                .offset(y = 5.dp)
                .clip(shape)
                .background(if (enabled) lip else Color(0xFFB9A9C6))
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(62.dp)
                .offset(y = faceOffset)
                .clip(shape)
                .background(
                    if (enabled) brush
                    else Brush.verticalGradient(listOf(Color(0xFFD9D0E2), Color(0xFFBEB2CB)))
                )
                .clickable(interactionSource = interaction, indication = null, enabled = enabled) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            // gloss
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.38f), Color.White.copy(alpha = 0f))
                        )
                    )
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text,
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    if (subtitle != null) {
                        Text(
                            subtitle,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        if (pressed) 0.95f else 1f,
        spring(dampingRatio = 0.4f, stiffness = 1200f),
        label = "ghostPress"
    )
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .scale(pressScale)
            .height(50.dp)
            .shadow(3.dp, shape, ambientColor = Color(0x22000000))
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Color(0xFFFFFFFF), CandyColors.creamShade)))
            .border(2.dp, CandyColors.pinkSoft, shape)
            .clickable(interactionSource = interaction, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = CandyColors.pinkDeep, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(text, color = CandyColors.ink, fontSize = 13.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun StatPill(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .shadow(3.dp, CircleShape, ambientColor = Color(0x22000000))
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(Color(0xFFFFFFFF), CandyColors.creamShade)))
            .border(2.dp, CandyColors.pinkSoft, CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = CandyColors.ink, fontSize = 13.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun StarRow(stars: Int, size: Dp = 16.dp, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(3) { index ->
            Text(
                if (index < stars) "â˜…" else "â˜†",
                fontSize = (size.value * 0.98f).sp,
                color = if (index < stars) CandyColors.gold else Color(0xFFB9A9C6)
            )
        }
    }
}

/** Small candy swatch used in HUD chips and lists. */
@Composable
fun CandyIcon(candy: CandyType, size: Dp, modifier: Modifier = Modifier) {
    CandyView(candy, modifier.size(size))
}

@Composable
fun JellyIcon(size: Dp, modifier: Modifier = Modifier, double: Boolean = false) {
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.22f))
            .background(
                Brush.verticalGradient(
                    if (double) listOf(Color(0xFFC56BFF), Color(0xFF9B3BE0))
                    else listOf(Color(0xFF8B6BE8), Color(0xFF6C4FD0))
                )
            )
            .border(size * 0.08f, Color.White.copy(alpha = 0.65f), RoundedCornerShape(size * 0.22f))
    )
}

@Composable
fun ChocolateIcon(size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        drawBlocker(com.sugarpopblast.game.BlockerType.CHOCOLATE, 1)
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, color: Color = CandyColors.ink) {
    Text(
        text,
        modifier = modifier,
        color = color.copy(alpha = 0.75f),
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 2.sp
    )
}

@Composable
fun GlowPulse(modifier: Modifier = Modifier, content: @Composable (Float) -> Unit) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val value by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulseValue"
    )
    content(value)
}

/** Big candy cluster used as decoration on the menus. */
@Composable
fun CandyDot(candy: CandyType, size: Dp) {
    CandyView(candy, Modifier.size(size))
}

@Composable
fun EmptyHint(text: String) {
    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(text, color = CandyColors.inkSoft, textAlign = TextAlign.Center, fontSize = 13.sp)
    }
}

