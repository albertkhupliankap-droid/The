package com.example.game

import com.example.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*
import kotlin.random.Random

class BattleRoyaleEngine(
    private val audioHaptics: AudioHaptics,
    val selectedMap: MapType = MapType.ZOGAM_TERRAIN,
    val selectedMode: GameMode = GameMode.CLASSIC_BR
) {
    private val engineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    val worldWidth = 3000f
    val worldHeight = 3000f

    // Landmarks
    val landmarks = listOf(
        Landmark("Tonzang Military Base", Vector2D(650f, 650f), "Heavy military loot and fortified bunkers", "Ultra Hot Drop"),
        Landmark("Khampat Docks", Vector2D(2350f, 700f), "Coastal shipping containers & crane towers", "High Tier"),
        Landmark("Singngat Hills", Vector2D(700f, 2250f), "High altitude radar observatory & sniper cliffs", "High Tier"),
        Landmark("Kana River City", Vector2D(1500f, 1500f), "Dense urban skyscrapers & bridges", "Ultra Hot Drop"),
        Landmark("Tuithaphai Bridge", Vector2D(1500f, 2100f), "Strategic crossing bottleneck", "Medium"),
        Landmark("Lamka Warehouses", Vector2D(2200f, 2200f), "Industrial storage & vehicle garages", "High Tier")
    )

    // Player state
    val humanPlayer = PlayerEntity(
        id = "player_human",
        name = "ZOGAM_COMMANDER",
        isHuman = true,
        isTeammate = true,
        position = Vector2D(1500f, 1500f),
        health = 100f,
        maxHealth = 100f,
        weapons = mutableListOf(
            WeaponPresets.createM416(reserve = 180),
            WeaponPresets.createAWM(reserve = 15)
        ),
        currentWeaponIndex = 0,
        helmetLevel = 2,
        vestLevel = 2,
        backpackLevel = 3,
        actionState = PlayerActionState.IN_PLANE,
        altitude = 1500f
    )

    // Squadmates (if team mode)
    val squadmates = listOf(
        PlayerEntity("sq_2", "Alpha-2 (Zogam)", isHuman = false, isTeammate = true, health = 100f, actionState = PlayerActionState.IN_PLANE),
        PlayerEntity("sq_3", "Echo-3 (Sniper)", isHuman = false, isTeammate = true, health = 100f, actionState = PlayerActionState.IN_PLANE),
        PlayerEntity("sq_4", "Ghost-4 (Medic)", isHuman = false, isTeammate = true, health = 100f, actionState = PlayerActionState.IN_PLANE)
    )

    // Bots (96 enemy players)
    val enemyBots = mutableListOf<PlayerEntity>()

    // Vehicles
    val vehicles = mutableListOf<VehicleEntity>()

    // Ground loot
    val lootItems = mutableListOf<LootDrop>()

    // Airdrop
    val airdrops = mutableListOf<AirdropCrate>()

    // Projectiles
    val projectiles = mutableListOf<Projectile>()
    val smokeClouds = mutableListOf<SmokeCloud>()

    // Killfeed
    private val _killFeed = MutableStateFlow<List<KillFeedItem>>(emptyList())
    val killFeed: StateFlow<List<KillFeedItem>> = _killFeed.asStateFlow()

    // Alive players count
    private val _alivePlayersCount = MutableStateFlow(100)
    val alivePlayersCount: StateFlow<Int> = _alivePlayersCount.asStateFlow()

    // Safe & Storm Circle
    var storm = StormCircle(
        currentCenter = Vector2D(1500f, 1500f),
        currentRadius = 1450f,
        targetCenter = Vector2D(1400f, 1400f),
        targetRadius = 900f,
        phase = 1,
        timeUntilShrink = 45f,
        shrinkDuration = 40f
    )

    // Flight path
    val planeStart = Vector2D(200f, 300f)
    val planeEnd = Vector2D(2800f, 2700f)
    var planeCurrentPos = planeStart
    var planeProgress = 0f // 0..1
    var planeAltitude = 1500f
    val planeFlightDuration = 35f // seconds across island

    // Dynamic UI states
    val isAimingDownSights = MutableStateFlow(false)
    val isReloading = MutableStateFlow(false)
    val reloadProgress = MutableStateFlow(0f)
    val currentVehicle = MutableStateFlow<VehicleEntity?>(null)
    val nearbyLoot = MutableStateFlow<List<LootDrop>>(emptyList())
    val cameraRecoilOffset = MutableStateFlow(0f)
    val isMatchFinished = MutableStateFlow(false)
    val matchPlacement = MutableStateFlow(100)
    val matchStats = MutableStateFlow<MatchEndStats?>(null)
    val announcementMessage = MutableStateFlow<String?>(null)

    private var isRunning = false
    private var lastTickTime = System.currentTimeMillis()

    init {
        initializeMatch()
    }

    private fun initializeMatch() {
        // Spawn 96 enemy bots
        val botNames = listOf(
            "Phoenix_99", "ShadowKukri", "TigerStriker", "ZomiViper", "DeltaWolf",
            "Valkyrie7", "IronClad", "PhantomReaper", "ChinCommando", "LamkaSniper",
            "KukiRecon", "MizoWarrior", "HavocMaker", "Spectre44", "BulletProof",
            "ApexPredator", "StormBreaker", "NightStalker", "ThunderBolt", "Warlord_X"
        )

        for (i in 1..96) {
            val name = if (i <= botNames.size) botNames[i - 1] else "Combatant_${Random.nextInt(100, 999)}"
            val bot = PlayerEntity(
                id = "bot_$i",
                name = name,
                isHuman = false,
                isTeammate = false,
                position = Vector2D(
                    Random.nextFloat() * (worldWidth - 400f) + 200f,
                    Random.nextFloat() * (worldHeight - 400f) + 200f
                ),
                health = 100f,
                actionState = PlayerActionState.IN_PLANE,
                altitude = 1500f,
                helmetLevel = Random.nextInt(1, 3),
                vestLevel = Random.nextInt(1, 3),
                weapons = mutableListOf(
                    if (Random.nextBoolean()) WeaponPresets.createAKM() else WeaponPresets.createM416(),
                    WeaponPresets.createVector()
                ),
                currentWeaponIndex = 0
            )
            enemyBots.add(bot)
        }

        // Spawn Vehicles at key roads and landmarks
        val vehicleSpawns = listOf(
            Pair(Vector2D(700f, 800f), VehicleType.TACTICAL_JEEP),
            Pair(Vector2D(2300f, 850f), VehicleType.DESERT_BUGGY),
            Pair(Vector2D(800f, 2200f), VehicleType.BATTLE_MOTORCYCLE),
            Pair(Vector2D(1450f, 1600f), VehicleType.ARMORED_TRUCK),
            Pair(Vector2D(1550f, 2150f), VehicleType.TACTICAL_JEEP),
            Pair(Vector2D(2150f, 2100f), VehicleType.DESERT_BUGGY)
        )

        vehicleSpawns.forEachIndexed { index, pair ->
            vehicles.add(
                VehicleEntity(
                    id = "veh_$index",
                    type = pair.second,
                    position = pair.first,
                    angle = Random.nextFloat() * 360f
                )
            )
        }

        // Spawn Loot drops around landmarks and fields
        spawnMapLoot()
    }

    private fun spawnMapLoot() {
        var lootId = 1
        landmarks.forEach { landmark ->
            // Spawn 15-20 items per landmark
            repeat(18) {
                val offset = Vector2D(
                    (Random.nextFloat() - 0.5f) * 220f,
                    (Random.nextFloat() - 0.5f) * 220f
                )
                val itemPos = landmark.position.add(offset)
                val roll = Random.nextInt(100)
                val item = when {
                    roll < 20 -> LootDrop(
                        id = "loot_${lootId++}",
                        name = "M416 Rifle",
                        category = WeaponCategory.ASSAULT_RIFLE,
                        position = itemPos,
                        weapon = WeaponPresets.createM416(reserve = 60),
                        ammoType = AmmoType.AMMO_556
                    )
                    roll < 35 -> LootDrop(
                        id = "loot_${lootId++}",
                        name = "AKM Heavy",
                        category = WeaponCategory.ASSAULT_RIFLE,
                        position = itemPos,
                        weapon = WeaponPresets.createAKM(reserve = 60),
                        ammoType = AmmoType.AMMO_762
                    )
                    roll < 45 -> LootDrop(
                        id = "loot_${lootId++}",
                        name = "Kar98k Sniper",
                        category = WeaponCategory.SNIPER_RIFLE,
                        position = itemPos,
                        weapon = WeaponPresets.createKar98k(reserve = 20),
                        ammoType = AmmoType.AMMO_762
                    )
                    roll < 55 -> LootDrop(
                        id = "loot_${lootId++}",
                        name = "First Aid Kit",
                        category = WeaponCategory.MELEE,
                        position = itemPos,
                        count = 2,
                        isMedical = true,
                        healAmount = 50f
                    )
                    roll < 65 -> LootDrop(
                        id = "loot_${lootId++}",
                        name = "Level 3 Military Helmet",
                        category = WeaponCategory.MELEE,
                        position = itemPos,
                        gearLevel = 3
                    )
                    roll < 75 -> LootDrop(
                        id = "loot_${lootId++}",
                        name = "Level 3 Tactical Vest",
                        category = WeaponCategory.MELEE,
                        position = itemPos,
                        gearLevel = 3
                    )
                    roll < 85 -> LootDrop(
                        id = "loot_${lootId++}",
                        name = "4x ACOG Scope",
                        category = WeaponCategory.ASSAULT_RIFLE,
                        position = itemPos,
                        attachment = AttachmentType.SCOPE_4X
                    )
                    else -> LootDrop(
                        id = "loot_${lootId++}",
                        name = "5.56mm Ammo (60 rounds)",
                        category = WeaponCategory.ASSAULT_RIFLE,
                        position = itemPos,
                        count = 60,
                        ammoType = AmmoType.AMMO_556
                    )
                }
                lootItems.add(item)
            }
        }
    }

    fun start() {
        isRunning = true
        lastTickTime = System.currentTimeMillis()
        engineScope.launch {
            while (isRunning) {
                val now = System.currentTimeMillis()
                val dt = ((now - lastTickTime) / 1000f).coerceIn(0.005f, 0.05f)
                lastTickTime = now

                updateGame(dt)
                delay(16) // ~60 FPS
            }
        }
    }

    fun stop() {
        isRunning = false
        engineScope.cancel()
    }

    private fun updateGame(dt: Float) {
        if (isMatchFinished.value) return

        // 1. Update Plane & Parachutes
        updatePlaneAndSkydiving(dt)

        // 2. Update Human Movement & Physics
        updateHumanPlayer(dt)

        // 3. Update Squadmates & Bots AI
        updateBotsAndTeammates(dt)

        // 4. Update Vehicles
        updateVehicles(dt)

        // 5. Update Projectiles & Explosions
        updateProjectiles(dt)

        // 6. Update Storm Gas & Safe Zone
        updateStorm(dt)

        // 7. Update Airdrops
        updateAirdrops(dt)

        // 8. Update Smoke Clouds
        updateSmokeClouds(dt)

        // 9. Update Nearby Loot for quick pickup
        updateNearbyLoot()

        // 10. Recoil recovery
        if (cameraRecoilOffset.value > 0.01f) {
            cameraRecoilOffset.value = (cameraRecoilOffset.value - dt * 2.5f).coerceAtLeast(0f)
        }

        // 11. Check win/loss conditions
        checkMatchConditions()
    }

    private fun updatePlaneAndSkydiving(dt: Float) {
        if (planeProgress < 1.0f) {
            planeProgress += dt / planeFlightDuration
            val currentX = planeStart.x + (planeEnd.x - planeStart.x) * planeProgress
            val currentY = planeStart.y + (planeEnd.y - planeStart.y) * planeProgress
            planeCurrentPos = Vector2D(currentX, currentY)

            // Auto-eject human if plane reaches end of flight path
            if (planeProgress >= 0.95f && humanPlayer.actionState == PlayerActionState.IN_PLANE) {
                triggerJump()
            }

            // Eject bots progressively across the path
            enemyBots.filter { it.actionState == PlayerActionState.IN_PLANE }.forEach { bot ->
                if (Random.nextFloat() < dt * 0.15f) {
                    bot.actionState = PlayerActionState.FREEFALL
                    bot.position = planeCurrentPos.add(Vector2D((Random.nextFloat() - 0.5f) * 40f, (Random.nextFloat() - 0.5f) * 40f))
                    bot.altitude = 1400f
                }
            }

            // Squadmates jump alongside human or at halfway
            squadmates.filter { it.actionState == PlayerActionState.IN_PLANE }.forEach { sq ->
                if (humanPlayer.actionState != PlayerActionState.IN_PLANE || planeProgress > 0.45f) {
                    sq.actionState = PlayerActionState.FREEFALL
                    sq.position = planeCurrentPos.add(Vector2D(Random.nextFloat() * 20f, Random.nextFloat() * 20f))
                    sq.altitude = 1400f
                }
            }
        }

        // Process skydiving entities
        val skydivingEntities = listOf(humanPlayer) + squadmates + enemyBots
        skydivingEntities.forEach { p ->
            when (p.actionState) {
                PlayerActionState.FREEFALL -> {
                    p.altitude -= 55f * dt
                    // Forward glide
                    val rad = Math.toRadians(p.facingAngle.toDouble())
                    val glideSpeed = 25f * dt
                    p.position = p.position.add(Vector2D((cos(rad) * glideSpeed).toFloat(), (sin(rad) * glideSpeed).toFloat()))

                    if (p.altitude <= 180f) {
                        p.actionState = PlayerActionState.PARACHUTING
                    }
                }
                PlayerActionState.PARACHUTING -> {
                    p.altitude -= 12f * dt
                    val rad = Math.toRadians(p.facingAngle.toDouble())
                    val glideSpeed = 10f * dt
                    p.position = p.position.add(Vector2D((cos(rad) * glideSpeed).toFloat(), (sin(rad) * glideSpeed).toFloat()))

                    if (p.altitude <= 0f) {
                        p.altitude = 0f
                        p.actionState = PlayerActionState.ON_FOOT
                    }
                }
                else -> {}
            }
        }
    }

    fun triggerJump() {
        if (humanPlayer.actionState == PlayerActionState.IN_PLANE) {
            humanPlayer.actionState = PlayerActionState.FREEFALL
            humanPlayer.position = planeCurrentPos
            humanPlayer.altitude = 1450f
            audioHaptics.playSoundEffect(android.media.ToneGenerator.TONE_PROP_BEEP2, 100)
            showAnnouncement("DEPLOYING: FREEFALL TOWARDS DROP ZONE")
        }
    }

    fun triggerDeployParachute() {
        if (humanPlayer.actionState == PlayerActionState.FREEFALL) {
            humanPlayer.actionState = PlayerActionState.PARACHUTING
            audioHaptics.playSoundEffect(android.media.ToneGenerator.TONE_PROP_ACK, 80)
        }
    }

    private fun updateHumanPlayer(dt: Float) {
        if (!humanPlayer.isAlive) return

        if (humanPlayer.actionState == PlayerActionState.ON_FOOT) {
            // Apply movement speed according to stance and boost
            val baseSpeed = when (humanPlayer.stance) {
                CharacterStance.SPRINT -> 7.0f
                CharacterStance.STAND -> 4.5f
                CharacterStance.CROUCH -> 2.6f
                CharacterStance.PRONE -> 1.2f
            }
            val boostBonus = if (humanPlayer.boost > 60f) 1.2f else 1.0f
            val currentSpeed = baseSpeed * boostBonus * 10f // scaled for world coords

            val moveX = humanPlayer.velocity.x * currentSpeed * dt
            val moveY = humanPlayer.velocity.y * currentSpeed * dt

            val newX = (humanPlayer.position.x + moveX).coerceIn(20f, worldWidth - 20f)
            val newY = (humanPlayer.position.y + moveY).coerceIn(20f, worldHeight - 20f)
            humanPlayer.position = Vector2D(newX, newY)

            // Health regeneration from boost
            if (humanPlayer.boost > 0f) {
                humanPlayer.boost = (humanPlayer.boost - dt * 0.8f).coerceAtLeast(0f)
                if (humanPlayer.health < 100f) {
                    humanPlayer.health = (humanPlayer.health + dt * 1.5f).coerceAtMost(100f)
                }
            }
        } else if (humanPlayer.actionState == PlayerActionState.KNOCKED_DOWN) {
            // Crawl speed
            val crawlSpeed = 15f * dt
            val rad = Math.toRadians(humanPlayer.facingAngle.toDouble())
            humanPlayer.position = humanPlayer.position.add(
                Vector2D((cos(rad) * crawlSpeed * humanPlayer.velocity.length()).toFloat(),
                         (sin(rad) * crawlSpeed * humanPlayer.velocity.length()).toFloat())
            )
            humanPlayer.bleedoutTimer -= dt
            if (humanPlayer.bleedoutTimer <= 0f) {
                humanPlayer.health = 0f
                humanPlayer.actionState = PlayerActionState.DEAD
                handleHumanEliminated("Bleedout in the battlefield")
            }
        }
    }

    private fun updateBotsAndTeammates(dt: Float) {
        val allAliveBots = enemyBots.filter { it.isAlive && it.actionState == PlayerActionState.ON_FOOT }

        allAliveBots.forEach { bot ->
            // AI Behavior:
            // 1. If outside storm, move towards storm target center
            val distToStormCenter = bot.position.distanceTo(storm.targetCenter)
            if (bot.position.distanceTo(storm.currentCenter) > storm.currentRadius - 30f) {
                val dir = storm.targetCenter.subtract(bot.position).normalized()
                bot.facingAngle = Math.toDegrees(atan2(dir.y.toDouble(), dir.x.toDouble())).toFloat()
                bot.position = bot.position.add(dir.multiply(40f * dt))
            } else {
                // 2. Patrol or engage nearby targets
                val distToHuman = bot.position.distanceTo(humanPlayer.position)
                if (humanPlayer.isAlive && humanPlayer.actionState == PlayerActionState.ON_FOOT && distToHuman < 85f) {
                    // Turn to human and fire occasional burst!
                    bot.facingAngle = bot.position.angleTo(humanPlayer.position)
                    if (Random.nextFloat() < dt * 0.85f) {
                        fireBotWeapon(bot, humanPlayer)
                    }
                } else {
                    // Wander or fight other bots
                    if (Random.nextFloat() < dt * 0.1f) {
                        bot.facingAngle += (Random.nextFloat() - 0.5f) * 90f
                    }
                    val rad = Math.toRadians(bot.facingAngle.toDouble())
                    bot.position = bot.position.add(Vector2D((cos(rad) * 20f * dt).toFloat(), (sin(rad) * 20f * dt).toFloat()))
                }
            }

            // Clamp bot to map
            bot.position = Vector2D(
                bot.position.x.coerceIn(20f, worldWidth - 20f),
                bot.position.y.coerceIn(20f, worldHeight - 20f)
            )

            // Random bot-vs-bot skirmishes in the distance to simulate dynamic 100-player BR matches
            if (Random.nextFloat() < dt * 0.08f && allAliveBots.size > 5) {
                simulateBotSkirmish(bot, allAliveBots)
            }
        }

        // Update squadmates
        squadmates.filter { it.isAlive && it.actionState == PlayerActionState.ON_FOOT }.forEach { sq ->
            // Follow human player with tactical formation
            val distToHuman = sq.position.distanceTo(humanPlayer.position)
            if (distToHuman > 15f) {
                val dir = humanPlayer.position.subtract(sq.position).normalized()
                sq.facingAngle = Math.toDegrees(atan2(dir.y.toDouble(), dir.x.toDouble())).toFloat()
                sq.position = sq.position.add(dir.multiply(38f * dt))
            }

            // Shoot enemies near human
            val targetEnemy = allAliveBots.firstOrNull { it.position.distanceTo(sq.position) < 70f }
            if (targetEnemy != null && Random.nextFloat() < dt * 1.2f) {
                fireBotWeapon(sq, targetEnemy)
            }
        }
    }

    private fun fireBotWeapon(shooter: PlayerEntity, target: PlayerEntity) {
        val dist = shooter.position.distanceTo(target.position)
        val hitChance = (0.65f - (dist / 200f)).coerceIn(0.2f, 0.75f)
        if (Random.nextFloat() < hitChance) {
            val baseDmg = Random.nextFloat() * 15f + 12f
            applyDamage(target, baseDmg, shooter.id, shooter.name, "M416", false, dist.toInt())
        }
    }

    private fun simulateBotSkirmish(attacker: PlayerEntity, candidateBots: List<PlayerEntity>) {
        val victim = candidateBots.filter { it.id != attacker.id }.randomOrNull() ?: return
        val isHeadshot = Random.nextBoolean()
        victim.health -= 60f
        if (victim.health <= 0f) {
            victim.health = 0f
            victim.actionState = PlayerActionState.DEAD
            val weapons = listOf("M416", "AKM", "Kar98k", "Vector", "AWM", "S12K", "UMP45")
            addFeedItem(attacker.name, victim.name, weapons.random(), isHeadshot, Random.nextInt(25, 280))
            _alivePlayersCount.value = countAlivePlayers()
        }
    }

    private fun updateVehicles(dt: Float) {
        vehicles.forEach { veh ->
            if (veh.driverId == humanPlayer.id && humanPlayer.actionState == PlayerActionState.DRIVING) {
                // Vehicle driven by human
                if (veh.speedKph > 0.5f || veh.speedKph < -0.5f) {
                    val rad = Math.toRadians(veh.angle.toDouble())
                    val speedMs = (veh.speedKph / 3.6f) * 1.5f // scale factor
                    val moveX = (cos(rad) * speedMs * dt).toFloat()
                    val moveY = (sin(rad) * speedMs * dt).toFloat()

                    veh.position = Vector2D(
                        (veh.position.x + moveX).coerceIn(30f, worldWidth - 30f),
                        (veh.position.y + moveY).coerceIn(30f, worldHeight - 30f)
                    )
                    humanPlayer.position = veh.position

                    // Vehicle collision with bots (roadkill!)
                    enemyBots.filter { it.isAlive && it.position.distanceTo(veh.position) < 8f }.forEach { hitBot ->
                        if (abs(veh.speedKph) > 25f) {
                            val roadkillDmg = abs(veh.speedKph) * 1.8f
                            applyDamage(hitBot, roadkillDmg, humanPlayer.id, humanPlayer.name, "Tactical Vehicle Impact", false, 0)
                            audioHaptics.playHitImpactHaptic()
                        }
                    }

                    // Natural friction decay if no gas pressed
                    veh.speedKph *= (1f - dt * 0.4f)
                }

                // Consume fuel
                if (abs(veh.speedKph) > 5f) {
                    veh.fuel = (veh.fuel - dt * 0.15f).coerceAtLeast(0f)
                    audioHaptics.playVehicleEngineRumble()
                }
            }
        }
    }

    private fun updateProjectiles(dt: Float) {
        val iterator = projectiles.iterator()
        while (iterator.hasNext()) {
            val proj = iterator.next()
            val step = proj.velocity.multiply(dt)
            proj.position = proj.position.add(step)
            proj.distanceTraveled += step.length()

            // Check hit against enemies/players
            val potentialTargets = if (proj.shooterId == humanPlayer.id) {
                enemyBots.filter { it.isAlive }
            } else {
                listOf(humanPlayer) + squadmates.filter { it.isAlive }
            }

            var hitTarget: PlayerEntity? = null
            for (target in potentialTargets) {
                if (target.position.distanceTo(proj.position) < 4.5f) {
                    hitTarget = target
                    break
                }
            }

            if (hitTarget != null) {
                val isHeadshot = Random.nextFloat() < 0.25f // 25% headshot roll on direct hit
                val finalDmg = if (isHeadshot) proj.damage * 2.2f else proj.damage
                val shooterName = if (proj.shooterId == humanPlayer.id) humanPlayer.name else "Enemy"
                val dist = humanPlayer.position.distanceTo(hitTarget.position).toInt()

                applyDamage(hitTarget, finalDmg, proj.shooterId, shooterName, "Bullet", isHeadshot, dist)
                if (proj.shooterId == humanPlayer.id) {
                    if (isHeadshot) {
                        audioHaptics.playHeadshotChime()
                        humanPlayer.headshots++
                    } else {
                        audioHaptics.playHitImpactHaptic()
                    }
                    humanPlayer.damageDealt += finalDmg
                }
                iterator.remove()
                continue
            }

            // Explosive grenade detonation
            if (proj.isExplosive && proj.distanceTraveled >= proj.maxDistance) {
                triggerExplosion(proj.position, proj.blastRadius, proj.damage, proj.shooterId)
                iterator.remove()
                continue
            }

            // Smoke grenade deploy
            if (proj.isSmoke && proj.distanceTraveled >= proj.maxDistance) {
                smokeClouds.add(SmokeCloud("smoke_${System.currentTimeMillis()}", proj.position))
                audioHaptics.playSoundEffect(android.media.ToneGenerator.TONE_PROP_BEEP2, 120)
                iterator.remove()
                continue
            }

            // Out of range
            if (proj.distanceTraveled >= proj.maxDistance) {
                iterator.remove()
            }
        }
    }

    private fun triggerExplosion(pos: Vector2D, radius: Float, damage: Float, shooterId: String) {
        audioHaptics.playHitImpactHaptic()
        audioHaptics.playSoundEffect(android.media.ToneGenerator.TONE_CDMA_ABBR_ALERT, 200)

        val affected = (listOf(humanPlayer) + squadmates + enemyBots).filter { it.isAlive }
        affected.forEach { target ->
            val d = target.position.distanceTo(pos)
            if (d < radius) {
                val dmgRatio = (1f - (d / radius)).coerceIn(0.2f, 1f)
                val finalDmg = damage * dmgRatio
                val shooterName = if (shooterId == humanPlayer.id) humanPlayer.name else "Combatant"
                applyDamage(target, finalDmg, shooterId, shooterName, "M67 Frag Grenade", false, d.toInt())
            }
        }
    }

    private fun applyDamage(
        target: PlayerEntity,
        damage: Float,
        shooterId: String,
        shooterName: String,
        weaponName: String,
        isHeadshot: Boolean,
        distMeters: Int
    ) {
        // Armor reduction
        var effectiveDmg = damage
        if (isHeadshot && target.helmetLevel > 0) {
            val helmetReduction = target.helmetLevel * 0.18f
            effectiveDmg *= (1f - helmetReduction)
        } else if (!isHeadshot && target.vestLevel > 0) {
            val vestReduction = target.vestLevel * 0.15f
            effectiveDmg *= (1f - vestReduction)
        }

        target.health -= effectiveDmg
        if (target.health <= 0f) {
            target.health = 0f
            target.actionState = PlayerActionState.DEAD

            if (shooterId == humanPlayer.id) {
                humanPlayer.kills++
                showAnnouncement("ELIMINATED $target.name WITH $weaponName")
                audioHaptics.playSoundEffect(android.media.ToneGenerator.TONE_PROP_ACK, 90)
            }

            addFeedItem(shooterName, target.name, weaponName, isHeadshot, distMeters)
            _alivePlayersCount.value = countAlivePlayers()

            // Spawn death crate
            lootItems.add(
                LootDrop(
                    id = "crate_${target.id}",
                    name = "${target.name}'s Death Crate",
                    category = WeaponCategory.ASSAULT_RIFLE,
                    position = target.position,
                    count = 90,
                    ammoType = AmmoType.AMMO_556,
                    isMedical = true,
                    healAmount = 50f
                )
            )

            if (target.id == humanPlayer.id) {
                handleHumanEliminated("Eliminated by $shooterName")
            }
        }
    }

    private fun updateStorm(dt: Float) {
        if (!storm.isShrinking) {
            storm.timeUntilShrink -= dt
            if (storm.timeUntilShrink <= 0f) {
                storm.isShrinking = true
                storm.shrinkProgress = 0f
                audioHaptics.playZoneWarning()
                showAnnouncement("WARNING: BLUE ZONE IS CONSTRICTING!")
            }
        } else {
            storm.shrinkProgress += dt / storm.shrinkDuration
            val t = storm.shrinkProgress.coerceIn(0f, 1f)

            val newX = storm.currentCenter.x + (storm.targetCenter.x - storm.currentCenter.x) * t
            val newY = storm.currentCenter.y + (storm.targetCenter.y - storm.currentCenter.y) * t
            storm.currentCenter = Vector2D(newX, newY)
            storm.currentRadius = storm.currentRadius - (storm.currentRadius - storm.targetRadius) * t

            if (t >= 1f) {
                // Next phase
                storm.phase++
                storm.isShrinking = false
                storm.currentRadius = storm.targetRadius
                storm.timeUntilShrink = 45f - (storm.phase * 5f).coerceAtMost(25f)
                storm.damagePerSecond += 1.2f

                // Pick next random safe circle inside current circle
                val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
                val offsetDist = Random.nextFloat() * (storm.currentRadius * 0.4f)
                val nextCenterX = (storm.currentCenter.x + cos(angle) * offsetDist).coerceIn(400f, worldWidth - 400f)
                val nextCenterY = (storm.currentCenter.y + sin(angle) * offsetDist).coerceIn(400f, worldHeight - 400f)
                storm.targetCenter = Vector2D(nextCenterX, nextCenterY)
                storm.targetRadius = (storm.currentRadius * 0.55f).coerceAtLeast(60f)
            }
        }

        // Damage players outside blue zone
        val allAlive = (listOf(humanPlayer) + squadmates + enemyBots).filter { it.isAlive }
        allAlive.forEach { p ->
            val dist = p.position.distanceTo(storm.currentCenter)
            if (dist > storm.currentRadius) {
                p.health -= storm.damagePerSecond * dt
                if (p.isHuman) {
                    audioHaptics.playGunshotHaptic()
                }
                if (p.health <= 0f) {
                    p.health = 0f
                    p.actionState = PlayerActionState.DEAD
                    addFeedItem("The Blue Zone", p.name, "Radiation Storm", false, 0)
                    _alivePlayersCount.value = countAlivePlayers()
                    if (p.isHuman) {
                        handleHumanEliminated("Eliminated outside the Safe Zone")
                    }
                }
            }
        }
    }

    private fun updateAirdrops(dt: Float) {
        // Chance to spawn airdrop
        if (airdrops.isEmpty() && Random.nextFloat() < dt * 0.05f) {
            val dropPos = Vector2D(
                Random.nextFloat() * (worldWidth - 600f) + 300f,
                Random.nextFloat() * (worldHeight - 600f) + 300f
            )
            airdrops.add(
                AirdropCrate(
                    id = "airdrop_${System.currentTimeMillis()}",
                    position = dropPos,
                    altitude = 800f,
                    weapons = listOf(WeaponPresets.createAWM(), WeaponPresets.createM249())
                )
            )
            showAnnouncement("AIR DROP INCOMING: CARGO PLANE HAS DROPPED A SUPPLY CRATE!")
            audioHaptics.playZoneWarning()
        }

        airdrops.forEach { crate ->
            if (!crate.isLanded) {
                crate.altitude -= 40f * dt
                if (crate.altitude <= 0f) {
                    crate.altitude = 0f
                    crate.isLanded = true
                    // Add loot crate on ground
                    lootItems.add(
                        LootDrop(
                            id = "crate_airdrop",
                            name = "AWM + Level 3 Ghillie Care Package",
                            category = WeaponCategory.SNIPER_RIFLE,
                            position = crate.position,
                            count = 1,
                            weapon = WeaponPresets.createAWM(reserve = 25),
                            gearLevel = 3,
                            attachment = AttachmentType.SCOPE_8X
                        )
                    )
                }
            } else {
                crate.smokeDurationSeconds -= dt
            }
        }
    }

    private fun updateSmokeClouds(dt: Float) {
        val iterator = smokeClouds.iterator()
        while (iterator.hasNext()) {
            val sc = iterator.next()
            sc.remainingSeconds -= dt
            sc.radius = (sc.radius + dt * 0.5f).coerceAtMost(22f)
            if (sc.remainingSeconds <= 0f) {
                iterator.remove()
            }
        }
    }

    private fun updateNearbyLoot() {
        val near = lootItems.filter { it.position.distanceTo(humanPlayer.position) < 4.5f }
        nearbyLoot.value = near
    }

    fun pickUpLoot(item: LootDrop) {
        lootItems.remove(item)
        audioHaptics.playSoundEffect(android.media.ToneGenerator.TONE_PROP_BEEP, 40)

        if (item.weapon != null) {
            if (humanPlayer.weapons.size < 2) {
                humanPlayer.weapons.add(item.weapon)
                humanPlayer.currentWeaponIndex = humanPlayer.weapons.size - 1
            } else {
                // Replace currently held weapon
                humanPlayer.weapons[humanPlayer.currentWeaponIndex] = item.weapon
            }
            showAnnouncement("EQUIPPED ${item.weapon.name}")
        } else if (item.isMedical) {
            humanPlayer.health = (humanPlayer.health + item.healAmount).coerceAtMost(100f)
            showAnnouncement("USED MEDICAL: HEALTH RESTORED")
        } else if (item.gearLevel > 0) {
            if (item.name.contains("Helmet")) {
                humanPlayer.helmetLevel = item.gearLevel
                showAnnouncement("EQUIPPED LEVEL ${item.gearLevel} HELMET")
            } else if (item.name.contains("Vest")) {
                humanPlayer.vestLevel = item.gearLevel
                showAnnouncement("EQUIPPED LEVEL ${item.gearLevel} VEST")
            }
        } else if (item.attachment != null) {
            val curr = humanPlayer.currentWeapon
            if (curr != null) {
                val updated = when (item.attachment.slot) {
                    AttachmentSlot.SCOPE -> curr.copy(equippedScope = item.attachment)
                    AttachmentSlot.MUZZLE -> curr.copy(equippedMuzzle = item.attachment)
                    AttachmentSlot.MAGAZINE -> curr.copy(equippedMag = item.attachment)
                    AttachmentSlot.FOREGRIP -> curr.copy(equippedGrip = item.attachment)
                    AttachmentSlot.STOCK -> curr
                }
                humanPlayer.weapons[humanPlayer.currentWeaponIndex] = updated
                showAnnouncement("ATTACHED ${item.attachment.title}")
            }
        }
    }

    fun fireCurrentWeapon() {
        if (!humanPlayer.isAlive || humanPlayer.actionState != PlayerActionState.ON_FOOT) return
        val weapon = humanPlayer.currentWeapon ?: return

        if (weapon.currentAmmo <= 0) {
            reloadWeapon()
            return
        }

        // Decrement ammo
        val updatedWeapon = weapon.copy(currentAmmo = weapon.currentAmmo - 1)
        humanPlayer.weapons[humanPlayer.currentWeaponIndex] = updatedWeapon

        // Recoil kick & audio
        audioHaptics.playGunshotHaptic()
        audioHaptics.playGunfireSound(isSniper = weapon.category == WeaponCategory.SNIPER_RIFLE)
        cameraRecoilOffset.value = (cameraRecoilOffset.value + weapon.actualRecoil * 12f).coerceAtMost(30f)

        // Spawn bullet projectile
        val rad = Math.toRadians(humanPlayer.facingAngle.toDouble())
        val spread = (Random.nextFloat() - 0.5f) * (if (isAimingDownSights.value) 0.02f else 0.08f)
        val finalAngle = rad + spread
        val speed = weapon.bulletSpeed
        val velocity = Vector2D((cos(finalAngle) * speed).toFloat(), (sin(finalAngle) * speed).toFloat())

        projectiles.add(
            Projectile(
                id = "proj_${System.currentTimeMillis()}",
                shooterId = humanPlayer.id,
                position = humanPlayer.position,
                velocity = velocity,
                damage = weapon.baseDamage,
                maxDistance = weapon.effectiveRangeMeters
            )
        )
    }

    private var continuousFireJob: kotlinx.coroutines.Job? = null
    val isHoldingFire = kotlinx.coroutines.flow.MutableStateFlow(false)

    fun startContinuousFire() {
        if (isHoldingFire.value) return
        isHoldingFire.value = true
        continuousFireJob?.cancel()
        continuousFireJob = engineScope.launch {
            while (isHoldingFire.value) {
                val weapon = humanPlayer.currentWeapon
                if (weapon == null || weapon.currentAmmo <= 0 || isReloading.value) {
                    if (weapon != null && weapon.currentAmmo <= 0) reloadWeapon()
                    break
                }
                fireCurrentWeapon()
                if (!weapon.isAuto) break
                val delayMs = (60000L / weapon.fireRateRpm).coerceIn(40L, 500L)
                kotlinx.coroutines.delay(delayMs)
            }
            isHoldingFire.value = false
        }
    }

    fun stopContinuousFire() {
        isHoldingFire.value = false
        continuousFireJob?.cancel()
        continuousFireJob = null
    }

    fun reloadWeapon() {
        val weapon = humanPlayer.currentWeapon ?: return
        if (isReloading.value || weapon.currentAmmo >= weapon.totalCapacity || weapon.reserveAmmo <= 0) return

        isReloading.value = true
        reloadProgress.value = 0f
        audioHaptics.playSoundEffect(android.media.ToneGenerator.TONE_PROP_PROMPT, 60)

        engineScope.launch {
            val totalSteps = 20
            val delayPerStep = (weapon.actualReloadSeconds * 1000f / totalSteps).toLong()
            for (step in 1..totalSteps) {
                delay(delayPerStep)
                reloadProgress.value = step.toFloat() / totalSteps
            }

            val needed = weapon.totalCapacity - weapon.currentAmmo
            val loaded = needed.coerceAtMost(weapon.reserveAmmo)
            val updated = weapon.copy(
                currentAmmo = weapon.currentAmmo + loaded,
                reserveAmmo = weapon.reserveAmmo - loaded
            )
            humanPlayer.weapons[humanPlayer.currentWeaponIndex] = updated
            isReloading.value = false
            reloadProgress.value = 0f
            audioHaptics.playSoundEffect(android.media.ToneGenerator.TONE_PROP_ACK, 50)
        }
    }

    fun throwGrenade(isSmoke: Boolean = false) {
        val rad = Math.toRadians(humanPlayer.facingAngle.toDouble())
        val throwSpeed = 45f
        val velocity = Vector2D((cos(rad) * throwSpeed).toFloat(), (sin(rad) * throwSpeed).toFloat())

        projectiles.add(
            Projectile(
                id = "gren_${System.currentTimeMillis()}",
                shooterId = humanPlayer.id,
                position = humanPlayer.position,
                velocity = velocity,
                damage = if (isSmoke) 0f else 180f,
                maxDistance = 50f,
                isExplosive = !isSmoke,
                blastRadius = 14f,
                isSmoke = isSmoke
            )
        )
        audioHaptics.playSoundEffect(android.media.ToneGenerator.TONE_PROP_BEEP2, 60)
        showAnnouncement(if (isSmoke) "SMOKE GRENADE THROWN" else "FRAG GRENADE THROWN!")
    }

    fun enterOrExitVehicle() {
        if (humanPlayer.actionState == PlayerActionState.DRIVING) {
            // Exit vehicle
            val veh = currentVehicle.value
            if (veh != null) {
                veh.driverId = null
                currentVehicle.value = null
            }
            humanPlayer.actionState = PlayerActionState.ON_FOOT
            humanPlayer.vehicleId = null
            showAnnouncement("EXITED VEHICLE")
        } else {
            // Check nearest vehicle
            val nearest = vehicles.firstOrNull { it.position.distanceTo(humanPlayer.position) < 5.5f }
            if (nearest != null) {
                nearest.driverId = humanPlayer.id
                currentVehicle.value = nearest
                humanPlayer.actionState = PlayerActionState.DRIVING
                humanPlayer.vehicleId = nearest.id
                humanPlayer.position = nearest.position
                showAnnouncement("DRIVING ${nearest.type.title}")
            }
        }
    }

    fun driveThrottle(forward: Boolean) {
        val veh = currentVehicle.value ?: return
        if (veh.fuel <= 0f) return
        val maxSpeed = veh.type.maxSpeedKph
        if (forward) {
            veh.speedKph = (veh.speedKph + 4.5f).coerceAtMost(maxSpeed)
        } else {
            veh.speedKph = (veh.speedKph - 3.5f).coerceAtLeast(-30f)
        }
    }

    fun driveSteer(left: Boolean) {
        val veh = currentVehicle.value ?: return
        val steerDelta = if (left) -4.0f else 4.0f
        veh.angle = (veh.angle + steerDelta) % 360f
        humanPlayer.facingAngle = veh.angle
    }

    fun honkHorn() {
        audioHaptics.playSoundEffect(android.media.ToneGenerator.TONE_CDMA_NETWORK_USA_RINGBACK, 180)
    }

    fun switchWeapon(index: Int) {
        if (index in humanPlayer.weapons.indices) {
            humanPlayer.currentWeaponIndex = index
            audioHaptics.playSoundEffect(android.media.ToneGenerator.TONE_PROP_BEEP, 30)
        }
    }

    fun toggleStance(targetStance: CharacterStance) {
        humanPlayer.stance = if (humanPlayer.stance == targetStance) CharacterStance.STAND else targetStance
    }

    fun useEnergyDrink() {
        humanPlayer.boost = (humanPlayer.boost + 40f).coerceAtMost(100f)
        showAnnouncement("CONSUMED ENERGY DRINK: BOOST INCREASED")
    }

    fun reviveTeammate() {
        val downedSq = squadmates.firstOrNull { it.isDowned && it.position.distanceTo(humanPlayer.position) < 4f }
        if (downedSq != null) {
            downedSq.isDowned = false
            downedSq.health = 45f
            downedSq.actionState = PlayerActionState.ON_FOOT
            showAnnouncement("REVIVED TEAMMATE ${downedSq.name}!")
            audioHaptics.playSoundEffect(android.media.ToneGenerator.TONE_PROP_ACK, 80)
        }
    }

    private fun addFeedItem(killer: String, victim: String, weapon: String, headshot: Boolean, distance: Int) {
        val item = KillFeedItem(
            killerName = killer,
            victimName = victim,
            weaponName = weapon,
            isHeadshot = headshot,
            distanceMeters = distance
        )
        val current = _killFeed.value.toMutableList()
        current.add(0, item)
        if (current.size > 5) current.removeAt(current.size - 1)
        _killFeed.value = current
    }

    private fun countAlivePlayers(): Int {
        var count = 0
        if (humanPlayer.isAlive) count++
        count += squadmates.count { it.isAlive }
        count += enemyBots.count { it.isAlive }
        return count
    }

    private fun handleHumanEliminated(reason: String) {
        val placement = countAlivePlayers() + 1
        matchPlacement.value = placement
        isMatchFinished.value = true
        matchStats.value = MatchEndStats(
            placement = placement,
            isVictory = false,
            kills = humanPlayer.kills,
            damage = humanPlayer.damageDealt.toInt(),
            headshots = humanPlayer.headshots,
            survivalSeconds = ((System.currentTimeMillis() - lastTickTime) / 1000).toInt() + 180,
            rankPointsGained = if (placement <= 10) 24 else -12,
            zogamCoinsEarned = 450 + humanPlayer.kills * 80,
            zogamPassXpEarned = 280 + humanPlayer.kills * 40
        )
    }

    private fun checkMatchConditions() {
        val aliveTotal = countAlivePlayers()
        val onlyTeamAlive = enemyBots.none { it.isAlive } && (humanPlayer.isAlive || squadmates.any { it.isAlive })

        if (onlyTeamAlive || aliveTotal == 1) {
            // WINNER WINNER CHICKEN DINNER! #1 ZOGAM CHAMPION!
            matchPlacement.value = 1
            isMatchFinished.value = true
            audioHaptics.playVictoryHaptic()
            matchStats.value = MatchEndStats(
                placement = 1,
                isVictory = true,
                kills = humanPlayer.kills,
                damage = humanPlayer.damageDealt.toInt(),
                headshots = humanPlayer.headshots,
                survivalSeconds = 480,
                rankPointsGained = 48,
                zogamCoinsEarned = 1800 + humanPlayer.kills * 120,
                zogamPassXpEarned = 600 + humanPlayer.kills * 60
            )
        }
    }

    private fun showAnnouncement(msg: String) {
        announcementMessage.value = msg
        engineScope.launch {
            delay(2800)
            if (announcementMessage.value == msg) {
                announcementMessage.value = null
            }
        }
    }
}

data class MatchEndStats(
    val placement: Int,
    val isVictory: Boolean,
    val kills: Int,
    val damage: Int,
    val headshots: Int,
    val survivalSeconds: Int,
    val rankPointsGained: Int,
    val zogamCoinsEarned: Int,
    val zogamPassXpEarned: Int
)
