package com.restonic4.healthiersleep.world;

import com.restonic4.healthiersleep.ModAttachments;
import com.restonic4.healthiersleep.compat.Gates;
import com.restonic4.healthiersleep.core.NightLedger;
import com.restonic4.healthiersleep.core.SleepHealModel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;

public final class SleepHealingService {
	private SleepHealingService() {}

	public static void healSleepers(ServerLevel level, long ambientTicks) {
		if (ambientTicks <= 0L) return;

		for (ServerPlayer player : level.players()) {
			if (player.isSleepingLongEnough() && !player.isSpectator()) heal(player, level.getDayTime(), ambientTicks);
		}
	}

	public static float heal(ServerPlayer player, long dayTime, long ambientTicks) {
		if (!player.serverLevel().getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION)) return 0f;
		if (!player.isHurt() || !Gates.allSatisfied(player)) return 0f;

		NightLedger ledger = player.getAttachedOrElse(ModAttachments.NIGHT_LEDGER, NightLedger.EMPTY).forNight(SleepHealModel.nightId(dayTime));
		float budget = SleepHealModel.nightBudget(player.getFoodData().getFoodLevel());
		float amount = SleepHealModel.healFor(budget, ledger.healed(), ambientTicks, player.getMaxHealth() - player.getHealth());
		if (amount <= 0f) return 0f;

		player.heal(amount);
		player.setAttached(ModAttachments.NIGHT_LEDGER, ledger.plus(amount));

		return amount;
	}
}
