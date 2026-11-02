package com.example.ui.lobby

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.model.GameMode
import com.example.model.MapType
import com.example.ui.theme.*
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.ScreenState

@Composable
fun LobbyScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.playerProfile.collectAsState()
    val currentMode by viewModel.selectedMode.collectAsState()
    val currentMap by viewModel.selectedMap.collectAsState()
    val isMatchmaking by viewModel.isMatchmaking.collectAsState()
    val matchTimer by viewModel.matchmakingTimer.collectAsState()

    var showModeDialog by remember { mutableStateOf(false) }

    // Idle soldier breathing animation
    val infiniteTransition = rememberInfiniteTransition(label = "pubg_idle")
    val idleOffsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "soldier_breath"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B11))
    ) {
        // Hero Background
        Image(
            painter = painterResource(id = R.drawable.zogam_lobby_bg),
            contentDescription = "PUBG Lobby",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dark Vignette & Atmospheric Gradients
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xCC050A10),
                            Color(0x22050A10),
                            Color(0x66050A10),
                            Color(0xEE050A10)
                        )
                    )
                )
        )

        // 1. TOP BAR: Player Avatar with Gold Frame, Currencies (BP & UC), and Quick Settings
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Player Profile & Rank Frame
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1B2838))
                        .border(2.dp, Color(0xFFF1C40F), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MilitaryTech,
                        contentDescription = "Avatar",
                        tint = Color(0xFFF1C40F),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = profile.username,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "[ZOGAM]",
                            color = Color(0xFFF1C40F),
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Lv.${profile.level}",
                            color = Color(0xFF00E5FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${profile.rankTier.tierName} (${profile.rankPoints} RP)",
                            color = Color(profile.rankTier.badgeColorHex),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Right: Currencies BP / UC & Settings
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // BP (Battle Points Coins)
                CurrencyBadge(
                    icon = Icons.Default.MonetizationOn,
                    iconTint = Color(0xFFF1C40F),
                    amount = "${profile.zogamCoins} BP"
                )

                // UC (Unknown Cash / Z-Points)
                CurrencyBadge(
                    icon = Icons.Default.Stars,
                    iconTint = Color(0xFF00E5FF),
                    amount = "${profile.zPoints} UC"
                )

                // Settings Gear Button
                IconButton(
                    onClick = { viewModel.navigateTo(ScreenState.SETTINGS) },
                    modifier = Modifier
                        .testTag("pubg_settings_button")
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x88000000))
                        .border(1.dp, Color(0x44FFFFFF), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 2. CENTER 3D SOLDIER & PEDESTAL SHOWCASE
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxHeight(0.75f)
                .width(320.dp)
                .offset(y = idleOffsetY.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f

                // Glowing circular floor pedestal
                drawOval(
                    color = Color(0x33F1C40F),
                    topLeft = Offset(cx - 120f, size.height - 45f),
                    size = androidx.compose.ui.geometry.Size(240f, 38f)
                )
                drawOval(
                    color = Color(0x6600E5FF),
                    topLeft = Offset(cx - 95f, size.height - 40f),
                    size = androidx.compose.ui.geometry.Size(190f, 26f)
                )

                // Soldier Legs
                drawRoundRect(
                    color = Color(0xFF1E272C),
                    topLeft = Offset(cx - 36f, cy + 50f),
                    size = androidx.compose.ui.geometry.Size(28f, 130f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )
                drawRoundRect(
                    color = Color(0xFF1E272C),
                    topLeft = Offset(cx + 8f, cy + 50f),
                    size = androidx.compose.ui.geometry.Size(28f, 130f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )

                // Combat Boots
                drawRoundRect(
                    color = Color(0xFF111111),
                    topLeft = Offset(cx - 40f, cy + 170f),
                    size = androidx.compose.ui.geometry.Size(34f, 22f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
                drawRoundRect(
                    color = Color(0xFF111111),
                    topLeft = Offset(cx + 6f, cy + 170f),
                    size = androidx.compose.ui.geometry.Size(34f, 22f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )

                // Tactical Combat Jacket
                drawRoundRect(
                    color = Color(0xFF1B3A57),
                    topLeft = Offset(cx - 48f, cy - 60f),
                    size = androidx.compose.ui.geometry.Size(96f, 120f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                )
                // Level 3 Armored Vest with Pouches
                drawRoundRect(
                    color = Color(0xFF111B27),
                    topLeft = Offset(cx - 38f, cy - 45f),
                    size = androidx.compose.ui.geometry.Size(76f, 85f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )
                drawRoundRect(
                    color = Color(0xFFF1C40F),
                    topLeft = Offset(cx - 24f, cy - 25f),
                    size = androidx.compose.ui.geometry.Size(48f, 10f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                )

                // Head & Spetsnaz Level 3 Helmet with Golden Visor
                drawCircle(
                    color = Color(0xFF2C3E50),
                    radius = 32f,
                    center = Offset(cx, cy - 100f)
                )
                drawRoundRect(
                    color = Color(0xFFFFD700),
                    topLeft = Offset(cx - 22f, cy - 104f),
                    size = androidx.compose.ui.geometry.Size(44f, 8f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                )

                // M416 Assault Rifle in hands
                drawLine(
                    color = Color(0xFF1A1A1A),
                    start = Offset(cx - 65f, cy + 20f),
                    end = Offset(cx + 55f, cy - 35f),
                    strokeWidth = 16f
                )
                // Barrel and scope
                drawLine(
                    color = Color(0xFFF1C40F),
                    start = Offset(cx + 45f, cy - 30f),
                    end = Offset(cx + 80f, cy - 48f),
                    strokeWidth = 8f
                )
            }

            // Equipped Skin Tag
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xCC000000))
                    .border(1.dp, Color(0xFFF1C40F), RoundedCornerShape(4.dp))
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${profile.selectedOutfit} • ${profile.selectedWeaponSkin}",
                    color = Color(0xFFF1C40F),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 3. LEFT: Squad Team Members Podia
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "TEAM SQUAD", color = Color(0xFFAAAAAA), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            PubgSquadMemberSlot(name = profile.username, isHost = true)
            PubgSquadMemberSlot(name = "Alpha-2", isHost = false)
            PubgSquadMemberSlot(name = "Echo-3", isHost = false)
            PubgSquadMemberSlot(name = "Ghost-4", isHost = false)
        }

        // 4. RIGHT DOCK: Iconic PUBG Vertical Menu (RP, INVENTORY, WORKSHOP, MISSIONS, CLAN, SHOP)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PubgVerticalDockItem(
                icon = Icons.Default.MilitaryTech,
                title = "RP",
                badge = "PASS",
                onClick = { viewModel.navigateTo(ScreenState.ZOGAM_PASS) }
            )
            PubgVerticalDockItem(
                icon = Icons.Default.Backpack,
                title = "INVENTORY",
                onClick = { viewModel.navigateTo(ScreenState.INVENTORY) }
            )
            PubgVerticalDockItem(
                icon = Icons.Default.ShoppingCart,
                title = "SHOP",
                badge = "SALE",
                onClick = { viewModel.navigateTo(ScreenState.STORE) }
            )
            PubgVerticalDockItem(
                icon = Icons.Default.Assignment,
                title = "MISSIONS",
                onClick = { viewModel.navigateTo(ScreenState.ZOGAM_PASS) }
            )
            PubgVerticalDockItem(
                icon = Icons.Default.Groups,
                title = "CLAN",
                onClick = { viewModel.navigateTo(ScreenState.SOCIAL_CLAN) }
            )
            PubgVerticalDockItem(
                icon = Icons.Default.FitnessCenter,
                title = "TRAINING",
                onClick = {
                    viewModel.selectedMode.value = GameMode.TRAINING_GROUND
                    viewModel.startMatchmaking()
                }
            )
        }

        // 5. BOTTOM LEFT: The Iconic PUBG Big Yellow START MATCH Button & Mode Card
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 16.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Big Yellow START Button (Exact PUBG Mobile style)
            Box(
                modifier = Modifier
                    .width(190.dp)
                    .height(62.dp)
                    .testTag("pubg_start_match_button")
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFFD54F), Color(0xFFFFB300), Color(0xFFFFA000))
                        )
                    )
                    .border(2.dp, Color(0xFFFFE082), RoundedCornerShape(6.dp))
                    .clickable { viewModel.startMatchmaking() }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "START",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "BATTLE ROYALE",
                            color = Color(0xFF4A3400),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            // Map & Perspective Mode Box
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xCC000000))
                    .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(6.dp))
                    .clickable { showModeDialog = true }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "CLASSIC • TPP • SQUAD",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFFF1C40F))
                }
                Text(
                    text = "${currentMap.mapName} (${currentMap.sizeKm})",
                    color = Color(0xFFF1C40F),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        // Mode Select Dialog
        if (showModeDialog) {
            ModeSelectDialog(
                currentMode = currentMode,
                currentMap = currentMap,
                onConfirm = { mode, map ->
                    viewModel.selectedMode.value = mode
                    viewModel.selectedMap.value = map
                    showModeDialog = false
                },
                onDismiss = { showModeDialog = false }
            )
        }

        // Matchmaking In-Progress Dialog
        if (isMatchmaking) {
            Dialog(onDismissRequest = {}) {
                Box(
                    modifier = Modifier
                        .width(340.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xEE0B1522))
                        .border(2.dp, Color(0xFFF1C40F), RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = Color(0xFFF1C40F),
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "MATCHING...",
                            color = Color(0xFFF1C40F),
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "${currentMode.displayName} • ${currentMap.mapName}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = "Estimated: 0:05 | Elapsed: 0:0$matchTimer",
                            color = Color(0xFFAAAAAA),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = { viewModel.cancelMatchmaking() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2D42)),
                            border = BorderStroke(1.dp, Color(0xFFE74C3C)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(text = "CANCEL", color = Color(0xFFE74C3C), fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrencyBadge(
    icon: ImageVector,
    iconTint: Color,
    amount: String
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x88000000))
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = amount, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
private fun PubgSquadMemberSlot(
    name: String,
    isHost: Boolean
) {
    Row(
        modifier = Modifier
            .width(135.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0x88000000))
            .border(1.dp, if (isHost) Color(0xFFF1C40F) else Color(0x33FFFFFF), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (isHost) Color(0xFFF1C40F) else Color(0xFF2980B9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
        }
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(text = name.take(8), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
            Text(text = if (isHost) "Leader" else "Ready", color = if (isHost) Color(0xFFF1C40F) else Color(0xFFAAAAAA), fontSize = 8.sp)
        }
    }
}

@Composable
private fun PubgVerticalDockItem(
    icon: ImageVector,
    title: String,
    badge: String? = null,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0x88000000))
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box {
            Icon(imageVector = icon, contentDescription = title, tint = Color(0xFFF1C40F), modifier = Modifier.size(20.dp))
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .offset(x = 10.dp, y = (-5).dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFE74C3C))
                        .padding(horizontal = 2.dp, vertical = 1.dp)
                ) {
                    Text(text = badge, color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Black)
                }
            }
        }
        Text(text = title, color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
    }
}
