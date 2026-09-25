package com.bettercontent.explorationtickgovernor.performance;

public final class PerformanceGovernorPolicy {
    private final Settings settings;
    private final TickBudgetWindow window;
    private boolean paused;
    private int healthyTicks;

    public PerformanceGovernorPolicy(final Settings settings) {
        this.settings = settings;
        this.window = new TickBudgetWindow(settings.sampleWindowTicks());
    }

    public Decision sample(final double milliseconds) {
        window.add(milliseconds);
        final TickBudgetWindow.Snapshot snapshot = window.snapshot();
        if (!window.isFull()) return new Decision(Action.NONE, snapshot, healthyTicks, "warming up");

        if (!paused) {
            if (snapshot.p95Ms() > settings.pauseP95Ms()) {
                paused = true;
                healthyTicks = 0;
                return new Decision(Action.PAUSE, snapshot, healthyTicks, "p95 budget exceeded");
            }
            if (snapshot.maxMs() > settings.spikePauseMs()) {
                paused = true;
                healthyTicks = 0;
                return new Decision(Action.PAUSE, snapshot, healthyTicks, "tick spike budget exceeded");
            }
            return new Decision(Action.NONE, snapshot, healthyTicks, "within budget");
        }

        if (snapshot.p95Ms() <= settings.resumeP95Ms() && snapshot.maxMs() <= settings.spikePauseMs()) {
            healthyTicks++;
            if (healthyTicks >= settings.recoveryTicks()) {
                paused = false;
                healthyTicks = 0;
                return new Decision(Action.RESUME, snapshot, healthyTicks, "recovery window satisfied");
            }
        } else {
            healthyTicks = 0;
        }
        return new Decision(Action.NONE, snapshot, healthyTicks, "waiting for recovery");
    }

    public TickBudgetWindow.Snapshot snapshot() {
        return window.snapshot();
    }

    public boolean isPaused() {
        return paused;
    }

    public record Settings(
            int sampleWindowTicks,
            double pauseP95Ms,
            double resumeP95Ms,
            double spikePauseMs,
            int recoveryTicks
    ) {
        public Settings {
            if (sampleWindowTicks < 1 || recoveryTicks < 1) throw new IllegalArgumentException("tick counts must be positive");
            if (resumeP95Ms < 0.0D || pauseP95Ms < resumeP95Ms || spikePauseMs < pauseP95Ms) {
                throw new IllegalArgumentException("thresholds must satisfy 0 <= resume <= pause <= spike");
            }
        }
    }

    public record Decision(Action action, TickBudgetWindow.Snapshot snapshot, int healthyTicks, String reason) {
    }

    public enum Action {
        NONE,
        PAUSE,
        RESUME
    }
}
