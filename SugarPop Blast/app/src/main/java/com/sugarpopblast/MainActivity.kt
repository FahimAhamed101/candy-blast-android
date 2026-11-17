package com.sugarpopblast

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sugarpopblast.game.GameViewModel
import com.sugarpopblast.game.ProgressStore
import com.sugarpopblast.ui.CandyTheme
import com.sugarpopblast.ui.GameScreen
import com.sugarpopblast.ui.HomeScreen
import com.sugarpopblast.ui.LevelMapScreen
import com.sugarpopblast.ui.SplashScreen

private enum class Screen { HOME, MAP, GAME }

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CandyTheme {
                val vm: GameViewModel = viewModel()
                val context = LocalContext.current
                val store = remember { ProgressStore(context) }
                var screen by remember { mutableStateOf(Screen.HOME) }
                var refresh by remember { mutableStateOf(0) }
                var musicOn by remember { mutableStateOf(store.musicEnabled) }

                // Looping background track shipped in res/raw.
                val player = remember {
                    MediaPlayer.create(context, R.raw.xtremefreddy_loop1)?.apply {
                        isLooping = true
                        setVolume(0.35f, 0.35f)
                    }
                }

                LaunchedEffect(musicOn, screen) {
                    if (player == null) return@LaunchedEffect
                    runCatching {
                        if (musicOn) {
                            if (!player.isPlaying) player.start()
                        } else {
                            if (player.isPlaying) player.pause()
                        }
                    }
                }

                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner, musicOn) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (player == null) return@LifecycleEventObserver
                        when (event) {
                            Lifecycle.Event.ON_STOP -> runCatching { if (player.isPlaying) player.pause() }
                            Lifecycle.Event.ON_START -> runCatching {
                                if (musicOn && !player.isPlaying) player.start()
                            }
                            else -> Unit
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                DisposableEffect(Unit) {
                    onDispose { runCatching { player?.release() } }
                }

                BackHandler(enabled = screen != Screen.HOME) {
                    screen = when (screen) {
                        Screen.GAME -> Screen.HOME
                        Screen.MAP -> Screen.HOME
                        else -> Screen.HOME
                    }
                    refresh++
                }

                var showSplash by remember { mutableStateOf(true) }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(1300)
                    showSplash = false
                }

                Crossfade(targetState = showSplash, animationSpec = tween(420), label = "splash") { splash ->
                    if (splash) {
                        SplashScreen()
                    } else {
                        AnimatedContent(
                            targetState = screen,
                            transitionSpec = {
                                (fadeIn(tween(240)) +
                                    scaleIn(initialScale = 0.94f, animationSpec = tween(240)))
                                    .togetherWith(fadeOut(tween(150)))
                            },
                            label = "screen"
                        ) { target ->
                            when (target) {
                                Screen.HOME -> HomeScreen(
                                    totalStars = store.totalStars(),
                                    coins = store.coins,
                                    currentLevel = store.currentLevel(),
                                    soundOn = store.soundEnabled,
                                    musicOn = musicOn,
                                    vibrationOn = store.vibrationEnabled,
                                    onPlay = {
                                        vm.startLevel(store.currentLevel())
                                        screen = Screen.GAME
                                    },
                                    onLevels = { screen = Screen.MAP },
                                    onToggleSound = { vm.toggleSound() },
                                    onToggleMusic = { musicOn = vm.toggleMusic() },
                                    onToggleVibration = { vm.toggleVibration() },
                                    onReset = {
                                        store.reset()
                                        musicOn = store.musicEnabled
                                        refresh++
                                    }
                                )

                                Screen.MAP -> LevelMapScreen(
                                    totalStars = store.totalStars(),
                                    starsFor = { store.stars(it) },
                                    unlocked = { store.isUnlocked(it) },
                                    currentLevel = store.currentLevel(),
                                    onLevel = { level ->
                                        vm.startLevel(level)
                                        screen = Screen.GAME
                                    },
                                    onBack = { screen = Screen.HOME }
                                )

                                Screen.GAME -> GameScreen(
                                    vm = vm,
                                    onExit = {
                                        screen = Screen.HOME
                                        refresh++
                                    },
                                    onOpenMap = {
                                        screen = Screen.MAP
                                        refresh++
                                    }
                                )
                            }
                        }
                    }
                }

                // Touch `refresh` so progress values are re-read after a level ends.
                @Suppress("UNUSED_EXPRESSION")
                refresh
            }
        }
    }
}

