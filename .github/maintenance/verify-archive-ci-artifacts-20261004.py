import contextlib
import copy
import hashlib
import importlib.util
import io
import json
import os
import tempfile
import unittest
from pathlib import Path
from unittest import mock
import zipfile

ROOT = Path(__file__).parent
spec = importlib.util.spec_from_file_location('cleanup_worker', ROOT / 'archive-ci-artifacts-20261004.py')
worker = importlib.util.module_from_spec(spec)
spec.loader.exec_module(worker)
CONFIG = json.loads((ROOT / 'archive-ci-artifacts-20261004.json').read_text())


def artifact(a):
    return {'id': a['id'], 'name': a['name'], 'size_in_bytes': a['size'], 'digest': a['digest'],
            'expired': False, 'workflow_run': {'id': a['run'], 'head_branch': a['branch'],
            'head_sha': a['head'], 'repository_id': CONFIG['repository_id'],
            'head_repository_id': CONFIG['repository_id']}}


class Fake:
    remaining = 5000

    def __init__(self):
        self.config = CONFIG
        self.source = {a['id']: artifact(a) for lane in ('archive', 'remove', 'protected') for a in CONFIG[lane]}
        self.source[1234567] = artifact({**CONFIG['protected'][0], 'id': 1234567, 'name': 'TempMail-v0.2-debug-apk'})
        self.deleted = []
        self.uploaded = []
        self.created = 0
        self.private = True
        self.heads_changed = False
        self.assets = {}
        for n, a in enumerate(CONFIG['archive']):
            name = 'actions-' + str(a['id']) + '-' + a['name'] + '.zip'
            self.assets[name] = {'id': n + 9000, 'name': name, 'size': a['size'], 'state': 'uploaded',
                                 'digest': a['digest'], 'browser_download_url': 'https://github.com/private/' + name}

    def __call__(self, path, method='GET', body=None):
        if method == 'DELETE':
            key = int(path.rsplit('/', 1)[-1])
            self.deleted.append(key)
            del self.source[key]
            return None
        if path == '':
            return {'id': CONFIG['repository_id'], 'private': self.private}
        if path == '/branches/main':
            return {'commit': {'sha': CONFIG['main']}}
        if path == '/pulls/45':
            return {'head': {'sha': 'wrong' if self.heads_changed else CONFIG['pr_head'],
                            'repo': {'id': CONFIG['repository_id']}}, 'base': {'repo': {'id': CONFIG['repository_id']}},
                    'state': 'open', 'merged': False}
        if path.startswith('/actions/artifacts?'):
            page = int(path.rsplit('=', 1)[-1])
            values = sorted(self.source.values(), key=lambda a: a['id'])
            return {'total_count': len(values), 'artifacts': values[(page - 1) * 100:page * 100]}
        if path == '/releases?per_page=100':
            return [{'id': 99, 'tag_name': CONFIG['archive_tag'], 'target_commitish': CONFIG['main'],
                     'draft': True, 'html_url': 'https://github.com/private/archive'}]
        if path.startswith('/releases/99/assets?'):
            page = int(path.rsplit('=', 1)[-1])
            values = sorted(self.assets.values(), key=lambda a: a['name'])
            return values[(page - 1) * 100:page * 100]
        raise AssertionError('Unexpected fake operation: ' + method + ' ' + path)

    def upload(self, release_id, name, path, content_type):
        self.uploaded.append(name)
        result = {'id': 77777, 'name': name, 'size': path.stat().st_size, 'state': 'uploaded',
                  'digest': worker.sha256(path), 'browser_download_url': 'https://github.com/private/' + name}
        self.assets[name] = result
        return result

    def download(self, path, destination):
        raise AssertionError('Unexpected fake download')


class SafetyTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.env = mock.patch.dict(os.environ, {'GITHUB_REPOSITORY': CONFIG['repository'],
                'GITHUB_REF': 'refs/heads/' + CONFIG['branch'], 'RUNNER_TEMP': self.temp.name,
                'GITHUB_STEP_SUMMARY': str(Path(self.temp.name) / 'summary'), 'GITHUB_API_URL': 'https://api.github.com',
                'ARTIFACT_CLEANUP_TOKEN': 'test-token'})
        self.env.start()
        self.addCleanup(self.env.stop)

    def run_worker(self, fake, config=CONFIG):
        with contextlib.redirect_stdout(io.StringIO()):
            return worker.run(config, fake)

    def fails_without_delete(self, fake):
        with self.assertRaises(RuntimeError):
            self.run_worker(fake)
        self.assertEqual(fake.deleted, [])

    def test_success_preserves_every_unselected_artifact_and_retries_idempotently(self):
        fake = Fake()
        before = {k: copy.deepcopy(v) for k, v in fake.source.items()
                  if k not in {a['id'] for a in CONFIG['archive'] + CONFIG['remove']}}
        result = self.run_worker(fake)
        self.assertEqual(result['deleted_count'], 529)
        self.assertEqual(result['archived_count'], 172)
        self.assertEqual(fake.source, before)
        self.assertEqual(self.run_worker(fake)['deleted_count'], 0)

    def test_changed_selected_digest_stops_before_any_mutation(self):
        fake = Fake()
        fake.source[CONFIG['remove'][0]['id']]['digest'] = 'sha256:' + '0' * 64
        self.fails_without_delete(fake)
        self.assertEqual(fake.uploaded, [])

    def test_missing_protected_original_stops(self):
        fake = Fake()
        del fake.source[CONFIG['protected'][0]['id']]
        self.fails_without_delete(fake)

    def test_changed_pr_head_stops(self):
        fake = Fake()
        fake.heads_changed = True
        self.fails_without_delete(fake)

    def test_public_repository_cannot_receive_the_archive(self):
        fake = Fake()
        fake.private = False
        self.fails_without_delete(fake)

    def test_wrong_backup_digest_stops(self):
        fake = Fake()
        next(iter(fake.assets.values()))['digest'] = 'sha256:' + '0' * 64
        self.fails_without_delete(fake)

    def test_missing_original_without_backup_stops(self):
        fake = Fake()
        original = CONFIG['archive'][0]
        del fake.source[original['id']]
        del fake.assets['actions-' + str(original['id']) + '-' + original['name'] + '.zip']
        self.fails_without_delete(fake)

    def test_bad_index_upload_prevents_all_deletions(self):
        fake = Fake()
        upload = fake.upload
        def bad_upload(*args):
            result = upload(*args)
            result['digest'] = 'sha256:' + '0' * 64
            return result
        fake.upload = bad_upload
        self.fails_without_delete(fake)

    def test_head_change_after_backup_prevents_all_deletions(self):
        fake = Fake()
        upload = fake.upload
        def changing_upload(*args):
            result = upload(*args)
            fake.heads_changed = True
            return result
        fake.upload = changing_upload
        self.fails_without_delete(fake)

    def test_overlapping_manifest_is_rejected(self):
        fake = Fake()
        config = copy.deepcopy(CONFIG)
        config['remove'][0] = config['protected'][0]
        with self.assertRaises(RuntimeError):
            self.run_worker(fake, config)
        self.assertEqual(fake.deleted, [])

    def test_insufficient_api_budget_prevents_deletion(self):
        fake = Fake()
        fake.remaining = 1
        self.fails_without_delete(fake)

    def test_original_download_is_hash_checked_before_upload(self):
        fake = Fake()
        selected = CONFIG['archive'][0]
        name = 'actions-' + str(selected['id']) + '-' + selected['name'] + '.zip'
        del fake.assets[name]
        fake.download = lambda path, destination: destination.write_bytes(b'incorrect source')
        with self.assertRaises(RuntimeError):
            worker.backup_one(fake, CONFIG, selected, {'id': 99}, fake.assets, fake.source, Path(self.temp.name))
        self.assertEqual(fake.uploaded, [])

    def test_real_zip_checksum_and_crc_path(self):
        fake = Fake()
        zipped = Path(self.temp.name) / 'source.zip'
        with zipfile.ZipFile(zipped, 'w') as z:
            z.writestr('synthetic.txt', 'synthetic test data')
        expected = {**CONFIG['archive'][0], 'size': zipped.stat().st_size, 'digest': worker.sha256(zipped)}
        name = 'actions-' + str(expected['id']) + '-' + expected['name'] + '.zip'
        del fake.assets[name]
        fake.download = lambda path, dest: dest.write_bytes(zipped.read_bytes())
        result = worker.backup_one(fake, CONFIG, expected, {'id': 99}, fake.assets, fake.source, Path(self.temp.name))
        self.assertEqual(result['backup_digest'], expected['digest'])
        self.assertEqual(fake.uploaded, [name])

    def test_storage_redirect_does_not_receive_authentication(self):
        client = worker.Api(CONFIG)
        class Redirect(io.BytesIO):
            code = 302
            headers = {'Location': 'https://storage.example.invalid/blob?signature=test'}
        client.request = lambda *args, **kwargs: Redirect()
        requests = []
        def unauthed_download(req, **kwargs):
            requests.append(req)
            return io.BytesIO(b'test')
        with mock.patch.object(worker.urllib.request, 'urlopen', unauthed_download):
            client.download('/actions/artifacts/1/zip', Path(self.temp.name) / 'redirect.zip')
        self.assertNotIn('Authorization', requests[0].headers)
        self.assertEqual((Path(self.temp.name) / 'redirect.zip').read_bytes(), b'test')


if __name__ == '__main__':
    unittest.main(verbosity=2)
