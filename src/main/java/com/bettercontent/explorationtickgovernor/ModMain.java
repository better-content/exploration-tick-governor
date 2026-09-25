package com.bettercontent.explorationtickgovernor;

import com.bettercontent.explorationtickgovernor.config.GovernorConfig;
import com.bettercontent.explorationtickgovernor.performance.DistantHorizonsGenerationControl;
import com.bettercontent.explorationtickgovernor.performance.PerformanceGovernorService;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.common.Mod;

@Mod(ModMain.MOD_ID)
public final class ModMain {
    public static final String MOD_ID = "exploration_tick_governor";

    public ModMain() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, GovernorConfig.SPEC);
        PerformanceGovernorService.configure(GovernorConfig::configuration, DistantHorizonsGenerationControl::create);
        MinecraftForge.EVENT_BUS.register(PerformanceGovernorService.class);
    }
}
