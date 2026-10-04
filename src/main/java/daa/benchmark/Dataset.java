package daa.benchmark;

import java.util.Random;

public final class Dataset {
    public static final long SEED = 42;
    public static final int GET_QUERIES = 10_000;
    public static final int CONTAINS_QUERIES = 1_000;
    public static final int EDIT_OPERATIONS = 1_000;

    final int[] values;
    final int[] getIndices;
    final int[] searchQueries;
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

    int expectedHits() {
        return (CONTAINS_QUERIES + 1) / 2;
    }
}
