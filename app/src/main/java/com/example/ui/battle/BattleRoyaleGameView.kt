package com.example.ui.battle

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.game.BattleRoyaleEngine
import com.example.game.engine3d.Renderer3D
import com.example.model.PlayerActionState

@Composable
fun BattleRoyaleGameView(
    engine: BattleRoyaleEngine,
    camPitch: Float = 0.05f,
    freeLookYaw: Float = 0f,
    modifier: Modifier = Modifier
) {
    val isADS by engine.isAimingDownSights.collectAsState()
    val isFiring = remember { mutableStateOf(false) }
    val renderer = remember { Renderer3D() }

    // Walk animation cycle
    val infiniteTransition = rememberInfiniteTransition(label = "walk_anim")
    val walkCycle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "walk_cycle"
    )

    // Watch weapon recoil for muzzle flash
    val recoilOffset by engine.cameraRecoilOffset.collectAsState()
    LaunchedEffect(recoilOffset) {
        if (recoilOffset > 2f) {
            isFiring.value = true
            kotlinx.coroutines.delay(65)
            isFiring.value = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val player = engine.humanPlayer
            val effectiveYaw = player.facingAngle

            renderer.renderScene(
                drawScope = this,
                engine = engine,
                camYaw = effectiveYaw,
                camPitch = camPitch,
                isADS = isADS,
                freeLookOffsetYaw = freeLookYaw,
                walkAnimProgress = if (player.velocity.length() > 0.05f) walkCycle else 0f,
                isFiring = isFiring.value
            )
        }
    }
}
