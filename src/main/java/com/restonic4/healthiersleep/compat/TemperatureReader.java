package com.restonic4.healthiersleep.compat;

import java.lang.reflect.Field;
import java.util.function.BiFunction;

public final class TemperatureReader<P> {
	public static final String TEMPERATURE_ATTACHMENTS = "com.restonic4.inconvenienttemperature.ModAttachments";
	public static final String TEMPERATURE_TUNING = "com.restonic4.inconvenienttemperature.config.Tuning";

	private final Object attachment;
	private final float barLimit;
	private final BiFunction<P, Object, Float> reader;

	private TemperatureReader(Object attachment, float barLimit, BiFunction<P, Object, Float> reader) {
		this.attachment = attachment;
		this.barLimit = barLimit;
		this.reader = reader;
	}

	public static <P> TemperatureReader<P> resolve(
		String attachmentsClass,
		String tuningClass,
		BiFunction<P, Object, Float> reader
	) throws ReflectiveOperationException {
		Field attachment = Class.forName(attachmentsClass).getField("BODY_TEMPERATURE");
		Field limit = Class.forName(tuningClass).getField("BAR_LIMIT");

		return new TemperatureReader<>(attachment.get(null), limit.getFloat(null), reader);
	}

	public float bodyTemperature(P player) { return reader.apply(player, attachment); }

	public float barLimit() { return barLimit; }
}
