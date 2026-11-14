#!/usr/bin/env bash
set -euo pipefail

IMAGE_COUNT="${IMAGE_COUNT:-600}"
RUNS="${RUNS:-3}"
FREQUENCY_MS="${FREQUENCY_MS:-0}"
OUTPUT_CSV="${OUTPUT_CSV:-experiments/results.csv}"

mkdir -p "$(dirname "$OUTPUT_CSV")"
printf 'thread_pool_size,run,client,average_send_latency_ms,throughput_msg_per_sec\n' > "$OUTPUT_CSV"

for size in 1 4 8 12; do
    for run in $(seq 1 "$RUNS"); do
        rm -rf yourTeam#
        log_file="experiments/server-${size}-${run}.log"
        java -jar server/build/libs/server.jar "--config=server-threadpool-${size}.cfg" > "$log_file" 2>&1 &
        server_pid=$!
        trap 'kill "$server_pid" 2>/dev/null || true' EXIT
        sleep 2

        client_logs=()
        client_pids=()
        for bus_id in 1 2 3 4; do
            client_log="experiments/client-${size}-${run}-${bus_id}.log"
            client_logs+=("$client_log")
            java -jar client/build/libs/client.jar "$bus_id" "$IMAGE_COUNT" \
                "$FREQUENCY_MS" 100 > "$client_log" 2>&1 &
            client_pids+=("$!")
        done

        for pid in "${client_pids[@]}"; do
            wait "$pid"
        done
        for bus_id in 1 2 3 4; do
            client_log="${client_logs[$((bus_id - 1))]}"
            summary=$(grep 'Client summary:' "$client_log" | tail -n 1 || true)
            latency=$(printf '%s\n' "$summary" | sed -n 's/.*averageSendLatencyMs=\([^ ]*\).*/\1/p')
            throughput=$(printf '%s\n' "$summary" | sed -n 's/.*throughput=\([^ ]*\).*/\1/p')
            printf '%s,%s,%s,%s,%s\n' "$size" "$run" "$bus_id" \
                "${latency:-NA}" "${throughput:-NA}" >> "$OUTPUT_CSV"
        done
        kill "$server_pid" 2>/dev/null || true
        sleep 1
        trap - EXIT
    done
done

echo "Experiment results written to $OUTPUT_CSV"