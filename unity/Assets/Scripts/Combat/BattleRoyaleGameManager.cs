using System.Collections;
using System.Collections.Generic;
using UnityEngine;

namespace ZogamMobile.Combat
{
    [System.Serializable]
    public class ZonePhase
    {
        public float waitTimeSeconds = 60f;
        public float shrinkDurationSeconds = 45f;
        public float radiusMeters = 800f;
        public float damagePerSecond = 2f;
    }

    public class BattleRoyaleGameManager : MonoBehaviour
    {
        public static BattleRoyaleGameManager Instance { get; private set; }

        [Header("Match Setup")]
        [SerializeField] private int totalCombatants = 100;
        [SerializeField] private float mapSize = 4000f;

        [Header("Zone Contracting Phases")]
        [SerializeField] private List<ZonePhase> zonePhases = new List<ZonePhase>();
        [SerializeField] private Transform blueZoneVisual;
        [SerializeField] private Transform safeZoneWhiteVisual;

        [Header("Aircraft Flight Path")]
        [SerializeField] private Transform transportPlane;
        [SerializeField] private float flightSpeed = 80f;

        private int currentPhaseIndex = 0;
        private Vector3 currentSafeCenter;
        private float currentSafeRadius;
        private Vector3 currentBlueCenter;
        private float currentBlueRadius;
        private bool isZoneShrinking = false;

        private int alivePlayers = 100;

        private void Awake()
        {
            if (Instance == null) Instance = this;
            else Destroy(gameObject);
        }

        private void Start()
        {
            InitializeMatch();
        }

        private void InitializeMatch()
        {
            alivePlayers = totalCombatants;
            currentBlueCenter = Vector3.zero;
            currentBlueRadius = mapSize / 2f;

            currentSafeCenter = new Vector3(Random.Range(-500f, 500f), 0f, Random.Range(-500f, 500f));
            currentSafeRadius = 1200f;

            StartCoroutine(ZoneLifecycleRoutine());
        }

        private IEnumerator ZoneLifecycleRoutine()
        {
            for (int i = 0; i < zonePhases.Count; i++)
            {
                currentPhaseIndex = i;
                ZonePhase phase = zonePhases[i];

                isZoneShrinking = false;
                yield return new WaitForSeconds(phase.waitTimeSeconds);

                // Shrink Zone
                isZoneShrinking = true;
                float elapsed = 0f;
                Vector3 startCenter = currentBlueCenter;
                float startRadius = currentBlueRadius;

                while (elapsed < phase.shrinkDurationSeconds)
                {
                    elapsed += Time.deltaTime;
                    float t = elapsed / phase.shrinkDurationSeconds;

                    currentBlueCenter = Vector3.Lerp(startCenter, currentSafeCenter, t);
                    currentBlueRadius = Mathf.Lerp(startRadius, currentSafeRadius, t);

                    UpdateZoneVisuals();
                    yield return null;
                }

                currentBlueRadius = currentSafeRadius;
                currentBlueCenter = currentSafeCenter;

                // Pick next safe zone center inside current
                currentSafeRadius *= 0.55f;
                currentSafeCenter += (Vector3)Random.insideUnitCircle * (currentSafeRadius * 0.4f);
                currentSafeCenter.y = 0f;

                UpdateZoneVisuals();
            }
        }

        private void UpdateZoneVisuals()
        {
            if (blueZoneVisual != null)
            {
                blueZoneVisual.position = currentBlueCenter;
                blueZoneVisual.localScale = new Vector3(currentBlueRadius * 2f, 200f, currentBlueRadius * 2f);
            }

            if (safeZoneWhiteVisual != null)
            {
                safeZoneWhiteVisual.position = currentSafeCenter;
                safeZoneWhiteVisual.localScale = new Vector3(currentSafeRadius * 2f, 1f, currentSafeRadius * 2f);
            }
        }

        public bool IsInsideSafeZone(Vector3 position)
        {
            Vector2 pos2D = new Vector2(position.x, position.z);
            Vector2 center2D = new Vector2(currentBlueCenter.x, currentBlueCenter.z);
            return Vector2.Distance(pos2D, center2D) <= currentBlueRadius;
        }

        public void ReportElimination()
        {
            alivePlayers = Mathf.Max(1, alivePlayers - 1);
            if (alivePlayers == 1)
            {
                TriggerChickenDinner();
            }
        }

        private void TriggerChickenDinner()
        {
            Debug.Log("[ZOGAM MOBILE] #1 / 100 WINNER WINNER CHICKEN DINNER!");
        }

        public int GetAlivePlayersCount() => alivePlayers;
    }
}
