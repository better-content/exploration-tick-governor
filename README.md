# Exploration Tick Governor

Measures active server tick p50/p95/p99/max over a configurable window. Under sustained load it pauses only its own Distant Horizons distant-generation API override, then clears that override after a recovery window. It does not change Distant Horizons config or enable a user-disabled feature. Use `/exploration_tick_governor performance_status` to inspect status.

Distant Horizons 2.4.5-b is optional; tick metrics remain available without it. Settings are in `config/exploration_tick_governor-common.toml`.

Build with `./gradlew verifyFast`, run full validation with `./gradlew verifyFull`, and stage the runtime jar with `./gradlew stageRuntimeJar`.
