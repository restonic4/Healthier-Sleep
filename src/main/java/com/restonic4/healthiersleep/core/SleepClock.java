package com.restonic4.healthiersleep.core;

import com.restonic4.healthiersleep.config.Tuning;

public final class SleepClock {
	private SleepClock() {}

	public static long nextMorning(long dayTime) { return (Math.floorDiv(dayTime, Tuning.DAY_LENGTH) + 1L) * Tuning.DAY_LENGTH; }

	public static long acceleratedTime(long before, long afterNatural, int speed) {
		long natural = afterNatural - before;
		if (natural <= 0L) return afterNatural;

		return Math.min(before + natural * speed, Math.max(afterNatural, nextMorning(before)));
	}

	public static boolean reachedMorning(long before, long after) { return after >= nextMorning(before); }
}
