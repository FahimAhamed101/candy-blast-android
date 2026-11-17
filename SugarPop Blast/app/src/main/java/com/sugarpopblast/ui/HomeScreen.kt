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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sugarpopblast.game.CandyType
import com.sugarpopblast.game.Levels

@Composable
fun HomeScreen(
    totalStars: Int,
    coins: Int,
    currentLevel: Int,
    soundOn: Boolean,
    musicOn: Boolean,
    vibrationOn: Boolean,
    onPlay: () -> Unit,
    onLevels: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleMusic: () -> Unit,
    onToggleVibration: () -> Unit,
    onReset: () -> Unit
) {
    var showSettings by remember { mutableStateOf(false) }
    val transition = rememberInfiniteTransition(label = "float")
    val float by transition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(tween(2100), RepeatMode.Reverse),
        label = "floatY"
    )

    CandyScaffold {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 22.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatPill("$coins coins")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatPill("â˜… $totalStars")
                    Spacer(Modifier.size(8.dp))
                    Box(
                        Modifier
                            .size(38.dp)
                            .shadow(3.dp, CircleShape, ambientColor = Color(0x33000000))
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, CandyColors.pinkSoft, CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showSettings = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = CandyColors.pinkDeep,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.weight(0.5f))

            // Candy cluster
            Row(
                Modifier.offset(y = float.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CandyClusterItem(CandyType.YELLOW_DROP, 50.dp, -14f)
                CandyClusterItem(CandyType.GREEN_SQUARE, 58.dp, -6f)
                CandyClusterItem(CandyType.RED_BEAN, 70.dp, 0f)
                CandyClusterItem(CandyType.BLUE_GUMDROP, 58.dp, 8f)
                CandyClusterItem(CandyType.PURPLE_FLOWER, 50.dp, 16f)
            }

            Spacer(Modifier.height(6.dp))

            BubbleText("CANDY", 54.sp, Brush.verticalGradient(listOf(Color(0xFFFFB3D1), Color(0xFFFF3D7F))))
            BubbleText("CRUSH", 54.sp, Brush.verticalGradient(listOf(Color(0xFFFFF0A8), Color(0xFFFFB300))))

            Spacer(Modifier.height(8.dp))

            Row(
                Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.85f))
                    .border(2.dp, CandyColors.pinkSoft, CircleShape)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("â˜… $totalStars / 144", color = CandyColors.ink, fontSize = 12.sp, fontWeight = FontWeight.Black)
                Text("  Â·  48 levels", color = CandyColors.inkSoft, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.weight(1f))

            CandyButton(
                text = "PLAY",
                subtitle = "LEVEL $currentLevel Â· ${Levels.world(currentLevel).name.uppercase()}",
                icon = Icons.Default.PlayArrow,
                onClick = onPlay,
                modifier = Modifier.fillMaxWidth(),
                brush = PinkGradient
            )

            Spacer(Modifier.height(10.dp))

            GhostButton(
                text = "LEVEL MAP",
                onClick = onLevels,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Swap two candies to match 3 or more.\nMatch 4 for stripes, 5 for a rainbow bomb.",
                color = CandyColors.ink.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
            Spacer(Modifier.height(6.dp))
        }
    }

    if (showSettings) {
        SettingsDialog(
            soundOn = soundOn,
            musicOn = musicOn,
            vibrationOn = vibrationOn,
            onToggleSound = onToggleSound,
            onToggleMusic = onToggleMusic,
            onToggleVibration = onToggleVibration,
            onReset = {
                onReset()
                showSettings = false
            },
            onDismiss = { showSettings = false }
        )
    }
}

@Composable
private fun CandyClusterItem(candy: CandyType, size: Dp, rotation: Float) {
    CandyView(candy, Modifier.size(size), rotation = rotation)
}

/** Bubbly logo text: white outline with a gradient fill. */
@Composable
fun BubbleText(text: String, size: TextUnit, brush: Brush) {
    Box(contentAlignment = Alignment.Center) {
        Text(
            text,
            style = TextStyle(
                fontSize = size,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                drawStyle = Stroke(width = 10f, join = StrokeJoin.Round),
                color = Color.White
            )
        )
        Text(
            text,
            style = TextStyle(
                fontSize = size,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                brush = brush
            )
        )
    }
}

@Composable
fun SettingsDialog(
    soundOn: Boolean,
    musicOn: Boolean,
    vibrationOn: Boolean,
    onToggleSound: () -> Unit,
    onToggleMusic: () -> Unit,
    onToggleVibration: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    var confirmReset by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CandyColors.cream,
        shape = RoundedCornerShape(26.dp),
        title = { Text("SETTINGS", color = CandyColors.ink, fontWeight = FontWeight.Black) },
        text = {
            Column {
                ToggleRow("Sound effects", soundOn, onToggleSound)
                ToggleRow("Background music", musicOn, onToggleMusic)
                ToggleRow("Vibration", vibrationOn, onToggleVibration)
                Spacer(Modifier.height(12.dp))
                Text(
                    "SugarPop Blast\n48 levels Â· 4 worlds Â· offline play",
                    color = CandyColors.inkSoft,
                    fontSize = 11.sp
                )
                Spacer(Modifier.height(8.dp))
                if (!confirmReset) {
                    TextButton(onClick = { confirmReset = true }) {
                        Text("Reset progress", color = CandyColors.pinkDeep, fontSize = 12.sp)
                    }
                } else {
                    TextButton(onClick = onReset) {
                        Text(
                            "Tap to erase all progress",
                            color = Color(0xFFD32F2F),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("DONE", color = CandyColors.pinkDeep, fontWeight = FontWeight.Black)
            }
        }
    )
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = CandyColors.ink, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Box(
            Modifier
                .size(42.dp, 24.dp)
                .clip(CircleShape)
                .background(if (checked) CandyColors.pink else Color(0xFFD6C7DE))
                .padding(3.dp)
                .clickable { onChange() }
        ) {
            Box(
                Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .align(if (checked) Alignment.CenterEnd else Alignment.CenterStart)
            )
        }
    }
}

