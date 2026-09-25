package com.bettercontent.explorationtickgovernor.performance;

final class UnavailableDistantGenerationControl implements DistantGenerationControl {
    private final String reason;

    UnavailableDistantGenerationControl(final String reason) {
        this.reason = reason;
    }

    @Override public boolean isAvailable() { return false; }
    @Override public boolean baseEnabled() { return false; }
    @Override public boolean overrideActive() { return false; }
    @Override public boolean pause() { return false; }
    @Override public void resume() { }
    @Override public String description() { return reason; }
}
