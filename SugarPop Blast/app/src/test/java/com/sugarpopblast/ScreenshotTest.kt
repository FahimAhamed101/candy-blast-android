package com.sugarpopblast

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import com.sugarpopblast.game.GameViewModel
import com.sugarpopblast.ui.CandyTheme
import com.sugarpopblast.ui.GameScreen
import com.sugarpopblast.ui.HomeScreen
import com.sugarpopblast.ui.LevelMapScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ScreenshotTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun capture(name: String) {
        rule.waitForIdle()
        val view = rule.activity.window.decorView
        val width = view.width.coerceAtLeast(1)
        val height = view.height.coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
        val dir = File("build/screenshots").apply { mkdirs() }
        FileOutputStream(File(dir, "$name.png")).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }

    @Test
    fun captureHome() {
        rule.setContent {
            CandyTheme {
                HomeScreen(
                    totalStars = 27,
                    coins = 420,
                    currentLevel = 13,
                    soundOn = true,
                    musicOn = true,
                    vibrationOn = true,
                    onPlay = {},
                    onLevels = {},
                    onToggleSound = {},
                    onToggleMusic = {},
                    onToggleVibration = {},
                    onReset = {}
                )
            }
        }
        capture("home")
    }

    @Test
    fun captureMap() {
        rule.setContent {
            CandyTheme {
                LevelMapScreen(
                    totalStars = 27,
                    starsFor = { level -> if (level < 13) (level % 3) + 1 else 0 },
                    unlocked = { it <= 13 },
                    currentLevel = 13,
                    onLevel = {},
                    onBack = {}
                )
            }
        }
        capture("map")
    }

    @Test
    fun captureGame() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = GameViewModel(app)
        vm.startLevel(4)
        rule.setContent {
            CandyTheme {
                GameScreen(vm = vm, onExit = {}, onOpenMap = {})
            }
        }
        capture("game")
    }
}

