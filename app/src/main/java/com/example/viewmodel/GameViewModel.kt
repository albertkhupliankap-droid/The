package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.game.AudioHaptics
import com.example.game.BattleRoyaleEngine
import com.example.game.MatchEndStats
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ScreenState {
    LOBBY,
    MATCH_PLAYING,
    TRAINING_GROUND,
    ZOGAM_PASS,
    INVENTORY,
    STORE,
    SOCIAL_CLAN,
    SETTINGS
}

class GameViewModel(application: Application) : AndroidViewModel(application) {
    val audioHaptics = AudioHaptics(application.applicationContext)

    // Current navigation state
    private val _currentScreen = MutableStateFlow(ScreenState.LOBBY)
    val currentScreen: StateFlow<ScreenState> = _currentScreen.asStateFlow()

    // Mode and Map selections
    val selectedMode = MutableStateFlow(GameMode.CLASSIC_BR)
    val selectedMap = MutableStateFlow(MapType.ZOGAM_TERRAIN)

    // Player Profile & Meta Data
    private val _playerProfile = MutableStateFlow(PlayerProfile())
    val playerProfile: StateFlow<PlayerProfile> = _playerProfile.asStateFlow()

    // Active Game Engine
    val activeEngine = MutableStateFlow<BattleRoyaleEngine?>(null)

    // Matchmaking modal
    val isMatchmaking = MutableStateFlow(false)
    val matchmakingTimer = MutableStateFlow(0)

    // Zogam Pass Data
    val passTiers = MutableStateFlow<List<ZogamPassTier>>(generatePassTiers())

    // Missions
    val missions = MutableStateFlow<List<MissionItem>>(generateInitialMissions())

    // Friends & Clan
    val friendsList = MutableStateFlow<List<FriendItem>>(generateInitialFriends())
    val clanMembers = MutableStateFlow<List<ClanMember>>(generateInitialClanMembers())

    // Shop Items
    val shopItems = MutableStateFlow<List<ShopItem>>(generateInitialShop())

    // Settings
    val graphicsSetting = MutableStateFlow("HD (60 FPS)")
    val audioVolume = MutableStateFlow(0.85f)
    val aimSensitivity = MutableStateFlow(0.65f)
    val scopeSensitivity = MutableStateFlow(0.50f)
    val gyroscopeEnabled = MutableStateFlow(true)
    val hapticFeedbackEnabled = MutableStateFlow(true)

    fun navigateTo(screen: ScreenState) {
        audioHaptics.playSoundEffect(android.media.ToneGenerator.TONE_PROP_BEEP, 30)
        _currentScreen.value = screen
    }

    fun startMatchmaking() {
        if (selectedMode.value == GameMode.TRAINING_GROUND) {
            startMatchDirect()
            return
        }

        isMatchmaking.value = true
        matchmakingTimer.value = 0
        audioHaptics.playSoundEffect(android.media.ToneGenerator.TONE_PROP_PROMPT, 50)

        viewModelScope.launch {
            for (i in 1..4) {
                kotlinx.coroutines.delay(800)
                matchmakingTimer.value = i
            }
            isMatchmaking.value = false
            startMatchDirect()
        }
    }

    fun cancelMatchmaking() {
        isMatchmaking.value = false
        matchmakingTimer.value = 0
    }

    private fun startMatchDirect() {
        activeEngine.value?.stop()
        val engine = BattleRoyaleEngine(
            audioHaptics = audioHaptics,
            selectedMap = selectedMap.value,
            selectedMode = selectedMode.value
        )
        activeEngine.value = engine
        engine.start()
        _currentScreen.value = ScreenState.MATCH_PLAYING
    }

    fun leaveMatch() {
        activeEngine.value?.stop()
        activeEngine.value = null
        _currentScreen.value = ScreenState.LOBBY
    }

    fun applyMatchStats(stats: MatchEndStats) {
        val curr = _playerProfile.value
        val newRankPoints = (curr.rankPoints + stats.rankPointsGained).coerceAtLeast(1000)
        val newTier = calculateTier(newRankPoints)
        val newCoins = curr.zogamCoins + stats.zogamCoinsEarned
        val newPassLevel = (curr.passLevel + (stats.zogamPassXpEarned / 500)).coerceAtMost(50)

        _playerProfile.value = curr.copy(
            matchesPlayed = curr.matchesPlayed + 1,
            totalWins = if (stats.isVictory) curr.totalWins + 1 else curr.totalWins,
            totalKills = curr.totalKills + stats.kills,
            headshots = curr.headshots + stats.headshots,
            rankPoints = newRankPoints,
            rankTier = newTier,
            zogamCoins = newCoins,
            passLevel = newPassLevel,
            damageDealtTotal = curr.damageDealtTotal + stats.damage
        )
    }

    private fun calculateTier(points: Int): RankTier {
        return when {
            points >= 5700 -> RankTier.CONQUEROR
            points >= 5200 -> RankTier.ACE_DOMINATOR
            points >= 4700 -> RankTier.ACE_MASTER
            points >= 4200 -> RankTier.ACE
            points >= 3700 -> RankTier.CROWN
            points >= 3200 -> RankTier.DIAMOND
            points >= 2700 -> RankTier.PLATINUM
            points >= 2200 -> RankTier.GOLD
            points >= 1700 -> RankTier.SILVER
            else -> RankTier.BRONZE
        }
    }

    fun claimMission(missionId: String) {
        val updated = missions.value.map {
            if (it.id == missionId && it.isCompleted && !it.isClaimed) {
                _playerProfile.value = _playerProfile.value.copy(
                    zogamCoins = _playerProfile.value.zogamCoins + it.coinReward
                )
                audioHaptics.playSoundEffect(android.media.ToneGenerator.TONE_PROP_ACK, 70)
                it.copy(isClaimed = true)
            } else it
        }
        missions.value = updated
    }

    fun unlockElitePass() {
        if (_playerProfile.value.zPoints >= 600) {
            _playerProfile.value = _playerProfile.value.copy(
                zPoints = _playerProfile.value.zPoints - 600,
                isPassEliteUnlocked = true
            )
            audioHaptics.playVictoryHaptic()
        }
    }

    fun equipOutfit(outfitName: String) {
        _playerProfile.value = _playerProfile.value.copy(selectedOutfit = outfitName)
        audioHaptics.playSoundEffect(android.media.ToneGenerator.TONE_PROP_BEEP, 30)
    }

    fun equipWeaponSkin(skinName: String) {
        _playerProfile.value = _playerProfile.value.copy(selectedWeaponSkin = skinName)
        audioHaptics.playSoundEffect(android.media.ToneGenerator.TONE_PROP_BEEP, 30)
    }

    fun purchaseShopItem(item: ShopItem) {
        val prof = _playerProfile.value
        if (item.priceZPoints > 0 && prof.zPoints >= item.priceZPoints) {
            _playerProfile.value = prof.copy(zPoints = prof.zPoints - item.priceZPoints)
            audioHaptics.playVictoryHaptic()
            shopItems.value = shopItems.value.map { if (it.id == item.id) it.copy(isOwned = true) else it }
        } else if (item.priceCoins > 0 && prof.zogamCoins >= item.priceCoins) {
            _playerProfile.value = prof.copy(zogamCoins = prof.zogamCoins - item.priceCoins)
            audioHaptics.playVictoryHaptic()
            shopItems.value = shopItems.value.map { if (it.id == item.id) it.copy(isOwned = true) else it }
        }
    }

    override fun onCleared() {
        super.onCleared()
        activeEngine.value?.stop()
        audioHaptics.release()
    }

    companion object {
        fun generatePassTiers(): List<ZogamPassTier> {
            return (1..50).map { lvl ->
                ZogamPassTier(
                    tier = lvl,
                    freeReward = when (lvl % 5) {
                        0 -> "500 ZG Coins"
                        1 -> "Crate Coupon"
                        2 -> "Silver Scrap x10"
                        3 -> "XP Boost Card"
                        else -> "250 ZG Coins"
                    },
                    freeAmount = "x1",
                    premiumReward = when (lvl) {
                        1 -> "Mythic Zogam Warrior Set"
                        10 -> "Glacial M416 Skin"
                        25 -> "Combat Helicopter Parachute"
                        35 -> "Royal Dragon Pan"
                        50 -> "Apex Conqueror Exosuit"
                        else -> if (lvl % 2 == 0) "100 Z-Points" else "Golden Weapon Crate"
                    },
                    premiumAmount = "x1",
                    isMilestone = lvl % 10 == 0
                )
            }
        }

        fun generateInitialMissions(): List<MissionItem> {
            return listOf(
                MissionItem("m1", "Highland Sharpshooter", "Eliminate 5 enemies with Sniper or DMR in Zogam Highlands", 3, 5, 150, 800, isCompleted = false, isClaimed = false),
                MissionItem("m2", "Adrenaline Rush", "Travel 3000 meters in vehicles across any map", 3000, 3000, 200, 1200, isCompleted = true, isClaimed = false),
                MissionItem("m3", "Tactical Survival", "Survive for more than 15 minutes in Classic BR", 1, 1, 120, 600, isCompleted = true, isClaimed = false),
                MissionItem("m4", "Chicken Dinner Seeker", "Finish in Top 3 in Squad Mode", 1, 1, 250, 1500, isCompleted = true, isClaimed = true),
                MissionItem("m5", "Air Drop Hunter", "Open 2 Air Drop crates in a single match", 1, 2, 180, 950, isCompleted = false, isClaimed = false),
                MissionItem("m6", "Field Medic", "Revive teammates 3 times", 2, 3, 140, 750, isCompleted = false, isClaimed = false)
            )
        }

        fun generateInitialFriends(): List<FriendItem> {
            return listOf(
                FriendItem("f1", "Chin_Captain", RankTier.ACE_DOMINATOR, "In Lobby (Squad 3/4)", true, "CC"),
                FriendItem("f2", "Lamka_Striker", RankTier.ACE, "Playing Classic (12 alive)", true, "LS"),
                FriendItem("f3", "Khampat_Sniper", RankTier.CROWN, "In Lobby (Solo)", true, "KS"),
                FriendItem("f4", "Zomi_Viper", RankTier.DIAMOND, "Online", true, "ZV"),
                FriendItem("f5", "Tonzang_Fox", RankTier.PLATINUM, "Offline 3h ago", false, "TF"),
                FriendItem("f6", "Singngat_Hawk", RankTier.GOLD, "Offline 1d ago", false, "SH")
            )
        }

        fun generateInitialClanMembers(): List<ClanMember> {
            return listOf(
                ClanMember("ZOGAM_COMMANDER", "Leader", 14500, RankTier.ACE),
                ClanMember("Chin_Captain", "Co-Leader", 13200, RankTier.ACE_DOMINATOR),
                ClanMember("Lamka_Striker", "Elite", 9800, RankTier.ACE),
                ClanMember("Mizo_Warrior_99", "Elite", 8400, RankTier.CROWN),
                ClanMember("Khampat_Sniper", "Member", 6200, RankTier.CROWN),
                ClanMember("Tuithaphai_Gunner", "Member", 4500, RankTier.DIAMOND)
            )
        }

        fun generateInitialShop(): List<ShopItem> {
            return listOf(
                ShopItem("s1", "Mythic Royal Zogam Set", "Outfit", priceZPoints = 1200, rarity = "Mythic", description = "Legendary military ceremonial armor with gold trim & cape."),
                ShopItem("s2", "Glacial Dragon M416", "Weapon", priceZPoints = 900, rarity = "Mythic", description = "Frostfire particle effect with custom kill broadcast animation."),
                ShopItem("s3", "Armored Buggy - Desert Phoenix", "Vehicle", priceCoins = 25000, rarity = "Legendary", description = "High-octane golden desert camouflage paint job."),
                ShopItem("s4", "Zogam Crest Parachute", "Parachute", priceCoins = 12000, rarity = "Epic", description = "Vibrant traditional tribal crest parachute canopy."),
                ShopItem("s5", "Cast Iron Gold Pan", "Weapon", priceCoins = 8000, rarity = "Epic", description = "Reflective mirror gold pan with bullet deflection finish.")
            )
        }
    }
}
