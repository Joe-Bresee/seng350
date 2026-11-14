# SENG 350 Distributed Observer Implementation

This project implements the distributed Observer pattern with ZeroC Ice. The
control center stores every received image under `yourTeam#/<busId>/`, then
notifies all subscribed buses for `GENERALINTEREST` or only the originating bus
for `CRITICAL`.

## Build

Use the included Gradle wrapper:

```bash
./gradlew clean build
```

The build requires the Ice Slice compiler. The Gradle configuration accepts
`-PiceHome=/path/to/ice` or `ICE_HOME`; it also checks `$HOME/.local/ice`.

## Run

Start the control center in one terminal:

```bash
./scripts/run-server.sh
```

Start a bus in another terminal:

```bash
java -jar client/build/libs/client.jar <busId> <imageCount> <frequencyMs> <criticalPercentage>
```

For example, this sends ten images with every image tagged `CRITICAL`:

```bash
java -jar client/build/libs/client.jar 1 10 0 100
```

The client captures the desktop when a graphical environment is available and
otherwise uses an image from `data/<busId>/` as the payload source.

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