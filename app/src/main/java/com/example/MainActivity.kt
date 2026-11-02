package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.model.PlayerActionState
import com.example.ui.battle.*
import com.example.ui.inventory.InventoryScreen
import com.example.ui.lobby.LobbyScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.shop.ShopScreen
import com.example.ui.social.SocialClanScreen
import com.example.ui.theme.ZogamMobileTheme
import com.example.ui.zogampass.ZogamPassScreen
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.ScreenState

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lock screen on and configure full-bleed immersive game display
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())

        enableEdgeToEdge()

        setContent {
            ZogamMobileTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF070B11)),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0) // Fullscreen game canvas
                ) { innerPadding ->
                    ZogamGameAppContent(
                        viewModel = viewModel,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun ZogamGameAppContent(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val engine by viewModel.activeEngine.collectAsState()

    var cameraPitch by remember { mutableFloatStateOf(0.05f) }
    var freeLookYaw by remember { mutableFloatStateOf(0f) }

    // System Back Handler
    BackHandler(enabled = currentScreen != ScreenState.LOBBY) {
        if (currentScreen == ScreenState.MATCH_PLAYING) {
            viewModel.leaveMatch()
        } else {
            viewModel.navigateTo(ScreenState.LOBBY)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B11))
    ) {
        when (currentScreen) {
            ScreenState.LOBBY -> {
                LobbyScreen(viewModel = viewModel)
            }

            ScreenState.MATCH_PLAYING -> {
                val activeEngine = engine
                if (activeEngine != null) {
                    val isADS by activeEngine.isAimingDownSights.collectAsState()
                    val isFinished by activeEngine.isMatchFinished.collectAsState()
                    val endStats by activeEngine.matchStats.collectAsState()
                    val currWeapon = activeEngine.humanPlayer.currentWeapon

                    Box(modifier = Modifier.fillMaxSize()) {
                        // 1. True 3D Third-Person Perspective (TPP) Battlefield Canvas
                        BattleRoyaleGameView(
                            engine = activeEngine,
                            camPitch = cameraPitch,
                            freeLookYaw = freeLookYaw
                        )

                        // 2. Optical Scope Reticle (when ADS zoom is triggered)
                        if (isADS && activeEngine.humanPlayer.actionState == PlayerActionState.ON_FOOT) {
                            ADSOverlay(equippedScope = currWeapon?.equippedScope)
                        }

                        // 3. Iconic PUBG Mobile HUD Controls & Compass
                        BattleHUD(
                            engine = activeEngine,
                            onPitchChange = { cameraPitch = it },
                            onFreeLookYawChange = { freeLookYaw = it },
                            onBackToLobby = { viewModel.leaveMatch() }
                        )

                        // 4. #1 WINNER WINNER CHICKEN DINNER Victory Screen
                        if (isFinished && endStats != null) {
                            VictoryDefeatDialog(
                                stats = endStats!!,
                                onReturnLobby = {
                                    viewModel.applyMatchStats(endStats!!)
                                    viewModel.leaveMatch()
                                },
                                onPlayAgain = {
                                    viewModel.applyMatchStats(endStats!!)
                                    viewModel.startMatchmaking()
                                }
                            )
                        }
                    }
                } else {
                    LobbyScreen(viewModel = viewModel)
                }
            }

            ScreenState.ZOGAM_PASS -> {
                ZogamPassScreen(viewModel = viewModel)
            }

            ScreenState.INVENTORY -> {
                InventoryScreen(viewModel = viewModel)
            }

            ScreenState.STORE -> {
                ShopScreen(viewModel = viewModel)
            }

            ScreenState.SOCIAL_CLAN -> {
                SocialClanScreen(viewModel = viewModel)
            }

            ScreenState.SETTINGS -> {
                SettingsScreen(viewModel = viewModel)
            }

            ScreenState.TRAINING_GROUND -> {
                LobbyScreen(viewModel = viewModel)
            }
        }
    }
}
