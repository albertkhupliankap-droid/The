package com.example.ui.battle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VehicleEntity
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun VehicleControlsOverlay(
    vehicle: VehicleEntity,
    onThrottle: (Boolean) -> Unit,
    onSteer: (Boolean) -> Unit,
    onHorn: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize().padding(16.dp)) {
        // Vehicle Speedometer & Fuel HUD (Top Center)
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xDD0C1827))
                .border(1.dp, ZogamCyanAccent, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = ZogamCyanAccent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${vehicle.speedKph.roundToInt()} KM/H",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
                Text(
                    text = "${vehicle.type.title} • FUEL: ${vehicle.fuel.roundToInt()}%",
                    color = ZogamGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Left Controls: Steering (Left / Right)
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 20.dp, start = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DriveRoundButton(
                icon = Icons.Default.ArrowBack,
                label = "LEFT",
                onClick = { onSteer(true) },
                testTag = "steer_left_button"
            )
            DriveRoundButton(
                icon = Icons.Default.ArrowForward,
                label = "RIGHT",
                onClick = { onSteer(false) },
                testTag = "steer_right_button"
            )
        }

        // Right Controls: Gas Pedal & Brake / Reverse & Horn & Exit
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp, end = 16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Horn
                DriveRoundButton(
                    icon = Icons.Default.VolumeUp,
                    label = "HORN",
                    onClick = onHorn,
                    size = 50,
                    btnColor = ZogamRoyalBlue,
                    testTag = "horn_button"
                )
                // Exit Vehicle Button
                DriveRoundButton(
                    icon = Icons.Default.ExitToApp,
                    label = "EXIT",
                    onClick = onExit,
                    size = 50,
                    btnColor = ZogamCrimsonRed,
                    testTag = "exit_vehicle_button"
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Reverse / Brake
                DriveRoundButton(
                    icon = Icons.Default.ArrowDownward,
                    label = "BRAKE",
                    onClick = { onThrottle(false) },
                    btnColor = Color(0xFFC0392B),
                    testTag = "brake_button"
                )
                // Throttle / Accelerate
                DriveRoundButton(
                    icon = Icons.Default.ArrowUpward,
                    label = "DRIVE",
                    onClick = { onThrottle(true) },
                    btnColor = ZogamGold,
                    iconTint = Color.Black,
                    size = 72,
                    testTag = "gas_pedal_button"
                )
            }
        }
    }
}

@Composable
private fun DriveRoundButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    size: Int = 64,
    btnColor: Color = Color(0x9914243B),
    iconTint: Color = Color.White,
    testTag: String = "drive_button"
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .testTag(testTag)
            .clip(CircleShape)
            .background(btnColor)
            .border(2.dp, Color(0x66FFFFFF), CircleShape)
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
