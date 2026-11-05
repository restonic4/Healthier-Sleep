package com.restonic4.healthiersleep.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.restonic4.healthiersleep.config.Tuning;
import org.junit.jupiter.api.Test;

class SleepHealModelTest {
	private static final float EPSILON = 1e-3f;

	@Test
	void foodGateOpensAtThreeQuartersOfTheBar() {
		assertFalse(SleepHealModel.foodAllowsHealing(14));
		assertTrue(SleepHealModel.foodAllowsHealing(15));
		assertTrue(SleepHealModel.foodAllowsHealing(20));
	}

	@Test
	void thirstGateOpensAtHalfTheBar() {
		assertFalse(SleepHealModel.thirstAllowsHealing(9));
		assertTrue(SleepHealModel.thirstAllowsHealing(10));
	}

	@Test
	void temperatureGateRejectsTheOuterQuarterOfEachSide() {
		assertTrue(SleepHealModel.temperatureAllowsHealing(0f, 100f));
		assertTrue(SleepHealModel.temperatureAllowsHealing(49.9f, 100f));
		assertTrue(SleepHealModel.temperatureAllowsHealing(-49.9f, 100f));
		assertFalse(SleepHealModel.temperatureAllowsHealing(50f, 100f));
		assertFalse(SleepHealModel.temperatureAllowsHealing(-50f, 100f));
		assertFalse(SleepHealModel.temperatureAllowsHealing(100f, 100f));
		assertFalse(SleepHealModel.temperatureAllowsHealing(-100f, 100f));
	}

	@Test
	void temperatureGateScalesWithTheBarLimit() {
		assertTrue(SleepHealModel.temperatureAllowsHealing(99f, 200f));
		assertFalse(SleepHealModel.temperatureAllowsHealing(101f, 200f));
	}

	@Test
	void budgetIsZeroBelowTheFoodGateAndGrowsLinearlyToFiveHearts() {
		assertEquals(0f, SleepHealModel.nightBudget(14), EPSILON);
		assertEquals(Tuning.HEAL_AT_MIN_FOOD, SleepHealModel.nightBudget(15), EPSILON);
		assertEquals(Tuning.HEAL_AT_FULL_FOOD, SleepHealModel.nightBudget(20), EPSILON);
		assertEquals(10f, SleepHealModel.nightBudget(20), EPSILON);

		float step = SleepHealModel.nightBudget(16) - SleepHealModel.nightBudget(15);
		assertEquals(step, SleepHealModel.nightBudget(19) - SleepHealModel.nightBudget(18), EPSILON);
	}

	@Test
	void fullWindowOfSleepHealsExactlyTheBudget() {
		float budget = 10f;
		long window = (long) Math.ceil(SleepHealModel.fullHealWindowTicks());

		assertEquals(budget, SleepHealModel.healFor(budget, 0f, window, 20f), EPSILON);
	}

	@Test
	void healingNeverExceedsTheRemainingBudgetOrTheMissingHealth() {
		assertEquals(1f, SleepHealModel.healFor(10f, 9f, 1_000_000L, 20f), EPSILON);
		assertEquals(0f, SleepHealModel.healFor(10f, 10f, 1_000_000L, 20f), EPSILON);
		assertEquals(0.5f, SleepHealModel.healFor(10f, 0f, 1_000_000L, 0.5f), EPSILON);
		assertEquals(0f, SleepHealModel.healFor(10f, 0f, 100L, 0f), EPSILON);
	}

	@Test
	void noAmbientTimeMeansNoHealing() {
		assertEquals(0f, SleepHealModel.healFor(10f, 0f, 0L, 20f), EPSILON);
		assertEquals(0f, SleepHealModel.healFor(10f, 0f, -5L, 20f), EPSILON);
	}

	@Test
	void leavingAndReturningToBedOnlyCountsTheTimeActuallySlept() {
		float budget = 10f;
		float healed = 0f;
		long slept = 0L;

		for (int segment = 0; segment < 5; segment++) {
			for (int tick = 0; tick < 1500; tick++) {
				float amount = SleepHealModel.healFor(budget, healed, 6L, 20f);
				healed += amount;
				slept += 6L;
			}

			// the player is out of bed here: no calls, so the gap costs nothing and refunds nothing
		}

		float expected = budget * slept / SleepHealModel.fullHealWindowTicks();
		assertEquals(Math.min(budget, expected), healed, EPSILON);
		assertTrue(healed <= budget + EPSILON);
	}

	@Test
	void sleepingTheWholeBedWindowNeverOverheals() {
		float healed = 0f;

		for (long tick = 0; tick < Tuning.BED_CLOSES_AT - Tuning.BED_OPENS_AT; tick++) {
			healed += SleepHealModel.healFor(10f, healed, 1L, 100f);
		}

		assertEquals(10f, healed, EPSILON);
	}

	@Test
	void tinyStepsNeverAccumulatePastTheBudget() {
		float healed = 0f;

		for (int i = 0; i < 100_000; i++) {
			healed += SleepHealModel.healFor(2f, healed, 6L, 100f);
		}

		assertTrue(healed <= 2f + EPSILON);
	}

	@Test
	void nightIdChangesAtNoonAndNotAtMidnight() {
		assertEquals(SleepHealModel.nightId(Tuning.BED_OPENS_AT), SleepHealModel.nightId(Tuning.DAY_LENGTH + 5999L));
		assertEquals(SleepHealModel.nightId(Tuning.BED_OPENS_AT), SleepHealModel.nightId(Tuning.DAY_LENGTH));
		assertFalse(SleepHealModel.nightId(5999L) == SleepHealModel.nightId(6000L));
		assertFalse(SleepHealModel.nightId(Tuning.BED_OPENS_AT) == SleepHealModel.nightId(Tuning.BED_OPENS_AT + Tuning.DAY_LENGTH));
	}
}
