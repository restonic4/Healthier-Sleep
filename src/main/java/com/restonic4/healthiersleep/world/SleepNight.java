package com.restonic4.healthiersleep.world;

import com.restonic4.healthiersleep.config.Tuning;
import com.restonic4.healthiersleep.core.SleepClock;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.SleepStatus;
import net.minecraft.world.level.GameRules;

public final class SleepNight {
	private SleepNight() {}

	public static boolean enoughDeepSleepers(ServerLevel level, SleepStatus sleepers) {
		int percentage = level.getGameRules().getInt(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE);
		return sleepers.areEnoughSleeping(percentage) && sleepers.areEnoughDeepSleeping(percentage, level.players());
	}

	/** Runs after vanilla advanced the clock by one tick. Returns true when the night is over and everyone must wake. */
	public static boolean afterTimeTick(ServerLevel level, long before, SleepStatus sleepers) {
		boolean accelerate = enoughDeepSleepers(level, sleepers);
		long natural = level.getDayTime();
		long target = accelerate ? SleepClock.acceleratedTime(before, natural, Tuning.SLEEP_SPEED) : natural;

		if (target != natural) {
			level.setDayTime(target);
			broadcastClock(level);
		}

		SleepHealingService.healSleepers(level, target - before);

		if (!accelerate || !isMorning(level, before, target)) return false;
		if (level.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE) && level.isRaining()) level.resetWeatherCycle();
		return true;
	}

	private static boolean isMorning(ServerLevel level, long before, long target) {
		if (!level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) return true;
		return SleepClock.reachedMorning(before, target) || level.isDay();
	}

	private static void broadcastClock(ServerLevel level) {
		ClientboundSetTimePacket packet = new ClientboundSetTimePacket(
			level.getGameTime(),
			level.getDayTime(),
			level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)
		);

		level.getServer().getPlayerList().broadcastAll(packet, level.dimension());
	}
}
