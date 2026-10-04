package daa.benchmark;

import daa.metrics.Metrics;

public record Trial(long nanos, long steps, long moves, long comparisons) {
    static Trial of(long nanos, Metrics metrics) {
        return new Trial(nanos, metrics.steps(), metrics.moves(), metrics.comparisons());
    }

    boolean sameCounters(Trial other) {
        return steps == other.steps && moves == other.moves && comparisons == other.comparisons;
    }
}
