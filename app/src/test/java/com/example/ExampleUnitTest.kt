package com.example

import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testVector2DMath() {
        val v1 = Vector2D(0f, 0f)
        val v2 = Vector2D(3f, 4f)
        assertEquals(5f, v1.distanceTo(v2), 0.001f)
        assertEquals(5f, v2.length(), 0.001f)

        val norm = v2.normalized()
        assertEquals(0.6f, norm.x, 0.001f)
        assertEquals(0.8f, norm.y, 0.001f)
    }

    @Test
    fun testWeaponRecoilAndAttachments() {
        val baseM4 = WeaponPresets.createM416()
        val compensatedM4 = baseM4.copy(
            equippedMuzzle = AttachmentType.COMPENSATOR,
            equippedGrip = AttachmentType.VERTICAL_GRIP
        )
        assertTrue(compensatedM4.actualRecoil < baseM4.actualRecoil)
    }

    @Test
    fun testZogamPassTierGeneration() {
        val tiers = com.example.viewmodel.GameViewModel.generatePassTiers()
        assertEquals(50, tiers.size)
        assertEquals(1, tiers.first().tier)
        assertEquals(50, tiers.last().tier)
    }

    @Test
    fun testPlayerProfileStats() {
        val profile = PlayerProfile(
            matchesPlayed = 10,
            totalWins = 2,
            totalKills = 40,
            headshots = 10
        )
        assertEquals(20f, profile.winRatePercent, 0.01f)
        assertEquals(5f, profile.kdRatio, 0.01f)
        assertEquals(25f, profile.headshotPercentage, 0.01f)
    }
}
