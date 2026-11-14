#!/usr/bin/env bash
set -euo pipefail

CONFIG_NAME="${1:-server.cfg}"
exec java -jar server/build/libs/server.jar "--config=${CONFIG_NAME}"