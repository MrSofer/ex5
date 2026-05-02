#!/bin/bash
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

COMPILER="${COMPILER:-$REPO_DIR/ex5/COMPILER}"
TESTS_DIR="${TESTS_DIR:-$SCRIPT_DIR/tests}"
EXPECTED_DIR="${EXPECTED_DIR:-$SCRIPT_DIR/expected_output}"
SPIM_BANNER='SPIM Version 8.0 of January 8, 2010
Copyright 1990-2010, James R. Larus.
All Rights Reserved.
See the file README for a full copyright notice.
Loaded: /usr/lib/spim/exceptions.s'
PASS=0; FAIL=0; FAILED=""
TMP_DIR=$(mktemp -d)
trap "rm -rf $TMP_DIR" EXIT
for test_file in $(ls "$TESTS_DIR"/TEST_*.txt | sort -t_ -k2 -n); do
    name=$(basename "$test_file" .txt)
    expected_file="$EXPECTED_DIR/${name}_Expected_Output.txt"
    [ ! -f "$expected_file" ] && { FAIL=$((FAIL+1)); FAILED="$FAILED\n  $name: Missing expected"; continue; }
    asm_file="$TMP_DIR/${name}.s"
    java -jar "$COMPILER" "$test_file" "$asm_file" 2>/dev/null
    [ ! -f "$asm_file" ] && { FAIL=$((FAIL+1)); FAILED="$FAILED\n  $name: No compiler output"; continue; }
    compiler_output=$(cat "$asm_file")
    expected_content=$(cat "$expected_file")
    expected_raw=$(cat "$expected_file" | od -An -tx1 | tr -d ' \n')
    if [ "$compiler_output" = "ERROR" ] || echo "$compiler_output" | grep -qE "^ERROR\([0-9]+\)$" || [ "$compiler_output" = "Register Allocation Failed" ]; then
        actual_raw=$(cat "$asm_file" | od -An -tx1 | tr -d ' \n')
        [ "$actual_raw" = "$expected_raw" ] && PASS=$((PASS+1)) || { FAIL=$((FAIL+1)); FAILED="$FAILED\n  $name: Got '$compiler_output' expected '$expected_content'"; }
        rm -f "$asm_file"; continue
    fi
    spim_output=$(echo "" | spim -file "$asm_file" 2>&1)
    program_output=$(echo "$spim_output" | sed -n '/^Loaded:/,$p' | tail -n +2)
    actual_output=$(printf '%s\n%s' "$SPIM_BANNER" "$program_output")
    actual_raw=$(printf '%s' "$actual_output" | od -An -tx1 | tr -d ' \n')
    [ "$actual_raw" = "$expected_raw" ] && PASS=$((PASS+1)) || { FAIL=$((FAIL+1)); FAILED="$FAILED\n  $name: Output mismatch"; }
    rm -f "$asm_file"
done
echo "=== Results ==="; echo "Passed: $PASS"; echo "Failed: $FAIL"; echo "Total: $((PASS+FAIL))"
[ -n "$FAILED" ] && echo -e "Failed:$FAILED"
