package daa.benchmark;

import daa.metrics.Metrics;

/** One execution of a workload: elapsed time of the measured phase and the counters it produced. */
public record Trial(long nanos, long steps, long moves, long comparisons) {

    static Trial of(long nanos, Metrics metrics) {
        return new Trial(nanos, metrics.steps(), metrics.moves(), metrics.comparisons());
    }

    boolean sameCounters(Trial other) {
        return steps == other.steps && moves == other.moves && comparisons == other.comparisons;
    }
}
