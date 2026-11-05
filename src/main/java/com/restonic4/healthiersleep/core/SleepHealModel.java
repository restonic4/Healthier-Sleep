package com.restonic4.healthiersleep.core;

import com.restonic4.healthiersleep.config.Tuning;

public final class SleepHealModel {
	private SleepHealModel() {}

	public static boolean foodAllowsHealing(int foodLevel) { return foodLevel >= Tuning.FOOD_MAX * Tuning.FOOD_MIN_FRACTION; }

	public static boolean thirstAllowsHealing(int thirstLevel) { return thirstLevel >= Tuning.THIRST_MAX * Tuning.THIRST_MIN_FRACTION; }

	public static boolean temperatureAllowsHealing(float bodyTemperature, float barLimit) {
		return Math.abs(bodyTemperature) < barLimit * (1f - 2f * Tuning.TEMPERATURE_EDGE_FRACTION);
	}

	public static float nightBudget(int foodLevel) {
		if (!foodAllowsHealing(foodLevel)) return 0f;

		float minFood = Tuning.FOOD_MAX * Tuning.FOOD_MIN_FRACTION;
		float fed = Math.min(1f, (foodLevel - minFood) / (Tuning.FOOD_MAX - minFood));

		return Tuning.HEAL_AT_MIN_FOOD + (Tuning.HEAL_AT_FULL_FOOD - Tuning.HEAL_AT_MIN_FOOD) * fed;
	}

	public static float fullHealWindowTicks() {
		return (Tuning.BED_CLOSES_AT - Tuning.BED_OPENS_AT) * Tuning.FULL_HEAL_NIGHT_FRACTION;
	}

	public static float healFor(float budget, float healedTonight, long ambientTicks, float missingHealth) {
		if (ambientTicks <= 0L) return 0f;

		float byTime = budget * ambientTicks / fullHealWindowTicks();
		float byBudget = budget - healedTonight;

		return Math.max(0f, Math.min(missingHealth, Math.min(byTime, byBudget)));
	}

	public static long nightId(long dayTime) { return Math.floorDiv(dayTime - Tuning.NIGHT_LEDGER_RESETS_AT, Tuning.DAY_LENGTH); }
}
