package com.restonic4.healthiersleep;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.restonic4.healthiersleep.core.NightLedger;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public final class ModAttachments {
	private static final Codec<NightLedger> NIGHT_LEDGER_CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.LONG.fieldOf("night").forGetter(NightLedger::nightId),
		Codec.FLOAT.fieldOf("healed").forGetter(NightLedger::healed)
	).apply(i, NightLedger::new));

	public static final AttachmentType<NightLedger> NIGHT_LEDGER = AttachmentRegistry.createPersistent(
		HealthierSleep.id("night_ledger"),
		NIGHT_LEDGER_CODEC
	);

	private ModAttachments() {}

	public static void register() {}
}
