package com.bettercontent.explorationtickgovernor.performance;

import com.seibel.distanthorizons.api.interfaces.config.IDhApiConfigValue;
import com.seibel.distanthorizons.core.api.external.methods.config.common.DhApiWorldGenerationConfig;
import net.minecraftforge.fml.ModList;

public final class DistantHorizonsGenerationControl {
    private DistantHorizonsGenerationControl() {
    }

    public static DistantGenerationControl create() {
        if (!ModList.get().isLoaded("distanthorizons")) {
            return new UnavailableDistantGenerationControl("Distant Horizons not loaded");
        }
        final DhApiWorldGenerationConfig config = DhApiWorldGenerationConfig.INSTANCE;
        if (config == null) {
            return new UnavailableDistantGenerationControl("Distant Horizons config API not initialized");
        }
        final IDhApiConfigValue<Boolean> value = config.enableDistantWorldGeneration();
        return new ApiOverrideDistantGenerationControl(new ApiOverrideDistantGenerationControl.BooleanSetting() {
            @Override public Boolean trueValue() { return value.getTrueValue(); }
            @Override public Boolean apiValue() { return value.getApiValue(); }
            @Override public boolean canBeOverridden() { return value.getCanBeOverrodeByApi(); }
            @Override public boolean set(final boolean enabled) { return value.setValue(enabled); }
            @Override public boolean clear() { return value.clearValue(); }
        }, "Distant Horizons 2.4.5 world-generation API");
    }
}
