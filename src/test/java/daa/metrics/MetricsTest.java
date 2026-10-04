package daa.metrics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MetricsTest {

    @Test
    void countsAndResets() {
        Metrics m = new Metrics();
        m.step();
        m.step();
        m.move();
        m.moves(3);
        m.compare();
        assertEquals(2, m.steps());
        assertEquals(4, m.moves());
        assertEquals(1, m.comparisons());
        assertEquals("steps=2, moves=4, comparisons=1", m.toString());

        m.reset();
        assertEquals(0, m.steps());
        assertEquals(0, m.moves());
        assertEquals(0, m.comparisons());
    }
}
