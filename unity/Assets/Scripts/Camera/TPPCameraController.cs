using UnityEngine;

namespace ZogamMobile.CameraSystems
{
    public class TPPCameraController : MonoBehaviour
    {
        [Header("Target Tracking")]
        [SerializeField] private Transform target;
        [SerializeField] private Vector3 targetOffset = new Vector3(0.4f, 1.6f, 0f); // Over-the-shoulder right offset
        [SerializeField] private float normalDistance = 2.8f;
        [SerializeField] private float adsDistance = 1.0f;
        [SerializeField] private float minDistance = 0.6f;

        [Header("Look Sensitivity")]
        [SerializeField] private float xSensitivity = 120.0f;
        [SerializeField] private float ySensitivity = 90.0f;
        [SerializeField] private float minPitch = -35.0f;
        [SerializeField] private float maxPitch = 60.0f;

        [Header("Collision Avoidance")]
        [SerializeField] private LayerMask collisionLayers;
        [SerializeField] private float collisionRadius = 0.25f;

        private float currentYaw = 0f;
        private float currentPitch = 10f;
        private float currentDistance;

        private bool isFreeLooking = false;
        private float freeLookYawOffset = 0f;

        private void Start()
        {
            currentDistance = normalDistance;
            if (target != null)
            {
                currentYaw = target.eulerAngles.y;
            }
        }

        private void LateUpdate()
        {
            if (target == null) return;

            UpdateCameraTransform();
        }

        private void UpdateCameraTransform()
        {
            float effectiveYaw = isFreeLooking ? currentYaw + freeLookYawOffset : currentYaw;
            Quaternion rotation = Quaternion.Euler(currentPitch, effectiveYaw, 0f);

            Vector3 pivotPosition = target.position + targetOffset;
            Vector3 desiredCameraPosition = pivotPosition - (rotation * Vector3.forward * currentDistance);

            // Obstacle collision check (SphereCast to avoid clipping inside walls)
            if (Physics.SphereCast(pivotPosition, collisionRadius, (desiredCameraPosition - pivotPosition).normalized, out RaycastHit hit, currentDistance, collisionLayers))
            {
                transform.position = pivotPosition - (rotation * Vector3.forward * Mathf.Clamp(hit.distance, minDistance, currentDistance));
            }
            else
            {
                transform.position = desiredCameraPosition;
            }

            transform.rotation = rotation;
        }

        public void ProcessTouchLook(Vector2 delta)
        {
            if (isFreeLooking)
            {
                freeLookYawOffset += delta.x * (xSensitivity / 100f);
            }
            else
            {
                currentYaw += delta.x * (xSensitivity / 100f);
            }

            currentPitch = Mathf.Clamp(currentPitch - delta.y * (ySensitivity / 100f), minPitch, maxPitch);
        }

        public void SetFreeLook(bool active)
        {
            isFreeLooking = active;
            if (!active) freeLookYawOffset = 0f;
        }

        public void SetADS(bool isADS)
        {
            currentDistance = isADS ? adsDistance : normalDistance;
        }
    }
}
