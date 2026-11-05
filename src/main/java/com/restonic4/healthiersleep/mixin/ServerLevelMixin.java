package com.restonic4.healthiersleep.mixin;

import com.restonic4.healthiersleep.world.SleepNight;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
	@Shadow
	@Final
	private SleepStatus sleepStatus;

	@Unique
	private long healthierSleep$dayTimeBefore;

	@Shadow
	protected abstract void wakeUpAllPlayers();

	@Redirect(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/players/SleepStatus;areEnoughDeepSleeping(ILjava/util/List;)Z"
		)
	)
	private boolean healthierSleep$noInstantSkip(SleepStatus status, int percentage, List<ServerPlayer> players) {
		return false;
	}

	@Inject(method = "tickTime", at = @At("HEAD"))
	private void healthierSleep$rememberTime(CallbackInfo ci) {
		healthierSleep$dayTimeBefore = ((ServerLevel) (Object) this).getDayTime();
	}

	@Inject(method = "tickTime", at = @At("TAIL"))
	private void healthierSleep$passTheNight(CallbackInfo ci) {
		if (SleepNight.afterTimeTick((ServerLevel) (Object) this, healthierSleep$dayTimeBefore, sleepStatus)) wakeUpAllPlayers();
	}
}
