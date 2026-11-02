package com.example.ui.battle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.model.LootDrop
import com.example.model.WeaponCategory
import com.example.ui.theme.*

@Composable
fun LootProximityOverlay(
    items: List<LootDrop>,
    onPickItem: (LootDrop) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    Box(
        modifier = modifier
            .width(220.dp)
            .heightIn(max = 240.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xDD060E18))
            .border(1.dp, ZogamNavyBorder, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Backpack,
                    contentDescription = null,
                    tint = ZogamGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "GROUND LOOT (${items.size})",
                    color = ZogamGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(items) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("loot_item_${item.id}")
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x9914243B))
                            .border(1.dp, Color(0x3300E5FF), RoundedCornerShape(8.dp))
                            .clickable { onPickItem(item) }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when {
                                item.weapon != null -> Icons.Default.Shield
                                item.isMedical -> Icons.Default.LocalHospital
                                item.gearLevel > 0 -> Icons.Default.Security
                                item.attachment != null -> Icons.Default.Tune
                                else -> Icons.Default.Inbox
                            },
                            contentDescription = null,
                            tint = when {
                                item.weapon != null -> ZogamGold
                                item.isMedical -> HealthGreen
                                item.gearLevel > 0 -> ZogamCyanAccent
                                else -> TextPrimary
                            },
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            if (item.count > 1) {
                                Text(
                                    text = "Quantity: ${item.count}",
                                    color = TextSecondary,
                                    fontSize = 9.sp
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = "Pick Up",
                            tint = ZogamGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
