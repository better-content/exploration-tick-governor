package com.bettercontent.explorationtickgovernor.performance;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class ApiOverrideDistantGenerationControlTest {
    @Test
    void neverEnablesOrOverridesAUserDisabledSetting() {
        final FakeSetting setting = new FakeSetting(false, null);
        final ApiOverrideDistantGenerationControl control = control(setting);
        assertFalse(control.pause());
        control.resume();
        assertNull(setting.api);
    }

    @Test
    void preservesAnOverrideOwnedByAnotherApiConsumer() {
        final FakeSetting setting = new FakeSetting(true, true);
        final ApiOverrideDistantGenerationControl control = control(setting);
        assertFalse(control.pause());
        control.resume();
        assertTrue(setting.api);
    }

    @Test
    void clearsOnlyThePauseItInstalled() {
        final FakeSetting setting = new FakeSetting(true, null);
        final ApiOverrideDistantGenerationControl control = control(setting);
        assertTrue(control.pause());
        assertTrue(control.overrideActive());
        assertFalse(setting.api);
        control.resume();
        assertNull(setting.api);
    }

    private static ApiOverrideDistantGenerationControl control(final FakeSetting setting) {
        return new ApiOverrideDistantGenerationControl(setting, "test");
    }

    private static final class FakeSetting implements ApiOverrideDistantGenerationControl.BooleanSetting {
        private final boolean base;
        private Boolean api;

        private FakeSetting(final boolean base, final Boolean api) {
            this.base = base;
            this.api = api;
        }

        @Override public Boolean trueValue() { return base; }
        @Override public Boolean apiValue() { return api; }
        @Override public boolean canBeOverridden() { return true; }
        @Override public boolean set(final boolean value) { api = value; return true; }
        @Override public boolean clear() { api = null; return true; }
    }
}
