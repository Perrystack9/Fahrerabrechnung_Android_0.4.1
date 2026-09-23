#!/usr/bin/env bash
set -uo pipefail
mkdir -p ci-logs/ui-screenshots
./gradlew :app:connectedDebugAndroidTest :data:connectedDebugAndroidTest --continue --stacktrace 2>&1 | tee ci-logs/instrumentation.log
result=${PIPESTATUS[0]}
# Collect while the emulator is still running, including after a failed test.
adb pull /sdcard/Android/data/de.fahrerabrechnung.app.debug/files/ui-screenshots/. ci-logs/ui-screenshots/ || true
exit "$result"
