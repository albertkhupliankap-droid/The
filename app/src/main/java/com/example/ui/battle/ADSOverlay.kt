package com.example.ui.battle

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.AttachmentType
import com.example.ui.theme.ZogamBrightRed
import com.example.ui.theme.ZogamCyanAccent
import com.example.ui.theme.ZogamGold

@Composable
fun ADSOverlay(
    equippedScope: AttachmentType?,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f

            // Vignette dark border for scope housing
            drawCircle(
                color = Color(0xDD050B14),
                radius = size.minDimension * 0.46f,
                center = Offset(cx, cy),
                style = Stroke(width = size.minDimension * 0.35f)
            )

            // Scope metal rim
            drawCircle(
                color = Color(0xFF1E2D42),
                radius = size.minDimension * 0.35f,
                center = Offset(cx, cy),
                style = Stroke(width = 6f)
            )

            when (equippedScope) {
                AttachmentType.SCOPE_8X -> {
                    // 8x Sniper Scope Mil-Dot Reticle
                    // Center fine crosshair
                    drawLine(
                        color = Color.Black,
                        start = Offset(cx - 240f, cy),
                        end = Offset(cx + 240f, cy),
                        strokeWidth = 2.5f
                    )
                    drawLine(
                        color = Color.Black,
                        start = Offset(cx, cy - 240f),
                        end = Offset(cx, cy + 240f),
                        strokeWidth = 2.5f
                    )
                    // Center chevron or red dot
                    drawCircle(color = ZogamBrightRed, radius = 3.5f, center = Offset(cx, cy))

                    // Mil-dot tick marks (elevation and windage)
                    for (i in -4..4) {
                        if (i == 0) continue
                        val offset = i * 40f
                        // Horizontal ticks
                        drawLine(
                            color = Color.Black,
                            start = Offset(cx + offset, cy - 6f),
                            end = Offset(cx + offset, cy + 6f),
                            strokeWidth = 2f
                        )
                        // Vertical ticks
                        drawLine(
                            color = Color.Black,
                            start = Offset(cx - 6f, cy + offset),
                            end = Offset(cx + 6f, cy + offset),
                            strokeWidth = 2f
                        )
                    }

                    // Rangefinder stadiametric markings
                    drawArc(
                        color = Color(0x6600E5FF),
                        startAngle = 135f,
                        sweepAngle = 90f,
                        useCenter = false,
                        topLeft = Offset(cx - 140f, cy - 140f),
                        size = androidx.compose.ui.geometry.Size(280f, 280f),
                        style = Stroke(width = 1.5f)
                    )
                }

                AttachmentType.SCOPE_4X -> {
                    // 4x ACOG Illuminated Chevron Reticle
                    drawLine(
                        color = Color.Black,
                        start = Offset(cx - 180f, cy),
                        end = Offset(cx - 20f, cy),
                        strokeWidth = 3f
                    )
                    drawLine(
                        color = Color.Black,
                        start = Offset(cx + 20f, cy),
                        end = Offset(cx + 180f, cy),
                        strokeWidth = 3f
                    )
                    drawLine(
                        color = Color.Black,
                        start = Offset(cx, cy + 20f),
                        end = Offset(cx, cy + 200f),
                        strokeWidth = 3f
                    )

                    // Glowing Red Chevron (inverted V)
                    drawLine(
                        color = ZogamBrightRed,
                        start = Offset(cx - 12f, cy + 10f),
                        end = Offset(cx, cy),
                        strokeWidth = 4f
                    )
                    drawLine(
                        color = ZogamBrightRed,
                        start = Offset(cx + 12f, cy + 10f),
                        end = Offset(cx, cy),
                        strokeWidth = 4f
                    )

                    // BDC holdover hashmarks
                    listOf(40f, 80f, 130f).forEach { distY ->
                        drawLine(
                            color = Color.Black,
                            start = Offset(cx - 12f, cy + distY),
                            end = Offset(cx + 12f, cy + distY),
                            strokeWidth = 2f
                        )
                    }
                }

                else -> {
                    // Red Dot Sight / 1x Reflex
                    drawCircle(
                        color = Color(0x4400E5FF),
                        radius = 28f,
                        center = Offset(cx, cy),
                        style = Stroke(width = 2f)
                    )
                    // Crisp bright red dot
                    drawCircle(
                        color = ZogamBrightRed,
                        radius = 4.5f,
                        center = Offset(cx, cy)
                    )
                }
            }
        }
    }
}
