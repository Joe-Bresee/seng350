# SENG 350 Distributed Observer Implementation

This project implements the distributed Observer pattern with ZeroC Ice. The
control center stores every received image under `Group9/<busId>/`, then
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
xvfb-run -a -s "-screen 0 1280x720x24" \
  env -u WAYLAND_DISPLAY -u XDG_SESSION_TYPE \
  java -Dawt.toolkit=sun.awt.X11.XToolkit \
  -jar client/build/libs/client.jar <busId> <imageCount> <frequencyMs> <criticalPercentage>
```

For example, this sends ten images with every image tagged `CRITICAL`:

```bash
XVFB_SCREEN=1280x720x24 ./scripts/run-clients.sh 10 0 100
```

The client captures the desktop when a graphical environment is available and
otherwise uses an image from `data/<busId>/` as the payload source.

On Wayland or on systems without a usable X11 session, the client runner
scripts launch each client with `xvfb-run` and capture from a virtual X11
display. Configure its size with `XVFB_SCREEN`, for example
`XVFB_SCREEN=1920x1080x24 ./scripts/run-clients.sh`.

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


# RESULTS


### Test executed

Build:

```bash
./scripts/build.sh
```

Result:

```text
BUILD SUCCESSFUL
```

Experiment:

```bash
XVFB_SCREEN=1280x720x24 ./scripts/run-experiments.sh
```

The script ran:

- 4 server configurations: thread pools `1`, `4`, `8`, `12`
- 3 runs per configuration
- 4 clients per run
- 600 images per client
- 2,400 images per run
- 100% `CRITICAL`
- Xvfb-backed X11 capture without Wayland screen-share prompts

Total:

```text
12 runs
48 client executions
28,800 images sent
28,800 images processed
```

### Verification

Every client log reported:

```text
messages=600
```

Every client sent:

```text
600 CRITICAL
0 NORMAL
```

Each client received `ACTION for BUSID` callbacks. The server logs each reported:

```text
Processed messages: 2400
```

No experiment log contained errors, exceptions, failures, or `NA` results.

The final run stored 2,400 images under:

```text
Group9/1/
Group9/2/
Group9/3/
Group9/4/
```

The filenames were in the expected format, for example:

```text
Group9/1/CRITICAL-2026-11-14-03-27-18-865.jpg
```

### Results summary

Results are saved in `experiments/results.csv`.

| Thread pool | Mean latency | Mean throughput | Samples |
|---:|---:|---:|---:|
| 1 | 0.340 ms | 1,716.22 msg/sec | 12 |
| 4 | 0.381 ms | 1,610.40 msg/sec | 12 |
| 8 | 0.352 ms | 1,739.02 msg/sec | 12 |
| 12 | 0.451 ms | 1,578.92 msg/sec | 12 |

Logs are stored in:

```text
experiments/server-*.log
experiments/client-*.log
```

The server and clients exited cleanly after the experiment.