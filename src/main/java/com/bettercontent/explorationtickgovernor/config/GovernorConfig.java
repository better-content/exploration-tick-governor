package com.bettercontent.explorationtickgovernor.config;

import com.bettercontent.explorationtickgovernor.performance.PerformanceGovernorPolicy;
import com.bettercontent.explorationtickgovernor.performance.PerformanceGovernorService;
import net.minecraftforge.common.ForgeConfigSpec;

public final class GovernorConfig {
    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.BooleanValue ENABLED;
    private static final ForgeConfigSpec.IntValue SAMPLE_WINDOW;
    private static final ForgeConfigSpec.DoubleValue PAUSE_P95, RESUME_P95, SPIKE_MS;
    private static final ForgeConfigSpec.IntValue RECOVERY_TICKS;
    static {
        var b = new ForgeConfigSpec.Builder();
        b.push("governor");
        ENABLED=b.define("enabled",true); SAMPLE_WINDOW=b.defineInRange("sampleWindowTicks",100,20,1200);
        PAUSE_P95=b.defineInRange("pauseP95Ms",50.0,1.0,1000.0); RESUME_P95=b.defineInRange("resumeP95Ms",40.0,1.0,1000.0);
        SPIKE_MS=b.defineInRange("spikePauseMs",100.0,1.0,5000.0); RECOVERY_TICKS=b.defineInRange("recoveryTicks",600,20,12000);
        b.pop(); SPEC=b.build();
    }
    private GovernorConfig() {}
    public static PerformanceGovernorService.Configuration configuration() {
        return new PerformanceGovernorService.Configuration(ENABLED.get(),new PerformanceGovernorPolicy.Settings(SAMPLE_WINDOW.get(),PAUSE_P95.get(),Math.min(RESUME_P95.get(),PAUSE_P95.get()),Math.max(SPIKE_MS.get(),PAUSE_P95.get()),RECOVERY_TICKS.get()));
    }
}
