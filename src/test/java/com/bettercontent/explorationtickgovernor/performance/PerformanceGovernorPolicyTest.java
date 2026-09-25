package com.bettercontent.explorationtickgovernor.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class PerformanceGovernorPolicyTest {
    private static PerformanceGovernorPolicy policy() {
        return new PerformanceGovernorPolicy(new PerformanceGovernorPolicy.Settings(4, 50, 40, 100, 3));
    }

    @Test
    void warmsUpBeforePausingAndPausesForP95() {
        final PerformanceGovernorPolicy policy = policy();
        for (int index = 0; index < 3; index++) {
            assertEquals(PerformanceGovernorPolicy.Action.NONE, policy.sample(60).action());
        }
        assertEquals(PerformanceGovernorPolicy.Action.PAUSE, policy.sample(60).action());
        assertTrue(policy.isPaused());
    }

    @Test
    void singleSpikePauses() {
        final PerformanceGovernorPolicy policy = policy();
        policy.sample(20);
        policy.sample(20);
        policy.sample(20);
        assertEquals(PerformanceGovernorPolicy.Action.PAUSE, policy.sample(101).action());
    }

    @Test
    void recoveryRequiresConsecutiveHealthySlidingWindows() {
        final PerformanceGovernorPolicy policy = policy();
        for (int index = 0; index < 4; index++) policy.sample(60);
        for (int index = 0; index < 3; index++) assertEquals(PerformanceGovernorPolicy.Action.NONE, policy.sample(20).action());
        assertEquals(PerformanceGovernorPolicy.Action.NONE, policy.sample(20).action());
        assertEquals(PerformanceGovernorPolicy.Action.NONE, policy.sample(20).action());
        assertEquals(PerformanceGovernorPolicy.Action.RESUME, policy.sample(20).action());
        assertFalse(policy.isPaused());
    }

    @Test
    void unhealthyWindowResetsRecoveryHysteresis() {
        final PerformanceGovernorPolicy policy = policy();
        for (int index = 0; index < 4; index++) policy.sample(60);
        for (int index = 0; index < 5; index++) policy.sample(20);
        assertEquals(PerformanceGovernorPolicy.Action.NONE, policy.sample(45).action());
        assertTrue(policy.isPaused());
    }
}
