package com.bettercontent.explorationtickgovernor.performance;

import java.util.Objects;

public final class PerformanceGovernor {
    private final PerformanceGovernorPolicy policy;
    private final DistantGenerationControl distantGeneration;
    private String lastReason = "warming up";

    public PerformanceGovernor(
            final PerformanceGovernorPolicy.Settings settings,
            final DistantGenerationControl distantGeneration) {
        this.policy = new PerformanceGovernorPolicy(settings);
        this.distantGeneration = Objects.requireNonNull(distantGeneration);
    }

    public Transition sample(final double milliseconds) {
        final PerformanceGovernorPolicy.Decision decision = policy.sample(milliseconds);
        if (decision.action() == PerformanceGovernorPolicy.Action.NONE) return Transition.NONE;
        lastReason = decision.reason();
        try {
            if (decision.action() == PerformanceGovernorPolicy.Action.PAUSE) {
                return distantGeneration.pause() ? Transition.PAUSED : Transition.PAUSE_SKIPPED;
            }
            distantGeneration.resume();
            return Transition.RESUMED;
        } catch (RuntimeException | LinkageError error) {
            lastReason = "Distant Horizons API failure: " + error.getClass().getSimpleName();
            return Transition.ADAPTER_FAILED;
        }
    }

    public void close() {
        try {
            distantGeneration.resume();
        } catch (RuntimeException | LinkageError ignored) {
            // Server shutdown must not be blocked by an optional integration.
        }
    }

    public Status status() {
        try {
            return new Status(
                    policy.snapshot(),
                    policy.isPaused(),
                    distantGeneration.isAvailable(),
                    distantGeneration.baseEnabled(),
                    distantGeneration.overrideActive(),
                    distantGeneration.description(),
                    lastReason);
        } catch (RuntimeException | LinkageError error) {
            return new Status(
                    policy.snapshot(), policy.isPaused(), false, false, false,
                    error.getClass().getSimpleName(), lastReason);
        }
    }

    public record Status(
            TickBudgetWindow.Snapshot tickTimes,
            boolean budgetPaused,
            boolean adapterAvailable,
            boolean distantGenerationBaseEnabled,
            boolean overrideActive,
            String adapter,
            String lastReason) {
    }

    public enum Transition {
        NONE,
        PAUSED,
        PAUSE_SKIPPED,
        RESUMED,
        ADAPTER_FAILED
    }
}
