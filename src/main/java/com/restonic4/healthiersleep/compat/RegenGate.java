package com.restonic4.healthiersleep.compat;

import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface RegenGate {
	boolean allows(ServerPlayer player);
}
