package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Vector2D
import com.example.ui.theme.ZogamCyanAccent
import com.example.ui.theme.ZogamGold
import kotlin.math.*

/**
 * Advanced Virtual Joystick for ZOGAM MOBILE (identical to PUBG Mobile).
 * Features 360-degree analog control, dynamic sprint lock track,
 * continuous sprint latching, cardinal degree markers, and visual feedback.
 */
@Composable
fun VirtualJoystick(
    onMove: (Vector2D, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    radiusDp: Float = 65f
) {
    var thumbOffset by remember { mutableStateOf(Offset.Zero) }
    var isSprintLocked by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }

    val sprintLockThresholdRatio = 1.35f // Dragging far up into the sprint corridor

    // Pulse animation when sprint is locked
    val infiniteTransition = rememberInfiniteTransition(label = "sprint_pulse")
    val sprintPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier
            .testTag("virtual_joystick_container")
            .size((radiusDp * 2).dp, (radiusDp * 2.8f).dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Sprint Lock Corridor & Indicator (Upper track)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-4).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSprintLocked) Color(0xFFF1C40F).copy(alpha = sprintPulseAlpha)
                        else Color(0x66000000)
                    )
                    .border(
                        1.dp,
                        if (isSprintLocked) Color(0xFFF1C40F) else Color(0x44FFFFFF),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        // Tap to cancel sprint lock
                        if (isSprintLocked) {
                            isSprintLocked = false
                            thumbOffset = Offset.Zero
                            onMove(Vector2D(0f, 0f), false)
                        }
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isSprintLocked) Icons.Default.Lock else Icons.Default.DirectionsRun,
                        contentDescription = "Sprint",
                        tint = if (isSprintLocked) Color.Black else Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isSprintLocked) "LOCKED" else "SPRINT",
                        color = if (isSprintLocked) Color.Black else Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 9.sp
                    )
                }
            }

            // Upward arrow chevrons towards sprint lock
            if (isDragging && !isSprintLocked) {
                Text(
                    text = "▲",
                    color = Color(0x88F1C40F),
                    fontSize = 11.sp,
                    modifier = Modifier.offset(y = 2.dp)
                )
            }
        }

        // Main Joystick Canvas (Base & Knob)
        Box(
            modifier = Modifier
                .size((radiusDp * 2).dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val drag = offset - center
                            val dist = sqrt(drag.x * drag.x + drag.y * drag.y)
                            val maxR = size.width / 2f

                            val clamped = if (dist > maxR) {
                                Offset(drag.x / dist * maxR, drag.y / dist * maxR)
                            } else {
                                drag
                            }
                            thumbOffset = clamped

                            // Check sprint threshold
                            val isSprint = clamped.y < -maxR * 0.75f
                            onMove(Vector2D(clamped.x / maxR, clamped.y / maxR), isSprint)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val newOffset = thumbOffset + dragAmount
                            val maxR = size.width / 2f

                            // If pulled far up into sprint corridor
                            if (newOffset.y < -maxR * sprintLockThresholdRatio && abs(newOffset.x) < maxR * 0.5f) {
                                isSprintLocked = true
                                thumbOffset = Offset(0f, -maxR)
                                onMove(Vector2D(0f, -1f), true)
                            } else {
                                val dist = sqrt(newOffset.x * newOffset.x + newOffset.y * newOffset.y)
                                thumbOffset = if (dist > maxR) {
                                    Offset(newOffset.x / dist * maxR, newOffset.y / dist * maxR)
                                } else {
                                    newOffset
                                }
                                val isSprint = thumbOffset.y < -maxR * 0.75f || isSprintLocked
                                onMove(Vector2D(thumbOffset.x / maxR, thumbOffset.y / maxR), isSprint)
                            }
                        },
                        onDragEnd = {
                            isDragging = false
                            if (!isSprintLocked) {
                                thumbOffset = Offset.Zero
                                onMove(Vector2D(0f, 0f), false)
                            }
                        },
                        onDragCancel = {
                            isDragging = false
                            if (!isSprintLocked) {
                                thumbOffset = Offset.Zero
                                onMove(Vector2D(0f, 0f), false)
                            }
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val outerRadius = size.width / 2f

                // Outer base circle
                drawCircle(
                    color = Color(0x66000000),
                    radius = outerRadius,
                    center = center
                )
                // Outer ring border
                drawCircle(
                    color = if (isSprintLocked) Color(0xFFF1C40F) else Color(0x55FFFFFF),
                    radius = outerRadius,
                    center = center,
                    style = Stroke(width = if (isSprintLocked) 3.5f else 2f)
                )

                // Inner reference ring (50% radius)
                drawCircle(
                    color = Color(0x22FFFFFF),
                    radius = outerRadius * 0.5f,
                    center = center,
                    style = Stroke(width = 1.2f)
                )

                // 8-Directional Cardinal Tick Marks
                for (angleDeg in 0 until 360 step 45) {
                    val rad = Math.toRadians(angleDeg.toDouble()).toFloat()
                    val cosA = cos(rad)
                    val sinA = sin(rad)
                    val startR = outerRadius * 0.82f
                    val endR = outerRadius * 0.98f

                    drawLine(
                        color = Color(0x44FFFFFF),
                        start = Offset(center.x + cosA * startR, center.y + sinA * startR),
                        end = Offset(center.x + cosA * endR, center.y + sinA * endR),
                        strokeWidth = if (angleDeg % 90 == 0) 2.5f else 1.5f
                    )
                }

                // Upward sprint directional guide track
                val sprintGuidePath = Path().apply {
                    moveTo(center.x - 14f, center.y - outerRadius * 0.3f)
                    lineTo(center.x, center.y - outerRadius * 0.85f)
                    lineTo(center.x + 14f, center.y - outerRadius * 0.3f)
                }
                drawPath(
                    path = sprintGuidePath,
                    color = if (isSprintLocked) Color(0xFFF1C40F).copy(alpha = 0.8f) else Color(0x33FFFFFF),
                    style = Stroke(width = 2f)
                )

                // Inner Thumb Knob (Follows finger)
                val knobCenter = center + thumbOffset
                val knobRadius = outerRadius * 0.36f

                // Knob Shadow
                drawCircle(
                    color = Color(0x88000000),
                    radius = knobRadius + 4f,
                    center = knobCenter + Offset(0f, 3f)
                )

                // Knob Metal Disc
                drawCircle(
                    color = if (isSprintLocked) Color(0xFFF1C40F) else Color(0xFFD4AF37),
                    radius = knobRadius,
                    center = knobCenter
                )
                // Knob Inner Accent Ring
                drawCircle(
                    color = Color(0xFF1E272C),
                    radius = knobRadius * 0.65f,
                    center = knobCenter
                )
                // Center Grip Dot
                drawCircle(
                    color = Color.White,
                    radius = knobRadius * 0.25f,
                    center = knobCenter
                )
            }
        }
    }
}
