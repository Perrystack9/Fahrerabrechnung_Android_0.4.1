import collections
import hashlib
import json
import os
import re
import shutil
import time
import urllib.error
import urllib.parse
import urllib.request
import zipfile
from pathlib import Path


class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        return None


class Api:
    def __init__(self, config):
        if os.environ['GITHUB_API_URL'] != 'https://api.github.com':
            raise RuntimeError('Unexpected API host')
        self.config = config
        self.prefix = 'https://api.github.com/repos/' + config['repository']
        self.opener = urllib.request.build_opener(NoRedirect())
        self.remaining = None
        self.last_write = 0.0

    def request(self, url, method='GET', data=None, content_type='application/json', accept='application/vnd.github+json'):
        parsed = urllib.parse.urlparse(url)
        expected_path = '/repos/' + self.config['repository'] + '/'
        if (parsed.scheme != 'https' or parsed.hostname not in {'api.github.com', 'uploads.github.com'}
                or not (parsed.path.startswith(expected_path) or
                        (parsed.hostname == 'api.github.com' and parsed.path == expected_path.rstrip('/')))):
            raise RuntimeError('Unexpected authenticated request destination')
        headers = {'Authorization': 'Bearer ' + os.environ['ARTIFACT_CLEANUP_TOKEN'],
                   'Accept': accept, 'X-GitHub-Api-Version': '2022-11-28'}
        if data is not None:
            headers['Content-Type'] = content_type
            headers['Content-Length'] = str(len(data))
        req = urllib.request.Request(url, method=method, data=data, headers=headers)
        if method != 'GET':
            pause = max(0.0, 1.0 - (time.monotonic() - self.last_write))
            if pause:
                time.sleep(pause)
            self.last_write = time.monotonic()
        try:
            response = self.opener.open(req, timeout=180)
        except urllib.error.HTTPError as error:
            response = error
        except urllib.error.URLError:
            raise RuntimeError('GitHub request connection failed') from None
        if response.headers.get('X-RateLimit-Remaining') is not None:
            self.remaining = int(response.headers['X-RateLimit-Remaining'])
        return response

    def __call__(self, path, method='GET', body=None):
        payload = None if body is None else json.dumps(body).encode('utf-8')
        with self.request(self.prefix + path, method, payload) as response:
            if method == 'GET' and response.code == 404:
                return None
            if method == 'DELETE':
                if response.code != 204:
                    raise RuntimeError('Deletion was not acknowledged')
                return None
            if response.code not in {200, 201}:
                raise RuntimeError('GitHub API operation failed: HTTP ' + str(response.code))
            return json.load(response)

    def download(self, path, destination):
        # Capture the redirect ourselves: never forward the GitHub token to blob storage.
        with self.request(self.prefix + path, accept='application/octet-stream') as response:
            if response.code == 200:
                with destination.open('wb') as stream:
                    shutil.copyfileobj(response, stream)
                return
            if response.code != 302:
                raise RuntimeError('Download redirect unavailable: HTTP ' + str(response.code))
            location = response.headers.get('Location', '')
        parsed = urllib.parse.urlparse(location)
        if parsed.scheme != 'https' or not parsed.hostname or parsed.username or parsed.password:
            raise RuntimeError('Unsafe download redirect')
        req = urllib.request.Request(location, headers={'Accept': 'application/octet-stream'})
        try:
            # This separate unauthenticated request may follow storage redirects.
            with urllib.request.urlopen(req, timeout=180) as response, destination.open('wb') as stream:
                shutil.copyfileobj(response, stream)
        except (urllib.error.HTTPError, urllib.error.URLError):
            raise RuntimeError('Archive download failed') from None

    def upload(self, release_id, name, path, content_type):
        url = ('https://uploads.github.com/repos/' + self.config['repository'] +
               '/releases/' + str(release_id) + '/assets?name=' + urllib.parse.quote(name, safe=''))
        with self.request(url, method='POST', data=path.read_bytes(), content_type=content_type) as response:
            if response.code != 201:
                raise RuntimeError('Private backup upload failed: HTTP ' + str(response.code))
            return json.load(response)


def sha256(path):
    with path.open('rb') as stream:
        return 'sha256:' + hashlib.file_digest(stream, 'sha256').hexdigest()


def metadata(artifact, config):
    run = artifact['workflow_run']
    if (run['repository_id'] != config['repository_id'] or
            run['head_repository_id'] != config['repository_id']):
        raise RuntimeError('Wrong artifact repository')
    return {'id': artifact['id'], 'name': artifact['name'], 'size': artifact['size_in_bytes'],
            'run': run['id'], 'branch': run['head_branch'], 'head': run['head_sha'], 'digest': artifact['digest']}


def check_heads(call, config):
    main = call('/branches/main')
    pr = call('/pulls/' + str(config['pr']))
    if main is None or main['commit']['sha'] != config['main']:
        raise RuntimeError('main changed; stop')
    if (pr is None or pr['head']['sha'] != config['pr_head'] or pr['state'] != 'open' or pr['merged'] or
            pr['base']['repo']['id'] != config['repository_id'] or
            pr['head']['repo']['id'] != config['repository_id']):
        raise RuntimeError('PR 45 changed; stop')


def inventory(call):
    artifacts = {}
    for page in range(1, 31):
        data = call('/actions/artifacts?per_page=100&page=' + str(page))
        if data is None or not isinstance(data.get('artifacts'), list):
            raise RuntimeError('Artifact inventory unavailable')
        for artifact in data['artifacts']:
            if artifact['id'] in artifacts:
                raise RuntimeError('Duplicate artifact in inventory')
            artifacts[artifact['id']] = artifact
        if len(data['artifacts']) < 100:
            return artifacts
    raise RuntimeError('Unexpectedly large artifact inventory')


def validate_manifest(config):
    groups = [config['archive'], config['remove'], config['protected']]
    all_ids = [a['id'] for group in groups for a in group]
    if len(all_ids) != len(set(all_ids)):
        raise RuntimeError('Overlapping fixed artifact manifest')
    if (len(config['archive']) != 172 or len(config['remove']) != 357 or len(config['protected']) != 159 or
            any(a['name'] not in {'FahrerTagebuch-device-test-update', 'android-0.4.1-instrumentation-results'}
                for a in config['archive']) or
            any(a['name'] not in {'Fahrerabrechnung-0.4.1-debug-apk', 'Fahrerabrechnung-0.4.1-internal-apk',
                                 'Fahrerabrechnung-Android-0.4.1-project'} for a in config['remove']) or
            any(not re.fullmatch(r'sha256:[0-9a-f]{64}', a['digest'] or '') for group in groups for a in group) or
            not {11032504544, 11153391471, 11161857220}.issubset({a['id'] for a in config['protected']})):
        raise RuntimeError('Invalid fixed artifact manifest')


def validate_snapshot(config, snapshot):
    for lane in ('archive', 'remove', 'protected'):
        for expected in config[lane]:
            artifact = snapshot.get(expected['id'])
            if artifact is None:
                if lane == 'protected':
                    raise RuntimeError('Protected artifact missing')
                continue  # Retry only after the backup has been verified below.
            if metadata(artifact, config) != expected or artifact['expired']:
                raise RuntimeError('Artifact identity changed')


def release_assets(call, release_id):
    assets = {}
    for page in range(1, 11):
        batch = call('/releases/' + str(release_id) + '/assets?per_page=100&page=' + str(page))
        if not isinstance(batch, list):
            raise RuntimeError('Private archive index unavailable')
        for asset in batch:
            if asset['name'] in assets:
                raise RuntimeError('Duplicate backup asset name')
            assets[asset['name']] = asset
        if len(batch) < 100:
            return assets
    raise RuntimeError('Unexpected archive size')


def verify_backup(client, asset, name, expected_digest, expected_size, scratch):
    if (asset['name'] != name or asset['state'] != 'uploaded' or asset['size'] != expected_size):
        raise RuntimeError('Backup name/state/size mismatch')
    if asset.get('digest') is not None:
        if asset['digest'] != expected_digest:
            raise RuntimeError('Backup digest mismatch')
        return
    # Older API responses may lack the server checksum. Verify the downloaded backup instead.
    path = scratch / ('verify-backup-' + str(asset['id']))
    client.download('/releases/assets/' + str(asset['id']), path)
    try:
        if path.stat().st_size != expected_size or sha256(path) != expected_digest:
            raise RuntimeError('Downloaded backup mismatch')
    finally:
        path.unlink(missing_ok=True)


def backup_one(client, config, expected, release, assets, snapshot, scratch):
    name = 'actions-' + str(expected['id']) + '-' + expected['name'] + '.zip'
    asset = assets.get(name)
    if asset is None:
        if expected['id'] not in snapshot:
            raise RuntimeError('Original missing without a verified backup')
        path = scratch / name
        client.download('/actions/artifacts/' + str(expected['id']) + '/zip', path)
        try:
            if path.stat().st_size != expected['size'] or sha256(path) != expected['digest']:
                raise RuntimeError('Original ZIP digest/size mismatch')
            with zipfile.ZipFile(path) as zipped:
                if zipped.testzip() is not None:
                    raise RuntimeError('Original ZIP failed CRC verification')
            asset = client.upload(release['id'], name, path, 'application/zip')
            assets[name] = asset
        finally:
            path.unlink(missing_ok=True)
    verify_backup(client, asset, name, expected['digest'], expected['size'], scratch)
    return {**expected, 'release_asset_id': asset['id'], 'release_asset_name': name,
            'backup_digest': expected['digest'], 'backup_url': asset['browser_download_url']}


def run(config, client):
    if (os.environ['GITHUB_REPOSITORY'] != config['repository'] or
            os.environ['GITHUB_REF'] != 'refs/heads/' + config['branch']):
        raise RuntimeError('Wrong maintenance repository/branch')
    validate_manifest(config)
    repo = client('')
    if repo['id'] != config['repository_id'] or not repo['private']:
        raise RuntimeError('Archive must remain in the existing private repository')
    check_heads(client, config)
    snapshot = inventory(client)
    validate_snapshot(config, snapshot)
    untouched = {key: value for key, value in snapshot.items()
                 if key not in {a['id'] for a in config['archive'] + config['remove']}}
    releases = client('/releases?per_page=100')
    matches = [r for r in releases if r['tag_name'] == config['archive_tag']]
    if len(matches) > 1:
        raise RuntimeError('Ambiguous private archive')
    release = matches[0] if matches else client('/releases', method='POST', body={
        'tag_name': config['archive_tag'], 'target_commitish': config['main'], 'draft': True,
        'prerelease': False, 'name': 'CI-Archiv 04.10.2026 (kein App-Release)',
        'body': ('Privates Wartungsarchiv: ältere CI-Testpakete und Testergebnisse, bytegenau mit SHA-256 geprüft. '
                 'Kein Produktrelease. main und PR #45 bleiben unverändert. '
                 'artifact-index.json dokumentiert Original-ID, CI-Lauf, Commit und Prüfsumme sowie die Archivdatei.')})
    if (not release['draft'] or release['tag_name'] != config['archive_tag'] or
            release['target_commitish'] != config['main']):
        raise RuntimeError('Unexpected archive release identity')
    assets = release_assets(client, release['id'])
    missing_backups = sum(('actions-' + str(a['id']) + '-' + a['name'] + '.zip') not in assets
                          for a in config['archive'])
    pending_deletions = sum(a['id'] in snapshot for a in config['archive'] + config['remove'])
    required_calls = missing_backups * 2 + pending_deletions + 30
    if client.remaining is not None and client.remaining < required_calls:
        raise RuntimeError('API budget too low for this fixed maintenance run; no originals removed')
    print('API_BUDGET_CHECK=' + json.dumps({'remaining': client.remaining, 'required': required_calls}), flush=True)
    scratch = Path(os.environ['RUNNER_TEMP']) / 'verified-ci-archive'
    scratch.mkdir(exist_ok=True)
    records = []
    for expected in config['archive']:
        records.append(backup_one(client, config, expected, release, assets, snapshot, scratch))
        print('BACKUP_VERIFIED', expected['id'], expected['name'], expected['size'], flush=True)
    # All required backups must exist before any original is removed.
    index = {'repository': config['repository'], 'main_unchanged': config['main'],
             'pr45_head_unchanged': config['pr_head'], 'archive_release_id': release['id'],
             'archived_artifacts': records, 'obsolete_generated_copies': config['remove'],
             'protected_actions_artifacts': config['protected']}
    path = scratch / 'artifact-index.json'
    path.write_text(json.dumps(index, ensure_ascii=False, sort_keys=True, indent=2) + '\n', encoding='utf-8')
    digest = sha256(path)
    existing_index = assets.get(path.name)
    index_asset = existing_index or client.upload(release['id'], path.name, path, 'application/json')
    verify_backup(client, index_asset, path.name, digest, path.stat().st_size, scratch)
    print('ARCHIVE_INDEX_VERIFIED=' + json.dumps({'release_id': release['id'], 'asset_id': index_asset['id'],
          'digest': digest, 'archived_count': len(records), 'url': release['html_url']}), flush=True)
    check_heads(client, config)
    fresh = inventory(client)
    validate_snapshot(config, fresh)
    for key, value in untouched.items():
        if fresh.get(key) != value:
            raise RuntimeError('Unselected artifact changed before deletion')
    deleted = []
    for expected in config['archive'] + config['remove']:
        if expected['id'] not in fresh:
            continue
        client('/actions/artifacts/' + str(expected['id']), method='DELETE')
        deleted.append(expected)
        print('ACTION_COPY_REMOVED', expected['id'], expected['name'], expected['size'], flush=True)
    after = inventory(client)
    for expected in config['archive'] + config['remove']:
        if expected['id'] in after:
            raise RuntimeError('Selected Actions copy remains')
    for key, value in untouched.items():
        if after.get(key) != value:
            raise RuntimeError('Protected/unrelated artifact changed')
    check_heads(client, config)
    remaining = [a for a in after.values() if not a['expired']]
    groups = collections.defaultdict(lambda: {'count': 0, 'bytes': 0})
    for artifact in remaining:
        groups[artifact['name']]['count'] += 1
        groups[artifact['name']]['bytes'] += artifact['size_in_bytes']
    result = {'archived_count': len(records), 'archived_bytes': sum(a['size'] for a in records),
              'deleted_count': len(deleted), 'deleted_bytes': sum(a['size'] for a in deleted),
              'protected_count': len(config['protected']), 'main': config['main'], 'pr45_head': config['pr_head'],
              'release_id': release['id'], 'index_asset_id': index_asset['id'], 'index_digest': digest}
    print('EXTENDED_ARTIFACT_CLEANUP_RESULT=' + json.dumps(result, sort_keys=True), flush=True)
    print('ARTIFACT_REMAINING_INVENTORY=' + json.dumps({'count': len(remaining),
          'bytes': sum(a['size_in_bytes'] for a in remaining), 'groups': dict(groups)}, sort_keys=True), flush=True)
    Path(os.environ['RUNNER_TEMP'], 'artifact-cleanup-quota-probe.txt').write_text(
        'Old CI copies removed after verified backup. main and PR 45 unchanged.\n', encoding='utf-8')
    with open(os.environ['GITHUB_STEP_SUMMARY'], 'a', encoding='utf-8') as stream:
        stream.write('## Verified CI archive and cleanup\n\n' + json.dumps(result, sort_keys=True) + '\n')
    return result


if __name__ == '__main__':
    config = json.loads(Path(os.environ['ARTIFACT_CLEANUP_CONFIG']).read_text(encoding='utf-8'))
    run(config, Api(config))
