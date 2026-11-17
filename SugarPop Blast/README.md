# SUGARPOP BLAST

SugarPop Blast is an Android match-3 puzzle game with animated swaps, combo scoring, level progression, sound, haptics, and local progress storage.

SugarPop Blast is an Android match-3 candy puzzle game built with Kotlin and Jetpack Compose. It uses an 8 x 8 board, animated candy swaps, combo scoring, level progression, sound effects, background music, vibration feedback, and locally saved preferences.

## Features

- Match-3 gameplay with tap and swipe controls.
- 8 x 8 candy board with automatic refill and gravity.
- Combo scoring, special candy effects, level targets, move limits, and timers.
- High score and settings persistence with local app storage.
- Sound effects, music loops, and optional haptic feedback.
- Game over, level up, settings, start, and rate-app overlays.
- Player progress and settings stored with Android SharedPreferences.

## Tech Stack

- Kotlin
- Android Gradle Plugin
- Jetpack Compose
- Material 3
- AndroidX Lifecycle and ViewModel
- Kotlin Coroutines
- JUnit, AndroidX Test, Compose UI Test, and Robolectric
- Gradle Version Catalog

The version catalog contains aliases for additional libraries, including Room, DataStore, Firebase, and Google Mobile Ads. These are not declared as app-module dependencies and are not active app features. No Firebase or advertising account setup is needed to build the current project.

## Project Structure

```text
app/src/main/java/com/candycrushblast/
  MainActivity.kt
  game/
    GameModels.kt
    GameViewModel.kt
    Levels.kt
    MatchEngine.kt
    ProgressStore.kt
    SoundFx.kt
  ui/
    CandyArt.kt
    Components.kt
    GameBoard.kt
    GameScreen.kt
    HomeScreen.kt
    LevelMapScreen.kt
    SplashScreen.kt
    Theme.kt
```

## Requirements

- Android Studio (tested version must be recorded before publishing)
- JDK 11 or newer
- Android SDK with compile SDK 36 support
- Gradle wrapper included in this repository

## Setup

1. Extract the CodeCanyon source archive.
2. In Android Studio, choose **Open** and select the extracted `SugarPop Blast` project folder.
3. Install a JDK 11 or newer and Android SDK Platform 36 if Android Studio prompts for them.
4. Allow Gradle sync to finish. Android Studio creates the machine-specific `local.properties` file for the local SDK; it is intentionally not shipped.
5. The current app uses the libraries declared in `app/build.gradle.kts`. Firebase, advertising, Room, and DataStore are not active app dependencies, so no service credentials are needed to build this version.

## Build

From the project root:

```powershell
.\gradlew.bat assembleDebug
```

Run unit tests:

```powershell
.\gradlew.bat test
```

## Package

```text
com.sugarpopblast (internal namespace; change it before publishing if your brand requires a matching namespace)
```

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

