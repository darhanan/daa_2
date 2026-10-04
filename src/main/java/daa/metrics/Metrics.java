package daa.metrics;

/**
 * Counts the physical operations a data structure performs.
 *
 * <ul>
 *   <li><b>steps</b> - one read of an array cell or one move to the next node;</li>
 *   <li><b>moves</b> - one element shifted/copied inside an array or one link (pointer) update in a list;</li>
 *   <li><b>comparisons</b> - one comparison of two elements.</li>
 * </ul>
 *
 * Every structure owns one instance and increments it inside its operations,
 * so the numbers are counted, not estimated afterwards.
 */
public final class Metrics {
    private long steps;
    private long moves;
    private long comparisons;

    public void step() {
        steps++;
    }

    public void move() {
        moves++;
    }

    public void moves(int count) {
        moves += count;
    }

    public void compare() {
        comparisons++;
    }

    public long steps() {
        return steps;
    }

    public long moves() {
        return moves;
    }

    public long comparisons() {
        return comparisons;
    }

    public void reset() {
        steps = 0;
        moves = 0;
        comparisons = 0;
    }

    @Override
    public String toString() {
        return "steps=" + steps + ", moves=" + moves + ", comparisons=" + comparisons;
    }
}
