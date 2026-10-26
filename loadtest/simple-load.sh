#!/bin/bash
# Very simple student load-test script
# Requires: curl + hey (or wrk)
#
# Install hey: go install github.com/rakyll/hey@latest
# Or use wrk if you prefer.

BASE_URL=${1:-http://localhost:8080}
SHORT_CODE=${2:-}

echo "=== URL Shortener Load Test ==="
echo "Target: $BASE_URL"

# 1. Create a short URL if none provided
if [ -z "$SHORT_CODE" ]; then
  echo "Creating a short URL..."
  RESPONSE=$(curl -s -X POST "$BASE_URL/api/shorten" \
    -H "Content-Type: application/json" \
    -d '{"url":"https://www.example.com/load-test-target"}')
  SHORT_CODE=$(echo "$RESPONSE" | grep -o '"shortCode":"[^"]*"' | cut -d'"' -f4)
  echo "Created short code: $SHORT_CODE"
  echo "$RESPONSE"
fi

if [ -z "$SHORT_CODE" ]; then
  echo "Could not get a short code. Exiting."
  exit 1
fi

TARGET="$BASE_URL/$SHORT_CODE"
echo "Load testing: $TARGET"
echo ""

# 2. Run hey if available
if command -v hey &> /dev/null; then
  echo "Using hey (10s, 200 concurrent workers)..."
  hey -z 10s -c 200 -q 0 "$TARGET"
elif command -v wrk &> /dev/null; then
  echo "Using wrk (12 threads, 400 connections, 15s)..."
  wrk -t12 -c400 -d15s "$TARGET"
else
  echo "Neither hey nor wrk found. Falling back to a crude curl loop."
  echo "Install hey or wrk for proper load testing."
  for i in {1..50}; do
    curl -s -o /dev/null -w "%{http_code}\n" "$TARGET" &
  done
  wait
  echo "Done (very light test)."
fi
