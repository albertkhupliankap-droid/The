# ZOGAM MOBILE - Unity Universal Render Pipeline (URP) Project Setup

This directory contains the production-grade Unity 3D project structure for **ZOGAM MOBILE**, configured specifically for AAA mobile battle royale performance using Unity's **Universal Render Pipeline (URP)** and targeting 60/90 FPS on Android devices.

---

## 1. Project Directory Structure

```text
/unity/
├── Packages/
│   └── manifest.json             # URP, Cinemachine, Input System, Netcode, NavMesh
├── ProjectSettings/
│   ├── ProjectSettings.asset     # Android package name, Vulkan/GLES3, IL2CPP, ARM64
│   ├── QualitySettings.asset     # 5 mobile tiers: Smooth, Balanced, HD, HDR, Ultra
│   └── GraphicsSettings.asset    # Links Zogam URP Pipeline Asset as active SRP
└── Assets/
    ├── Settings/
    │   ├── ZogamURP_MobilePipelineAsset.asset  # Mobile URP pipeline config (SRP Batcher, MSAA 2x, HDR)
    │   └── ZogamURP_MobileRenderer.asset       # Forward renderer for tile-based mobile GPUs
    └── Scripts/
        ├── Optimization/
        │   └── MobileURPOptimizer.cs           # Target 60/90 FPS, Dynamic resolution, Thermal control
        ├── Player/
        │   ├── PlayerController.cs             # TPP character movement, sprint lock, stances, skydiving
        │   └── PlayerWeaponManager.cs          # Ballistics, recoil patterns, ADS optics, and inventory
        ├── Camera/
        │   └── TPPCameraController.cs          # Over-the-shoulder camera, collision avoidance, freelook
        ├── Combat/
        │   └── BattleRoyaleGameManager.cs      # 100-player matches, contracting Blue Zone, airdrops
        └── Vehicles/
            └── VehicleController.cs            # 4-wheel arcade physics for UAZ 4x4 Jeep / Buggy
```

---

## 2. Universal Render Pipeline (URP) Configuration Highlights

The URP configuration (`ZogamURP_MobilePipelineAsset.asset`) is tuned for mobile battery life and high frame-rates:

1. **SRP Batcher (`m_UseSRPBatcher: 1`):**
   - Drastically reduces CPU draw-call overhead on Mali, Adreno, and PowerVR mobile chipsets.
2. **Forward Rendering (`m_RenderingMode: 0`):**
   - Ideal for mobile tile-based deferred renderers (TBDR), avoiding large memory bandwidth penalties from multi-render target G-buffers.
3. **Optimized Shadows:**
   - 2-4 cascade shadow map (75m distance on HD, 120m on HDR, disabled on Smooth mode).
   - Soft shadow filtering with depth and normal bias to eliminate shadow acne.
4. **Anti-Aliasing:**
   - 2x MSAA enabled by default for crisp weapon silhouettes and foliage without blurry post-process FXAA.
5. **Color Grading & Tone Mapping:**
   - Neutral / ACES filmic LUT for high-contrast, vivid tactical battlefield lighting.

---

## 3. Mobile Quality Levels (Matching PUBG Mobile)

Configured in `QualitySettings.asset` and managed at runtime via `MobileURPOptimizer.cs`:

| Quality Tier | Target FPS | Render Scale | Shadows | Texture Quality | Device Class |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Smooth** | 60 FPS | 0.80x | None | Half Res | Low-end (e.g. Helio G85) |
| **Balanced** | 60 FPS | 0.90x | Hard (50m) | Full Res | Mid-range (e.g. Dimensity 7050) |
| **HD** *(Default)* | 60 FPS | 1.0x | Soft (75m, MSAA 2x) | Full Res | Mainstream (e.g. Snapdragon 778G) |
| **HDR** | 60 FPS | 1.0x | Soft (120m, MSAA 4x, Bloom) | Full Res | Flagship (e.g. Snapdragon 8 Gen 2) |
| **Ultra** | 90 FPS | 1.0x | Soft (160m, 4 Cascades) | Full Res | Gaming Devices (90Hz / 120Hz displays) |

---

## 4. How to Open & Build in Unity

1. **Recommended Unity Version:**
   - Unity **2022.3 LTS** (or Unity **6000 LTS**).
2. **Opening Project:**
   - Launch Unity Hub -> Click **Open** -> Select the `/unity` directory.
   - Unity Hub will resolve the URP packages defined in `Packages/manifest.json`.
3. **Android Build Settings:**
   - Go to **File -> Build Settings...**
   - Select **Android** platform -> Click **Switch Platform**.
   - Verify:
     - Texture Compression: **ASTC**
     - Scripting Backend: **IL2CPP**
     - Target Architectures: **ARM64**
   - Click **Build** to produce the release APK/AAB or check **Export Project** to output as a Gradle library.
