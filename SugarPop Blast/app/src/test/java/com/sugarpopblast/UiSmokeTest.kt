package com.sugarpopblast

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class UiSmokeTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun homeScreenRenders() {
        rule.setContent {
            CandyTheme {
                HomeScreen(
                    totalStars = 0,
                    coins = 150,
                    currentLevel = 1,
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
        rule.onNodeWithText("PLAY").assertIsDisplayed()
        rule.onNodeWithText("LEVEL MAP").assertIsDisplayed()
    }

    @Test
    fun levelMapRenders() {
        rule.setContent {
            CandyTheme {
                LevelMapScreen(
                    totalStars = 3,
                    starsFor = { if (it == 1) 3 else 0 },
                    unlocked = { it <= 2 },
                    currentLevel = 2,
                    onLevel = {},
                    onBack = {}
                )
            }
        }
        rule.onNodeWithText("LEVEL MAP").assertIsDisplayed()
        rule.onNodeWithText("WORLD 1 Â· BERRY GROVE").assertExists()
    }

    @Test
    fun gameScreenRenders() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = GameViewModel(app)
        vm.startLevel(1)
        rule.setContent {
            CandyTheme {
                GameScreen(vm = vm, onExit = {}, onOpenMap = {})
            }
        }
        rule.onNodeWithText("LEVEL 1").assertIsDisplayed()
        rule.onNodeWithText("MOVES").assertExists()
    }
}

