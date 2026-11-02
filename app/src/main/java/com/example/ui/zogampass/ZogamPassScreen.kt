package com.example.ui.zogampass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ZogamPassTier
import com.example.ui.components.ZogamTacticalButton
import com.example.ui.theme.*
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.ScreenState

@Composable
fun ZogamPassScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.playerProfile.collectAsState()
    val passTiers by viewModel.passTiers.collectAsState()
    val missions by viewModel.missions.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0: Rewards Track, 1: Missions

    Box(modifier = modifier.fillMaxSize().background(ZogamNavyDark)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.navigateTo(ScreenState.LOBBY) },
                        modifier = Modifier
                            .testTag("back_to_lobby_pass")
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x9914243B))
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "ZOGAM PASS",
                            color = ZogamGold,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "SEASON 1: TACTICAL REVOLUTION",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Elite Pass Badge
                if (profile.isPassEliteUnlocked) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.horizontalGradient(listOf(ZogamAmberGold, ZogamCrimsonRed)))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "ELITE PASS ACTIVE",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Button(
                        onClick = { viewModel.unlockElitePass() },
                        colors = ButtonDefaults.buttonColors(containerColor = ZogamGold),
                        modifier = Modifier
                            .testTag("unlock_elite_button")
                            .height(36.dp)
                    ) {
                        Text("UNLOCK ELITE (600 ZP)", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Level & Progress Bar Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ZogamNavySurface)
                    .border(1.dp, ZogamNavyBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(ZogamGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${profile.passLevel}",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "PASS LEVEL ${profile.passLevel} / 50",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(180.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF222222))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.65f)
                                    .fillMaxHeight()
                                    .background(ZogamCyanAccent)
                            )
                        }
                    }
                }

                Text(
                    text = "325 / 500 XP",
                    color = ZogamCyanAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Switcher Tabs: REWARDS TRACK vs MISSIONS
            Row(modifier = Modifier.fillMaxWidth()) {
                TabButton(text = "REWARDS TRACK", isSelected = selectedTab == 0, onClick = { selectedTab = 0 }, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                TabButton(text = "SEASON MISSIONS (${missions.count { it.isCompleted && !it.isClaimed }} CLAIMABLE)", isSelected = selectedTab == 1, onClick = { selectedTab = 1 }, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Content Area
            if (selectedTab == 0) {
                // Horizontal scrollable Rewards Track (Free top row, Elite bottom row)
                LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(passTiers) { tier ->
                        PassTierColumnCard(
                            tier = tier,
                            currentPassLevel = profile.passLevel,
                            isElite = profile.isPassEliteUnlocked
                        )
                    }
                }
            } else {
                // Missions List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(missions) { mission ->
                        MissionCard(
                            mission = mission,
                            onClaim = { viewModel.claimMission(mission.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PassTierColumnCard(
    tier: ZogamPassTier,
    currentPassLevel: Int,
    isElite: Boolean
) {
    val isReached = tier.tier <= currentPassLevel

    Column(
        modifier = Modifier
            .width(135.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x990C1827))
            .border(
                width = if (tier.tier == currentPassLevel) 2.dp else 1.dp,
                color = if (tier.tier == currentPassLevel) ZogamGold else ZogamNavyBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tier Header Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isReached) ZogamGold else Color(0x6614243B))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = "LV.${tier.tier}",
                color = if (isReached) Color.Black else TextSecondary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Free Reward Box
        Text(text = "FREE PASS", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(85.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(ZogamNavyCard)
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = null,
                    tint = ZogamGold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tier.freeReward,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Elite Reward Box
        Text(text = "ELITE PASS", color = ZogamGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(105.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isElite) Color(0xCC1A3052) else Color(0x55111B2C))
                .border(
                    width = 1.dp,
                    color = if (isElite) ZogamAmberGold else Color(0x33FFB300),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if (tier.isMilestone) Icons.Default.MilitaryTech else Icons.Default.Stars,
                    contentDescription = null,
                    tint = if (tier.isMilestone) ZogamBrightRed else ZogamGold,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tier.premiumReward,
                    color = if (isElite) ZogamGold else TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun MissionCard(
    mission: com.example.model.MissionItem,
    onClaim: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(ZogamNavySurface)
            .border(1.dp, if (mission.isCompleted && !mission.isClaimed) ZogamGold else ZogamNavyBorder, RoundedCornerShape(10.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mission.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = mission.description,
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Progress: ${mission.progress} / ${mission.maxProgress}",
                    color = ZogamCyanAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "+${mission.xpReward} XP  •  +${mission.coinReward} Coins",
                    color = ZogamGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        if (mission.isClaimed) {
            Text(text = "CLAIMED", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        } else if (mission.isCompleted) {
            Button(
                onClick = onClaim,
                colors = ButtonDefaults.buttonColors(containerColor = ZogamGold),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "CLAIM", color = Color.Black, fontWeight = FontWeight.Black)
            }
        } else {
            OutlinedButton(
                onClick = {},
                enabled = false,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "GO", color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) ZogamGold else Color(0x3314243B))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.Black else TextSecondary,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}
