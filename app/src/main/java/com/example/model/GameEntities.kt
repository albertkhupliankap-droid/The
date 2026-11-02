package com.example.model

import kotlin.math.*

data class Vector2D(val x: Float = 0f, val y: Float = 0f) {
    fun distanceTo(other: Vector2D): Float {
        val dx = x - other.x
        val dy = y - other.y
        return sqrt(dx * dx + dy * dy)
    }

    fun angleTo(other: Vector2D): Float {
        return Math.toDegrees(atan2((other.y - y).toDouble(), (other.x - x).toDouble())).toFloat()
    }

    fun add(other: Vector2D) = Vector2D(x + other.x, y + other.y)
    fun subtract(other: Vector2D) = Vector2D(x - other.x, y - other.y)
    fun multiply(scalar: Float) = Vector2D(x * scalar, y * scalar)

    fun length(): Float = sqrt(x * x + y * y)

    fun normalized(): Vector2D {
        val len = length()
        return if (len > 0.0001f) Vector2D(x / len, y / len) else Vector2D(0f, 0f)
    }
}

data class PlayerEntity(
    val id: String,
    val name: String,
    val isHuman: Boolean = false,
    val isTeammate: Boolean = false,
    var position: Vector2D = Vector2D(1000f, 1000f),
    var velocity: Vector2D = Vector2D(0f, 0f),
    var facingAngle: Float = 0f,
    var health: Float = 100f,
    var maxHealth: Float = 100f,
    var boost: Float = 0f, // 0..100
    var helmetLevel: Int = 0, // 0..3 (reduces headshot dmg)
    var helmetDurability: Float = 100f,
    var vestLevel: Int = 0, // 0..3 (reduces body dmg)
    var vestDurability: Float = 100f,
    var backpackLevel: Int = 1, // 1..3
    var weapons: MutableList<Weapon> = mutableListOf(),
    var currentWeaponIndex: Int = 0,
    var stance: CharacterStance = CharacterStance.STAND,
    var actionState: PlayerActionState = PlayerActionState.IN_PLANE,
    var altitude: Float = 1500f, // For aircraft & skydiving
    var kills: Int = 0,
    var damageDealt: Float = 0f,
    var headshots: Int = 0,
    var isDowned: Boolean = false,
    var bleedoutTimer: Float = 30f,
    var isReviving: Boolean = false,
    var reviveProgress: Float = 0f,
    var vehicleId: String? = null,
    var outfitColor: Long = 0xFF1976D2
) {
    val isAlive: Boolean
        get() = health > 0f && actionState != PlayerActionState.DEAD

    val currentWeapon: Weapon?
        get() = if (weapons.isNotEmpty() && currentWeaponIndex in weapons.indices) weapons[currentWeaponIndex] else null
}

data class VehicleEntity(
    val id: String,
    val type: VehicleType,
    var position: Vector2D,
    var angle: Float = 0f,
    var speedKph: Float = 0f,
    var health: Float = type.maxHealth,
    var fuel: Float = type.fuelCapacity,
    var driverId: String? = null,
    val passengerIds: MutableList<String> = mutableListOf()
) {
    val isDestroyed: Boolean get() = health <= 0f
}

data class LootDrop(
    val id: String,
    val name: String,
    val category: WeaponCategory,
    val position: Vector2D,
    val count: Int = 1,
    val ammoType: AmmoType = AmmoType.NONE,
    val weapon: Weapon? = null,
    val attachment: AttachmentType? = null,
    val gearLevel: Int = 0, // 1, 2, 3 for helmet/vest/pack
    val isMedical: Boolean = false,
    val healAmount: Float = 0f
)

data class AirdropCrate(
    val id: String,
    var position: Vector2D,
    var altitude: Float = 800f,
    var isLanded: Boolean = false,
    val weapons: List<Weapon>,
    val highTierGear: String = "Level 3 Helmet & Ghillie Suit",
    var smokeDurationSeconds: Float = 120f
)

data class Projectile(
    val id: String,
    val shooterId: String,
    var position: Vector2D,
    val velocity: Vector2D,
    val damage: Float,
    val maxDistance: Float,
    var distanceTraveled: Float = 0f,
    val isExplosive: Boolean = false,
    val blastRadius: Float = 0f,
    val isSmoke: Boolean = false
)

data class SmokeCloud(
    val id: String,
    val position: Vector2D,
    var radius: Float = 15f,
    var remainingSeconds: Float = 25f
)

data class StormCircle(
    var currentCenter: Vector2D,
    var currentRadius: Float,
    var targetCenter: Vector2D,
    var targetRadius: Float,
    var shrinkProgress: Float = 0f, // 0..1
    var phase: Int = 1,
    var isShrinking: Boolean = false,
    var timeUntilShrink: Float = 60f,
    var shrinkDuration: Float = 45f,
    var damagePerSecond: Float = 1.5f
)

data class KillFeedItem(
    val id: Long = System.currentTimeMillis(),
    val killerName: String,
    val victimName: String,
    val weaponName: String,
    val isHeadshot: Boolean = false,
    val distanceMeters: Int = 50,
    val isTeammateKill: Boolean = false
)

data class Landmark(
    val name: String,
    val position: Vector2D,
    val description: String,
    val lootTier: String // "Ultra Hot Drop", "High Tier", "Medium"
)
