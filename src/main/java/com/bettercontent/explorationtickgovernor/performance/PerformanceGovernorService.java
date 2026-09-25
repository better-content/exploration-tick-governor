package com.bettercontent.explorationtickgovernor.performance;

import com.mojang.logging.LogUtils;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;

import java.util.Locale;
import java.util.function.Supplier;

public final class PerformanceGovernorService {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static Supplier<Configuration> configuration = Configuration::defaults;
    private static Supplier<DistantGenerationControl> distantGeneration =
            () -> new UnavailableDistantGenerationControl("Distant Horizons not configured");
    private static PerformanceGovernor governor;
    private static long tickStartNanos;
    private static boolean adapterFailureLogged;

    private PerformanceGovernorService() {
    }

    public static void configure(
            final Supplier<Configuration> configurationSupplier,
            final Supplier<DistantGenerationControl> distantGenerationSupplier) {
        configuration = configurationSupplier;
        distantGeneration = distantGenerationSupplier;
    }

    @SubscribeEvent
    public static void onServerStarted(final ServerStartedEvent event) {
        final Configuration config = configuration.get();
        governor = config.enabled()
                ? new PerformanceGovernor(config.policy(), safeControl())
                : null;
        adapterFailureLogged = false;
    }

    @SubscribeEvent
    public static void onServerStopping(final ServerStoppingEvent event) {
        if (governor != null) governor.close();
        governor = null;
        tickStartNanos = 0L;
    }

    @SubscribeEvent
    public static void onServerTick(final TickEvent.ServerTickEvent event) {
        if (governor == null) return;
        if (event.phase == TickEvent.Phase.START) {
            tickStartNanos = System.nanoTime();
            return;
        }
        if (tickStartNanos == 0L) return;
        final double elapsedMs = (System.nanoTime() - tickStartNanos) / 1_000_000.0D;
        tickStartNanos = 0L;
        final PerformanceGovernor.Transition transition = governor.sample(elapsedMs);
        if (transition != PerformanceGovernor.Transition.NONE) {
            final PerformanceGovernor.Status status = governor.status();
            if (transition == PerformanceGovernor.Transition.ADAPTER_FAILED && !adapterFailureLogged) {
                adapterFailureLogged = true;
                LOGGER.warn("Distant Horizons performance adapter failed; tick metrics remain active: {}", status.lastReason());
                return;
            }
            LOGGER.info(
                    "Exploration tick governor transition={} p95={}ms max={}ms dhBase={} dhOverride={} reason={}",
                    transition,
                    format(status.tickTimes().p95Ms()),
                    format(status.tickTimes().maxMs()),
                    status.distantGenerationBaseEnabled(),
                    status.overrideActive(),
                    status.lastReason());
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(final RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("exploration_tick_governor")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("performance_status").executes(context -> {
                            final PerformanceGovernor current = governor;
                            if (current == null) {
                                context.getSource().sendSuccess(
                                        () -> Component.literal("Performance governor is disabled or the server is not running."), false);
                                return 1;
                            }
                            final PerformanceGovernor.Status status = current.status();
                            final TickBudgetWindow.Snapshot ticks = status.tickTimes();
                            context.getSource().sendSuccess(() -> Component.literal(String.format(
                                    Locale.ROOT,
                                    "ticks=%d p50=%.2fms p95=%.2fms p99=%.2fms max=%.2fms budgetPaused=%s",
                                    ticks.count(), ticks.p50Ms(), ticks.p95Ms(), ticks.p99Ms(), ticks.maxMs(), status.budgetPaused())), false);
                            context.getSource().sendSuccess(() -> Component.literal(
                                    "DH adapter=" + status.adapter() +
                                            " available=" + status.adapterAvailable() +
                                            " baseEnabled=" + status.distantGenerationBaseEnabled() +
                                            " override=" + status.overrideActive() +
                                            " lastReason=" + status.lastReason()), false);
                            return 1;
                        })));
    }

    private static DistantGenerationControl safeControl() {
        try {
            return distantGeneration.get();
        } catch (LinkageError | RuntimeException error) {
            LOGGER.warn("Distant Horizons performance adapter unavailable; tick metrics remain active", error);
            return new UnavailableDistantGenerationControl(error.getClass().getSimpleName());
        }
    }

    private static String format(final double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    public record Configuration(boolean enabled, PerformanceGovernorPolicy.Settings policy) {
        public static Configuration defaults() {
            return new Configuration(true, new PerformanceGovernorPolicy.Settings(100, 50.0D, 40.0D, 100.0D, 600));
        }
    }
}
