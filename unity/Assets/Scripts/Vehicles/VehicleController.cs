using UnityEngine;

namespace ZogamMobile.Vehicles
{
    public class VehicleController : MonoBehaviour
    {
        [Header("Wheel Colliders")]
        [SerializeField] private WheelCollider frontLeftWheel;
        [SerializeField] private WheelCollider frontRightWheel;
        [SerializeField] private WheelCollider rearLeftWheel;
        [SerializeField] private WheelCollider rearRightWheel;

        [Header("Wheel Meshes")]
        [SerializeField] private Transform frontLeftMesh;
        [SerializeField] private Transform frontRightMesh;
        [SerializeField] private Transform rearLeftMesh;
        [SerializeField] private Transform rearRightMesh;

        [Header("Engine & Handling")]
        [SerializeField] private float motorTorque = 1800f;
        [SerializeField] private float brakeTorque = 3500f;
        [SerializeField] private float maxSteerAngle = 32f;
        [SerializeField] private float maxSpeedKph = 115f;

        [Header("Center of Mass")]
        [SerializeField] private Vector3 centerOfMassOffset = new Vector3(0f, -0.4f, 0f);

        private Rigidbody rb;
        private float currentSpeed;

        private void Awake()
        {
            rb = GetComponent<Rigidbody>();
            if (rb != null)
            {
                rb.centerOfMass += centerOfMassOffset;
            }
        }

        private void FixedUpdate()
        {
            currentSpeed = rb.velocity.magnitude * 3.6f;
        }

        public void ApplyDriveInput(float throttle, float steer, bool isBraking)
        {
            // Steering
            float steerAngle = steer * maxSteerAngle;
            frontLeftWheel.steerAngle = steerAngle;
            frontRightWheel.steerAngle = steerAngle;

            // Motor Drive (All-Wheel Drive)
            if (isBraking)
            {
                frontLeftWheel.brakeTorque = brakeTorque;
                frontRightWheel.brakeTorque = brakeTorque;
                rearLeftWheel.brakeTorque = brakeTorque;
                rearRightWheel.brakeTorque = brakeTorque;

                frontLeftWheel.motorTorque = 0f;
                frontRightWheel.motorTorque = 0f;
                rearLeftWheel.motorTorque = 0f;
                rearRightWheel.motorTorque = 0f;
            }
            else
            {
                frontLeftWheel.brakeTorque = 0f;
                frontRightWheel.brakeTorque = 0f;
                rearLeftWheel.brakeTorque = 0f;
                rearRightWheel.brakeTorque = 0f;

                float torque = (currentSpeed < maxSpeedKph) ? (throttle * motorTorque / 4f) : 0f;
                frontLeftWheel.motorTorque = torque;
                frontRightWheel.motorTorque = torque;
                rearLeftWheel.motorTorque = torque;
                rearRightWheel.motorTorque = torque;
            }

            UpdateWheelVisuals(frontLeftWheel, frontLeftMesh);
            UpdateWheelVisuals(frontRightWheel, frontRightMesh);
            UpdateWheelVisuals(rearLeftWheel, rearLeftMesh);
            UpdateWheelVisuals(rearRightWheel, rearRightMesh);
        }

        private void UpdateWheelVisuals(WheelCollider collider, Transform mesh)
        {
            if (mesh == null) return;
            collider.GetWorldPose(out Vector3 pos, out Quaternion rot);
            mesh.position = pos;
            mesh.rotation = rot;
        }

        public float GetSpeedKph() => currentSpeed;
    }
}
