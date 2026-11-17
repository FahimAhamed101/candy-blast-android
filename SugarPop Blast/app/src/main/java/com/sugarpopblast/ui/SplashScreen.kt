package com.sugarpopblast.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sugarpopblast.game.CandyType

@Composable
fun SplashScreen() {
    val transition = rememberInfiniteTransition(label = "splash")
    val pulse by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse"
    )
    val dots by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "dots"
    )

    CandyScaffold {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.scale(pulse)
            ) {
                CandyView(CandyType.YELLOW_DROP, Modifier.size(38.dp), rotation = -16f)
                CandyView(CandyType.GREEN_SQUARE, Modifier.size(46.dp), rotation = -8f)
                CandyView(CandyType.RED_BEAN, Modifier.size(56.dp))
                CandyView(CandyType.BLUE_GUMDROP, Modifier.size(46.dp), rotation = 8f)
                CandyView(CandyType.PURPLE_FLOWER, Modifier.size(38.dp), rotation = 16f)
            }
            Spacer(Modifier.height(12.dp))
            BubbleText("CANDY", 44.sp, Brush.verticalGradient(listOf(Color(0xFFFFB3D1), Color(0xFFFF3D7F))))
            BubbleText("CRUSH", 44.sp, Brush.verticalGradient(listOf(Color(0xFFFFF0A8), Color(0xFFFFB300))))
            Spacer(Modifier.height(10.dp))
            Text(
                "48 LEVELS Â· 4 WORLDS",
                color = CandyColors.ink.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp
            )
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(3) { index ->
                    Box(
                        Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .alpha(if (index == 1) dots else dots * 0.55f)
                            .background(CandyColors.pinkDeep)
                    )
                }
            }
        }
    }
}

