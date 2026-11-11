#!/usr/bin/env bash

set -euo pipefail

SRC_DIR="${1:-.}"
OUT_FILE="${2:-project_dump.txt}"

EXCLUDE_DIRS=(
  "*/target/*"
  "*/build/*"
  "*/.git/*"
  "*/.idea/*"
  "*/.mvn/*"
  "*/node_modules/*"
  "*/out/*"
)

INCLUDE_PATTERNS=(
  "*.java"
  "*.yml"
  "*.yaml"
  "*.xml"
  "*.properties"
  "*.feature"
)

exclude_args=()
for dir in "${EXCLUDE_DIRS[@]}"; do
  exclude_args+=(-not -path "$dir")
done

include_args=()
for i in "${!INCLUDE_PATTERNS[@]}"; do
  if [ "$i" -gt 0 ]; then
    include_args+=(-o)
  fi
  include_args+=(-name "${INCLUDE_PATTERNS[$i]}")
done

: > "$OUT_FILE"

count=0

while IFS= read -r -d '' file; do
  rel_path="${file#"$SRC_DIR"/}"
  {
    echo "================================================================================"
    echo "FILE: $rel_path"
    echo "================================================================================"
    cat "$file"
    echo ""
    echo ""
  } >> "$OUT_FILE"
  count=$((count + 1))
done < <(find "$SRC_DIR" -type f \( "${include_args[@]}" \) "${exclude_args[@]}" -print0 | sort -z)

echo "Done. Concatenated $count files into: $OUT_FILE"
