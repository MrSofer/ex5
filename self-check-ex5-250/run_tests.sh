#!/bin/bash
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

COMPILER="${COMPILER:-$REPO_DIR/ex5/COMPILER}"
TESTS_DIR="${TESTS_DIR:-$SCRIPT_DIR/tests}"
EXPECTED_DIR="${EXPECTED_DIR:-$SCRIPT_DIR/expected_output}"
OUTPUT_DIR="${OUTPUT_DIR:-$REPO_DIR/ex5/output}"
MIPS_FILE="$OUTPUT_DIR/MIPS.txt"
MIPS_OUTPUT_FILE="$OUTPUT_DIR/MIPS_OUTPUT.txt"
PASS=0; FAIL=0; FAILED=""
mkdir -p "$OUTPUT_DIR"
for test_file in $(ls "$TESTS_DIR"/TEST_*.txt | sort -t_ -k2 -n); do
    name=$(basename "$test_file" .txt)
    expected_file="$EXPECTED_DIR/${name}_Expected_Output.txt"
    [ ! -f "$expected_file" ] && { FAIL=$((FAIL+1)); FAILED="$FAILED\n  $name: Missing expected"; continue; }
    java -jar "$COMPILER" "$test_file" "$MIPS_FILE" 2>/dev/null 1>/dev/null
    [ ! -f "$MIPS_FILE" ] && { FAIL=$((FAIL+1)); FAILED="$FAILED\n  $name: No compiler output"; continue; }
    compiler_output=$(cat "$MIPS_FILE")
    expected_content=$(cat "$expected_file")
    expected_raw=$(cat "$expected_file" | od -An -tx1 | tr -d ' \n')
    if [ "$compiler_output" = "ERROR" ] || echo "$compiler_output" | grep -qE "^ERROR\([0-9]+\)$" || [ "$compiler_output" = "Register Allocation Failed" ]; then
        actual_raw=$(cat "$MIPS_FILE" | od -An -tx1 | tr -d ' \n')
        [ "$actual_raw" = "$expected_raw" ] && PASS=$((PASS+1)) || { FAIL=$((FAIL+1)); FAILED="$FAILED\n  $name: Got '$compiler_output' expected '$expected_content'"; }
        continue
    fi
    spim -file "$MIPS_FILE" > "$MIPS_OUTPUT_FILE" 2>&1
    actual_raw=$(cat "$MIPS_OUTPUT_FILE" | od -An -tx1 | tr -d ' \n')
    [ "$actual_raw" = "$expected_raw" ] && PASS=$((PASS+1)) || { FAIL=$((FAIL+1)); FAILED="$FAILED\n  $name: Output mismatch"; }
done
echo "=== Results ==="; echo "Passed: $PASS"; echo "Failed: $FAIL"; echo "Total: $((PASS+FAIL))"
[ -n "$FAILED" ] && echo -e "Failed:$FAILED"
