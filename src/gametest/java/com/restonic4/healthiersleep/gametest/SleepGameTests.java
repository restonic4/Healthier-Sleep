package com.restonic4.healthiersleep.gametest;

import com.restonic4.healthiersleep.config.Tuning;
import com.restonic4.healthiersleep.core.SleepHealModel;
import com.restonic4.healthiersleep.world.SleepHealingService;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;

public class SleepGameTests implements FabricGameTest {
	private static final long NIGHT = 14000L;
	private static final long WHOLE_WINDOW = (long) Math.ceil(SleepHealModel.fullHealWindowTicks());

	private final List<String> failures = new ArrayList<>();

	private static ServerPlayer hurtPlayer(GameTestHelper helper, int food) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setHealth(1f);
		player.getFoodData().setFoodLevel(food);

		return player;
	}

	@GameTest(template = EMPTY_STRUCTURE)
	public void vanillaHungerRegenerationIsOff(GameTestHelper helper) {
		ServerPlayer player = hurtPlayer(helper, 20);
		player.getFoodData().setSaturation(10f);

		for (int tick = 0; tick < 400; tick++) { player.getFoodData().tick(player); }

		expect("a full stomach no longer heals", player.getHealth() == 1f);
		finish(helper);
	}

	@GameTest(template = EMPTY_STRUCTURE)
	public void sleepHealsTheBudgetOncePerNight(GameTestHelper helper) {
		ServerPlayer player = hurtPlayer(helper, 20);

		float first = SleepHealingService.heal(player, NIGHT, WHOLE_WINDOW);
		float again = SleepHealingService.heal(player, NIGHT + 2000L, WHOLE_WINDOW);
		float nextNight = SleepHealingService.heal(player, NIGHT + Tuning.DAY_LENGTH, WHOLE_WINDOW);

		expect("a whole night heals five hearts", Math.abs(first - 10f) < 0.01f);
		expect("sleeping again the same night heals nothing", again == 0f);
		expect("the next night heals again, up to the missing health", Math.abs(nextNight - 9f) < 0.01f);
		expect("health follows the heals", Math.abs(player.getHealth() - 20f) < 0.01f);
		finish(helper);
	}

	@GameTest(template = EMPTY_STRUCTURE)
	public void sleepHealingIsSpreadOverTheTimeSlept(GameTestHelper helper) {
		ServerPlayer player = hurtPlayer(helper, 20);
		float total = 0f;

		for (int slice = 0; slice < 3; slice++) { total += SleepHealingService.heal(player, NIGHT + slice * 5000L, 1000L); }

		float expected = 10f * 3000L / SleepHealModel.fullHealWindowTicks();
		expect("only the slept ticks count", Math.abs(total - expected) < 0.01f);
		finish(helper);
	}

	@GameTest(template = EMPTY_STRUCTURE)
	public void hungerBlocksSleepHealing(GameTestHelper helper) {
		ServerPlayer player = hurtPlayer(helper, 14);

		expect("under three quarters of the food bar heals nothing", SleepHealingService.heal(player, NIGHT, WHOLE_WINDOW) == 0f);
		player.getFoodData().setFoodLevel(15);
		expect("exactly three quarters heals the minimum", Math.abs(SleepHealingService.heal(player, NIGHT, WHOLE_WINDOW) - Tuning.HEAL_AT_MIN_FOOD) < 0.01f);
		finish(helper);
	}

	@GameTest(template = EMPTY_STRUCTURE)
	public void naturalRegenerationRuleBlocksSleepHealing(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		GameRules.BooleanValue rule = level.getGameRules().getRule(GameRules.RULE_NATURAL_REGENERATION);
		ServerPlayer player = hurtPlayer(helper, 20);

		rule.set(false, level.getServer());
		float blocked = SleepHealingService.heal(player, NIGHT, WHOLE_WINDOW);
		rule.set(true, level.getServer());

		expect("the rule off stops sleep healing too", blocked == 0f);
		finish(helper);
	}

	@GameTest(template = EMPTY_STRUCTURE)
	public void healthNeverPassesTheMaximum(GameTestHelper helper) {
		ServerPlayer player = hurtPlayer(helper, 20);
		player.setHealth(player.getMaxHealth() - 1f);

		float healed = SleepHealingService.heal(player, NIGHT, WHOLE_WINDOW);

		expect("only the missing health is healed", Math.abs(healed - 1f) < 0.01f);
		expect("health is full, not above", player.getHealth() == player.getMaxHealth());
		finish(helper);
	}

	private void expect(String what, boolean ok) {
		if (!ok) { failures.add(what); }
	}

	private void finish(GameTestHelper helper) {
		if (failures.isEmpty()) {
			helper.succeed();
		} else {
			String message = String.join("; ", failures.stream().limit(15).toList()) + " (" + failures.size() + " failures)";
			failures.clear();
			helper.fail(message);
		}
	}
}
