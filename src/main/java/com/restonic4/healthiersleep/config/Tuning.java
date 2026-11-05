package com.restonic4.healthiersleep.config;

public final class Tuning {
	public static final long DAY_LENGTH = 24000L;
	public static final long BED_OPENS_AT = 12542L;
	public static final long BED_CLOSES_AT = 23460L;
	public static final long NIGHT_LEDGER_RESETS_AT = 6000L;

	public static final int SLEEP_SPEED = 20;

	public static final int FOOD_MAX = 20;
	public static final float FOOD_MIN_FRACTION = 0.75f;
	public static final float HEAL_AT_MIN_FOOD = 2f;
	public static final float HEAL_AT_FULL_FOOD = 10f;
	public static final float FULL_HEAL_NIGHT_FRACTION = 0.85f;

	public static final int THIRST_MAX = 20;
	public static final float THIRST_MIN_FRACTION = 0.5f;

	public static final float TEMPERATURE_EDGE_FRACTION = 0.25f;

	private Tuning() {}
}
