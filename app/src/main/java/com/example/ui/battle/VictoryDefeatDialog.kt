package com.example.ui.battle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.MatchEndStats
import com.example.ui.components.ZogamTacticalButton
import com.example.ui.theme.*

@Composable
fun VictoryDefeatDialog(
    stats: MatchEndStats,
    onReturnLobby: () -> Unit,
    onPlayAgain: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(ZogamNavySurface)
                .border(2.dp, if (stats.isVictory) ZogamGold else ZogamCrimsonRed, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Victory / Defeat Header
                if (stats.isVictory) {
                    Box(
                        modifier = Modifier
                            .clip(CutCornerShape(8.dp))
                            .background(Brush.horizontalGradient(listOf(ZogamGold, ZogamAmberGold)))
                            .padding(horizontal = 24.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "#1 / 100 WINNER WINNER",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                    Text(
                        text = "CHICKEN DINNER!",
                        color = Color(0xFFF1C40F),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .clip(CutCornerShape(8.dp))
                            .background(Color(0xFF333333))
                            .padding(horizontal = 24.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "#${stats.placement} / 100",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                    Text(
                        text = "BETTER LUCK NEXT TIME!",
                        color = ZogamCrimsonRed,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Stats Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatCard(title = "ELIMINATIONS", value = "${stats.kills}", icon = Icons.Default.MilitaryTech)
                    StatCard(title = "DAMAGE", value = "${stats.damage}", icon = Icons.Default.FlashOn)
                    StatCard(title = "HEADSHOTS", value = "${stats.headshots}", icon = Icons.Default.GpsFixed)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Rewards Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ZogamNavyCard)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "RANK POINTS", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (stats.rankPointsGained >= 0) "+${stats.rankPointsGained} RP" else "${stats.rankPointsGained} RP",
                            color = if (stats.rankPointsGained >= 0) ZogamGold else ZogamCrimsonRed,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "ZG COINS", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "+${stats.zogamCoinsEarned}",
                            color = ZogamAmberGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "PASS XP", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "+${stats.zogamPassXpEarned}",
                            color = ZogamCyanAccent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onReturnLobby,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("return_lobby_button"),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TextSecondary)
                    ) {
                        Text("LOBBY", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    ZogamTacticalButton(
                        text = "PLAY AGAIN",
                        onClick = onPlayAgain,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        testTag = "play_again_button"
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        modifier = Modifier
            .width(85.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x9914243B))
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ZogamGold,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
        Text(text = title, color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
    }
}
