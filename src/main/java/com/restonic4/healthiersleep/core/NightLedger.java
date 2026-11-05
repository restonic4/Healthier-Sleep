package com.restonic4.healthiersleep.core;

public record NightLedger(long nightId, float healed) {
	public static final NightLedger EMPTY = new NightLedger(Long.MIN_VALUE, 0f);

	public NightLedger forNight(long id) { return id == nightId ? this : new NightLedger(id, 0f); }

	public NightLedger plus(float amount) { return new NightLedger(nightId, healed + amount); }
}
