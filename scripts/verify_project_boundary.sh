#!/bin/sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
cd "$ROOT"
for module in app core data domain feature report; do
  test -f "$module/build.gradle.kts" || { echo "Missing module: $module" >&2; exit 1; }
done
for token in VOLLSTAENDIG TEILWEISE UNKLAR KEINE_DATEN NICHT_ANWENDBAR ROUTE_VOLLSTAENDIG ROUTE_TEILWEISE NUR_GEN2_ORTSDATEN KEINE_ORTSDATEN ORTSDATEN_UNKLAR; do
  grep -Rqs "$token" domain/src/main/kotlin || { echo "Missing required token: $token" >&2; exit 1; }
done
if grep -RInE 'fallbackToDestructiveMigration|FirebaseAnalytics|Crashlytics' --include='*.kt' --include='*.kts' .; then
  echo "Forbidden project-start implementation found" >&2
  exit 1
fi
echo "Project-boundary static checks passed."
