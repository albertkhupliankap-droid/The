package com.example.ui.lobby

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.window.Dialog
import com.example.model.GameMode
import com.example.model.MapType
import com.example.ui.components.ZogamTacticalButton
import com.example.ui.theme.*

@Composable
fun ModeSelectDialog(
    currentMode: GameMode,
    currentMap: MapType,
    onConfirm: (GameMode, MapType) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMode by remember { mutableStateOf(currentMode) }
    var selectedMap by remember { mutableStateOf(currentMap) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Game Mode, 1: Maps

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, ZogamGold, RoundedCornerShape(16.dp)),
            color = ZogamNavySurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DEPLOYMENT CONFIGURATION",
                        color = ZogamGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs: MODES vs MAPS
                Row(modifier = Modifier.fillMaxWidth()) {
                    TabButton(
                        text = "GAME MODES",
                        isSelected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TabButton(
                        text = "BATTLE MAPS",
                        isSelected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content list
                Box(modifier = Modifier.weight(1f)) {
                    if (selectedTab == 0) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(GameMode.values()) { mode ->
                                val isSelected = mode == selectedMode
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) ZogamNavyCard else Color(0x6614243B))
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) ZogamGold else ZogamNavyBorder,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { selectedMode = mode }
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when (mode) {
                                            GameMode.CLASSIC_BR -> Icons.Default.Shield
                                            GameMode.RANKED_BR -> Icons.Default.MilitaryTech
                                            GameMode.SQUAD_4P -> Icons.Default.Groups
                                            GameMode.DUO_2P -> Icons.Default.Group
                                            GameMode.SOLO -> Icons.Default.Person
                                            GameMode.TDM_ARENA -> Icons.Default.FlashOn
                                            GameMode.PAYLOAD -> Icons.Default.RocketLaunch
                                            GameMode.TRAINING_GROUND -> Icons.Default.FitnessCenter
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) ZogamGold else TextSecondary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = mode.displayName,
                                            color = if (isSelected) Color.White else TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = mode.description,
                                            color = TextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = ZogamGold,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(MapType.values()) { map ->
                                val isSelected = map == selectedMap
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) ZogamNavyCard else Color(0x6614243B))
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) ZogamCyanAccent else ZogamNavyBorder,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { selectedMap = map }
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = map.mapName,
                                            color = if (isSelected) ZogamCyanAccent else Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = map.sizeKm,
                                            color = ZogamGold,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = map.environment,
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                    Text(
                                        text = "Landmarks: ${map.landmarks.joinToString(", ")}",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm button
                ZogamTacticalButton(
                    text = "CONFIRM SELECTION (${selectedMode.displayName})",
                    onClick = {
                        onConfirm(selectedMode, selectedMap)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "confirm_mode_button"
                )
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
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) ZogamGold else Color(0x3314243B))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.Black else TextSecondary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}
