#!/usr/bin/env python3
"""
Automates version synchronization, changelog.md, app/src/main/assets/whatsnew.json,
README.md version badge, and GitHub Release notes extraction for Nota Kasir Pro.
Uses Python 3 standard library only (zero external dependencies).
"""

from __future__ import annotations

import argparse
import datetime
import json
import os
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
GRADLE_FILE = ROOT / "app" / "build.gradle.kts"
VIEWMODEL_FILE = ROOT / "app" / "src" / "main" / "java" / "com" / "eco" / "nota" / "models" / "NotaViewModel.kt"
CHANGELOG_FILE = ROOT / "changelog.md"
WHATSNEW_FILE = ROOT / "app" / "src" / "main" / "assets" / "whatsnew.json"
README_FILE = ROOT / "README.md"


def run_git(args: list[str]) -> str:
    try:
        res = subprocess.run(
            ["git", *args],
            cwd=ROOT,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            check=False,
        )
        return res.stdout.strip()
    except Exception:
        return ""


def read_gradle_version() -> tuple[int, str]:
    content = GRADLE_FILE.read_text(encoding="utf-8")
    code_match = re.search(r"versionCode\s*=\s*(\d+)", content)
    name_match = re.search(r'versionName\s*=\s*"([^"]+)"', content)
    if not code_match or not name_match:
        raise RuntimeError("Could not find versionCode or versionName in app/build.gradle.kts")
    return int(code_match.group(1)), name_match.group(1).strip()


def tag_exists(tag: str) -> bool:
    if not tag:
        return False
    local = run_git(["tag", "-l", tag])
    if local == tag:
        return True
    remote = run_git(["ls-remote", "--tags", "origin", f"refs/tags/{tag}"])
    return bool(remote.strip())


def bump_semver(version: str, mode: str) -> str:
    parts = [int(x) if x.isdigit() else 0 for x in version.split(".")]
    while len(parts) < 3:
        parts.append(0)
    major, minor, patch = parts[0], parts[1], parts[2]
    if mode == "major":
        return f"{major + 1}.0.0"
    if mode == "minor":
        return f"{major}.{minor + 1}.0"
    if mode == "patch":
        return f"{major}.{minor}.{patch + 1}"
    return version


def update_version_files(new_code: int, new_version: str) -> list[str]:
    changed: list[str] = []

    gradle_text = GRADLE_FILE.read_text(encoding="utf-8")
    updated_gradle = re.sub(r"versionCode\s*=\s*\d+", f"versionCode = {new_code}", gradle_text, count=1)
    updated_gradle = re.sub(r'versionName\s*=\s*"[^"]+"', f'versionName = "{new_version}"', updated_gradle, count=1)
    if updated_gradle != gradle_text:
        GRADLE_FILE.write_text(updated_gradle, encoding="utf-8")
        changed.append(str(GRADLE_FILE.relative_to(ROOT)))

    if VIEWMODEL_FILE.exists():
        vm_text = VIEWMODEL_FILE.read_text(encoding="utf-8")
        updated_vm = re.sub(
            r'pInfo\.versionName\s*\?:\s*"[^"]+"',
            f'pInfo.versionName ?: "{new_version}"',
            vm_text,
            count=1,
        )
        updated_vm = re.sub(
            r'(catch\s*\([^)]+\)\s*\{\s*")[^"]+("\s*\})',
            rf'\g<1>{new_version}\g<2>',
            updated_vm,
            count=1,
        )
        if updated_vm != vm_text:
            VIEWMODEL_FILE.write_text(updated_vm, encoding="utf-8")
            changed.append(str(VIEWMODEL_FILE.relative_to(ROOT)))

    if README_FILE.exists():
        readme_text = README_FILE.read_text(encoding="utf-8")
        updated_readme = re.sub(
            r"Version-v[0-9]+\.[0-9]+\.[0-9]+",
            f"Version-v{new_version}",
            readme_text,
        )
        updated_readme = re.sub(
            r"versi terbaru \(\*\*v[0-9]+\.[0-9]+\.[0-9]+\*\*\)",
            f"versi terbaru (**v{new_version}**)",
            updated_readme,
        )
        if updated_readme != readme_text:
            README_FILE.write_text(updated_readme, encoding="utf-8")
            changed.append(str(README_FILE.relative_to(ROOT)))

    return changed


def collect_commit_bullets(custom_notes: str | None) -> list[str]:
    if custom_notes and custom_notes.strip():
        lines = [line.strip().lstrip("-*• ").strip() for line in custom_notes.splitlines() if line.strip()]
        if lines:
            return lines

    last_tag = run_git(["describe", "--tags", "--abbrev=0"])
    log_range = f"{last_tag}..HEAD" if last_tag else "-n 10"
    raw = run_git(["log", log_range, "--no-merges", "--pretty=format:%s"])
    commits: list[str] = []
    skip_prefixes = ("chore(release):", "ci(release):", "[skip ci]", "merge ")
    for line in raw.splitlines():
        msg = line.strip()
        lower = msg.lower()
        if not msg or any(lower.startswith(p) or "[skip ci]" in lower for p in skip_prefixes):
            continue
        if msg not in commits:
            commits.append(msg)

    if not commits:
        commits = ["Pembaruan aplikasi, peningkatan stabilitas, dan optimasi performa."]
    return commits[:15]


def extract_changelog_section(version: str) -> str | None:
    if not CHANGELOG_FILE.exists():
        return None
    content = CHANGELOG_FILE.read_text(encoding="utf-8")
    pattern = re.compile(
        rf"^##\s+\[v{re.escape(version)}\][^\n]*\n(.*?)(?=\n---(?:\n|\Z)|\n##\s+\[|\Z)",
        re.MULTILINE | re.DOTALL,
    )
    match = pattern.search(content)
    if match:
        body = match.group(1).strip()
        if body:
            return body
    return None


def prepend_changelog(version: str, bullets: list[str], title_suffix: str) -> tuple[bool, str]:
    existing = extract_changelog_section(version)
    if existing:
        return False, existing

    today = datetime.date.today().isoformat()
    bullet_block = "\n".join(f"- {b}" for b in bullets)
    new_section = f"## [v{version}] — {title_suffix} ({today})\n{bullet_block}\n\n---\n\n"

    if CHANGELOG_FILE.exists():
        old_text = CHANGELOG_FILE.read_text(encoding="utf-8")
        marker = "\n---\n\n"
        if marker in old_text:
            head, tail = old_text.split(marker, 1)
            updated = f"{head}{marker}{new_section}{tail}"
        else:
            updated = new_section + old_text
    else:
        updated = f"# Changelog — Nota Kasir Pro\n\n---\n\n{new_section}"

    CHANGELOG_FILE.write_text(updated, encoding="utf-8")
    return True, bullet_block


def ensure_whatsnew(version: str, bullets: list[str], title_prefix: str) -> bool:
    if WHATSNEW_FILE.exists():
        try:
            entries = json.loads(WHATSNEW_FILE.read_text(encoding="utf-8"))
            if not isinstance(entries, list):
                entries = []
        except Exception:
            entries = []
    else:
        WHATSNEW_FILE.parent.mkdir(parents=True, exist_ok=True)
        entries = []

    if any(str(item.get("version", "")).strip() == version for item in entries if isinstance(item, dict)):
        return False

    summary = "; ".join(b.rstrip(".") for b in bullets[:4])
    if len(summary) > 260:
        summary = summary[:257].rstrip() + "..."
    if not summary.endswith("."):
        summary += "."

    new_entry = {
        "version": version,
        "icon": "new_releases",
        "title": f"{title_prefix} (v{version})",
        "description": summary,
    }
    entries.insert(0, new_entry)
    WHATSNEW_FILE.write_text(json.dumps(entries, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    return True


def write_github_output(values: dict[str, str]) -> None:
    gh_output = os.environ.get("GITHUB_OUTPUT")
    if not gh_output:
        return
    with open(gh_output, "a", encoding="utf-8") as fh:
        for k, v in values.items():
            fh.write(f"{k}={v}\n")


def main() -> int:
    parser = argparse.ArgumentParser(description="Auto update version, changelog.md, and whatsnew.json")
    parser.add_argument("--bump", choices=["auto", "none", "patch", "minor", "major"], default="auto")
    parser.add_argument("--notes", default="", help="Optional custom release notes")
    parser.add_argument("--title", default="Pembaruan Aplikasi", help="Section title when auto-generating notes")
    parser.add_argument("--output-notes", default="", help="Optional file path to write GitHub Release body")
    args = parser.parse_args()

    current_code, current_version = read_gradle_version()
    new_version = current_version
    new_code = current_code

    if args.bump == "auto":
        if tag_exists(f"v{current_version}"):
            new_version = bump_semver(current_version, "patch")
            new_code = current_code + 1
    elif args.bump in ("patch", "minor", "major"):
        new_version = bump_semver(current_version, args.bump)
        new_code = current_code + 1

    changed_files: list[str] = []
    if new_version != current_version or new_code != current_code:
        changed_files.extend(update_version_files(new_code, new_version))

    bullets = collect_commit_bullets(args.notes)
    changelog_updated, section_body = prepend_changelog(new_version, bullets, args.title)
    if changelog_updated:
        changed_files.append(str(CHANGELOG_FILE.relative_to(ROOT)))

    whatsnew_updated = ensure_whatsnew(new_version, bullets, args.title)
    if whatsnew_updated:
        changed_files.append(str(WHATSNEW_FILE.relative_to(ROOT)))

    release_body = (
        f"## Nota Kasir Pro v{new_version} (Build {new_code})\n\n"
        f"### Catatan Pembaruan\n{section_body}\n\n"
        f"---\n"
        f"- **In-App Update**: Pengguna aplikasi versi sebelumnya dapat langsung mengunduh & memasang pembaruan ini dari dalam aplikasi.\n"
        f"- **Instal Manual**: Unduh file `Nota-Kasir-Pro-v{new_version}.apk` di bagian **Assets** di bawah ini."
    )

    if args.output_notes:
        Path(args.output_notes).write_text(release_body + "\n", encoding="utf-8")

    tag = f"v{new_version}"
    has_repo_changes = "true" if changed_files else "false"
    write_github_output(
        {
            "version": new_version,
            "version_code": str(new_code),
            "tag": tag,
            "has_repo_changes": has_repo_changes,
            "changed_files": " ".join(changed_files),
        }
    )

    print(f"Target Release: {tag} (versionCode={new_code})")
    print(f"Updated files: {changed_files or 'none (already up to date)'}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
