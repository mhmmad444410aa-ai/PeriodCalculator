#!/usr/bin/env bash
set -euo pipefail
mkdir -p screenshots
adb wait-for-device
adb shell wm size 1080x1920
adb shell wm density 400
adb shell input keyevent 82
APK=app/build/outputs/apk/offline/debug/app-offline-debug.apk
test -s "$APK"
adb install -r "$APK"
PKG=com.mhmmad.periodcalculator.preview
for pair in 'tools:Cycle planning studio' 'planner:Event planner' 'accuracy:Forecast check' 'kit:Essentials checklist' 'visit:Prepare for a health visit'; do
  route="${pair%%:*}"
  label="${pair#*:}"
  adb shell am force-stop "$PKG"
  adb shell am start -W -n "$PKG/com.mhmmad.periodcalculator.MainActivity" --es capture_route "$route"
  found=0
  for attempt in $(seq 1 8); do
    sleep 3
    adb shell uiautomator dump /sdcard/window.xml >/dev/null 2>&1 || continue
    if adb shell cat /sdcard/window.xml | grep -Fq "$label"; then
      found=1
      break
    fi
  done
  test "$found" -eq 1 || { echo "Expected screen missing: $label"; exit 1; }
  adb exec-out screencap -p > "screenshots/$route.png"
  python3 -c 'import struct,sys; d=open(sys.argv[1],"rb").read(); assert d[:8]==b"\x89PNG\r\n\x1a\n" and len(d)>10000; assert struct.unpack(">II",d[16:24])==(1080,1920)' "screenshots/$route.png"
done
