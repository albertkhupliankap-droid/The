package com.example.game.engine3d

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.game.BattleRoyaleEngine
import com.example.model.*
import kotlin.math.*

/**
 * High-Performance 3D Third-Person Perspective (TPP) / FPP Engine for ZOGAM MOBILE.
 * Features true 3D camera projection, 3D terrain receding to horizon, 3D skybox,
 * 3D buildings, 3D trees, 3D vehicles, 3D player soldier model with animated running/firing,
 * 3D enemy combatants, 3D bullet tracers, and 3D airdrop care packages.
 */
class Renderer3D {

    data class Point3D(val x: Float, val y: Float, val z: Float)

    data class ScreenPoint(val x: Float, val y: Float, val depth: Float, val scale: Float)

    // Project world point (X, Y, Z) into 2D screen coordinates relative to camera
    fun project(
        worldPt: Point3D,
        camPos: Point3D,
        camYawRad: Float,
        camPitchRad: Float,
        fovFactor: Float,
        screenWidth: Float,
        screenHeight: Float
    ): ScreenPoint? {
        val dx = worldPt.x - camPos.x
        val dy = worldPt.y - camPos.y
        val dz = worldPt.z - camPos.z

        // Rotate around Yaw (horizontal angle)
        // Note: in 2D world, X is east, Y is south.
        val cosY = cos(-camYawRad)
        val sinY = sin(-camYawRad)
        val rx = dx * cosY - dy * sinY
        val forward = dx * sinY + dy * cosY

        if (forward <= 0.8f) return null // Behind camera clipping plane

        // Rotate around Pitch (vertical tilt)
        val cosP = cos(camPitchRad)
        val sinP = sin(camPitchRad)
        val rz = dz * cosP - forward * sinP
        val depth = forward * cosP + dz * sinP

        if (depth <= 0.8f) return null

        val scale = (fovFactor / depth) * (screenWidth / 640f)
        val sx = (screenWidth / 2f) + rx * scale
        val sy = (screenHeight / 2f) - rz * scale

        return ScreenPoint(sx, sy, depth, scale)
    }

    fun renderScene(
        drawScope: DrawScope,
        engine: BattleRoyaleEngine,
        camYaw: Float,
        camPitch: Float,
        isADS: Boolean,
        freeLookOffsetYaw: Float = 0f,
        walkAnimProgress: Float = 0f,
        isFiring: Boolean = false
    ) {
        val width = drawScope.size.width
        val height = drawScope.size.height
        val player = engine.humanPlayer

        val effectiveYaw = (camYaw + freeLookOffsetYaw) % 360f
        val yawRad = Math.toRadians(effectiveYaw.toDouble()).toFloat()
        val pitchRad = camPitch.coerceIn(-0.35f, 0.45f)

        val fov = if (isADS) 1150f else 620f
        val camDist = if (isADS) 6f else 28f
        val camHeight = if (player.stance == CharacterStance.PRONE) 4.5f else if (player.stance == CharacterStance.CROUCH) 10f else 16f

        // Camera position in 3D world space
        val camX = player.position.x - sin(yawRad) * camDist
        val camY = player.position.y - cos(yawRad) * camDist
        val camZ = camHeight
        val camPos = Point3D(camX, camY, camZ)

        // 1. Draw 3D Skybox & Atmosphere
        val horizonY = (height / 2f) + tan(pitchRad) * fov * 0.8f
        drawSkybox(drawScope, width, height, horizonY, engine.selectedMap)

        // 2. Draw 3D Terrain Plane & Grid
        drawTerrain(drawScope, width, height, horizonY, engine.selectedMap)

        // 3. Draw Safe Zone & Storm Blue Zone in 3D
        drawStorm3D(drawScope, engine, camPos, yawRad, pitchRad, fov, width, height)

        // 4. Collect & Sort 3D World Objects by depth (Painter's algorithm)
        val renderObjects = mutableListOf<Renderable3D>()

        // Add 3D Landmarks & Buildings
        engine.landmarks.forEach { lm ->
            val dist = player.position.distanceTo(lm.position)
            if (dist < 350f) {
                renderObjects.add(RenderableBuilding(lm.position, lm.name, dist))
            }
        }

        // Add 3D Trees
        getProceduralTrees(engine).forEach { treePos ->
            val d = player.position.distanceTo(treePos)
            if (d < 220f) {
                renderObjects.add(RenderableTree(treePos, d))
            }
        }

        // Add 3D Vehicles
        engine.vehicles.forEach { veh ->
            val d = player.position.distanceTo(veh.position)
            if (d < 250f) {
                renderObjects.add(RenderableVehicle(veh, d))
            }
        }

        // Add 3D Ground Loot
        engine.lootItems.forEach { loot ->
            val d = player.position.distanceTo(loot.position)
            if (d < 60f) {
                renderObjects.add(RenderableLoot(loot, d))
            }
        }

        // Add 3D Airdrop Care Packages
        engine.airdrops.forEach { crate ->
            val d = player.position.distanceTo(crate.position)
            renderObjects.add(RenderableAirdrop(crate, d))
        }

        // Add 3D Smoke Clouds
        engine.smokeClouds.forEach { sc ->
            val d = player.position.distanceTo(sc.position)
            if (d < 200f) {
                renderObjects.add(RenderableSmoke(sc, d))
            }
        }

        // Add 3D Enemy Combatants & Squadmates
        val allSoldiers = (listOf(player) + engine.squadmates + engine.enemyBots).filter { it.isAlive }
        allSoldiers.forEach { s ->
            if (s.id == player.id) {
                // If not in ADS mode, render player soldier in 3D Third-Person View!
                if (!isADS && s.actionState != PlayerActionState.IN_PLANE) {
                    renderObjects.add(RenderablePlayerSoldier(s, 0f, isLocalPlayer = true))
                }
            } else {
                val d = player.position.distanceTo(s.position)
                if (d < 300f && s.actionState != PlayerActionState.IN_PLANE) {
                    renderObjects.add(RenderablePlayerSoldier(s, d, isLocalPlayer = false))
                }
            }
        }

        // Add 3D Projectiles / Tracers
        engine.projectiles.forEach { p ->
            val d = player.position.distanceTo(p.position)
            if (d < 350f) {
                renderObjects.add(RenderableProjectile(p, d))
            }
        }

        // Sort far to near
        renderObjects.sortByDescending { it.sortDistance }

        // Render each 3D entity
        renderObjects.forEach { obj ->
            obj.draw(drawScope, this, camPos, yawRad, pitchRad, fov, width, height, walkAnimProgress, isFiring)
        }

        // 5. If ADS mode (First-Person Aiming), render the detailed Weapon in hands!
        if (isADS && player.actionState == PlayerActionState.ON_FOOT) {
            drawFirstPersonWeapon(drawScope, width, height, player.currentWeapon, isFiring)
        }
    }

    private fun drawSkybox(
        drawScope: DrawScope,
        width: Float,
        height: Float,
        horizonY: Float,
        mapType: MapType
    ) {
        val skyTopColor = when (mapType) {
            MapType.DESERT_MIRAMAR -> Color(0xFFF39C12)
            MapType.SNOW_VIKENDI -> Color(0xFF7F8C8D)
            MapType.JUNGLE_SANHOK -> Color(0xFF16A085)
            else -> Color(0xFF1B3A60) // Zogam Highlands Twilight Blue
        }
        val skyBottomColor = when (mapType) {
            MapType.DESERT_MIRAMAR -> Color(0xFFFDEBD0)
            MapType.SNOW_VIKENDI -> Color(0xFFE5E8E8)
            MapType.JUNGLE_SANHOK -> Color(0xFFA2D9CE)
            else -> Color(0xFF85A7CE)
        }

        // Draw sky
        val skyRectHeight = horizonY.coerceAtLeast(0f)
        drawScope.drawRect(
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                listOf(skyTopColor, skyBottomColor),
                startY = 0f,
                endY = skyRectHeight
            ),
            topLeft = Offset.Zero,
            size = Size(width, skyRectHeight)
        )

        // Distant Mountain Silhouettes along the horizon
        val mountainPath = Path().apply {
            moveTo(0f, horizonY)
            var curX = 0f
            while (curX < width) {
                val step = width / 8f
                val peakHeight = 25f + sin(curX * 0.015f) * 18f
                lineTo(curX + step / 2f, horizonY - peakHeight)
                lineTo(curX + step, horizonY)
                curX += step
            }
            lineTo(width, horizonY + 20f)
            lineTo(0f, horizonY + 20f)
            close()
        }
        drawScope.drawPath(path = mountainPath, color = Color(0x772C3E50))

        // Sun / Moon flare
        drawScope.drawCircle(
            color = Color(0x66FFF5CC),
            radius = 35f,
            center = Offset(width * 0.75f, horizonY * 0.45f)
        )
    }

    private fun drawTerrain(
        drawScope: DrawScope,
        width: Float,
        height: Float,
        horizonY: Float,
        mapType: MapType
    ) {
        val groundColor = when (mapType) {
            MapType.DESERT_MIRAMAR -> Color(0xFFD4A373)
            MapType.SNOW_VIKENDI -> Color(0xFFD5DBDB)
            MapType.JUNGLE_SANHOK -> Color(0xFF2E7D32)
            MapType.URBAN_RUINS -> Color(0xFF4A4E53)
            else -> Color(0xFF3B6E47) // Zogam highland terrain
        }
        val groundDarkColor = when (mapType) {
            MapType.DESERT_MIRAMAR -> Color(0xFFB07D4C)
            MapType.SNOW_VIKENDI -> Color(0xFFBDC3C7)
            MapType.JUNGLE_SANHOK -> Color(0xFF1B5E20)
            MapType.URBAN_RUINS -> Color(0xFF2E3136)
            else -> Color(0xFF2A4E33)
        }

        // Draw ground from horizon to bottom of screen
        val groundHeight = (height - horizonY).coerceAtLeast(0f)
        drawScope.drawRect(
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                listOf(groundDarkColor, groundColor),
                startY = horizonY,
                endY = height
            ),
            topLeft = Offset(0f, horizonY),
            size = Size(width, groundHeight)
        )

        // 3D Perspective Grid lines (tactical ground perspective)
        val numLines = 9
        for (i in 0..numLines) {
            val t = i.toFloat() / numLines
            val screenBottomX = width * t
            val horizonX = (width / 2f) + (screenBottomX - width / 2f) * 0.08f
            drawScope.drawLine(
                color = Color(0x18000000),
                start = Offset(horizonX, horizonY),
                end = Offset(screenBottomX, height),
                strokeWidth = 1.5f
            )
        }
        // Horizontal depth lines
        listOf(0.1f, 0.22f, 0.38f, 0.58f, 0.82f).forEach { depthFactor ->
            val y = horizonY + groundHeight * depthFactor
            drawScope.drawLine(
                color = Color(0x15000000),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.5f
            )
        }
    }

    private fun drawStorm3D(
        drawScope: DrawScope,
        engine: BattleRoyaleEngine,
        camPos: Point3D,
        camYawRad: Float,
        camPitchRad: Float,
        fov: Float,
        width: Float,
        height: Float
    ) {
        val storm = engine.storm
        // Project center
        val centerPt = project(
            Point3D(storm.currentCenter.x, storm.currentCenter.y, 0f),
            camPos, camYawRad, camPitchRad, fov, width, height
        ) ?: return

        val radiusScale = centerPt.scale * storm.currentRadius
        if (radiusScale > 5f) {
            // Giant energy storm cylinder base
            drawScope.drawOval(
                color = Color(0x4400B4D8),
                topLeft = Offset(centerPt.x - radiusScale, centerPt.y - radiusScale * 0.3f),
                size = Size(radiusScale * 2f, radiusScale * 0.6f),
                style = Stroke(width = 4f)
            )
        }
    }

    private fun getProceduralTrees(engine: BattleRoyaleEngine): List<Vector2D> {
        val list = mutableListOf<Vector2D>()
        // Scatter static trees around map compounds
        val seeds = listOf(
            Vector2D(600f, 600f), Vector2D(720f, 680f), Vector2D(640f, 750f),
            Vector2D(1400f, 1420f), Vector2D(1580f, 1460f), Vector2D(1520f, 1580f),
            Vector2D(2250f, 680f), Vector2D(2400f, 760f), Vector2D(750f, 2200f),
            Vector2D(1500f, 2050f), Vector2D(1480f, 2180f), Vector2D(2180f, 2150f)
        )
        seeds.forEach { s ->
            for (dx in -3..3) {
                for (dy in -3..3) {
                    if ((dx + dy) % 2 == 0) {
                        list.add(Vector2D(s.x + dx * 28f, s.y + dy * 28f))
                    }
                }
            }
        }
        return list
    }

    private fun drawFirstPersonWeapon(
        drawScope: DrawScope,
        width: Float,
        height: Float,
        weapon: Weapon?,
        isFiring: Boolean
    ) {
        val cx = width / 2f
        val bottomY = height

        // 3D Weapon receiver & barrel extending from bottom-center
        val recoilY = if (isFiring) -12f else 0f
        val recoilX = if (isFiring) (Math.random().toFloat() - 0.5f) * 6f else 0f

        // Receiver Body
        val weaponPath = Path().apply {
            moveTo(cx + 40f + recoilX, bottomY)
            lineTo(cx + 25f + recoilX, bottomY - 140f + recoilY)
            lineTo(cx - 25f + recoilX, bottomY - 140f + recoilY)
            lineTo(cx - 60f + recoilX, bottomY)
            close()
        }
        drawScope.drawPath(path = weaponPath, color = Color(0xFF1E272C))

        // Top Tactical Rail & Optical Scope
        val scopeWidth = 50f
        val scopeHeight = 35f
        drawScope.drawRoundRect(
            color = Color(0xFF2C3E50),
            topLeft = Offset(cx - scopeWidth / 2f + recoilX, bottomY - 175f + recoilY),
            size = Size(scopeWidth, scopeHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )

        // Barrel and flash hider
        drawScope.drawRect(
            color = Color(0xFF151B1E),
            topLeft = Offset(cx - 6f + recoilX, bottomY - 210f + recoilY),
            size = Size(12f, 35f)
        )

        // Muzzle Flash when firing in ADS!
        if (isFiring) {
            val flashCenter = Offset(cx + recoilX, bottomY - 220f + recoilY)
            drawScope.drawCircle(color = Color(0xFFFFCC00), radius = 28f, center = flashCenter)
            drawScope.drawCircle(color = Color(0xFFFF3300), radius = 16f, center = flashCenter)
            drawScope.drawCircle(color = Color.White, radius = 8f, center = flashCenter)
        }
    }
}

// Renderable 3D entities
interface Renderable3D {
    val sortDistance: Float
    fun draw(
        drawScope: DrawScope,
        renderer: Renderer3D,
        camPos: Renderer3D.Point3D,
        camYawRad: Float,
        camPitchRad: Float,
        fov: Float,
        width: Float,
        height: Float,
        walkAnim: Float,
        isFiring: Boolean
    )
}

class RenderableBuilding(val pos: Vector2D, val name: String, override val sortDistance: Float) : Renderable3D {
    override fun draw(
        drawScope: DrawScope,
        renderer: Renderer3D,
        camPos: Renderer3D.Point3D,
        camYawRad: Float,
        camPitchRad: Float,
        fov: Float,
        width: Float,
        height: Float,
        walkAnim: Float,
        isFiring: Boolean
    ) {
        val basePt = renderer.project(Renderer3D.Point3D(pos.x, pos.y, 0f), camPos, camYawRad, camPitchRad, fov, width, height) ?: return
        val topPt = renderer.project(Renderer3D.Point3D(pos.x, pos.y, 25f), camPos, camYawRad, camPitchRad, fov, width, height) ?: return

        val bWidth = basePt.scale * 45f
        val bHeight = (basePt.y - topPt.y).coerceAtLeast(10f)

        // Building Walls (Military Concrete Barracks)
        drawScope.drawRect(
            color = Color(0xFF4A5568),
            topLeft = Offset(basePt.x - bWidth / 2f, topPt.y),
            size = Size(bWidth, bHeight)
        )
        // Roof Trim
        drawScope.drawRect(
            color = Color(0xFF2D3748),
            topLeft = Offset(basePt.x - bWidth / 2f - 4f, topPt.y - 6f),
            size = Size(bWidth + 8f, 6f)
        )
        // Windows & Door
        if (bWidth > 20f) {
            val winSize = bWidth * 0.18f
            drawScope.drawRect(
                color = Color(0xFF1A202C),
                topLeft = Offset(basePt.x - bWidth * 0.35f, topPt.y + bHeight * 0.3f),
                size = Size(winSize, winSize)
            )
            drawScope.drawRect(
                color = Color(0xFF1A202C),
                topLeft = Offset(basePt.x + bWidth * 0.17f, topPt.y + bHeight * 0.3f),
                size = Size(winSize, winSize)
            )
        }
    }
}

class RenderableTree(val pos: Vector2D, override val sortDistance: Float) : Renderable3D {
    override fun draw(
        drawScope: DrawScope,
        renderer: Renderer3D,
        camPos: Renderer3D.Point3D,
        camYawRad: Float,
        camPitchRad: Float,
        fov: Float,
        width: Float,
        height: Float,
        walkAnim: Float,
        isFiring: Boolean
    ) {
        val basePt = renderer.project(Renderer3D.Point3D(pos.x, pos.y, 0f), camPos, camYawRad, camPitchRad, fov, width, height) ?: return
        val topPt = renderer.project(Renderer3D.Point3D(pos.x, pos.y, 22f), camPos, camYawRad, camPitchRad, fov, width, height) ?: return

        val tHeight = (basePt.y - topPt.y).coerceAtLeast(8f)
        val trunkW = (basePt.scale * 3.5f).coerceAtLeast(2f)

        // Trunk
        drawScope.drawRect(
            color = Color(0xFF5D4037),
            topLeft = Offset(basePt.x - trunkW / 2f, topPt.y + tHeight * 0.45f),
            size = Size(trunkW, tHeight * 0.55f)
        )

        // Foliage (Pine Tree Triangles in 3D)
        val foliageR = basePt.scale * 16f
        val treePath = Path().apply {
            moveTo(basePt.x, topPt.y)
            lineTo(basePt.x - foliageR, topPt.y + tHeight * 0.6f)
            lineTo(basePt.x + foliageR, topPt.y + tHeight * 0.6f)
            close()
        }
        drawScope.drawPath(path = treePath, color = Color(0xFF1B4D3E))
    }
}

class RenderableVehicle(val veh: VehicleEntity, override val sortDistance: Float) : Renderable3D {
    override fun draw(
        drawScope: DrawScope,
        renderer: Renderer3D,
        camPos: Renderer3D.Point3D,
        camYawRad: Float,
        camPitchRad: Float,
        fov: Float,
        width: Float,
        height: Float,
        walkAnim: Float,
        isFiring: Boolean
    ) {
        val p = renderer.project(Renderer3D.Point3D(veh.position.x, veh.position.y, 0f), camPos, camYawRad, camPitchRad, fov, width, height) ?: return
        val vW = p.scale * 24f
        val vH = p.scale * 14f

        val bodyColor = when (veh.type) {
            VehicleType.TACTICAL_JEEP -> Color(0xFF2C3E50)
            VehicleType.DESERT_BUGGY -> Color(0xFFD35400)
            VehicleType.BATTLE_MOTORCYCLE -> Color(0xFFC0392B)
            else -> Color(0xFF34495E)
        }

        // 3D Chassis
        drawScope.drawRoundRect(
            color = bodyColor,
            topLeft = Offset(p.x - vW / 2f, p.y - vH),
            size = Size(vW, vH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
        // Wheels
        val wheelR = vH * 0.35f
        drawScope.drawCircle(color = Color.Black, radius = wheelR, center = Offset(p.x - vW * 0.35f, p.y))
        drawScope.drawCircle(color = Color.Black, radius = wheelR, center = Offset(p.x + vW * 0.35f, p.y))
    }
}

class RenderableLoot(val loot: LootDrop, override val sortDistance: Float) : Renderable3D {
    override fun draw(
        drawScope: DrawScope,
        renderer: Renderer3D,
        camPos: Renderer3D.Point3D,
        camYawRad: Float,
        camPitchRad: Float,
        fov: Float,
        width: Float,
        height: Float,
        walkAnim: Float,
        isFiring: Boolean
    ) {
        val p = renderer.project(Renderer3D.Point3D(loot.position.x, loot.position.y, 0.5f), camPos, camYawRad, camPitchRad, fov, width, height) ?: return
        val lootColor = when {
            loot.weapon != null -> Color(0xFFFFD700)
            loot.isMedical -> Color(0xFF2ED573)
            loot.gearLevel > 0 -> Color(0xFF00E5FF)
            else -> Color.White
        }
        val sizePx = (p.scale * 5f).coerceIn(3f, 14f)
        drawScope.drawCircle(color = lootColor, radius = sizePx, center = Offset(p.x, p.y))
        drawScope.drawCircle(color = Color.White, radius = sizePx * 0.5f, center = Offset(p.x, p.y))
    }
}

class RenderableAirdrop(val crate: AirdropCrate, override val sortDistance: Float) : Renderable3D {
    override fun draw(
        drawScope: DrawScope,
        renderer: Renderer3D,
        camPos: Renderer3D.Point3D,
        camYawRad: Float,
        camPitchRad: Float,
        fov: Float,
        width: Float,
        height: Float,
        walkAnim: Float,
        isFiring: Boolean
    ) {
        val z = crate.altitude * 0.1f
        val p = renderer.project(Renderer3D.Point3D(crate.position.x, crate.position.y, z), camPos, camYawRad, camPitchRad, fov, width, height) ?: return

        val cSize = (p.scale * 12f).coerceAtLeast(6f)
        // Red Care Package
        drawScope.drawRect(
            color = Color(0xFFE74C3C),
            topLeft = Offset(p.x - cSize / 2f, p.y - cSize),
            size = Size(cSize, cSize)
        )
        // Blue Tarpaulin Top
        drawScope.drawRect(
            color = Color(0xFF2980B9),
            topLeft = Offset(p.x - cSize / 2f, p.y - cSize),
            size = Size(cSize, cSize * 0.3f)
        )

        // Parachute if still in air
        if (!crate.isLanded) {
            val chuteR = cSize * 1.8f
            drawScope.drawOval(
                color = Color(0xFFF1C40F),
                topLeft = Offset(p.x - chuteR, p.y - cSize * 2.2f),
                size = Size(chuteR * 2f, chuteR)
            )
        } else {
            // Red Smoke Flare rising into sky
            drawScope.drawCircle(
                color = Color(0x88FF1744),
                radius = cSize * 1.5f,
                center = Offset(p.x, p.y - cSize * 2f)
            )
        }
    }
}

class RenderableSmoke(val sc: SmokeCloud, override val sortDistance: Float) : Renderable3D {
    override fun draw(
        drawScope: DrawScope,
        renderer: Renderer3D,
        camPos: Renderer3D.Point3D,
        camYawRad: Float,
        camPitchRad: Float,
        fov: Float,
        width: Float,
        height: Float,
        walkAnim: Float,
        isFiring: Boolean
    ) {
        val p = renderer.project(Renderer3D.Point3D(sc.position.x, sc.position.y, 4f), camPos, camYawRad, camPitchRad, fov, width, height) ?: return
        val r = p.scale * sc.radius
        if (r > 2f) {
            drawScope.drawCircle(color = Color(0x99CCCCCC), radius = r, center = Offset(p.x, p.y))
        }
    }
}

class RenderableProjectile(val proj: Projectile, override val sortDistance: Float) : Renderable3D {
    override fun draw(
        drawScope: DrawScope,
        renderer: Renderer3D,
        camPos: Renderer3D.Point3D,
        camYawRad: Float,
        camPitchRad: Float,
        fov: Float,
        width: Float,
        height: Float,
        walkAnim: Float,
        isFiring: Boolean
    ) {
        val p = renderer.project(Renderer3D.Point3D(proj.position.x, proj.position.y, 8f), camPos, camYawRad, camPitchRad, fov, width, height) ?: return
        val tracerColor = if (proj.isExplosive) Color(0xFFFF1744) else Color(0xFFFFD700)
        drawScope.drawCircle(color = tracerColor, radius = 3.5f, center = Offset(p.x, p.y))
    }
}

/**
 * 3D Animated Soldier Character (Player in Third-Person or Enemy/Squadmate)
 */
class RenderablePlayerSoldier(
    val player: PlayerEntity,
    override val sortDistance: Float,
    val isLocalPlayer: Boolean
) : Renderable3D {
    override fun draw(
        drawScope: DrawScope,
        renderer: Renderer3D,
        camPos: Renderer3D.Point3D,
        camYawRad: Float,
        camPitchRad: Float,
        fov: Float,
        width: Float,
        height: Float,
        walkAnim: Float,
        isFiring: Boolean
    ) {
        val groundZ = 0f
        val headZ = if (player.stance == CharacterStance.PRONE) 3f else if (player.stance == CharacterStance.CROUCH) 8f else 14f

        val basePt = renderer.project(Renderer3D.Point3D(player.position.x, player.position.y, groundZ), camPos, camYawRad, camPitchRad, fov, width, height) ?: return
        val headPt = renderer.project(Renderer3D.Point3D(player.position.x, player.position.y, headZ), camPos, camYawRad, camPitchRad, fov, width, height) ?: return

        val sHeight = (basePt.y - headPt.y).coerceAtLeast(8f)
        val sWidth = sHeight * 0.42f
        val cx = basePt.x
        val cy = basePt.y

        val outfitColor = if (isLocalPlayer) Color(0xFF1E3A5F) else if (player.isTeammate) Color(0xFF009688) else Color(0xFF8B0000)

        // Running cycle stride offset
        val stride = sin(walkAnim * 12f) * (sWidth * 0.3f)

        if (player.stance != CharacterStance.PRONE) {
            // Legs (Left & Right)
            val legW = sWidth * 0.28f
            val legH = sHeight * 0.45f
            drawScope.drawRoundRect(
                color = Color(0xFF1F2937),
                topLeft = Offset(cx - sWidth * 0.35f + stride, cy - legH),
                size = Size(legW, legH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
            )
            drawScope.drawRoundRect(
                color = Color(0xFF1F2937),
                topLeft = Offset(cx + sWidth * 0.07f - stride, cy - legH),
                size = Size(legW, legH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
            )

            // Torso / Tactical Combat Vest
            val torsoH = sHeight * 0.40f
            drawScope.drawRoundRect(
                color = outfitColor,
                topLeft = Offset(cx - sWidth * 0.42f, cy - legH - torsoH),
                size = Size(sWidth * 0.84f, torsoH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )

            // Armor Plate (Level 2/3 Vest)
            drawScope.drawRoundRect(
                color = Color(0xFF111827),
                topLeft = Offset(cx - sWidth * 0.32f, cy - legH - torsoH * 0.85f),
                size = Size(sWidth * 0.64f, torsoH * 0.7f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
            )

            // Head & Spetsnaz Helmet
            val headRadius = sHeight * 0.16f
            val headCenter = Offset(cx, cy - legH - torsoH - headRadius * 0.7f)
            drawScope.drawCircle(color = Color(0xFF374151), radius = headRadius, center = headCenter)
            // Visor gold/black slit
            drawScope.drawRoundRect(
                color = Color(0xFFFFD700),
                topLeft = Offset(headCenter.x - headRadius * 0.6f, headCenter.y - headRadius * 0.2f),
                size = Size(headRadius * 1.2f, headRadius * 0.35f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(1f, 1f)
            )

            // Held Weapon (Assault Rifle)
            val gunLength = sWidth * 0.95f
            val gunY = cy - legH - torsoH * 0.5f
            drawScope.drawLine(
                color = Color(0xFF111111),
                start = Offset(cx - gunLength * 0.3f, gunY),
                end = Offset(cx + gunLength * 0.7f, gunY),
                strokeWidth = sHeight * 0.08f
            )

            // Muzzle flash on gun tip when firing
            if (isFiring && isLocalPlayer) {
                val tip = Offset(cx + gunLength * 0.75f, gunY)
                drawScope.drawCircle(color = Color(0xFFFFD700), radius = sHeight * 0.18f, center = tip)
                drawScope.drawCircle(color = Color(0xFFFF5722), radius = sHeight * 0.10f, center = tip)
            }
        } else {
            // Prone Stance: lying flat on the 3D ground!
            drawScope.drawOval(
                color = outfitColor,
                topLeft = Offset(cx - sWidth * 0.9f, cy - sHeight * 0.3f),
                size = Size(sWidth * 1.8f, sHeight * 0.3f)
            )
            drawScope.drawCircle(
                color = Color(0xFF374151),
                radius = sHeight * 0.12f,
                center = Offset(cx + sWidth * 0.8f, cy - sHeight * 0.15f)
            )
        }

        // Distance & Health Overhead Indicator (For enemy combatants)
        if (!isLocalPlayer && !player.isTeammate && sortDistance > 10f) {
            val tagY = headPt.y - 12f
            val barW = (sWidth * 1.2f).coerceIn(24f, 45f)
            val hpRatio = (player.health / player.maxHealth).coerceIn(0f, 1f)
            drawScope.drawRect(
                color = Color.Black,
                topLeft = Offset(cx - barW / 2f, tagY),
                size = Size(barW, 4f)
            )
            drawScope.drawRect(
                color = Color(0xFFE53935),
                topLeft = Offset(cx - barW / 2f, tagY),
                size = Size(barW * hpRatio, 4f)
            )
        }
    }
}
