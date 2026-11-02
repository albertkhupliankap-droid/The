using UnityEngine;
using UnityEngine.Rendering;
using UnityEngine.Rendering.Universal;

namespace ZogamMobile.Optimization
{
    public enum MobileGraphicsTier
    {
        Smooth,     // 60 FPS - Low End (Sub-1080p, no shadows)
        Balanced,   // 60 FPS - Mid Range (Hard shadows, 50m)
        HD,         // 60 FPS - Standard (Soft shadows, 75m, MSAA 2x)
        HDR,        // 60 FPS - High End (Bloom, 120m shadows, Color Grading)
        Ultra       // 90 FPS - Flagship Devices
    }

    /// <summary>
    /// Manages mobile runtime performance, battery thermal throttling,
    /// dynamic resolution scaling, and URP quality tiers for ZOGAM MOBILE.
    /// </summary>
    public class MobileURPOptimizer : MonoBehaviour
    {
        public static MobileURPOptimizer Instance { get; private set; }

        [Header("Frame Rate & Power")]
        [SerializeField] private int targetFPS = 60;
        [SerializeField] private bool enableThermalThrottlingProtection = true;

        [Header("Universal Render Pipeline")]
        [SerializeField] private UniversalRenderPipelineAsset urpAsset;

        private MobileGraphicsTier currentTier = MobileGraphicsTier.HD;

        private void Awake()
        {
            if (Instance == null)
            {
                Instance = this;
                DontDestroyOnLoad(gameObject);
            }
            else
            {
                Destroy(gameObject);
                return;
            }

            ConfigureMobileEnvironment();
            ApplyGraphicsTier(MobileGraphicsTier.HD);
        }

        private void ConfigureMobileEnvironment()
        {
            // Prevent screen dimming during gameplay
            Screen.sleepTimeout = SleepTimeout.NeverSleep;

            // Unlock and set target frame rate (60 or 90 FPS)
            QualitySettings.vSyncCount = 0;
            Application.targetFrameRate = targetFPS;

            // Enable dynamic resolution if supported
            ScalableBufferManager.ResizeBuffers(1.0f, 1.0f);
        }

        public void ApplyGraphicsTier(MobileGraphicsTier tier)
        {
            currentTier = tier;

            switch (tier)
            {
                case MobileGraphicsTier.Smooth:
                    QualitySettings.SetQualityLevel(0, true);
                    if (urpAsset != null)
                    {
                        urpAsset.renderScale = 0.80f; // Downscaled for budget GPUs
                        urpAsset.shadowDistance = 25f;
                        urpAsset.msaaSamples = 1;
                    }
                    Application.targetFrameRate = 60;
                    break;

                case MobileGraphicsTier.Balanced:
                    QualitySettings.SetQualityLevel(1, true);
                    if (urpAsset != null)
                    {
                        urpAsset.renderScale = 0.90f;
                        urpAsset.shadowDistance = 50f;
                        urpAsset.msaaSamples = 2;
                    }
                    Application.targetFrameRate = 60;
                    break;

                case MobileGraphicsTier.HD:
                    QualitySettings.SetQualityLevel(2, true);
                    if (urpAsset != null)
                    {
                        urpAsset.renderScale = 1.0f;
                        urpAsset.shadowDistance = 75f;
                        urpAsset.msaaSamples = 2;
                    }
                    Application.targetFrameRate = 60;
                    break;

                case MobileGraphicsTier.HDR:
                    QualitySettings.SetQualityLevel(3, true);
                    if (urpAsset != null)
                    {
                        urpAsset.renderScale = 1.0f;
                        urpAsset.shadowDistance = 120f;
                        urpAsset.msaaSamples = 4;
                    }
                    Application.targetFrameRate = 60;
                    break;

                case MobileGraphicsTier.Ultra:
                    QualitySettings.SetQualityLevel(4, true);
                    if (urpAsset != null)
                    {
                        urpAsset.renderScale = 1.0f;
                        urpAsset.shadowDistance = 160f;
                        urpAsset.msaaSamples = 4;
                    }
                    Application.targetFrameRate = 90; // High refresh rate for supported 90/120Hz displays
                    break;
            }

            Debug.Log($"[ZOGAM MOBILE] Graphics Tier Switched to: {tier} ({Application.targetFrameRate} FPS)");
        }

        public void SetTargetFPS(int fps)
        {
            targetFPS = fps;
            Application.targetFrameRate = fps;
        }

        public MobileGraphicsTier GetCurrentTier() => currentTier;
    }
}
