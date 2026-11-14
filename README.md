# Distributed Observer experiment

This repository contains the Java Ice client/server implementation for the
SENG 350 Need 1 partial advancement. The project is hosted on GitHub.

## Build

```sh
gradle build
```

The client captures a screen image for every message. Its arguments are:

```text
client.jar <bus-id> <images> <delay-ms> <critical-percentage>
```

The server stores every image below `data/serverImages/<bus-id>/` using
`<tag>-YYYY-MM-DD-HH-mm-ss-SSS.jpg`. GENERALINTEREST is broadcast to all
legacy subscribers; CRITICAL sends `ACTION for <bus-id>` only to the
originating bus.

## Thread-pool experiment

Run one server with four clients for each configured pool size:

```sh
./script.sh 600 0 100
```

The four configurations are in `server/configs/`, and client latency and
throughput measurements are written to the generated client logs.
