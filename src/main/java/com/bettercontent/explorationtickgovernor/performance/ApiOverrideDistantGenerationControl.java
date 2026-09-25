package com.bettercontent.explorationtickgovernor.performance;

import java.util.Objects;

/** Owns only the API override it installs; a user's base value and other API overrides are preserved. */
public final class ApiOverrideDistantGenerationControl implements DistantGenerationControl {
    private final BooleanSetting setting;
    private final String description;
    private boolean ownedOverride;

    public ApiOverrideDistantGenerationControl(final BooleanSetting setting, final String description) {
        this.setting = Objects.requireNonNull(setting);
        this.description = Objects.requireNonNull(description);
    }

    @Override public boolean isAvailable() { return true; }

    @Override public boolean baseEnabled() { return Boolean.TRUE.equals(setting.trueValue()); }

    @Override public boolean overrideActive() {
        return ownedOverride && Boolean.FALSE.equals(setting.apiValue());
    }

    @Override
    public boolean pause() {
        if (!baseEnabled() || !setting.canBeOverridden()) return false;
        if (setting.apiValue() != null && !ownedOverride) return false;
        if (!setting.set(false)) return false;
        ownedOverride = true;
        return true;
    }

    @Override
    public void resume() {
        if (!ownedOverride) return;
        setting.clear();
        ownedOverride = false;
    }

    @Override public String description() { return description; }

    public interface BooleanSetting {
        Boolean trueValue();
        Boolean apiValue();
        boolean canBeOverridden();
        boolean set(boolean value);
        boolean clear();
    }
}
