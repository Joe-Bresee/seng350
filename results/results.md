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
