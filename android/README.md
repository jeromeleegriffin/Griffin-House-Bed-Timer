# Griffin House Bed Timer Android prototype

Status: source scaffold created, NOT built or phone-tested. Do not rely on this for work alarms until real-device validation passes.

## Build
Open the `android/` directory in Android Studio, install Android SDK 35, sync Gradle and build a debug APK. This environment has no Android SDK/Gradle toolchain, so compilation has not been verified.

## Current behavior
- The Android WebView loads a bundled copy of `bed-times.html` using a secure HTTPS-like local asset origin.
- JavaScript syncs the selected bed rotation, interval, start time, and mute status through `BedNative`.
- Android AlarmManager schedules the next due bed, and the receiver posts a high-priority notification using the device alarm tone and vibration, then schedules the next bed.
- Restart receiver re-establishes the schedule.
- If exact-alarm access is denied, scheduling falls back to an inexact alarm and cannot promise exact timing.
- Camera OCR still needs its recognition library downloaded; camera and notification permissions require user approval.

## Important limitations
- This is not a compiled APK and none of the behavior has been tested on an Android phone.
- Existing website localStorage does NOT automatically migrate into the app's separate storage. Users must re-enter schedule and products or a migration mechanism must be developed before real use.
- The native notification does not yet play the selected website voice or custom sound, and it is not a full-screen ringing alarm.
- Android force-stop and some manufacturer battery policies prevent reliable alarm delivery.
- Reboot restoration requires at least one prior app launch; permission and battery tests are mandatory.
- OCR recognition can fail; manually verify every code and quantity.

## Required validation
1. Build successfully in Android Studio; fix any build/runtime errors.
2. Confirm 8 beds visible, correct bed numbering, and native bridge scheduling.
3. Test exact time, locked phone, screen off, swipe-away, reboot, and permission denied.
4. Change interval, start time, bed count, and mute; ensure old alarms are canceled.
5. Confirm bed number in notifications and no repeated/omitted advances.
6. Test OCR camera, no stored original images, and manual product editing.
7. Plan migration of existing web schedule and product data before release.

Do not deploy or replace the live website with this prototype.
