#!/usr/bin/env bash
set -euo pipefail

IMAGE_COUNT="${1:-600}"
FREQUENCY_MS="${2:-0}"
CRITICAL_PERCENTAGE="${3:-100}"

pids=()
for bus_id in 1 2 3 4; do
    java -Djava.awt.headless=true -jar client/build/libs/client.jar "$bus_id" "$IMAGE_COUNT" \
        "$FREQUENCY_MS" "$CRITICAL_PERCENTAGE" &
    pids+=("$!")
done

status=0
for pid in "${pids[@]}"; do
    wait "$pid" || status=1
done
exit "$status"