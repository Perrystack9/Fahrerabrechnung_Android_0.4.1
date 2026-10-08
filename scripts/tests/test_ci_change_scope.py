"""Exercise CI routing against real Git changes, especially deletions and renames."""

import importlib.util
import os
from pathlib import Path
import subprocess
import tempfile
import unittest


spec = importlib.util.spec_from_file_location(
    "classify_ci_changes", Path(__file__).parents[1] / "classify_ci_changes.py"
)
scope = importlib.util.module_from_spec(spec)
spec.loader.exec_module(scope)
SCHEMA_CHECK = Path(__file__).parents[1] / "verify_room_schemas.sh"


class GitChangeScopeTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.previous = Path.cwd()
        os.chdir(self.root)
        self.addCleanup(os.chdir, self.previous)
        self.git("init", "-q")
        self.git("config", "user.email", "ci-fixture@example.invalid")
        self.git("config", "user.name", "CI fixture")
        self.write("README.md", "documentation\n")
        self.write("docs/test.md", "test instructions\n")
        self.write("app/src/main/Example.kt", "product\n")
        self.write("data/schemas/11.json", "{}\n")
        self.base = self.commit()

    def git(self, *args):
        return subprocess.check_output(["git", *args], text=True).strip()

    def write(self, path, text):
        file = self.root / path
        file.parent.mkdir(parents=True, exist_ok=True)
        file.write_text(text)

    def commit(self):
        self.git("add", "-A")
        self.git("commit", "-qm", "fixture")
        return self.git("rev-parse", "HEAD")

    def test_documentation_edit_is_lightweight(self):
        self.write("README.md", "updated\n")
        self.write("docs/test.md", "updated instructions\n")
        self.assertFalse(scope.android_required(self.base, self.commit()))

    def test_documentation_deletion_is_lightweight(self):
        (self.root / "docs/test.md").unlink()
        self.assertFalse(scope.android_required(self.base, self.commit()))

    def test_product_edit_always_runs_android(self):
        self.write("app/src/main/Example.kt", "changed product\n")
        self.assertTrue(scope.android_required(self.base, self.commit()))

    def test_product_rename_into_docs_still_runs_android(self):
        self.git("mv", "app/src/main/Example.kt", "docs/example.md")
        self.assertTrue(scope.android_required(self.base, self.commit()))

    def test_mixed_docs_and_schema_change_runs_android(self):
        self.write("README.md", "updated\n")
        self.write("data/schemas/11.json", '{"changed": true}\n')
        self.assertTrue(scope.android_required(self.base, self.commit()))

    def test_ci_script_workflow_and_embedded_markdown_are_not_documentation(self):
        for path in [".github/workflows/check.yml", "scripts/helper.py", "app/src/main/assets/help.md"]:
            with self.subTest(path=path):
                self.write(path, "new content\n")
                self.assertTrue(scope.android_required(self.base, self.commit()))

    def test_missing_base_dispatch_and_initial_push_run_everything(self):
        for base in ["", "0" * 40, "f" * 40]:
            with self.subTest(base=base):
                self.assertTrue(scope.android_required(base, self.base))

    def test_empty_diff_runs_everything(self):
        self.assertTrue(scope.android_required(self.base, self.base))

    def test_schema_export_check_accepts_clean_tree(self):
        result = subprocess.run(["bash", str(SCHEMA_CHECK), str(self.root)], capture_output=True)
        self.assertEqual(result.returncode, 0, result.stderr)

    def test_schema_export_check_rejects_changed_and_missing_exports(self):
        self.write("data/schemas/11.json", '{"changed": true}\n')
        changed = subprocess.run(["bash", str(SCHEMA_CHECK), str(self.root)], capture_output=True)
        self.assertNotEqual(changed.returncode, 0)
        self.write("data/schemas/11.json", "{}\n")
        self.write("data/schemas/12.json", "{}\n")
        missing = subprocess.run(["bash", str(SCHEMA_CHECK), str(self.root)], capture_output=True)
        self.assertNotEqual(missing.returncode, 0)


if __name__ == "__main__":
    unittest.main()
