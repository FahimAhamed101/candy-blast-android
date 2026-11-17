package com.sugarpopblast.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sugarpopblast.game.GameViewModel
import com.sugarpopblast.game.GoalState
import com.sugarpopblast.game.GoalType
import com.sugarpopblast.game.LevelConfig
import com.sugarpopblast.game.Levels
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    vm: GameViewModel,
    onExit: () -> Unit,
    onOpenMap: () -> Unit
) {
    val state = vm.state
    val level = state.level ?: return
    val frame = state.frame ?: return
    var showPause by remember { mutableStateOf(false) }
    val shownScore by animateIntAsState(frame.score, tween(320), label = "score")

    // the score pill pops each time points land
    var scoreBump by remember { mutableStateOf(false) }
    LaunchedEffect(frame.score) {
        if (frame.score <= 0) return@LaunchedEffect
        scoreBump = true
        delay(130)
        scoreBump = false
    }
    val scoreScale by animateFloatAsState(
        if (scoreBump) 1.12f else 1f,
        spring(dampingRatio = 0.42f, stiffness = 900f),
        label = "scoreScale"
    )

    CandyScaffold {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            // ------------------------------------------------------------ top bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(40.dp)
                        .shadow(3.dp, CircleShape, ambientColor = Color(0x33000000))
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, CandyColors.pinkSoft, CircleShape)
                        .clickable { showPause = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = CandyColors.pinkDeep,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "LEVEL ${level.number}",
                        color = Color.White,
                        fontSize = 18.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        style = TextStyle(shadow = TitleShadow)
                    )
                    Text(
                        level.worldName.uppercase(),
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 9.sp,
                        lineHeight = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        style = TextStyle(shadow = TitleShadow)
                    )
                }
                Box(
                    Modifier
                        .scale(scoreScale)
                        .shadow(3.dp, CircleShape, ambientColor = Color(0x33000000))
                        .clip(CircleShape)
                        .background(Brush.verticalGradient(listOf(Color.White, CandyColors.creamShade)))
                        .border(2.dp, CandyColors.pinkSoft, CircleShape)
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        "%,d".format(shownScore),
                        color = CandyColors.ink,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(Modifier.height(9.dp))

            // ------------------------------------------------------------- HUD
            CreamPanel(modifier = Modifier.fillMaxWidth(), corner = 18.dp, padding = 8.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MovesBadge(state.movesLeft)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        StarRow(stars = starsFor(frame.score, level), size = 15.dp)
                        Spacer(Modifier.height(4.dp))
                        val next = nextStarTarget(frame.score, level)
                        val prev = previousStarTarget(frame.score, level)
                        val progress = if (next <= prev) 1f
                        else ((frame.score - prev).toFloat() / (next - prev)).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                            color = CandyColors.gold,
                            trackColor = Color(0xFFE3D3C4)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        frame.goals.forEach { goal -> GoalChip(goal) }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ----------------------------------------------------------- board
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                val side = minOf(maxWidth, maxHeight)
                Box(Modifier.size(side)) {
                    GameBoard(
                        level = level,
                        frame = frame,
                        selected = state.selected,
                        hint = state.hint,
                        shakeTick = state.shakeTick,
                        onTap = vm::onCellTapped,
                        onSwipe = vm::onSwipe,
                        onPress = vm::onPressFeedback,
                        modifier = Modifier.fillMaxSize()
                    )
                    ComboBanner(state.comboFlash, Modifier.align(Alignment.TopCenter))
                }
            }

            Spacer(Modifier.height(8.dp))

            // ------------------------------------------------------------ tray
            Box(
                Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(24.dp), ambientColor = Color(0x33000000))
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFFFF9CC2), Color(0xFFFF6BA4))))
                    .border(3.dp, Color.White.copy(alpha = 0.75f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BoosterButton(
                        icon = Icons.Default.Hardware,
                        badge = "${state.hammers}",
                        active = state.hammerMode,
                        onClick = vm::toggleHammer
                    )
                    BoosterButton(
                        icon = Icons.Default.Refresh,
                        badge = null,
                        active = false,
                        onClick = vm::shuffleBoard
                    )
                    BoosterButton(
                        icon = Icons.Default.Lightbulb,
                        badge = null,
                        active = state.hint != null,
                        onClick = vm::showHint
                    )
                    BoosterButton(
                        icon = Icons.Default.Pause,
                        badge = null,
                        active = false,
                        onClick = { showPause = true }
                    )
                }
            }
        }
    }

    if (state.status == GameViewModel.Status.WON) {
        WinDialog(
            level = level,
            score = frame.score,
            stars = state.stars,
            coins = state.coins,
            hasNext = level.number < Levels.COUNT,
            onNext = { vm.nextLevel() },
            onReplay = { vm.retry() },
            onMap = onOpenMap
        )
    }

    if (state.status == GameViewModel.Status.LOST) {
        LoseDialog(
            score = frame.score,
            coins = state.coins,
            onContinue = { vm.continueWithMoves() },
            onRetry = { vm.retry() },
            onMap = onOpenMap
        )
    }

    if (showPause) {
        PauseDialog(
            levelNumber = level.number,
            soundOn = vm.soundEnabled(),
            musicOn = vm.musicEnabled(),
            vibrationOn = vm.vibrationEnabled(),
            onToggleSound = { vm.toggleSound() },
            onToggleMusic = { vm.toggleMusic() },
            onToggleVibration = { vm.toggleVibration() },
            onResume = { showPause = false },
            onRestart = {
                showPause = false
                vm.retry()
            },
            onQuit = {
                showPause = false
                onExit()
            }
        )
    }
}

// ------------------------------------------------------------------ helpers

private fun starsFor(score: Int, level: LevelConfig): Int =
    level.starScores.count { score >= it }

private fun nextStarTarget(score: Int, level: LevelConfig): Int {
    val earned = starsFor(score, level)
    return level.starScores.getOrNull(earned) ?: level.starScores.last()
}

private fun previousStarTarget(score: Int, level: LevelConfig): Int {
    val earned = starsFor(score, level)
    return if (earned == 0) 0 else level.starScores[earned - 1]
}

// ------------------------------------------------------------------ widgets

@Composable
private fun MovesBadge(moves: Int) {
    val urgent = moves <= 3
    val transition = rememberInfiniteTransition(label = "moves")
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (urgent) 1.09f else 1f,
        animationSpec = infiniteRepeatable(tween(520), RepeatMode.Reverse),
        label = "movesPulse"
    )
    Box(
        Modifier
            .scale(pulse)
            .size(56.dp)
            .shadow(4.dp, CircleShape, ambientColor = Color(0x33000000))
            .clip(CircleShape)
            .background(
                if (urgent) Brush.verticalGradient(listOf(Color(0xFFFF8A80), Color(0xFFE53935)))
                else Brush.verticalGradient(listOf(Color(0xFF8E7BFF), Color(0xFF5B3FE0)))
            )
            .border(3.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$moves", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black)
            Text(
                "MOVES",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 7.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun GoalChip(goal: GoalState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (goal.done) Color(0xFFDDF5E3) else Color.White.copy(alpha = 0.75f))
            .border(
                1.5.dp,
                if (goal.done) CandyColors.mint else Color(0xFFEADFD2),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 5.dp, vertical = 4.dp)
    ) {
        Box(Modifier.size(22.dp), contentAlignment = Alignment.Center) {
            when (goal.type) {
                GoalType.COLLECT -> goal.candy?.let { CandyIcon(it, 22.dp) }
                GoalType.JELLY -> JellyIcon(18.dp)
                GoalType.CHOCOLATE -> ChocolateIcon(20.dp)
                GoalType.SCORE -> Text("â˜…", color = CandyColors.gold, fontSize = 16.sp)
            }
        }
        Text(
            "${goal.current}/${goal.target}",
            color = CandyColors.ink,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun BoosterButton(
    icon: ImageVector,
    badge: String?,
    active: Boolean,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        if (pressed) 0.88f else 1f,
        spring(dampingRatio = 0.35f, stiffness = 900f),
        label = "boosterPress"
    )

    Box(contentAlignment = Alignment.TopEnd) {
        Box(
            Modifier
                .scale(pressScale)
                .size(52.dp)
                .shadow(4.dp, CircleShape, ambientColor = Color(0x33000000))
                .clip(CircleShape)
                .background(
                    if (active) Brush.verticalGradient(listOf(Color(0xFFFFE082), Color(0xFFFFB300)))
                    else Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFFFE3EF)))
                )
                .border(3.dp, if (active) Color.White else CandyColors.pinkSoft, CircleShape)
                .clickable(interactionSource = interaction, indication = null) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (active) Color(0xFF6B3E00) else CandyColors.pinkDeep,
                modifier = Modifier.size(24.dp)
            )
        }
        if (badge != null) {
            Box(
                Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF3D7F))
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(badge, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun ComboBanner(combo: Int, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = combo > 1,
        modifier = modifier.padding(top = 10.dp),
        enter = fadeIn(tween(150)) +
            scaleIn(spring(dampingRatio = 0.45f, stiffness = 320f), initialScale = 0.45f),
        // a slow, soft exit â€” the old fadeOut() blinked out in one frame
        exit = fadeOut(tween(420)) + scaleOut(tween(420), targetScale = 1.35f)
    ) {
        // re-pops when the combo climbs from x2 to x3 instead of silently
        // swapping the text
        AnimatedContent(
            targetState = combo,
            transitionSpec = {
                (fadeIn(tween(120)) +
                    scaleIn(spring(dampingRatio = 0.4f, stiffness = 420f), initialScale = 0.6f))
                    .togetherWith(fadeOut(tween(120)) + scaleOut(tween(120), targetScale = 1.5f))
            },
            label = "comboValue"
        ) { value ->
            Text(
                "COMBO x$value",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .shadow(6.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFFFF7FB2), Color(0xFFFF3D7F))))
                    .border(2.dp, Color.White, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }
}

// ------------------------------------------------------------------ dialogs

@Composable
private fun DialogShell(
    onDismiss: () -> Unit,
    dismissable: Boolean = false,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = { if (dismissable) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = dismissable,
            dismissOnClickOutside = dismissable,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 26.dp)
                .shadow(14.dp, RoundedCornerShape(26.dp), ambientColor = Color(0x55000000))
                .clip(RoundedCornerShape(26.dp))
                .background(Brush.verticalGradient(listOf(CandyColors.cream, CandyColors.creamShade)))
                .border(4.dp, CandyColors.pinkSoft, RoundedCornerShape(26.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
private fun WinDialog(
    level: LevelConfig,
    score: Int,
    stars: Int,
    coins: Int,
    hasNext: Boolean,
    onNext: () -> Unit,
    onReplay: () -> Unit,
    onMap: () -> Unit
) {
    var reveal by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        repeat(stars) {
            delay(220)
            reveal++
        }
    }

    DialogShell(onDismiss = onMap) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "DELICIOUS!",
                color = CandyColors.pinkDeep,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                "Level ${level.number} cleared Â· ${level.worldName}",
                color = CandyColors.inkSoft,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { index ->
                    val filled = index < reveal
                    val scale by animateFloatAsState(
                        if (filled) 1f else 0.55f,
                        spring(dampingRatio = 0.42f, stiffness = 380f),
                        label = "star"
                    )
                    Text(
                        if (filled) "â˜…" else "â˜†",
                        fontSize = 44.sp,
                        modifier = Modifier.scale(scale),
                        color = if (filled) CandyColors.gold else Color(0xFFCDBBD8)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("%,d".format(score), color = CandyColors.ink, fontSize = 30.sp, fontWeight = FontWeight.Black)
            Text("SCORE", color = CandyColors.inkSoft, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                "+${30 + stars * 25} coins  Â·  balance $coins",
                color = CandyColors.goldDeep,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(16.dp))

            if (hasNext) {
                CandyButton(
                    text = "NEXT LEVEL",
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth(),
                    brush = PinkGradient
                )
                Spacer(Modifier.height(10.dp))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GhostButton("REPLAY", onReplay, Modifier.weight(1f))
                GhostButton("MAP", onMap, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun LoseDialog(
    score: Int,
    coins: Int,
    onContinue: () -> Unit,
    onRetry: () -> Unit,
    onMap: () -> Unit
) {
    DialogShell(onDismiss = onMap) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "OUT OF MOVES",
                color = CandyColors.pinkDeep,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(6.dp))
            Text("%,d".format(score), color = CandyColors.ink, fontSize = 26.sp, fontWeight = FontWeight.Black)
            Text("FINAL SCORE", color = CandyColors.inkSoft, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            Spacer(Modifier.height(16.dp))
            CandyButton(
                text = "+5 MOVES",
                subtitle = if (coins >= 50) "costs 50 coins Â· balance $coins" else "need 50 coins Â· balance $coins",
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                brush = PurpleGradient,
                lip = Color(0xFF4A1B9E),
                enabled = coins >= 50
            )
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GhostButton("RETRY", onRetry, Modifier.weight(1f))
                GhostButton("MAP", onMap, Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Tip: match 4 or 5 to make striped candies",
                color = CandyColors.inkSoft,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PauseDialog(
    levelNumber: Int,
    soundOn: Boolean,
    musicOn: Boolean,
    vibrationOn: Boolean,
    onToggleSound: () -> Unit,
    onToggleMusic: () -> Unit,
    onToggleVibration: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onQuit: () -> Unit
) {
    DialogShell(onDismiss = onResume, dismissable = true) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "PAUSED",
                    color = CandyColors.pinkDeep,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = CandyColors.inkSoft,
                    modifier = Modifier.size(20.dp).clickable { onResume() }
                )
            }
            Text("Level $levelNumber", color = CandyColors.inkSoft, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            PauseToggle("Sound effects", soundOn, onToggleSound)
            PauseToggle("Music", musicOn, onToggleMusic)
            PauseToggle("Vibration", vibrationOn, onToggleVibration)
            Spacer(Modifier.height(14.dp))
            CandyButton("RESUME", onResume, Modifier.fillMaxWidth(), PinkGradient)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GhostButton("RESTART", onRestart, Modifier.weight(1f))
                GhostButton("QUIT", onQuit, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PauseToggle(label: String, checked: Boolean, onChange: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onChange() }
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = CandyColors.ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Box(
            Modifier
                .size(42.dp, 24.dp)
                .clip(CircleShape)
                .background(if (checked) CandyColors.pink else Color(0xFFD6C7DE))
                .padding(3.dp)
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

