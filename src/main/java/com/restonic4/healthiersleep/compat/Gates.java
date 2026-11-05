package com.restonic4.healthiersleep.compat;

import com.restonic4.healthiersleep.HealthierSleep;
import com.restonic4.healthiersleep.core.SleepHealModel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;

public final class Gates {
	private static final String TEMPERATURE_MOD = "inconvenient_temperature";
	private static final String THIRST_MOD = "dehydration";

	private static final AtomicBoolean thirstFailureLogged = new AtomicBoolean();

	private static volatile List<RegenGate> resolved;

	private Gates() {}

	public static boolean allSatisfied(ServerPlayer player) {
		for (RegenGate gate : gates()) {
			if (!gate.allows(player)) return false;
		}

		return true;
	}

	private static List<RegenGate> gates() {
		List<RegenGate> gates = resolved;
		if (gates != null) return gates;

		gates = new ArrayList<>();
		if (FabricLoader.getInstance().isModLoaded(TEMPERATURE_MOD)) addTemperature(gates);
		if (FabricLoader.getInstance().isModLoaded(THIRST_MOD)) addThirst(gates);

		resolved = List.copyOf(gates);
		return resolved;
	}

	@SuppressWarnings("unchecked")
	private static void addTemperature(List<RegenGate> gates) {
		try {
			TemperatureReader<ServerPlayer> reader = TemperatureReader.resolve(
				TemperatureReader.TEMPERATURE_ATTACHMENTS,
				TemperatureReader.TEMPERATURE_TUNING,
				(player, attachment) -> player.getAttachedOrElse((AttachmentType<Float>) attachment, 0f)
			);

			gates.add(player -> SleepHealModel.temperatureAllowsHealing(reader.bodyTemperature(player), reader.barLimit()));
		} catch (ReflectiveOperationException | RuntimeException e) {
			HealthierSleep.LOGGER.warn("Inconvenient Temperature is installed but its body temperature could not be read; the temperature condition is off", e);
		}
	}

	private static void addThirst(List<RegenGate> gates) {
		try {
			ThirstReader reader = ThirstReader.resolve(ThirstReader.DEHYDRATION_ACCESS, ThirstReader.DEHYDRATION_MANAGER);
			gates.add(player -> allowsThirst(reader, player));
		} catch (ReflectiveOperationException | RuntimeException e) {
			HealthierSleep.LOGGER.warn("Dehydration is installed but its thirst level could not be read; the thirst condition is off", e);
		}
	}

	private static boolean allowsThirst(ThirstReader reader, ServerPlayer player) {
		try {
			return !reader.tracksThirst(player) || SleepHealModel.thirstAllowsHealing(reader.thirstLevel(player));
		} catch (ReflectiveOperationException e) {
			if (thirstFailureLogged.compareAndSet(false, true)) {
				HealthierSleep.LOGGER.warn("Dehydration thirst level could not be read; the thirst condition is off from now on", e);
			}

			return true;
		}
	}
}
