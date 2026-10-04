package daa.benchmark;

import daa.structures.DynamicArray;
import daa.structures.IntList;
import daa.structures.MinHeap;
import daa.structures.MyLinkedList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Runs workloads W1-W4 for n = 100, 1 000, 10 000, 100 000 and writes
 * {@code results.csv}; also writes {@code buildheap.csv} for bonus task B.
 *
 * <p>Before measuring, the whole suite runs once for {@link #WARMUP_SIZES} and is discarded,
 * so the JIT has compiled every workload. Then every case is executed {@link #WARMUP_RUNS}
 * more times without recording and {@link #MEASURED_RUNS} times; the median time is saved. The counters are the same
 * in every run because the input is fixed by the seed, and this is checked.
 *
 * <p>Usage: {@code java daa.benchmark.BenchmarkRunner [outputDir]} (default {@code results}).
 */
public final class BenchmarkRunner {
    static final int[] SIZES = {100, 1_000, 10_000, 100_000};
    static final int[] WARMUP_SIZES = {100, 1_000, 10_000};
    static final int WARMUP_RUNS = 5;
    static final int MEASURED_RUNS = 5;
    static final String HEADER = "workload,variant,structure,n,time_ms,steps,moves,comparisons\n";

    /** One measurable case; creates a fresh structure on every call. */
    @FunctionalInterface
    interface Case {
        Trial run();
    }

    private BenchmarkRunner() {
    }

    public static void main(String[] args) throws IOException {
        Path outDir = Path.of(args.length > 0 ? args[0] : "results");
        Files.createDirectories(outDir);

        System.out.println("Global warm-up (n = 100, 1 000, 10 000; results discarded) ...");
        StringBuilder discarded = new StringBuilder();
        for (int n : WARMUP_SIZES) {
            runWorkloads(Dataset.of(n), discarded);
            runBuildHeap(Dataset.of(n), discarded);
        }

        StringBuilder results = new StringBuilder(HEADER);
        StringBuilder buildHeap = new StringBuilder(HEADER);
        for (int n : SIZES) {
            Dataset d = Dataset.of(n);
            runWorkloads(d, results);
            runBuildHeap(d, buildHeap);
        }

        Files.writeString(outDir.resolve("results.csv"), results);
        Files.writeString(outDir.resolve("buildheap.csv"), buildHeap);
        System.out.println();
        System.out.print(results);
        System.out.println("Wrote " + outDir.resolve("results.csv") + " and " + outDir.resolve("buildheap.csv")
                + " (checksum " + Workloads.sink() + ")");
    }

    private static void runWorkloads(Dataset d, StringBuilder out) {
        int n = d.size();
        record("W1", "-", "DynamicArray", n, () -> Workloads.randomAccess(new DynamicArray(), d), out);
        record("W1", "-", "MyLinkedList", n, () -> Workloads.randomAccess(new MyLinkedList(), d), out);
        record("W2", "-", "DynamicArray", n, () -> Workloads.search(new DynamicArray(), d), out);
        record("W2", "-", "MyLinkedList", n, () -> Workloads.search(new MyLinkedList(), d), out);
        for (String variant : new String[] {"head", "middle"}) {
            boolean middle = variant.equals("middle");
            record("W3", variant, "DynamicArray", n, () -> Workloads.insertRemove(new DynamicArray(), d, middle), out);
            record("W3", variant, "MyLinkedList", n, () -> Workloads.insertRemove(new MyLinkedList(), d, middle), out);
        }
        record("W4", "-", "MinHeap", n, () -> Workloads.priority(new MinHeap(), d), out);
    }

    private static void runBuildHeap(Dataset d, StringBuilder out) {
        int n = d.size();
        int[] random = d.values;
        int[] descending = Workloads.descending(n);
        record("BuildHeap", "insert-random", "MinHeap", n, () -> Workloads.buildByInserts(random), out);
        record("BuildHeap", "floyd-random", "MinHeap", n, () -> Workloads.buildByFloyd(random), out);
        record("BuildHeap", "insert-descending", "MinHeap", n, () -> Workloads.buildByInserts(descending), out);
        record("BuildHeap", "floyd-descending", "MinHeap", n, () -> Workloads.buildByFloyd(descending), out);
    }

    private static void record(String workload, String variant, String structure, int n,
                               Case c, StringBuilder out) {
        Trial median = measure(c);
        String row = String.format(Locale.ROOT, "%s,%s,%s,%d,%.4f,%d,%d,%d%n",
                workload, variant, structure, n, median.nanos() / 1e6,
                median.steps(), median.moves(), median.comparisons());
        out.append(row);
        System.out.print("  " + row);
    }

    /** Warm-up runs are discarded; returns the measured trial with the median time. */
    static Trial measure(Case c) {
        for (int i = 0; i < WARMUP_RUNS; i++) {
            c.run();
        }
        Trial[] trials = new Trial[MEASURED_RUNS];
        for (int i = 0; i < MEASURED_RUNS; i++) {
            System.gc();
            trials[i] = c.run();
            if (!trials[i].sameCounters(trials[0])) {
                throw new IllegalStateException("counters differ between runs of the same case");
            }
        }
        sortByTime(trials);
        return trials[MEASURED_RUNS / 2];
    }

    /** Insertion sort by time; only 5 elements, and keeps java.util collections out of the main code. */
    private static void sortByTime(Trial[] trials) {
        for (int i = 1; i < trials.length; i++) {
            Trial key = trials[i];
            int j = i - 1;
            while (j >= 0 && trials[j].nanos() > key.nanos()) {
                trials[j + 1] = trials[j];
                j--;
            }
            trials[j + 1] = key;
        }
    }
}
