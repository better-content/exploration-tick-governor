package com.bettercontent.explorationtickgovernor.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class TickBudgetWindowTest {
    @Test
    void reportsNearestRankPercentilesAndRollsForward() {
        final TickBudgetWindow window = new TickBudgetWindow(4);
        window.add(10);
        window.add(20);
        window.add(30);
        window.add(40);
        assertEquals(new TickBudgetWindow.Snapshot(4, 20, 40, 40, 40), window.snapshot());
        window.add(100);
        assertEquals(new TickBudgetWindow.Snapshot(4, 30, 100, 100, 100), window.snapshot());
    }
}
