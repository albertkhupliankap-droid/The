package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.theme.*
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.ScreenState

@Composable
fun SettingsScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val graphics by viewModel.graphicsSetting.collectAsState()
    val audioVolume by viewModel.audioVolume.collectAsState()
    val aimSens by viewModel.aimSensitivity.collectAsState()
    val scopeSens by viewModel.scopeSensitivity.collectAsState()
    val gyro by viewModel.gyroscopeEnabled.collectAsState()
    val haptics by viewModel.hapticFeedbackEnabled.collectAsState()

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
                        .testTag("back_to_lobby_settings")
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x9914243B))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "SETTINGS & GRAPHICS ENGINE",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Graphics Section
                item {
                    SettingsCard(title = "GRAPHICS & FRAME RATE") {
                        Text(text = "Quality Preset", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Smooth", "Balanced", "HD (60 FPS)", "HDR", "Ultra (90 FPS)").forEach { preset ->
                                val isSel = graphics == preset
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) ZogamGold else Color(0x3314243B))
                                        .border(1.dp, if (isSel) ZogamGold else ZogamNavyBorder, RoundedCornerShape(6.dp))
                                        .clickable { viewModel.graphicsSetting.value = preset },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = preset.take(7),
                                        color = if (isSel) Color.Black else TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Controls & Sensitivity Section
                item {
                    SettingsCard(title = "SENSITIVITY & GYROSCOPE") {
                        Text(text = "General Camera Sensitivity: ${(aimSens * 100).toInt()}%", color = TextSecondary, fontSize = 12.sp)
                        Slider(
                            value = aimSens,
                            onValueChange = { viewModel.aimSensitivity.value = it },
                            colors = SliderDefaults.colors(thumbColor = ZogamGold, activeTrackColor = ZogamGold)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "ADS Scope Sensitivity: ${(scopeSens * 100).toInt()}%", color = TextSecondary, fontSize = 12.sp)
                        Slider(
                            value = scopeSens,
                            onValueChange = { viewModel.scopeSensitivity.value = it },
                            colors = SliderDefaults.colors(thumbColor = ZogamCyanAccent, activeTrackColor = ZogamCyanAccent)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Gyroscope Aiming", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Switch(
                                checked = gyro,
                                onCheckedChange = { viewModel.gyroscopeEnabled.value = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = ZogamGold, checkedTrackColor = ZogamNavyCard)
                            )
                        }
                    }
                }

                // Audio & Haptics Section
                item {
                    SettingsCard(title = "AUDIO & HAPTIC VIBRATION") {
                        Text(text = "Master Game Volume: ${(audioVolume * 100).toInt()}%", color = TextSecondary, fontSize = 12.sp)
                        Slider(
                            value = audioVolume,
                            onValueChange = { viewModel.audioVolume.value = it },
                            colors = SliderDefaults.colors(thumbColor = ZogamGold, activeTrackColor = ZogamGold)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Tactile Weapon Recoil Haptics", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = "Simulates realistic gunfire vibrations and vehicle rumble", color = TextSecondary, fontSize = 10.sp)
                            }
                            Switch(
                                checked = haptics,
                                onCheckedChange = { viewModel.hapticFeedbackEnabled.value = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = ZogamCyanAccent, checkedTrackColor = ZogamNavyCard)
                            )
                        }
                    }
                }

                // Account & Zogam Identity Section
                item {
                    SettingsCard(title = "ZOGAM ESPORTS ACCOUNT") {
                        Text(text = "Player ID: ${viewModel.playerProfile.value.playerId}", color = ZogamGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(text = "Server Region: Southeast Asia / Indo-Pacific (35ms Ping)", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                        Text(text = "ZOGAM Anti-Cheat Engine: Active & Verified", color = HealthGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ZogamNavySurface)
            .border(1.dp, ZogamNavyBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Text(
            text = title,
            color = ZogamGold,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}
