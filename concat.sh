#!/usr/bin/env bash
#
# concat_project.sh
#
# Concatenates all Java project source/config files (.java, .yml, .yaml, .xml,
# .properties) into a single output file, with a header before each file
# showing its relative path, for easy sharing/reviewing.
#
# Usage:
#   ./concat_project.sh [source_dir] [output_file]
#
# Defaults:
#   source_dir  = current directory (.)
#   output_file = project_dump.txt
#
# Examples:
#   ./concat_project.sh
#   ./concat_project.sh ~/code/gym-crm all_sources.txt

set -euo pipefail

SRC_DIR="${1:-.}"
OUT_FILE="${2:-project_dump.txt}"

# Directories to skip entirely (build output, VCS, IDE metadata, deps)
EXCLUDE_DIRS=(
  "*/target/*"
  "*/build/*"
  "*/.git/*"
  "*/.idea/*"
  "*/.mvn/*"
  "*/node_modules/*"
  "*/out/*"
)

# File extensions to include
INCLUDE_PATTERNS=(
  "*.java"
  "*.yml"
  "*.yaml"
  "*.xml"
  "*.properties"
  "*.feature"
)

# Build the `find` exclude args
exclude_args=()
for dir in "${EXCLUDE_DIRS[@]}"; do
  exclude_args+=(-not -path "$dir")
done

# Build the `find` include args (as an -o joined group)
include_args=()
for i in "${!INCLUDE_PATTERNS[@]}"; do
  if [ "$i" -gt 0 ]; then
    include_args+=(-o)
  fi
  include_args+=(-name "${INCLUDE_PATTERNS[$i]}")
done

# Reset output file
: > "$OUT_FILE"

count=0

# Use -print0/read -d '' to handle filenames with spaces safely
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
