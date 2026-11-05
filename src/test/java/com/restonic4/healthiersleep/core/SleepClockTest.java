package com.restonic4.healthiersleep.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SleepClockTest {
	@Test
	void anAcceleratedTickMovesTheClockBySpeedTimesTheNaturalStep() {
		assertEquals(13006L, SleepClock.acceleratedTime(13000L, 13001L, 6));
		assertEquals(13012L, SleepClock.acceleratedTime(13000L, 13002L, 6));
	}

	@Test
	void aFrozenClockStaysFrozen() {
		assertEquals(13000L, SleepClock.acceleratedTime(13000L, 13000L, 6));
	}

	@Test
	void theClockNeverOvershootsTheMorning() {
		assertEquals(24000L, SleepClock.acceleratedTime(23998L, 23999L, 6));
		assertEquals(24000L, SleepClock.acceleratedTime(23999L, 24000L, 6));
		assertEquals(48000L, SleepClock.acceleratedTime(47995L, 47996L, 6));
	}

	@Test
	void aNaturalStepPastTheMorningIsKept() {
		assertEquals(24005L, SleepClock.acceleratedTime(23990L, 24005L, 6));
	}

	@Test
	void morningIsTheNextMultipleOfADay() {
		assertEquals(24000L, SleepClock.nextMorning(0L));
		assertEquals(24000L, SleepClock.nextMorning(23999L));
		assertEquals(48000L, SleepClock.nextMorning(24000L));
	}

	@Test
	void reachingMorningIsDetectedOnTheTickThatCrossesIt() {
		assertFalse(SleepClock.reachedMorning(13000L, 13006L));
		assertTrue(SleepClock.reachedMorning(23998L, 24000L));
		assertTrue(SleepClock.reachedMorning(23999L, 24000L));
	}
}
