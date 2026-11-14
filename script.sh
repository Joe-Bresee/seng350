#!/usr/bin/env bash
set -euo pipefail

images_per_client="${1:-600}"
frequency_ms="${2:-0}"
critical_percentage="${3:-100}"
clients=(1 2 3 4)

gradle build

for size in 1 4 8 12; do
    log="server_run_${size}.log"
    java -jar server/build/libs/server.jar "server/configs/server-${size}.cfg" >"$log" 2>&1 &
    server_pid=$!
    trap 'kill "$server_pid" 2>/dev/null || true' EXIT
    sleep 2

    client_pids=()
    for bus_id in "${clients[@]}"; do
        java -jar client/build/libs/client.jar "$bus_id" "$images_per_client" \
            "$frequency_ms" "$critical_percentage" >"client_${size}_${bus_id}.log" 2>&1 &
        client_pids+=("$!")
    done
    for client_pid in "${client_pids[@]}"; do
        wait "$client_pid"
    done

    kill "$server_pid" 2>/dev/null || true
    wait "$server_pid" 2>/dev/null || true
    trap - EXIT
done

echo "Completed thread-pool experiments. See server_run_{1,4,8,12}.log and client_*.log."
