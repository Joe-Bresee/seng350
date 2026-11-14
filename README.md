# SENG 350 Distributed Observer Implementation

This project implements the distributed Observer pattern with ZeroC Ice. The
control center stores every received image under `Group9/<busId>/`. then
notifies all subscribed buses for `GENERALINTEREST` or only the original sending bus
for `CRITICAL`.

## Build

```bash
./gradlew clean build
```

## Run

Start the control center in one terminal:

```bash
./scripts/run-server.sh
```

Start a bus in another terminal:

```bash
java -Djava.awt.headless=true \
  -jar client/build/libs/client.jar <busId> <imageCount> <frequencyMs> <criticalPercentage>
```

For example, this sends ten images with every image tagged `CRITICAL`:

```bash
./scripts/run-clients.sh 10 0 100
```

The client captures the desktop when a graphical environment is available. In
headless mode, it renders a non-empty 1280x720 frame off-screen with
`BufferedImage` and `Graphics2D`, then JPEG-encodes it before sending.
The runner scripts use `-Djava.awt.headless=true`, so they do not require X11,
Wayland, Xvfb, or screen-sharing permissions.

## Experiments

The experiment runner starts four clients, each sending 600 images tagged
`CRITICAL`, for thread-pool sizes 1, 4, 8, and 12. It runs each configuration
three times by default and writes client latency and throughput to CSV:

```bash
./scripts/build.sh
./scripts/run-experiments.sh
```

Change the number of repetitions or output location with environment variables:

```bash
RUNS=5 OUTPUT_CSV=experiments/results.csv ./scripts/run-experiments.sh
```

The four configurations are in `server/src/main/resources/` and are selected
by the runner with `--config=server-threadpool-<size>.cfg`.

Received images are stored under `Group9/<busId>/`. The experiment runner
clears `Group9/` before each thread-pool/run combination.