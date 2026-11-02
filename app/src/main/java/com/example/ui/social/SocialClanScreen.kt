package com.example.ui.social

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClanMember
import com.example.model.FriendItem
import com.example.ui.theme.*
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.ScreenState

@Composable
fun SocialClanScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.playerProfile.collectAsState()
    val friends by viewModel.friendsList.collectAsState()
    val clanMembers by viewModel.clanMembers.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0: Friends, 1: Clan, 2: Leaderboard

    Box(modifier = modifier.fillMaxSize().background(ZogamNavyDark)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(ScreenState.LOBBY) },
                    modifier = Modifier
                        .testTag("back_to_lobby_social")
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x9914243B))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "SOCIAL & CLAN WARFARE",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Navigation Tabs
            Row(modifier = Modifier.fillMaxWidth()) {
                TabButton(text = "FRIENDS (${friends.count { it.isOnline }})", isSelected = selectedTab == 0, onClick = { selectedTab = 0 }, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                TabButton(text = "CLAN ROSTER", isSelected = selectedTab == 1, onClick = { selectedTab = 1 }, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                TabButton(text = "LEADERBOARD", isSelected = selectedTab == 2, onClick = { selectedTab = 2 }, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // Friends List
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(friends) { friend ->
                            FriendRowCard(friend = friend)
                        }
                    }
                }
                1 -> {
                    // Clan Information & Members
                    Column {
                        // Clan banner
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ZogamNavySurface)
                                .border(1.dp, ZogamGold, RoundedCornerShape(12.dp))
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "${profile.clanTag} ${profile.clanName}",
                                    color = ZogamGold,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Clan Level ${profile.clanLevel} • Ranked #4 Regional",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            Button(
                                onClick = {},
                                colors = ButtonDefaults.buttonColors(containerColor = ZogamRoyalBlue),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("TOURNAMENT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(clanMembers) { member ->
                                ClanMemberRow(member = member)
                            }
                        }
                    }
                }
                2 -> {
                    // Leaderboards (Top Conquerors)
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(listOf(
                            Triple(1, "ZOGAM_TITAN", "6,420 RP (Conqueror)"),
                            Triple(2, "CHIN_COMMANDO_1", "6,150 RP (Conqueror)"),
                            Triple(3, "MIZO_WARLORD", "5,980 RP (Conqueror)"),
                            Triple(4, "KUKI_SHADOW", "5,840 RP (Conqueror)"),
                            Triple(5, "ZOMI_STRIKER", "5,720 RP (Conqueror)"),
                            Triple(6, "LAMKA_APEX", "5,490 RP (Ace Dominator)"),
                            Triple(7, profile.username, "${profile.rankPoints} RP (${profile.rankTier.tierName})")
                        )) { (rank, name, rp) ->
                            LeaderboardRow(rank = rank, name = name, score = rp, isUser = name == profile.username)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendRowCard(friend: FriendItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(ZogamNavySurface)
            .border(1.dp, ZogamNavyBorder, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (friend.isOnline) ZogamRoyalBlue else Color(0xFF333333)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = friend.avatarInitials, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = friend.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = "${friend.rank.tierName} • ${friend.status}", color = if (friend.isOnline) ZogamCyanAccent else TextMuted, fontSize = 11.sp)
            }
        }

        if (friend.isOnline) {
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = ZogamGold),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(text = "INVITE", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun ClanMemberRow(member: ClanMember) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ZogamNavySurface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Shield, contentDescription = null, tint = ZogamGold, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = member.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = "${member.role} • ${member.tier.tierName}", color = TextSecondary, fontSize = 11.sp)
            }
        }
        Text(text = "${member.seasonPoints} Pts", color = ZogamGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
private fun LeaderboardRow(rank: Int, name: String, score: String, isUser: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isUser) ZogamNavyCard else ZogamNavySurface)
            .border(1.dp, if (isUser) ZogamGold else ZogamNavyBorder, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        when (rank) {
                            1 -> ZogamGold
                            2 -> Color(0xFFC0C0C0)
                            3 -> Color(0xFFCD7F32)
                            else -> Color(0x33FFFFFF)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#$rank",
                    color = if (rank <= 3) Color.Black else Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (isUser) "$name (YOU)" else name,
                color = if (isUser) ZogamGold else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
        Text(text = score, color = ZogamCyanAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
            fontSize = 11.sp
        )
    }
}
