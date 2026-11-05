package com.restonic4.healthiersleep.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ReaderTest {
	public static class FakeManager {
		private final int level;
		private final boolean tracked;

		public FakeManager(int level, boolean tracked) {
			this.level = level;
			this.tracked = tracked;
		}

		public int getThirstLevel() { return level; }

		public boolean hasThirst() { return tracked; }
	}

	public static class FakePlayer {
		private final FakeManager manager;
		private final float temperature;

		public FakePlayer(int thirst, boolean tracked, float temperature) {
			this.manager = new FakeManager(thirst, tracked);
			this.temperature = temperature;
		}

		public FakeManager getThirstManager() { return manager; }
	}

	public static class FakeAttachments {
		public static final Object BODY_TEMPERATURE = "token";
	}

	public static class FakeTuning {
		public static final float BAR_LIMIT = 100f;
	}

	private static final String PLAYER = ReaderTest.class.getName() + "$FakePlayer";
	private static final String MANAGER = ReaderTest.class.getName() + "$FakeManager";

	@Test
	void thirstReaderCallsThePublicDehydrationShape() throws Exception {
		ThirstReader reader = ThirstReader.resolve(PLAYER, MANAGER);
		FakePlayer thirsty = new FakePlayer(7, true, 0f);

		assertEquals(7, reader.thirstLevel(thirsty));
		assertTrue(reader.tracksThirst(thirsty));
		assertFalse(reader.tracksThirst(new FakePlayer(0, false, 0f)));
	}

	@Test
	void thirstReaderFailsLoudlyWhenTheModShapeChanged() {
		assertThrows(ClassNotFoundException.class, () -> ThirstReader.resolve("net.dehydration.access.Missing", MANAGER));
		assertThrows(NoSuchMethodException.class, () -> ThirstReader.resolve(PLAYER, FakeAttachments.class.getName()));
	}

	@Test
	void temperatureReaderResolvesTheStaticsAndReadsThroughTheCallback() throws Exception {
		TemperatureReader<FakePlayer> reader = TemperatureReader.resolve(
			FakeAttachments.class.getName(),
			FakeTuning.class.getName(),
			(player, attachment) -> {
				assertEquals("token", attachment);
				return player.temperature;
			}
		);

		assertEquals(100f, reader.barLimit());
		assertEquals(-42f, reader.bodyTemperature(new FakePlayer(0, true, -42f)));
	}

	@Test
	void temperatureReaderFailsLoudlyWhenTheModShapeChanged() {
		assertThrows(NoSuchFieldException.class, () -> TemperatureReader.<FakePlayer>resolve(
			FakeTuning.class.getName(),
			FakeTuning.class.getName(),
			(player, attachment) -> 0f
		));
		assertThrows(ClassNotFoundException.class, () -> TemperatureReader.<FakePlayer>resolve(
			"missing.Attachments",
			FakeTuning.class.getName(),
			(player, attachment) -> 0f
		));
	}
}
