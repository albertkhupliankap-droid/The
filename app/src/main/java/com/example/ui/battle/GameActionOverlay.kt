package com.example.ui.battle

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.BattleRoyaleEngine
import com.example.model.CharacterStance
import com.example.model.PlayerActionState
import com.example.model.Weapon
import com.example.ui.theme.HealthGreen
import com.example.ui.theme.ZogamBrightRed
import com.example.ui.theme.ZogamCyanAccent
import com.example.ui.theme.ZogamGold

/**
 * Primary Touch Input and Action Button Overlay for ZOGAM MOBILE.
 * Controls weapon firing (single-tap & continuous auto-fire with recoil drag),
 * ADS scope toggling, reload, stance switches (Jump, Crouch, Prone),
 * and 360-degree freelook.
 */
@Composable
fun GameActionOverlay(
    engine: BattleRoyaleEngine,
    onFreeLookYawDelta: (Float) -> Unit,
    onPitchDelta: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val player = engine.humanPlayer
    val currWeapon = player.currentWeapon
    val isADS by engine.isAimingDownSights.collectAsState()
    val isReloading by engine.isReloading.collectAsState()
    val reloadProgress by engine.reloadProgress.collectAsState()
    val isHoldingFire by engine.isHoldingFire.collectAsState()

    var isEyeDragging by remember { mutableStateOf(false) }

    // Pulsing glow when holding fire
    val infiniteTransition = rememberInfiniteTransition(label = "fire_burst")
    val fireGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(120, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // -------------------------------------------------------------
        // UPPER LEFT: Secondary Claw-Grip Fire Button
        // -------------------------------------------------------------
        if (player.actionState == PlayerActionState.ON_FOOT) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 70.dp, start = 16.dp)
                    .size(54.dp)
                    .testTag("claw_fire_trigger")
                    .clip(CircleShape)
                    .background(Color(0x77000000))
                    .border(2.dp, if (isHoldingFire) Color(0xFFF1C40F) else Color(0x66FFFFFF), CircleShape)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                engine.startContinuousFire()
                                tryAwaitRelease()
                                engine.stopContinuousFire()
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "Left Claw Fire",
                    tint = Color(0xFFF1C40F),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // -------------------------------------------------------------
        // BOTTOM RIGHT: Combat Action Cluster
        // -------------------------------------------------------------
        if (player.actionState == PlayerActionState.ON_FOOT) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ROW 1: Eye Freelook, Reload Button, ADS Scope Button
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Freelook Eye Tool (360 freelook without changing player angle)
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .testTag("eye_freelook_trigger")
                            .clip(CircleShape)
                            .background(if (isEyeDragging) Color(0xFFF1C40F) else Color(0x88000000))
                            .border(1.dp, Color(0x66FFFFFF), CircleShape)
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { isEyeDragging = true },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        onFreeLookYawDelta(dragAmount.x * 0.45f)
                                        onPitchDelta(dragAmount.y * -0.002f)
                                    },
                                    onDragEnd = {
                                        isEyeDragging = false
                                        onFreeLookYawDelta(0f)
                                    },
                                    onDragCancel = {
                                        isEyeDragging = false
                                        onFreeLookYawDelta(0f)
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Eye Look",
                            tint = if (isEyeDragging) Color.Black else Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Reload Button with Circular Timer
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .testTag("action_reload_button")
                                .clip(CircleShape)
                                .background(Color(0x88000000))
                                .border(1.5.dp, Color(0x66FFFFFF), CircleShape)
                                .clickable { engine.reloadWeapon() },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reload",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "RELOAD",
                                    color = Color.White,
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        if (isReloading) {
                            CircularProgressIndicator(
                                progress = { reloadProgress },
                                color = Color(0xFFF1C40F),
                                modifier = Modifier.size(50.dp),
                                strokeWidth = 3.dp
                            )
                        }
                    }

                    // Scope / ADS Button
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .testTag("action_scope_button")
                            .clip(CircleShape)
                            .background(if (isADS) Color(0xFFF1C40F) else Color(0x99000000))
                            .border(2.dp, Color(0x66FFFFFF), CircleShape)
                            .clickable { engine.isAimingDownSights.value = !engine.isAimingDownSights.value },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.GpsFixed,
                                contentDescription = "Scope",
                                tint = if (isADS) Color.Black else Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                            Text(
                                text = if (currWeapon?.equippedScope != null) currWeapon.equippedScope!!.title.take(4) else "SCOPE",
                                color = if (isADS) Color.Black else Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // ROW 2: Stance Triangular Cluster (Grenade, Crouch, Prone, Jump)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Frag Grenade Button
                    ActionCircleBtn(
                        icon = Icons.Default.FlashOn,
                        label = "FRAG",
                        onClick = { engine.throwGrenade(isSmoke = false) },
                        btnColor = Color(0x88000000),
                        iconTint = Color(0xFFE74C3C),
                        size = 44,
                        testTag = "action_frag_button"
                    )

                    // Crouch Button
                    ActionCircleBtn(
                        icon = Icons.Default.AirlineSeatReclineExtra,
                        label = "CROUCH",
                        onClick = { engine.toggleStance(CharacterStance.CROUCH) },
                        btnColor = if (player.stance == CharacterStance.CROUCH) Color(0xFFF1C40F) else Color(0x88000000),
                        iconTint = if (player.stance == CharacterStance.CROUCH) Color.Black else Color.White,
                        size = 46,
                        testTag = "action_crouch_button"
                    )

                    // Prone Button
                    ActionCircleBtn(
                        icon = Icons.Default.SingleBed,
                        label = "PRONE",
                        onClick = { engine.toggleStance(CharacterStance.PRONE) },
                        btnColor = if (player.stance == CharacterStance.PRONE) Color(0xFFF1C40F) else Color(0x88000000),
                        iconTint = if (player.stance == CharacterStance.PRONE) Color.Black else Color.White,
                        size = 46,
                        testTag = "action_prone_button"
                    )

                    // Jump / Vault Button
                    ActionCircleBtn(
                        icon = Icons.Default.North,
                        label = "JUMP",
                        onClick = {
                            // Jump action
                        },
                        btnColor = Color(0x88000000),
                        iconTint = Color.White,
                        size = 48,
                        testTag = "action_jump_button"
                    )
                }

                // ROW 3: Primary FIRE BUTTON (Big Circular Trigger with Continuous Fire & Recoil Drag)
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .testTag("action_primary_fire_button")
                        .clip(CircleShape)
                        .background(
                            if (isHoldingFire) {
                                Brush.radialGradient(
                                    listOf(
                                        Color(0xFFFFD54F),
                                        Color(0xFFE53935).copy(alpha = fireGlowAlpha),
                                        Color(0xFF000000)
                                    )
                                )
                            } else {
                                Brush.radialGradient(
                                    listOf(
                                        Color(0xDDFFFFFF),
                                        Color(0xAAFFC107),
                                        Color(0x99000000)
                                    )
                                )
                            }
                        )
                        .border(
                            width = if (isHoldingFire) 3.5.dp else 2.5.dp,
                            color = if (isHoldingFire) Color(0xFFE53935) else Color(0xFFF1C40F),
                            shape = CircleShape
                        )
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = {
                                    engine.startContinuousFire()
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    // Pull down on fire button while spraying to control vertical recoil!
                                    if (dragAmount.y > 0) {
                                        onPitchDelta(dragAmount.y * -0.0015f)
                                    }
                                    player.facingAngle = (player.facingAngle + dragAmount.x * 0.15f) % 360f
                                },
                                onDragEnd = {
                                    engine.stopContinuousFire()
                                },
                                onDragCancel = {
                                    engine.stopContinuousFire()
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Primary Fire",
                            tint = Color.Black,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = if (currWeapon != null) "${currWeapon.currentAmmo}" else "FIRE",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // CONTEXTUAL INTERACTION POPUP (Near Vehicle or Downed Teammate)
        // -------------------------------------------------------------
        val downedTeammate = engine.squadmates.firstOrNull { it.isDowned && it.position.distanceTo(player.position) < 5f }
        if (downedTeammate != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xDD000000))
                    .border(1.5.dp, HealthGreen, RoundedCornerShape(8.dp))
                    .clickable { engine.reviveTeammate() }
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalHospital, contentDescription = null, tint = HealthGreen, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "REVIVE ${downedTeammate.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ActionCircleBtn(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    btnColor: Color,
    iconTint: Color,
    size: Int,
    testTag: String
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .testTag(testTag)
            .clip(CircleShape)
            .background(btnColor)
            .border(1.dp, Color(0x55FFFFFF), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size((size * 0.45f).dp)
            )
            Text(
                text = label,
                color = iconTint,
                fontWeight = FontWeight.Bold,
                fontSize = (size * 0.16f).sp
            )
        }
    }
}
