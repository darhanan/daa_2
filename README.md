# DAA Assignment 2 - Data Structures (In-Memory Workload Engine)

Own implementations of **DynamicArray**, **MyLinkedList** and **MinHeap** for primitive `int`
values, written from scratch (no `java.util` collections in `src/main`), with operation counters,
JUnit 5 tests, a reproducible benchmark (W1-W4), plots and a report.

- Report with complexity table, loop-invariant proofs, plots and discussion: [REPORT.md](REPORT.md) (Word version: [REPORT.docx](REPORT.docx))
- Assignment description: [DAA_Assignment2_Data_Structures.pdf](DAA_Assignment2_Data_Structures.pdf)
- Raw results: [results/results.csv](results/results.csv), bonus: [results/buildheap.csv](results/buildheap.csv), [results/memory.csv](results/memory.csv)
- Charts: [results/plots/](results/plots/)

## Requirements

- JDK 17 or newer (developed on OpenJDK 25.0.1). Maven does **not** need to be installed:
  the included Maven Wrapper (`./mvnw`, `mvnw.cmd` on Windows) downloads Maven 3.9.11 on first use.
- Python 3 with `matplotlib`, only for redrawing the charts (`pip install matplotlib`).

## Build and test

```bash
./mvnw clean package          # compiles and runs all 87 JUnit 5 tests
./mvnw test                   # tests only
```

## Run the benchmark (one command -> results/results.csv)

```bash
./mvnw -q compile exec:exec@benchmark
```

The benchmark uses `new Random(42)`, so the same command always produces the same data and the same
counters (times differ slightly between machines). It runs a discarded warm-up pass for
n = 100, 1 000, 10 000, then for every case 5 more discarded warm-up runs and 5 measured runs, and
writes the **median** time. It takes about 10 seconds on an Apple M3 and writes:

| File | Content |
|------|---------|
| `results/results.csv` | W1-W4, columns `workload,variant,structure,n,time_ms,steps,moves,comparisons` |
| `results/buildheap.csv` | Bonus B: n x `insert` vs Floyd `buildHeap` on random and descending input |

Bonus A, the memory footprint measured with JOL, writes `results/memory.csv`:

```bash
./mvnw -q compile exec:exec@memory
```

Redraw all charts in `results/plots/`:

```bash
python3 scripts/plot_results.py
```

Everything in one go: `./run_all.sh`.

## Project layout

```
src/main/java/daa/structures   IntList (common interface), DynamicArray, MyLinkedList, MinHeap
src/main/java/daa/metrics      Metrics: steps / moves / comparisons counters
src/main/java/daa/benchmark    Dataset (seed 42), Workloads W1-W4, BenchmarkRunner, MemoryFootprint (JOL)
src/test/java/...              JUnit 5 tests (correctness vs java.util, edge cases, heap property, counters)
scripts/plot_results.py        charts from the CSV files
results/                       results.csv, buildheap.csv, memory.csv, plots/
REPORT.md, REPORT.docx           the report (Markdown and Word)
```

## What is counted

Counters live in `daa.metrics.Metrics` and are incremented **inside** the operations:

| Counter | Meaning | Examples |
|---------|---------|----------|
| `steps` | one read of an array cell, or one move to the next node | `DynamicArray.get` = 1 step; `MyLinkedList.get(i)` = `min(i, n-1-i) + 1` steps |
| `moves` | one element shifted or copied inside an array, or one link (pointer) update in a list | `add(0, x)` on an array shifts n elements; on the list it updates 4 links |
| `comparisons` | one comparison of two elements | `contains`, `siftUp`, `siftDown` |

Writing a brand-new value into its free slot is not a shift, so it is not counted. Copying elements
into a bigger array on resize is counted as moves. `toArray()` is a test helper and is not counted.

## Git workflow

`main` holds only working code and is tagged `v1.0`. Work was done on `feature/metrics`, `feature/array`,
`feature/list`, `feature/heap` and merged with `--no-ff`, so the branches are visible in `git log --graph`.
