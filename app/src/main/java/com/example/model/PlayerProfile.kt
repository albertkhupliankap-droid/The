package com.example.model

data class PlayerProfile(
    val playerId: String = "ZG_77921",
    var username: String = "ZOGAM_COMMANDER",
    var rankTier: RankTier = RankTier.ACE,
    var rankPoints: Int = 4350,
    var level: Int = 48,
    var levelXp: Int = 6800,
    var nextLevelXp: Int = 8500,
    var zogamCoins: Int = 48500, // Free currency
    var zPoints: Int = 1250,    // Premium UC currency
    var clanName: String = "CHIN_KUKI_MIZO_WARRIORS",
    var clanTag: String = "[ZOGAM]",
    var clanLevel: Int = 5,
    // Lifetime Stats
    var matchesPlayed: Int = 342,
    var totalWins: Int = 78,
    var topTens: Int = 195,
    var totalKills: Int = 1420,
    var headshots: Int = 412,
    var highestKills: Int = 19,
    var damageDealtTotal: Long = 215400L,
    // Customization selections
    var selectedOutfit: String = "Royal Zogam Commander",
    var selectedWeaponSkin: String = "Glacial M416 Gold",
    var selectedParachute: String = "Zogam Phoenix Crest",
    var selectedEmote: String = "Zogam Victory Salute",
    var isPassEliteUnlocked: Boolean = true,
    var passLevel: Int = 32
) {
    val winRatePercent: Float
        get() = if (matchesPlayed > 0) (totalWins.toFloat() / matchesPlayed * 100f) else 0f

    val kdRatio: Float
        get() = if (matchesPlayed - totalWins > 0) (totalKills.toFloat() / (matchesPlayed - totalWins)) else totalKills.toFloat()

    val headshotPercentage: Float
        get() = if (totalKills > 0) (headshots.toFloat() / totalKills * 100f) else 0f
}

data class ZogamPassTier(
    val tier: Int,
    val freeReward: String,
    val freeAmount: String,
    val isFreeClaimed: Boolean = false,
    val premiumReward: String,
    val premiumAmount: String,
    val isPremiumClaimed: Boolean = false,
    val isMilestone: Boolean = false
)

data class MissionItem(
    val id: String,
    val title: String,
    val description: String,
    val progress: Int,
    val maxProgress: Int,
    val xpReward: Int,
    val coinReward: Int,
    val isCompleted: Boolean,
    val isClaimed: Boolean
)

data class FriendItem(
    val id: String,
    val name: String,
    val rank: RankTier,
    val status: String, // "In Lobby", "Playing Classic - 14 alive", "Offline 2h"
    val isOnline: Boolean,
    val avatarInitials: String
)

data class ClanMember(
    val name: String,
    val role: String, // "Leader", "Co-Leader", "Elite", "Member"
    val seasonPoints: Int,
    val tier: RankTier
)

data class ShopItem(
    val id: String,
    val name: String,
    val category: String, // "Outfit", "Weapon", "Vehicle", "Parachute", "Crate"
    val priceZPoints: Int = 0,
    val priceCoins: Int = 0,
    val rarity: String, // "Mythic", "Legendary", "Epic"
    val description: String,
    val isOwned: Boolean = false
)
