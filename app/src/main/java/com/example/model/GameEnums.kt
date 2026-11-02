package com.example.model

enum class GameMode(val displayName: String, val description: String, val iconName: String) {
    CLASSIC_BR("Classic BR", "100-Player Traditional Battle Royale", "Military"),
    RANKED_BR("Ranked Royale", "Competitive Tier Matches with RP Points", "Trophy"),
    SQUAD_4P("Squad (4 Players)", "Deploy with 4-player tactical squad", "Groups"),
    DUO_2P("Duo (2 Players)", "Partner up for tactical survival", "Group"),
    SOLO("Solo", "Every soldier for themselves", "Person"),
    TDM_ARENA("Team Deathmatch", "Fast-paced 4v4 respawn combat in Warehouses", "SportsMma"),
    PAYLOAD("Payload Combat", "Heavy rocket launchers and armored vehicles", "Rocket"),
    TRAINING_GROUND("Training Ground", "Shooting range, vehicle track & target dummies", "FitnessCenter")
}

enum class MapType(
    val mapName: String,
    val subtitle: String,
    val sizeKm: String,
    val environment: String,
    val landmarks: List<String>
) {
    ZOGAM_TERRAIN(
        "Zogam Highlands",
        "Home of the Braves",
        "8x8 km",
        "Mountainous pine hills, bridges, deep valleys & rivers",
        listOf("Tonzang Military Base", "Khampat Docks", "Singngat Hills", "Kana River City", "Tuithaphai Bridge", "Lamka Warehouses")
    ),
    DESERT_MIRAMAR(
        "Miramar Badlands",
        "Arid Desert Valley",
        "8x8 km",
        "Rocky canyons, open plateaus, and dusty towns",
        listOf("Los Leones", "Pecado Arena", "El Pozo", "Hacienda del Patrón")
    ),
    SNOW_VIKENDI(
        "Vikendi Tundra",
        "Frozen Pine Forest",
        "6x6 km",
        "Snowfields, frozen lakes, and historic castles",
        listOf("Cosmodrome", "Castle Ruins", "Goroka Lake", "Dino Park")
    ),
    JUNGLE_SANHOK(
        "Sanhok Rainforest",
        "Tropical Dense Canopy",
        "4x4 km",
        "Lush bamboo jungles, ancient ruins, and misty rivers",
        listOf("Bootcamp", "Paradise Resort", "Ruins", "Camp Alpha")
    ),
    URBAN_RUINS(
        "Urban Zone",
        "Close Quarters Warzone",
        "2x2 km",
        "High-density skyscrapers, underground tunnels & plazas",
        listOf("Central Metro", "Hospital Plaza", "Port Cargo", "Broadcast Tower")
    )
}

enum class WeaponCategory(val title: String) {
    ASSAULT_RIFLE("Assault Rifle"),
    SUBMACHINE_GUN("SMG"),
    SNIPER_RIFLE("Sniper"),
    DMR("Marksman Rifle"),
    SHOTGUN("Shotgun"),
    LIGHT_MACHINE_GUN("LMG"),
    PISTOL("Pistol"),
    MELEE("Melee"),
    THROWABLE("Throwable")
}

enum class AmmoType(val label: String, val caliber: String, val weight: Float) {
    AMMO_556("5.56mm", "Green Box", 0.75f),
    AMMO_762("7.62mm", "Orange Box", 0.90f),
    AMMO_9MM("9mm", "Blue Box", 0.35f),
    AMMO_300MAG(".300 Magnum", "Heavy AWM Box", 1.20f),
    AMMO_12GAUGE("12 Gauge", "Red Shells", 1.25f),
    AMMO_45ACP(".45 ACP", "Yellow Box", 0.40f),
    NONE("None", "", 0f)
}

enum class AttachmentSlot {
    SCOPE,
    MUZZLE,
    MAGAZINE,
    FOREGRIP,
    STOCK
}

enum class AttachmentType(val slot: AttachmentSlot, val title: String, val statBonus: String) {
    RED_DOT(AttachmentSlot.SCOPE, "Red Dot Sight", "+Fast Target Acquisition"),
    HOLO_SIGHT(AttachmentSlot.SCOPE, "Holographic 1x", "+Clear Reticle"),
    SCOPE_4X(AttachmentSlot.SCOPE, "4x ACOG Scope", "+400% Zoom Aiming"),
    SCOPE_8X(AttachmentSlot.SCOPE, "8x High Power Scope", "+800% Long Range Sniper Zoom"),
    COMPENSATOR(AttachmentSlot.MUZZLE, "AR Compensator", "-25% Recoil Climb"),
    SUPPRESSOR(AttachmentSlot.MUZZLE, "Tactical Suppressor", "Muffled Gunshot & No Radar Flash"),
    FLASH_HIDER(AttachmentSlot.MUZZLE, "Flash Hider", "-10% Recoil & Zero Muzzle Flash"),
    EXTENDED_MAG(AttachmentSlot.MAGAZINE, "Extended Mag", "+10 Ammo Capacity"),
    QUICKDRAW_MAG(AttachmentSlot.MAGAZINE, "Extended Quickdraw", "+10 Ammo & -30% Reload Time"),
    VERTICAL_GRIP(AttachmentSlot.FOREGRIP, "Vertical Foregrip", "-20% Vertical Recoil"),
    TACTICAL_STOCK(AttachmentSlot.STOCK, "Tactical Stock", "+Stability & Recoil Recovery")
}

enum class VehicleType(
    val title: String,
    val maxSpeedKph: Float,
    val maxHealth: Float,
    val seats: Int,
    val fuelCapacity: Float
) {
    TACTICAL_JEEP("Tactical 4x4 UAZ", 115f, 1000f, 4, 100f),
    DESERT_BUGGY("High-Speed Buggy", 130f, 750f, 2, 80f),
    BATTLE_MOTORCYCLE("Combat Bike", 150f, 500f, 2, 60f),
    ARMORED_TRUCK("Zogam Armored Beast", 95f, 1800f, 6, 120f),
    PATROL_BOAT("River Assault Boat", 85f, 900f, 4, 90f)
}

enum class RankTier(
    val tierName: String,
    val minPoints: Int,
    val badgeColorHex: Long,
    val tierIcon: String
) {
    BRONZE("Bronze", 1200, 0xFFCD7F32, "Shield"),
    SILVER("Silver", 1700, 0xFFC0C0C0, "Shield"),
    GOLD("Gold", 2200, 0xFFFFD700, "MilitaryTech"),
    PLATINUM("Platinum", 2700, 0xFF00CED1, "Verified"),
    DIAMOND("Diamond", 3200, 0xFF00BFFF, "Diamond"),
    CROWN("Crown", 3700, 0xFFFF4500, "WorkspacePremium"),
    ACE("Ace", 4200, 0xFFFF1493, "Star"),
    ACE_MASTER("Ace Master", 4700, 0xFF9932CC, "Stars"),
    ACE_DOMINATOR("Ace Dominator", 5200, 0xFFFF0055, "AutoAwesome"),
    CONQUEROR("Conqueror", 5700, 0xFFFFD700, "EmojiEvents")
}

enum class CharacterStance {
    STAND,
    CROUCH,
    PRONE,
    SPRINT
}

enum class PlayerActionState {
    IN_PLANE,
    FREEFALL,
    PARACHUTING,
    ON_FOOT,
    DRIVING,
    KNOCKED_DOWN,
    DEAD,
    VICTORY
}
