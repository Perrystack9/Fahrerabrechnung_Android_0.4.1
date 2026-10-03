#!/usr/bin/env bash
set -euo pipefail

project_root=$(cd -- "$(dirname -- "$0")/.." && pwd)
cd "$project_root"
baseline_sha=e957834d2cfa5e1dcd4e54bb458a1e078f2a57f5
baseline_root="${RUNNER_TEMP:?}/fahrer-card-schema10-baseline"
output_dir="$project_root/ci-artifacts/device-test-update"
sdk_tools="${ANDROID_SDK_ROOT:?}/build-tools/36.0.0"
test ! -e "$baseline_root"
mkdir -p "$output_dir" ci-logs
git worktree add --detach "$baseline_root" "$baseline_sha"

# Build-only overlay: preserve the tested PR-41 product sources and schema 10.
cp app/build.gradle.kts "$baseline_root/app/build.gradle.kts"
mkdir -p "$baseline_root/ci" "$baseline_root/app/src/deviceTest/res/values" "$baseline_root/gradle/wrapper"
cp ci/device-test.jks "$baseline_root/ci/device-test.jks"
cp app/src/deviceTest/res/values/strings.xml "$baseline_root/app/src/deviceTest/res/values/strings.xml"
cp gradlew "$baseline_root/gradlew"
cp gradle/wrapper/gradle-wrapper.jar gradle/wrapper/gradle-wrapper.properties "$baseline_root/gradle/wrapper/"
python3 - "$baseline_root" <<'PY'
from pathlib import Path
import re, sys
root = Path(sys.argv[1])
config = root / 'app/build.gradle.kts'
content = config.read_text()
assert len(re.findall(r'versionCode = \d+', content)) == 1
config.write_text(re.sub(r'versionCode = \d+', 'versionCode = 40106', content))
assert 'const val SCHEMA_VERSION = 10' in (root / 'data/src/main/kotlin/de/fahrerabrechnung/data/db/AppDatabase.kt').read_text()
PY
"$baseline_root/gradlew" -p "$baseline_root" :app:assembleDeviceTest --stacktrace > ci-logs/device-test-baseline-build.log 2>&1

shopt -s nullglob
baseline_apks=("$baseline_root"/app/build/outputs/apk/deviceTest/*.apk)
current_apks=(app/build/outputs/apk/deviceTest/*.apk)
test "${#baseline_apks[@]}" -eq 1
test "${#current_apks[@]}" -eq 1
cp "${baseline_apks[0]}" "$output_dir/01-FahrerTagebuch-Test-Schema10.apk"
cp "${current_apks[0]}" "$output_dir/02-FahrerTagebuch-Test-Update.apk"
cp docs/EVENT_FAULT_TIMELINE_CHECK.md "$output_dir/ANLEITUNG.md"

keytool -exportcert -keystore ci/device-test.jks -storepass android -alias androiddebugkey > ci-logs/device-test-public-cert.der
for variant in 01-FahrerTagebuch-Test-Schema10 02-FahrerTagebuch-Test-Update; do
    "$sdk_tools/apksigner" verify --verbose --print-certs "$output_dir/$variant.apk" > "ci-logs/$variant-signature.txt"
    "$sdk_tools/aapt2" dump badging "$output_dir/$variant.apk" > "ci-logs/$variant-manifest.txt"
done
python3 - <<'PY'
from pathlib import Path
import hashlib, re
logs = Path('ci-logs')
certificate = hashlib.sha256((logs / 'device-test-public-cert.der').read_bytes()).hexdigest()
versions = []
for variant in ('01-FahrerTagebuch-Test-Schema10', '02-FahrerTagebuch-Test-Update'):
    signature = (logs / (variant + '-signature.txt')).read_text()
    assert re.search(r'Signer #1 certificate SHA-256 digest: ([0-9a-f]+)', signature).group(1) == certificate
    manifest = (logs / (variant + '-manifest.txt')).read_text()
    package = re.search(r"package: name='([^']+)' versionCode='(\d+)'", manifest)
    assert package and package.group(1) == 'de.fahrerabrechnung.app.devicetest'
    assert 'FahrerTagebuch Test' in manifest
    versions.append(int(package.group(2)))
assert versions[0] == 40106 and versions[1] > versions[0], versions
output = Path('ci-artifacts/device-test-update')
lines = [hashlib.sha256(p.read_bytes()).hexdigest() + '  ' + p.name for p in sorted(output.glob('*.apk'))]
(output / 'SHA256SUMS.txt').write_text('\n'.join(lines) + '\n')
print('Both APKs verified: stable test certificate, separate test package and increasing versions.')
PY
