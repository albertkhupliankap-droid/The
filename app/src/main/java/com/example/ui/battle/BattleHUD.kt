package com.example.ui.battle

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.BattleRoyaleEngine
import com.example.model.*
import com.example.ui.components.VirtualJoystick
import com.example.ui.theme.*
import kotlin.math.*

@Composable
fun BattleHUD(
    engine: BattleRoyaleEngine,
    onPitchChange: (Float) -> Unit = {},
    onFreeLookYawChange: (Float) -> Unit = {},
    onBackToLobby: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aliveCount by engine.alivePlayersCount.collectAsState()
    val killFeed by engine.killFeed.collectAsState()
    val isADS by engine.isAimingDownSights.collectAsState()
    val isReloading by engine.isReloading.collectAsState()
    val reloadProgress by engine.reloadProgress.collectAsState()
    val nearbyLoot by engine.nearbyLoot.collectAsState()
    val inVehicle = engine.humanPlayer.actionState == PlayerActionState.DRIVING
    val announcement by engine.announcementMessage.collectAsState()

    val player = engine.humanPlayer
    val currWeapon = player.currentWeapon

    var pitch by remember { mutableFloatStateOf(0.05f) }
    var freeLookYaw by remember { mutableFloatStateOf(0f) }
    var isEyeDragging by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        // Right-Half Camera Swipe Surface (Look & Aim)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.55f)
                .align(Alignment.CenterEnd)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val sens = if (isADS) 0.18f else 0.32f
                        player.facingAngle = (player.facingAngle + dragAmount.x * sens) % 360f
                        pitch = (pitch - dragAmount.y * 0.0025f).coerceIn(-0.35f, 0.40f)
                        onPitchChange(pitch)
                    }
                }
        )

        // 1. TOP CENTER: Iconic PUBG Horizontal Compass Ribbon
        PubgCompassRibbon(
            facingAngle = player.facingAngle,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
        )

        // 2. TOP LEFT: ALIVE / KILLED & Squad Status Card
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 10.dp, start = 16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ALIVE & KILLED Banner
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0x99000000))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ALIVE ",
                        color = Color(0xFFAAAAAA),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "$aliveCount",
                        color = Color(0xFFF1C40F), // PUBG Yellow
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "KILLED ",
                        color = Color(0xFFAAAAAA),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${player.kills}",
                        color = Color(0xFFF1C40F),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Squad Teammates Mini List
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    val colors = listOf(Color(0xFF2ECC71), Color(0xFFF1C40F), Color(0xFFE67E22), Color(0xFF3498DB))
                    val team = listOf(player) + engine.squadmates
                    team.forEachIndexed { idx, member ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(colors[idx % colors.size]),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "${idx + 1}", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Black)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (member.isHuman) "You" else member.name.take(7),
                                color = if (member.isAlive) Color.White else Color(0xFF888888),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // HP bar
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(Color(0xFF333333))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(member.health / 100f)
                                        .fillMaxHeight()
                                        .background(if (member.isAlive) Color(0xFF2ECC71) else Color(0xFFE74C3C))
                                )
                            }
                        }
                    }
                }
            }
        }
        // 3. TOP RIGHT: Radar Mini-Map & Killfeed Stack
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 16.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Radar Mini-Map
            PubgRadarMiniMap(engine = engine)

            Spacer(modifier = Modifier.height(6.dp))

            // Dynamic Killfeed in PUBG layout
            Column(
                modifier = Modifier.width(240.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.End
            ) {
                killFeed.take(3).forEach { feed ->
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0x88000000))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = feed.killerName,
                            color = if (feed.killerName == player.name) Color(0xFFF1C40F) else Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = " [${feed.weaponName}] ",
                            color = if (feed.isHeadshot) Color(0xFFE74C3C) else Color(0xFFCCCCCC),
                            fontSize = 9.sp
                        )
                        Text(
                            text = feed.victimName,
                            color = Color(0xFFAAAAAA),
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }

        // Center Announcements Banner
        if (announcement != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 56.dp)
                    .clip(CutCornerShape(4.dp))
                    .background(Color(0xDD000000))
                    .border(1.dp, Color(0xFFF1C40F), CutCornerShape(4.dp))
                    .padding(horizontal = 18.dp, vertical = 6.dp)
            ) {
                Text(
                    text = announcement ?: "",
                    color = Color(0xFFF1C40F),
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }
        }

        // Skydiving Freefall & Parachute Deployment (PUBG style)
        if (player.actionState == PlayerActionState.IN_PLANE) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 60.dp)
            ) {
                Button(
                    onClick = { engine.triggerJump() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1C40F)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("pubg_jump_button").height(54.dp),
                    contentPadding = PaddingValues(horizontal = 36.dp)
                ) {
                    Text(text = "JUMP", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 18.sp, letterSpacing = 2.sp)
                }
            }
        } else if (player.actionState == PlayerActionState.FREEFALL) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 60.dp)
            ) {
                Button(
                    onClick = { engine.triggerDeployParachute() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1C40F)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("pubg_chute_button").height(50.dp)
                ) {
                    Text(text = "OPEN PARACHUTE", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
        }

        // Proximity Loot Box (Pop up on left when standing on loot)
        if (nearbyLoot.isNotEmpty() && player.actionState == PlayerActionState.ON_FOOT) {
            LootProximityOverlay(
                items = nearbyLoot,
                onPickItem = { engine.pickUpLoot(it) },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
            )
        }

        // 4. BOTTOM LEFT: Analog Movement Joystick & Armor Status
        if (!inVehicle && player.actionState == PlayerActionState.ON_FOOT) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, bottom = 24.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Analog Joystick with Sprint Tab
                VirtualJoystick(
                    onMove = { vec, isSprint ->
                        player.velocity = vec
                        player.stance = if (isSprint) CharacterStance.SPRINT else CharacterStance.STAND
                    },
                    radiusDp = 64f
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Backpack, Helmet & Vest Status Icons
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Backpack
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x88000000))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Backpack, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Lv.${player.backpackLevel}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    // Helmet
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x88000000))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = if (player.helmetLevel > 0) Color(0xFFF1C40F) else Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Lv.${player.helmetLevel}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    // Vest
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x88000000))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = if (player.vestLevel > 0) Color(0xFF00E5FF) else Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Lv.${player.vestLevel}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 5. BOTTOM CENTER: Health Bar, Boost Bar & Dual Weapon Dock (Exact PUBG Mobile Layout)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Weapon Dock Slots (Horizontal PUBG weapon switcher)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Medical Kit Quick Slot
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0x99000000))
                        .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(4.dp))
                        .clickable { engine.useEnergyDrink() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LocalDrink, contentDescription = "Boost", tint = Color(0xFFFFA500), modifier = Modifier.size(18.dp))
                        Text(text = "BOOST", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Primary Weapon Slot 1
                PubgWeaponSlotCard(
                    weapon = player.weapons.getOrNull(0),
                    isSelected = player.currentWeaponIndex == 0,
                    onClick = { engine.switchWeapon(0) },
                    modifier = Modifier.width(150.dp)
                )

                // Secondary Weapon Slot 2
                PubgWeaponSlotCard(
                    weapon = player.weapons.getOrNull(1),
                    isSelected = player.currentWeaponIndex == 1,
                    onClick = { engine.switchWeapon(1) },
                    modifier = Modifier.width(150.dp)
                )

                // Vehicle Drive / Exit Button (if vehicle nearby)
                val nearVeh = engine.vehicles.any { it.position.distanceTo(player.position) < 6f }
                if (nearVeh || inVehicle) {
                    Box(
                        modifier = Modifier
                            .testTag("pubg_drive_button")
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (inVehicle) Color(0xFFE74C3C) else Color(0xFF2980B9))
                            .border(1.5.dp, Color.White, CircleShape)
                            .clickable { engine.enterOrExitVehicle() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = "Drive", tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Health Bar & 4-Stage Energy Boost Bar
            Column(
                modifier = Modifier
                    .width(280.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xAA000000))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                // White Health Gauge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(Color(0xFF222222))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(player.health / 100f)
                            .fillMaxHeight()
                            .background(if (player.health > 25f) Color.White else Color(0xFFE74C3C))
                    )
                }

                // Orange 4-Segment Boost Bar
                if (player.boost > 0f) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(Color(0xFF222222))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(player.boost / 100f)
                                .fillMaxHeight()
                                .background(Color(0xFFFFA500))
                        )
                    }
                }
            }
        }

        // 6. Action Button Overlay (Firing, Continuous Auto-fire, ADS, Reload, Stances, Freelook)
        if (!inVehicle) {
            GameActionOverlay(
                engine = engine,
                onFreeLookYawDelta = { delta ->
                    freeLookYaw = (freeLookYaw + delta) % 360f
                    onFreeLookYawChange(freeLookYaw)
                },
                onPitchDelta = { delta ->
                    pitch = (pitch + delta).coerceIn(-0.35f, 0.40f)
                    onPitchChange(pitch)
                }
            )
        }

        // Vehicle Driving Overlay (if driving)
        if (inVehicle && engine.currentVehicle.value != null) {
            VehicleControlsOverlay(
                vehicle = engine.currentVehicle.value!!,
                onThrottle = { engine.driveThrottle(it) },
                onSteer = { engine.driveSteer(it) },
                onHorn = { engine.honkHorn() },
                onExit = { engine.enterOrExitVehicle() }
            )
        }
    }
}

@Composable
private fun PubgCompassRibbon(
    facingAngle: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(280.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0x88000000))
            .border(0.8.dp, Color(0x33FFFFFF), RoundedCornerShape(3.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f

            // Degree marks
            val degrees = listOf(
                Pair(0, "N"), Pair(45, "NE"), Pair(90, "E"), Pair(135, "SE"),
                Pair(180, "S"), Pair(225, "SW"), Pair(270, "W"), Pair(315, "NW")
            )

            // Tick marks
            for (d in -90..90 step 15) {
                val worldDeg = ((facingAngle + d + 360) % 360).toInt()
                val x = cx + (d / 90f) * (size.width / 2f)

                val isCardinal = degrees.any { abs(it.first - worldDeg) < 3 }
                val tickHeight = if (isCardinal) 10f else 5f
                drawLine(
                    color = if (isCardinal) Color(0xFFF1C40F) else Color(0x88FFFFFF),
                    start = Offset(x, size.height),
                    end = Offset(x, size.height - tickHeight),
                    strokeWidth = 1.2f
                )
            }

            // Center Pointer Needle
            drawLine(
                color = Color(0xFFF1C40F),
                start = Offset(cx, 0f),
                end = Offset(cx, 12f),
                strokeWidth = 2.5f
            )
        }

        Text(
            text = "${((facingAngle % 360 + 360) % 360).toInt()}°",
            color = Color(0xFFF1C40F),
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            modifier = Modifier.align(Alignment.TopCenter).offset(y = 1.dp)
        )
    }
}

@Composable
private fun PubgRadarMiniMap(
    engine: BattleRoyaleEngine,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(96.dp)
            .clip(CircleShape)
            .background(Color(0xCC000000))
            .border(1.5.dp, Color(0xFFF1C40F), CircleShape)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scale = size.width / engine.worldWidth

            // Safe circle
            val sCenter = Offset(
                engine.storm.targetCenter.x * scale,
                engine.storm.targetCenter.y * scale
            )
            val sRadius = engine.storm.targetRadius * scale
            drawCircle(color = Color.White, radius = sRadius, center = sCenter, style = Stroke(width = 1.2f))

            // Storm blue circle
            val bCenter = Offset(
                engine.storm.currentCenter.x * scale,
                engine.storm.currentCenter.y * scale
            )
            val bRadius = engine.storm.currentRadius * scale
            drawCircle(color = Color(0xAA00B4D8), radius = bRadius, center = bCenter, style = Stroke(width = 1.5f))

            // Human player
            val pX = engine.humanPlayer.position.x * scale
            val pY = engine.humanPlayer.position.y * scale
            drawCircle(color = Color(0xFFF1C40F), radius = 3.5f, center = Offset(pX, pY))

            // Squadmates
            engine.squadmates.filter { it.isAlive }.forEach { sq ->
                drawCircle(color = Color(0xFF00E5FF), radius = 2.5f, center = Offset(sq.position.x * scale, sq.position.y * scale))
            }

            // Airdrop
            engine.airdrops.forEach { ad ->
                drawCircle(color = Color(0xFFE74C3C), radius = 3f, center = Offset(ad.position.x * scale, ad.position.y * scale))
            }
        }

        // Zone countdown badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-3).dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xDD000000))
                .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text(
                text = "${engine.storm.timeUntilShrink.roundToInt()}s",
                color = if (engine.storm.isShrinking) Color(0xFFE74C3C) else Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PubgWeaponSlotCard(
    weapon: Weapon?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (weapon == null) {
        Box(
            modifier = modifier
                .height(48.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0x55000000))
                .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "EMPTY SLOT", color = Color(0xFF666666), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        return
    }

    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) Color(0xDD111B27) else Color(0x77000000))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) Color(0xFFF1C40F) else Color(0x33FFFFFF),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = weapon.name.take(10),
                    color = if (isSelected) Color(0xFFF1C40F) else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Text(
                    text = if (weapon.isAuto) "AUTO" else "SINGLE",
                    color = Color(0xFF999999),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${weapon.currentAmmo}",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                    Text(
                        text = " / ${weapon.reserveAmmo}",
                        color = Color(0xFFAAAAAA),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                if (weapon.equippedScope != null) {
                    Text(
                        text = weapon.equippedScope.title.take(4),
                        color = Color(0xFF00E5FF),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
