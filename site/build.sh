#!/usr/bin/env bash
# Assembles the website into the given directory: the static pages, the
# markdown docs they render, and the Scaladoc from `sbt snipy/doc` if built.
set -euo pipefail
here="$(cd "$(dirname "$0")" && pwd)"
repo="$(dirname "$here")"
out="${1:-$repo/_site}"

rm -rf "$out"
mkdir -p "$out/docs"
cp -R "$here"/index.html "$here"/docs.html "$here"/*.css "$here"/*.js "$here"/favicon.svg "$here"/assets "$out"/
rm -f "$out/assets/credits.json"
cp "$repo"/docs/*.md "$out/docs/"
cp "$repo/CHANGELOG.md" "$out/"

api="$(ls -d "$repo"/target/out/native0.5/scala-3.*/snipy/api 2>/dev/null | head -1 || true)"
if [ -n "$api" ]; then cp -R "$api" "$out/api"; else echo "no Scaladoc found; run sbt snipy/doc" >&2; fi
touch "$out/.nojekyll"
echo "site assembled in $out"
