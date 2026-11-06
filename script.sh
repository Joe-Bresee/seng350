#!/bin/bash

set -e

echo " Starting Performance Quality Attribute Experimentation  "

TOTAL_IMAGES_PER_CLIENT=600
FREQUENCY_MS=200
GI_PROBABILITY=0

TOTAL_EXPECTED_MSGS=$(( TOTAL_IMAGES_PER_CLIENT * 3 ))

gradle build

java -jar server/build/libs/server.jar "$TOTAL_EXPECTED_MSGS" > server_run.log 2>&1 &
SERVER_PID=$!
sleep 2.5

java -jar client/build/libs/client.jar 1 "$TOTAL_IMAGES_PER_CLIENT" "$FREQUENCY_MS" "$GI_PROBABILITY" &
C1_PID=$!
java -jar client/build/libs/client.jar 2 "$TOTAL_IMAGES_PER_CLIENT" "$FREQUENCY_MS" "$GI_PROBABILITY" &
C2_PID=$!
java -jar client/build/libs/client.jar 3 "$TOTAL_IMAGES_PER_CLIENT" "$FREQUENCY_MS" "$GI_PROBABILITY" &
C3_PID=$!

wait $C1_PID
wait $C2_PID
wait $C3_PID
echo "Completed, see analysis"

sleep 1.0

echo ""
cat server_run.log | grep -A 4 "=== EXPERIMENT RUN RESULTS ===" || echo "Error: Profiler results missing from server logs."

if kill -0 $SERVER_PID 2>/dev/null; then
    kill -9 $SERVER_PID
fi

# rm -f server_run.log
echo "Experiment execution successfully processed."
