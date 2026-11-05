package com.restonic4.healthiersleep.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class NightLedgerTest {
	@Test
	void sameNightKeepsTheLedger() {
		NightLedger ledger = new NightLedger(3L, 4f);

		assertSame(ledger, ledger.forNight(3L));
	}

	@Test
	void aNewNightStartsEmpty() {
		NightLedger fresh = new NightLedger(3L, 4f).forNight(4L);

		assertEquals(4L, fresh.nightId());
		assertEquals(0f, fresh.healed());
	}

	@Test
	void theEmptyLedgerBelongsToNoNight() {
		assertEquals(0f, NightLedger.EMPTY.forNight(0L).healed());
		assertEquals(0L, NightLedger.EMPTY.forNight(0L).nightId());
	}

	@Test
	void plusAddsToTheSameNight() {
		NightLedger ledger = new NightLedger(3L, 1f).plus(2.5f);

		assertEquals(3L, ledger.nightId());
		assertEquals(3.5f, ledger.healed());
	}
}
