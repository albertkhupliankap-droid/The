using UnityEngine;

namespace ZogamMobile.Player
{
    public enum StanceState
    {
        Stand,
        Crouch,
        Prone,
        Sprint
    }

    public enum SkydiveState
    {
        InPlane,
        Freefall,
        Parachuting,
        Landed
    }

    [RequireComponent(typeof(CharacterController))]
    public class PlayerController : MonoBehaviour
    {
        [Header("Movement Attributes")]
        [SerializeField] private float walkSpeed = 4.5f;
        [SerializeField] private float sprintSpeed = 7.2f;
        [SerializeField] private float crouchSpeed = 2.8f;
        [SerializeField] private float proneSpeed = 1.2f;
        [SerializeField] private float jumpHeight = 1.4f;
        [SerializeField] private float gravity = -18.0f;

        [Header("Skydiving Attributes")]
        [SerializeField] private float freefallFallSpeed = 55.0f;
        [SerializeField] private float parachuteFallSpeed = 12.0f;
        [SerializeField] private float glideForwardSpeed = 24.0f;

        [Header("References")]
        [SerializeField] private Transform cameraTransform;
        [SerializeField] private Animator characterAnimator;

        private CharacterController controller;
        private Vector3 velocity;
        private bool isGrounded;

        private StanceState currentStance = StanceState.Stand;
        private SkydiveState skydiveState = SkydiveState.Landed;

        private Vector2 movementInput;
        private float targetRotationAngle;
        private float rotationVelocity;

        private void Awake()
        {
            controller = GetComponent<CharacterController>();
        }

        private void Update()
        {
            if (skydiveState == SkydiveState.InPlane) return;

            if (skydiveState == SkydiveState.Freefall || skydiveState == SkydiveState.Parachuting)
            {
                HandleSkydiving();
                return;
            }

            HandleGroundMovement();
        }

        private void HandleGroundMovement()
        {
            isGrounded = controller.isGrounded;
            if (isGrounded && velocity.y < 0)
            {
                velocity.y = -2f;
            }

            float currentSpeed = currentStance switch
            {
                StanceState.Sprint => sprintSpeed,
                StanceState.Crouch => crouchSpeed,
                StanceState.Prone => proneSpeed,
                _ => walkSpeed
            };

            Vector3 direction = new Vector3(movementInput.x, 0f, movementInput.y).normalized;

            if (direction.magnitude >= 0.1f)
            {
                targetRotationAngle = Mathf.Atan2(direction.x, direction.z) * Mathf.Rad2Deg + cameraTransform.eulerAngles.y;
                float angle = Mathf.SmoothDampAngle(transform.eulerAngles.y, targetRotationAngle, ref rotationVelocity, 0.08f);
                transform.rotation = Quaternion.Euler(0f, angle, 0f);

                Vector3 moveDir = Quaternion.Euler(0f, targetRotationAngle, 0f) * Vector3.forward;
                controller.Move(moveDir.normalized * (currentSpeed * Time.deltaTime));
            }

            // Gravity
            velocity.y += gravity * Time.deltaTime;
            controller.Move(velocity * Time.deltaTime);

            UpdateAnimationParameters(direction.magnitude);
        }

        private void HandleSkydiving()
        {
            float fallRate = skydiveState == SkydiveState.Freefall ? freefallFallSpeed : parachuteFallSpeed;
            Vector3 forwardGlide = transform.forward * (glideForwardSpeed * (skydiveState == SkydiveState.Freefall ? 1.0f : 0.4f));
            Vector3 skydiveVelocity = forwardGlide + Vector3.down * fallRate;

            controller.Move(skydiveVelocity * Time.deltaTime);

            // Turn towards camera heading
            transform.rotation = Quaternion.Euler(0f, cameraTransform.eulerAngles.y, 0f);

            if (controller.isGrounded || transform.position.y <= 1.0f)
            {
                skydiveState = SkydiveState.Landed;
                if (characterAnimator != null)
                {
                    characterAnimator.SetTrigger("OnTouchdown");
                }
            }
        }

        public void SetMovementInput(Vector2 input, bool isSprintRequested)
        {
            movementInput = input;
            if (isSprintRequested && currentStance != StanceState.Prone)
            {
                currentStance = StanceState.Sprint;
            }
            else if (currentStance == StanceState.Sprint && !isSprintRequested)
            {
                currentStance = StanceState.Stand;
            }
        }

        public void ToggleStance(StanceState stance)
        {
            currentStance = (currentStance == stance) ? StanceState.Stand : stance;
            // Adjust CharacterController height according to stance
            controller.height = currentStance switch
            {
                StanceState.Crouch => 1.3f,
                StanceState.Prone => 0.5f,
                _ => 1.9f
            };
            controller.center = new Vector3(0f, controller.height / 2f, 0f);
        }

        public void Jump()
        {
            if (isGrounded && currentStance != StanceState.Prone)
            {
                velocity.y = Mathf.Sqrt(jumpHeight * -2f * gravity);
                if (characterAnimator != null) characterAnimator.SetTrigger("Jump");
            }
        }

        public void DeployParachute()
        {
            if (skydiveState == SkydiveState.Freefall)
            {
                skydiveState = SkydiveState.Parachuting;
            }
        }

        public void StartSkydiving(Vector3 spawnPosition)
        {
            transform.position = spawnPosition;
            skydiveState = SkydiveState.Freefall;
        }

        private void UpdateAnimationParameters(float moveMagnitude)
        {
            if (characterAnimator == null) return;
            characterAnimator.SetFloat("Speed", moveMagnitude * (currentStance == StanceState.Sprint ? 2f : 1f), 0.1f, Time.deltaTime);
            characterAnimator.SetInteger("Stance", (int)currentStance);
            characterAnimator.SetBool("IsGrounded", isGrounded);
        }

        public StanceState GetCurrentStance() => currentStance;
        public SkydiveState GetSkydiveState() => skydiveState;
    }
}
