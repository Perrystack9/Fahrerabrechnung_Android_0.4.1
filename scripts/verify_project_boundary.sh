#!/bin/sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
cd "$ROOT"
for module in app core data domain feature report; do
  test -f "$module/build.gradle.kts" || { echo "Missing module: $module" >&2; exit 1; }
done
for token in VOLLSTAENDIG TEILWEISE UNKLAR KEINE_DATEN NICHT_ANWENDBAR; do
  grep -Rqs "$token" domain/src/main/kotlin || { echo "Missing required token: $token" >&2; exit 1; }
done
if grep -RInE 'fallbackToDestructiveMigration|FirebaseAnalytics|Crashlytics|ACCESS_COARSE_LOCATION|ACCESS_BACKGROUND_LOCATION|LocationSource|RouteStatus' --include='*.kt' --include='*.kts' .; then
  echo "Forbidden project-start implementation found" >&2
  exit 1
fi
if grep -RIn "ACCESS_FINE_LOCATION" --include="*.kt" --include="*.kts" app core data domain report; then
  echo "Foreground location must remain in the onboarding home picker" >&2
  exit 1
fi
if ! grep -q 'android.permission.ACCESS_FINE_LOCATION' app/src/main/AndroidManifest.xml; then
  echo "Home picker permission missing" >&2
  exit 1
fi
echo "Project-boundary static checks passed."
