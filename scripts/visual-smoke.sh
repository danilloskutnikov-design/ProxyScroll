#!/usr/bin/env bash
set -euo pipefail
mkdir -p device-evidence
collect_evidence() {
  timeout 20s adb pull /sdcard/Android/data/com.proxyscroll.app/files/qa device-evidence/ || true
  timeout 20s adb logcat -d -s AndroidRuntime > device-evidence/runtime.txt || true
}
trap collect_evidence EXIT
adb shell settings put system accelerometer_rotation 0
adb shell settings put system user_rotation 0
gradle --no-daemon :app:connectedDebugAndroidTest
