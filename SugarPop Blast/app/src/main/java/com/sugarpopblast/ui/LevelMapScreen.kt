package com.sugarpopblast.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sugarpopblast.game.CandyType
import com.sugarpopblast.game.Levels

@Composable
fun LevelMapScreen(
    totalStars: Int,
    starsFor: (Int) -> Int,
    unlocked: (Int) -> Boolean,
    currentLevel: Int,
    onLevel: (Int) -> Unit,
    onBack: () -> Unit
) {
    // open on the player's current world instead of always at level 1
    val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = (((currentLevel - 1) / 12) * 13).coerceAtLeast(0)
    )

    CandyScaffold {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 14.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(38.dp)
                        .shadow(3.dp, CircleShape, ambientColor = Color(0x33000000))
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, CandyColors.pinkSoft, CircleShape)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = CandyColors.pinkDeep,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "LEVEL MAP",
                        color = Color.White,
                        fontSize = 21.sp,
                        lineHeight = 23.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        style = TextStyle(shadow = TitleShadow)
                    )
                    Text(
                        "48 levels Â· 4 worlds",
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(shadow = TitleShadow)
                    )
                }
                StatPill("â˜… $totalStars")
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                state = gridState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Levels.worlds.forEach { world ->
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        WorldHeader(world, starsFor)
                    }
                    items((1..12).map { (world.index - 1) * 12 + it }) { level ->
                        LevelNode(
                            level = level,
                            stars = starsFor(level),
                            unlocked = unlocked(level),
                            isCurrent = level == currentLevel,
                            onClick = { onLevel(level) }
                        )
                    }
                }
            }
        }
    }
}

private val WorldCandy = listOf(
    CandyType.RED_BEAN,
    CandyType.ORANGE_LOZENGE,
    CandyType.PURPLE_FLOWER,
    CandyType.BLUE_GUMDROP
)

@Composable
private fun WorldHeader(world: Levels.World, starsFor: (Int) -> Int) {
    val start = (world.index - 1) * 12 + 1
    val earned = (start until start + 12).sumOf { starsFor(it) }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 2.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.55f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CandyView(
            WorldCandy[(world.index - 1).coerceIn(0, 3)],
            Modifier.size(28.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "WORLD ${world.index} Â· ${world.name.uppercase()}",
                color = CandyColors.ink,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.6.sp
            )
            Text(world.tagline, color = CandyColors.inkSoft, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            "$earned/36 â˜…",
            color = CandyColors.goldDeep,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun LevelNode(
    level: Int,
    stars: Int,
    unlocked: Boolean,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "node")
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(tween(850), RepeatMode.Reverse),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (unlocked) 1f else 0.62f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .scale(if (isCurrent && unlocked) pulse else 1f)
                .shadow(5.dp, CircleShape, ambientColor = Color(0x33000000))
                .clip(CircleShape)
                .background(
                    when {
                        !unlocked -> Brush.verticalGradient(listOf(Color(0xFFBFB3C9), Color(0xFF9C8FA8)))
                        stars > 0 -> GoldGradient
                        else -> PinkGradient
                    }
                )
                .border(3.dp, Color.White, CircleShape)
                .clickable(enabled = unlocked) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (unlocked) {
                Text(
                    "$level",
                    color = if (stars > 0) Color(0xFF5A3A00) else Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black
                )
            } else {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(17.dp)
                )
            }
        }
        Spacer(Modifier.height(2.dp))
        StarRow(stars = stars, size = 9.dp)
    }
}

