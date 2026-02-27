#!/bin/bash
# Format TypeScript/JavaScript files using Prettier
# Usage: ./format-ts-code.sh <file_path>

# Read JSON input from stdin if no arguments provided (PostToolUse hook pattern)
if [ $# -eq 0 ]; then
  INPUT_JSON=$(cat)
  # Extract file path using python for reliability
  FILE_PATH=$(echo "$INPUT_JSON" | python3 -c "import sys, json; print(json.load(sys.stdin).get('tool_input', {}).get('file_path', ''))")
else
  FILE_PATH="$1"
fi

# Check if file path is empty
if [ -z "$FILE_PATH" ]; then
    exit 0
fi

# Check if file exists
if [ ! -f "$FILE_PATH" ]; then
    exit 0
fi

# Check if file extension is supported
if [[ "$FILE_PATH" != *.ts && "$FILE_PATH" != *.tsx && "$FILE_PATH" != *.js && "$FILE_PATH" != *.jsx ]]; then
    exit 0
fi

# Apply formatting using Prettier
if command -v npx >/dev/null 2>&1; then
    npx prettier --write "$FILE_PATH" >/dev/null 2>&1
    echo "Formatted TS/JS file: $FILE_PATH" >&2
fi
