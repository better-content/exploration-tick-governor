package com.bettercontent.explorationtickgovernor.performance;

import java.util.Arrays;

public final class TickBudgetWindow {
    private final double[] values;
    private int cursor;
    private int count;

    public TickBudgetWindow(final int capacity) {
        if (capacity < 1) throw new IllegalArgumentException("capacity must be positive");
        this.values = new double[capacity];
    }

    public void add(final double milliseconds) {
        values[cursor] = Math.max(0.0D, milliseconds);
        cursor = (cursor + 1) % values.length;
        count = Math.min(count + 1, values.length);
    }

    public boolean isFull() {
        return count == values.length;
    }

    public Snapshot snapshot() {
        if (count == 0) return Snapshot.EMPTY;
        final double[] sorted = Arrays.copyOf(values, count);
        Arrays.sort(sorted);
        return new Snapshot(
                count,
                percentile(sorted, 0.50D),
                percentile(sorted, 0.95D),
                percentile(sorted, 0.99D),
                sorted[sorted.length - 1]);
    }

    private static double percentile(final double[] sorted, final double quantile) {
        final int index = Math.max(0, (int) Math.ceil(quantile * sorted.length) - 1);
        return sorted[index];
    }

    public record Snapshot(int count, double p50Ms, double p95Ms, double p99Ms, double maxMs) {
        public static final Snapshot EMPTY = new Snapshot(0, 0.0D, 0.0D, 0.0D, 0.0D);
    }
}
