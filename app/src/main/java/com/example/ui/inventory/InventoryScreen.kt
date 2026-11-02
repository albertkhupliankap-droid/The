package com.example.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.ui.components.ZogamTacticalButton
import com.example.ui.theme.*
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.ScreenState

data class LockerItem(
    val id: String,
    val name: String,
    val category: String, // "Outfit", "Weapon", "Helmet", "Parachute"
    val rarity: String,
    val isEquipped: Boolean
)

@Composable
fun InventoryScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.playerProfile.collectAsState()
    var selectedCategory by remember { mutableStateOf("Outfit") }

    val outfitList = remember(profile.selectedOutfit) {
        listOf(
            LockerItem("o1", "Royal Zogam Commander", "Outfit", "Mythic", profile.selectedOutfit == "Royal Zogam Commander"),
            LockerItem("o2", "Ghillie Woodland Suit", "Outfit", "Legendary", profile.selectedOutfit == "Ghillie Woodland Suit"),
            LockerItem("o3", "Urban Commando Tactical", "Outfit", "Epic", profile.selectedOutfit == "Urban Commando Tactical"),
            LockerItem("o4", "Chin Tribal Warrior", "Outfit", "Epic", profile.selectedOutfit == "Chin Tribal Warrior"),
            LockerItem("o5", "Cyber Vanguard Exosuit", "Outfit", "Mythic", profile.selectedOutfit == "Cyber Vanguard Exosuit"),
            LockerItem("o6", "Standard Combat Fatigues", "Outfit", "Rare", profile.selectedOutfit == "Standard Combat Fatigues")
        )
    }

    val weaponList = remember(profile.selectedWeaponSkin) {
        listOf(
            LockerItem("w1", "Glacial M416 Gold", "Weapon", "Mythic", profile.selectedWeaponSkin == "Glacial M416 Gold"),
            LockerItem("w2", "Hellfire Crimson AKM", "Weapon", "Legendary", profile.selectedWeaponSkin == "Hellfire Crimson AKM"),
            LockerItem("w3", "Royal Dragon AWM", "Weapon", "Mythic", profile.selectedWeaponSkin == "Royal Dragon AWM"),
            LockerItem("w4", "Golden Cast Iron Pan", "Weapon", "Epic", profile.selectedWeaponSkin == "Golden Cast Iron Pan"),
            LockerItem("w5", "Carbon Fiber Vector", "Weapon", "Rare", profile.selectedWeaponSkin == "Carbon Fiber Vector")
        )
    }

    val displayedList = if (selectedCategory == "Outfit") outfitList else weaponList

    Box(modifier = modifier.fillMaxSize().background(ZogamNavyDark)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.navigateTo(ScreenState.LOBBY) },
                        modifier = Modifier
                            .testTag("back_to_lobby_inv")
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x9914243B))
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "LOCKER & INVENTORY",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Outfit", "Weapon", "Helmet", "Parachute").forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) ZogamGold else Color(0x3314243B))
                            .clickable { selectedCategory = cat },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat.uppercase(),
                            color = if (isSelected) Color.Black else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Locker Items Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayedList) { item ->
                    LockerCard(
                        item = item,
                        onEquip = {
                            if (item.category == "Outfit") {
                                viewModel.equipOutfit(item.name)
                            } else {
                                viewModel.equipWeaponSkin(item.name)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LockerCard(
    item: LockerItem,
    onEquip: () -> Unit
) {
    val rarityColor = when (item.rarity) {
        "Mythic" -> ZogamCrimsonRed
        "Legendary" -> ZogamAmberGold
        "Epic" -> Color(0xFF9B51E0)
        else -> ZogamCyanAccent
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(ZogamNavySurface)
            .border(
                width = if (item.isEquipped) 2.dp else 1.dp,
                color = if (item.isEquipped) ZogamGold else ZogamNavyBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(rarityColor.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.rarity.uppercase(),
                        color = rarityColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 9.sp
                    )
                }

                if (item.isEquipped) {
                    Text(
                        text = "EQUIPPED",
                        color = ZogamGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.category == "Outfit") Icons.Default.Person else Icons.Default.Shield,
                    contentDescription = null,
                    tint = rarityColor,
                    modifier = Modifier.size(44.dp)
                )
            }

            Text(
                text = item.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (!item.isEquipped) {
                Button(
                    onClick = onEquip,
                    colors = ButtonDefaults.buttonColors(containerColor = ZogamNavyCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ZogamGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("EQUIP", color = ZogamGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
