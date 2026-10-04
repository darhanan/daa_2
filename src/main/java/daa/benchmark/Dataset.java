package daa.benchmark;

import java.util.Random;

/**
 * All input of one size {@code n}, generated from a single {@code new Random(42)},
 * so every structure and every run receives exactly the same numbers.
 */
public final class Dataset {
    public static final long SEED = 42;
    public static final int GET_QUERIES = 10_000;
    public static final int CONTAINS_QUERIES = 1_000;
    public static final int EDIT_OPERATIONS = 1_000;

    /** Values used to fill the structures; all of them are non-negative. */
    final int[] values;
    /** W1: random indices in [0, n). */
    final int[] getIndices;
    /** W2: even positions hold a stored value, odd positions a negative value that is never stored. */
    final int[] searchQueries;
    /** W3: values inserted at the head or in the middle. */
    final int[] insertValues;

    private Dataset(int n) {
        Random rnd = new Random(SEED);
        values = new int[n];
        for (int i = 0; i < n; i++) {
            values[i] = rnd.nextInt(Integer.MAX_VALUE);
        }
        getIndices = new int[GET_QUERIES];
        for (int i = 0; i < GET_QUERIES; i++) {
            getIndices[i] = rnd.nextInt(n);
        }
        searchQueries = new int[CONTAINS_QUERIES];
        for (int i = 0; i < CONTAINS_QUERIES; i++) {
            searchQueries[i] = (i % 2 == 0)
                    ? values[rnd.nextInt(n)]
                    : -1 - rnd.nextInt(Integer.MAX_VALUE);
        }
        insertValues = new int[EDIT_OPERATIONS];
        for (int i = 0; i < EDIT_OPERATIONS; i++) {
            insertValues[i] = rnd.nextInt(Integer.MAX_VALUE);
        }
    }

    public static Dataset of(int n) {
        if (n < 1) {
            throw new IllegalArgumentException("n must be positive: " + n);
        }
        return new Dataset(n);
    }

    public int size() {
        return values.length;
    }

    /** Number of W2 queries whose value is present in the structure. */
    int expectedHits() {
        return (CONTAINS_QUERIES + 1) / 2;
    }
}
