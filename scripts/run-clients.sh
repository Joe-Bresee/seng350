#!/usr/bin/env bash
set -euo pipefail

IMAGE_COUNT="${1:-600}"
FREQUENCY_MS="${2:-0}"
CRITICAL_PERCENTAGE="${3:-100}"
XVFB_SCREEN="${XVFB_SCREEN:-1280x720x24}"

if ! command -v xvfb-run >/dev/null 2>&1; then
    echo "need xvfb-run" >&2
    exit 1
fi

pids=()
for bus_id in 1 2 3 4; do
    xvfb-run -a -s "-screen 0 ${XVFB_SCREEN}" \
        env -u WAYLAND_DISPLAY -u XDG_SESSION_TYPE \
        java -Dawt.toolkit=sun.awt.X11.XToolkit \
        -jar client/build/libs/client.jar "$bus_id" "$IMAGE_COUNT" \
        "$FREQUENCY_MS" "$CRITICAL_PERCENTAGE" &
    pids+=("$!")
done

status=0
for pid in "${pids[@]}"; do
    wait "$pid" || status=1
done
exit "$status"