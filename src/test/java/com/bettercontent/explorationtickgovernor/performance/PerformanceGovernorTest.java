package com.bettercontent.explorationtickgovernor.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class PerformanceGovernorTest {
    @Test
    void respectsUserDisabledDistantGeneration() {
        final FakeControl control = new FakeControl(false);
        final PerformanceGovernor governor = governor(control);
        assertEquals(PerformanceGovernor.Transition.NONE, governor.sample(60));
        assertEquals(PerformanceGovernor.Transition.PAUSE_SKIPPED, governor.sample(60));
        assertFalse(control.override);
    }

    @Test
    void ownsAndClearsOnlyItsPauseOverride() {
        final FakeControl control = new FakeControl(true);
        final PerformanceGovernor governor = governor(control);
        governor.sample(60);
        assertEquals(PerformanceGovernor.Transition.PAUSED, governor.sample(60));
        assertTrue(control.override);
        governor.close();
        assertFalse(control.override);
    }

    @Test
    void adapterFailureDoesNotEscapeTickHandling() {
        final DistantGenerationControl failure = new FakeControl(true) {
            @Override public boolean pause() { throw new IllegalStateException("API changed"); }
        };
        final PerformanceGovernor governor = governor(failure);
        governor.sample(60);
        assertEquals(PerformanceGovernor.Transition.ADAPTER_FAILED, governor.sample(60));
    }

    private static PerformanceGovernor governor(final DistantGenerationControl control) {
        return new PerformanceGovernor(new PerformanceGovernorPolicy.Settings(2, 50, 40, 100, 2), control);
    }

    private static class FakeControl implements DistantGenerationControl {
        private final boolean baseEnabled;
        private boolean override;

        FakeControl(final boolean baseEnabled) { this.baseEnabled = baseEnabled; }
        @Override public boolean isAvailable() { return true; }
        @Override public boolean baseEnabled() { return baseEnabled; }
        @Override public boolean overrideActive() { return override; }
        @Override public boolean pause() { override = baseEnabled; return override; }
        @Override public void resume() { override = false; }
        @Override public String description() { return "fake"; }
    }
}
