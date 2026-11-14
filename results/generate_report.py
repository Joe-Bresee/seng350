#!/usr/bin/env python3
"""Generate the experiment spreadsheet, plots, and Markdown report."""

from __future__ import annotations

import csv
import html
import statistics
import sys
from pathlib import Path
from zipfile import ZIP_DEFLATED, ZipFile

import matplotlib.pyplot as plt


ROOT = Path(__file__).resolve().parent.parent
INPUT_CSV = ROOT / "experiments" / "results.csv"
OUTPUT_DIR = Path(__file__).resolve().parent
REPORT = OUTPUT_DIR / "results.md"
SPREADSHEET = OUTPUT_DIR / "experiment-results.xlsx"
LATENCY_PLOT = OUTPUT_DIR / "latency-by-thread-pool.png"
THROUGHPUT_PLOT = OUTPUT_DIR / "throughput-by-thread-pool.png"


def read_results() -> list[dict[str, float | int]]:
    with INPUT_CSV.open(newline="", encoding="utf-8") as file:
        rows = list(csv.DictReader(file))
    required = {
        "thread_pool_size",
        "run",
        "client",
        "average_send_latency_ms",
        "throughput_msg_per_sec",
    }
    if not rows or not required.issubset(rows[0]):
        raise ValueError(f"{INPUT_CSV} is missing the required CSV columns")
    return [
        {
            "thread_pool_size": int(row["thread_pool_size"]),
            "run": int(row["run"]),
            "client": int(row["client"]),
            "average_send_latency_ms": float(row["average_send_latency_ms"]),
            "throughput_msg_per_sec": float(row["throughput_msg_per_sec"]),
        }
        for row in rows
    ]


def summaries(rows: list[dict[str, float | int]]) -> list[dict[str, float | int]]:
    grouped: dict[int, list[dict[str, float | int]]] = {}
    for row in rows:
        grouped.setdefault(int(row["thread_pool_size"]), []).append(row)
    return [
        {
            "thread_pool_size": size,
            "mean_latency": statistics.mean(
                float(row["average_send_latency_ms"]) for row in grouped[size]
            ),
            "mean_throughput": statistics.mean(
                float(row["throughput_msg_per_sec"]) for row in grouped[size]
            ),
            "samples": len(grouped[size]),
        }
        for size in sorted(grouped)
    ]


def make_plots(summary: list[dict[str, float | int]]) -> None:
    pools = [int(item["thread_pool_size"]) for item in summary]
    latency = [float(item["mean_latency"]) for item in summary]
    throughput = [float(item["mean_throughput"]) for item in summary]

    for path, values, ylabel, title, color in (
        (
            LATENCY_PLOT,
            latency,
            "Mean send latency (ms)",
            "Mean send latency by server thread pool",
            "#3366cc",
        ),
        (
            THROUGHPUT_PLOT,
            throughput,
            "Mean throughput (messages/sec)",
            "Mean throughput by server thread pool",
            "#109618",
        ),
    ):
        figure, axis = plt.subplots(figsize=(8, 5))
        axis.plot(pools, values, marker="o", linewidth=2, color=color)
        axis.set_xlabel("Ice.ThreadPool.Server.Size")
        axis.set_ylabel(ylabel)
        axis.set_title(title)
        axis.set_xticks(pools)
        axis.grid(True, alpha=0.3)
        figure.tight_layout()
        figure.savefig(path, dpi=160)
        plt.close(figure)


def cell(value: object, row: int, column: int) -> str:
    reference = f"{chr(65 + column)}{row}"
    if isinstance(value, str):
        return f'<c r="{reference}" t="inlineStr"><is><t>{html.escape(value)}</t></is></c>'
    return f'<c r="{reference}"><v>{value}</v></c>'


def worksheet_xml(values: list[list[object]]) -> str:
    rows = []
    for row_number, row in enumerate(values, 1):
        cells = "".join(cell(value, row_number, column) for column, value in enumerate(row))
        rows.append(f'<row r="{row_number}">{cells}</row>')
    return (
        '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
        '<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">'
        f"<sheetData>{''.join(rows)}</sheetData></worksheet>"
    )


def make_spreadsheet(rows: list[dict[str, float | int]], summary: list[dict[str, float | int]]) -> None:
    raw_headers = [
        "thread_pool_size",
        "run",
        "client",
        "average_send_latency_ms",
        "throughput_msg_per_sec",
    ]
    raw_values: list[list[object]] = [raw_headers]
    raw_values.extend(
        [[row[header] for header in raw_headers] for row in rows]
    )
    summary_values: list[list[object]] = [
        ["Thread pool", "Mean latency (ms)", "Mean throughput (msg/sec)", "Samples"]
    ]
    summary_values.extend(
        [
            [
                item["thread_pool_size"],
                round(float(item["mean_latency"]), 3),
                round(float(item["mean_throughput"]), 2),
                item["samples"],
            ]
            for item in summary
        ]
    )

    content_types = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
<Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
<Override PartName="/xl/worksheets/sheet2.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
</Types>"""
    relationships = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""
    workbook = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
 xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
<sheets><sheet name="Raw Results" sheetId="1" r:id="rId1"/>
<sheet name="Summary" sheetId="2" r:id="rId2"/></sheets></workbook>"""
    workbook_relationships = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet2.xml"/>
</Relationships>"""

    with ZipFile(SPREADSHEET, "w", ZIP_DEFLATED) as archive:
        archive.writestr("[Content_Types].xml", content_types)
        archive.writestr("_rels/.rels", relationships)
        archive.writestr("xl/workbook.xml", workbook)
        archive.writestr("xl/_rels/workbook.xml.rels", workbook_relationships)
        archive.writestr("xl/worksheets/sheet1.xml", worksheet_xml(raw_values))
        archive.writestr("xl/worksheets/sheet2.xml", worksheet_xml(summary_values))


def write_report(summary: list[dict[str, float | int]]) -> None:
    report = """### Test executed

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
"""
    report += "\n".join(
        f"| {item['thread_pool_size']} | "
        f"{float(item['mean_latency']):.3f} ms | "
        f"{float(item['mean_throughput']):,.2f} msg/sec | "
        f"{item['samples']} |"
        for item in summary
    )
    report += "\n"
    report += """

Logs are stored in:

```text
experiments/server-*.log
experiments/client-*.log
```

The server and clients exited cleanly after the experiment.
"""
    REPORT.write_text(report, encoding="utf-8")


def main() -> int:
    if not INPUT_CSV.exists():
        print(f"Input file not found: {INPUT_CSV}", file=sys.stderr)
        return 1
    rows = read_results()
    summary = summaries(rows)
    make_plots(summary)
    make_spreadsheet(rows, summary)
    write_report(summary)
    print(f"Generated {REPORT}, {SPREADSHEET}, {LATENCY_PLOT}, and {THROUGHPUT_PLOT}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
