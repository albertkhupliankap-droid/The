package com.example.model

data class Weapon(
    val id: String,
    val name: String,
    val category: WeaponCategory,
    val ammoType: AmmoType,
    val baseDamage: Float,
    val fireRateRpm: Int, // rounds per minute
    val currentAmmo: Int,
    val magCapacity: Int,
    val reserveAmmo: Int,
    val reloadTimeSeconds: Float,
    val bulletSpeed: Float,
    val recoilFactor: Float,
    val effectiveRangeMeters: Float,
    val isAuto: Boolean,
    val skinName: String = "Standard Military",
    val equippedScope: AttachmentType? = null,
    val equippedMuzzle: AttachmentType? = null,
    val equippedMag: AttachmentType? = null,
    val equippedGrip: AttachmentType? = null
) {
    val totalCapacity: Int
        get() = if (equippedMag == AttachmentType.EXTENDED_MAG || equippedMag == AttachmentType.QUICKDRAW_MAG) {
            magCapacity + 10
        } else {
            magCapacity
        }

    val actualRecoil: Float
        get() {
            var factor = recoilFactor
            if (equippedMuzzle == AttachmentType.COMPENSATOR) factor *= 0.75f
            if (equippedGrip == AttachmentType.VERTICAL_GRIP) factor *= 0.80f
            return factor
        }

    val actualReloadSeconds: Float
        get() = if (equippedMag == AttachmentType.QUICKDRAW_MAG) reloadTimeSeconds * 0.7f else reloadTimeSeconds
}

object WeaponPresets {
    fun createM416(reserve: Int = 120, skin: String = "Glacier Gold") = Weapon(
        id = "m416",
        name = "M416 Tactical",
        category = WeaponCategory.ASSAULT_RIFLE,
        ammoType = AmmoType.AMMO_556,
        baseDamage = 41f,
        fireRateRpm = 700,
        currentAmmo = 30,
        magCapacity = 30,
        reserveAmmo = reserve,
        reloadTimeSeconds = 2.1f,
        bulletSpeed = 880f,
        recoilFactor = 0.35f,
        effectiveRangeMeters = 300f,
        isAuto = true,
        skinName = skin,
        equippedScope = AttachmentType.RED_DOT
    )

    fun createAKM(reserve: Int = 90, skin: String = "Hellfire Crimson") = Weapon(
        id = "akm",
        name = "AKM Heavy",
        category = WeaponCategory.ASSAULT_RIFLE,
        ammoType = AmmoType.AMMO_762,
        baseDamage = 48f,
        fireRateRpm = 600,
        currentAmmo = 30,
        magCapacity = 30,
        reserveAmmo = reserve,
        reloadTimeSeconds = 2.3f,
        bulletSpeed = 715f,
        recoilFactor = 0.55f,
        effectiveRangeMeters = 250f,
        isAuto = true,
        skinName = skin
    )

    fun createAWM(reserve: Int = 20, skin: String = "Royal Zogam Dragon") = Weapon(
        id = "awm",
        name = "AWM Monster Sniper",
        category = WeaponCategory.SNIPER_RIFLE,
        ammoType = AmmoType.AMMO_300MAG,
        baseDamage = 105f,
        fireRateRpm = 35,
        currentAmmo = 5,
        magCapacity = 5,
        reserveAmmo = reserve,
        reloadTimeSeconds = 3.6f,
        bulletSpeed = 945f,
        recoilFactor = 0.90f,
        effectiveRangeMeters = 900f,
        isAuto = false,
        skinName = skin,
        equippedScope = AttachmentType.SCOPE_8X,
        equippedMuzzle = AttachmentType.SUPPRESSOR
    )

    fun createKar98k(reserve: Int = 40) = Weapon(
        id = "kar98k",
        name = "Kar98k Marksman",
        category = WeaponCategory.SNIPER_RIFLE,
        ammoType = AmmoType.AMMO_762,
        baseDamage = 79f,
        fireRateRpm = 40,
        currentAmmo = 5,
        magCapacity = 5,
        reserveAmmo = reserve,
        reloadTimeSeconds = 3.8f,
        bulletSpeed = 760f,
        recoilFactor = 0.80f,
        effectiveRangeMeters = 600f,
        isAuto = false,
        equippedScope = AttachmentType.SCOPE_4X
    )

    fun createVector(reserve: Int = 100) = Weapon(
        id = "vector",
        name = "Vector SMG",
        category = WeaponCategory.SUBMACHINE_GUN,
        ammoType = AmmoType.AMMO_9MM,
        baseDamage = 31f,
        fireRateRpm = 1100,
        currentAmmo = 19,
        magCapacity = 19,
        reserveAmmo = reserve,
        reloadTimeSeconds = 1.8f,
        bulletSpeed = 400f,
        recoilFactor = 0.20f,
        effectiveRangeMeters = 100f,
        isAuto = true,
        equippedScope = AttachmentType.RED_DOT
    )

    fun createUMP45(reserve: Int = 90) = Weapon(
        id = "ump45",
        name = "UMP45 Striker",
        category = WeaponCategory.SUBMACHINE_GUN,
        ammoType = AmmoType.AMMO_45ACP,
        baseDamage = 38f,
        fireRateRpm = 650,
        currentAmmo = 25,
        magCapacity = 25,
        reserveAmmo = reserve,
        reloadTimeSeconds = 2.0f,
        bulletSpeed = 400f,
        recoilFactor = 0.28f,
        effectiveRangeMeters = 150f,
        isAuto = true
    )

    fun createS12K(reserve: Int = 30) = Weapon(
        id = "s12k",
        name = "S12K Semi-Auto Shotgun",
        category = WeaponCategory.SHOTGUN,
        ammoType = AmmoType.AMMO_12GAUGE,
        baseDamage = 198f, // multi-pellet
        fireRateRpm = 250,
        currentAmmo = 5,
        magCapacity = 5,
        reserveAmmo = reserve,
        reloadTimeSeconds = 2.4f,
        bulletSpeed = 350f,
        recoilFactor = 0.85f,
        effectiveRangeMeters = 50f,
        isAuto = false
    )

    fun createM249(reserve: Int = 150) = Weapon(
        id = "m249",
        name = "M249 Heavy Saw LMG",
        category = WeaponCategory.LIGHT_MACHINE_GUN,
        ammoType = AmmoType.AMMO_556,
        baseDamage = 45f,
        fireRateRpm = 800,
        currentAmmo = 100,
        magCapacity = 100,
        reserveAmmo = reserve,
        reloadTimeSeconds = 6.2f,
        bulletSpeed = 915f,
        recoilFactor = 0.60f,
        effectiveRangeMeters = 400f,
        isAuto = true
    )

    fun createDesertEagle(reserve: Int = 35) = Weapon(
        id = "deagle",
        name = "Desert Eagle .45",
        category = WeaponCategory.PISTOL,
        ammoType = AmmoType.AMMO_45ACP,
        baseDamage = 62f,
        fireRateRpm = 300,
        currentAmmo = 7,
        magCapacity = 7,
        reserveAmmo = reserve,
        reloadTimeSeconds = 1.6f,
        bulletSpeed = 450f,
        recoilFactor = 0.70f,
        effectiveRangeMeters = 80f,
        isAuto = false
    )

    fun createTacticalPan() = Weapon(
        id = "pan",
        name = "Cast Iron Tactical Pan",
        category = WeaponCategory.MELEE,
        ammoType = AmmoType.NONE,
        baseDamage = 80f,
        fireRateRpm = 90,
        currentAmmo = 1,
        magCapacity = 1,
        reserveAmmo = 0,
        reloadTimeSeconds = 0f,
        bulletSpeed = 0f,
        recoilFactor = 0f,
        effectiveRangeMeters = 2f,
        isAuto = false
    )

    fun createFragGrenade(count: Int = 2) = Weapon(
        id = "grenade_frag",
        name = "M67 Fragmentation Grenade",
        category = WeaponCategory.THROWABLE,
        ammoType = AmmoType.NONE,
        baseDamage = 150f,
        fireRateRpm = 60,
        currentAmmo = count,
        magCapacity = count,
        reserveAmmo = 0,
        reloadTimeSeconds = 1.0f,
        bulletSpeed = 25f,
        recoilFactor = 0f,
        effectiveRangeMeters = 40f,
        isAuto = false
    )

    fun createSmokeGrenade(count: Int = 2) = Weapon(
        id = "grenade_smoke",
        name = "M18 Smoke Screen Grenade",
        category = WeaponCategory.THROWABLE,
        ammoType = AmmoType.NONE,
        baseDamage = 0f,
        fireRateRpm = 60,
        currentAmmo = count,
        magCapacity = count,
        reserveAmmo = 0,
        reloadTimeSeconds = 1.0f,
        bulletSpeed = 25f,
        recoilFactor = 0f,
        effectiveRangeMeters = 40f,
        isAuto = false
    )
}
