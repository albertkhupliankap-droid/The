using System.Collections;
using UnityEngine;

namespace ZogamMobile.Player
{
    public enum WeaponType
    {
        AssaultRifle,
        SniperRifle,
        SubmachineGun,
        Shotgun,
        Pistol,
        Melee
    }

    [System.Serializable]
    public class WeaponData
    {
        public string weaponName = "M416 Tactical";
        public WeaponType type = WeaponType.AssaultRifle;
        public int currentAmmo = 30;
        public int magCapacity = 30;
        public int reserveAmmo = 120;
        public float baseDamage = 41f;
        public float fireRateRPM = 700f;
        public float reloadTime = 2.1f;
        public float recoilPitch = 1.4f;
        public float recoilYaw = 0.5f;
        public bool isAutomatic = true;
        public GameObject weaponModel;
        public ParticleSystem muzzleFlash;
        public AudioClip fireSound;
        public AudioClip reloadSound;
    }

    public class PlayerWeaponManager : MonoBehaviour
    {
        [Header("Weapon Inventory")]
        [SerializeField] private WeaponData[] weaponSlots = new WeaponData[2];
        [SerializeField] private int activeSlotIndex = 0;

        [Header("Aim & Ballistics")]
        [SerializeField] private Camera mainCamera;
        [SerializeField] private LayerMask hitLayers;
        [SerializeField] private Transform firePoint;

        [Header("ADS & Optics")]
        [SerializeField] private float normalFOV = 60f;
        [SerializeField] private float adsFOV = 40f;
        [SerializeField] private float adsSpeed = 12f;

        private bool isADS = false;
        private bool isReloading = false;
        private float nextFireTime = 0f;

        private void Start()
        {
            EquipWeaponSlot(0);
        }

        private void Update()
        {
            HandleADS();
        }

        public void FireActiveWeapon()
        {
            if (isReloading) return;

            WeaponData weapon = GetCurrentWeapon();
            if (weapon == null) return;

            if (weapon.currentAmmo <= 0)
            {
                StartCoroutine(ReloadRoutine(weapon));
                return;
            }

            if (Time.time < nextFireTime) return;

            float fireInterval = 60f / weapon.fireRateRPM;
            nextFireTime = Time.time + fireInterval;

            weapon.currentAmmo--;

            // Muzzle flash particle
            if (weapon.muzzleFlash != null)
            {
                weapon.muzzleFlash.Play();
            }

            // Raycast Ballistics
            Ray ray = mainCamera.ViewportPointToRay(new Vector3(0.5f, 0.5f, 0f));
            if (Physics.Raycast(ray, out RaycastHit hit, 500f, hitLayers))
            {
                // Damage target if has health component
                Debug.Log($"[ZOGAM WEAPON] Hit: {hit.collider.name} for {weapon.baseDamage} damage");
            }

            // Apply recoil
            ApplyRecoil(weapon);
        }

        public void Reload()
        {
            WeaponData weapon = GetCurrentWeapon();
            if (weapon != null && !isReloading && weapon.currentAmmo < weapon.magCapacity && weapon.reserveAmmo > 0)
            {
                StartCoroutine(ReloadRoutine(weapon));
            }
        }

        private IEnumerator ReloadRoutine(WeaponData weapon)
        {
            isReloading = true;
            yield return new WaitForSeconds(weapon.reloadTime);

            int needed = weapon.magCapacity - weapon.currentAmmo;
            int loaded = Mathf.Min(needed, weapon.reserveAmmo);
            weapon.currentAmmo += loaded;
            weapon.reserveAmmo -= loaded;

            isReloading = false;
        }

        private void HandleADS()
        {
            float targetFOV = isADS ? adsFOV : normalFOV;
            mainCamera.fieldOfView = Mathf.Lerp(mainCamera.fieldOfView, targetFOV, Time.deltaTime * adsSpeed);
        }

        public void ToggleADS()
        {
            isADS = !isADS;
        }

        public void EquipWeaponSlot(int slotIndex)
        {
            if (slotIndex < 0 || slotIndex >= weaponSlots.Length) return;
            activeSlotIndex = slotIndex;

            for (int i = 0; i < weaponSlots.Length; i++)
            {
                if (weaponSlots[i]?.weaponModel != null)
                {
                    weaponSlots[i].weaponModel.SetActive(i == activeSlotIndex);
                }
            }
        }

        private void ApplyRecoil(WeaponData weapon)
        {
            float recoilAmount = isADS ? weapon.recoilPitch * 0.5f : weapon.recoilPitch;
            mainCamera.transform.localRotation *= Quaternion.Euler(-recoilAmount, Random.Range(-weapon.recoilYaw, weapon.recoilYaw), 0f);
        }

        public WeaponData GetCurrentWeapon() => (activeSlotIndex >= 0 && activeSlotIndex < weaponSlots.Length) ? weaponSlots[activeSlotIndex] : null;
        public bool IsAimingDownSights() => isADS;
        public bool IsReloading() => isReloading;
    }
}
