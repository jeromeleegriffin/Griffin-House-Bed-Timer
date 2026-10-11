# Griffin Bed Timer Android alarm prototype
This is an **unbuilt, untested** native alarm scaffold. The current web page does not yet call the BedNative bridge, so installing it as-is will NOT schedule alarms. Do not rely on it for work.
Requires Android Studio, Android SDK 35 and Gradle. Open the android/ folder as a project.
Next: wire HTML schedule changes to BedNative.schedule JSON {start:epochMillis, interval:milliseconds, count:integer, beds:[numbers in rotation order]}; cancel on explicit alarm disable. Keep bed numbers and scheduling distinct. Validate with device tests (locked, swipe away, reboot, permission denied, reschedule).
Native notifications use Android notification channel sounds, not necessarily the web-selected tone/voice. Exact alarm access must be granted on Android 12+. Battery/OEM policies can affect delivery; force-stop blocks alarms. A WebView pointed at the hosted page requires internet to load.
