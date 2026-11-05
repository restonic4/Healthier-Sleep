package com.restonic4.healthiersleep.compat;

import java.lang.reflect.Method;

public final class ThirstReader {
	public static final String DEHYDRATION_ACCESS = "net.dehydration.access.ThirstManagerAccess";
	public static final String DEHYDRATION_MANAGER = "net.dehydration.thirst.ThirstManager";

	private final Method getManager;
	private final Method getLevel;
	private final Method hasThirst;

	private ThirstReader(Method getManager, Method getLevel, Method hasThirst) {
		this.getManager = getManager;
		this.getLevel = getLevel;
		this.hasThirst = hasThirst;
	}

	public static ThirstReader resolve(String accessClass, String managerClass) throws ReflectiveOperationException {
		Method getManager = Class.forName(accessClass).getMethod("getThirstManager");
		Class<?> manager = Class.forName(managerClass);

		return new ThirstReader(getManager, manager.getMethod("getThirstLevel"), manager.getMethod("hasThirst"));
	}

	public boolean tracksThirst(Object player) throws ReflectiveOperationException {
		return (boolean) hasThirst.invoke(getManager.invoke(player));
	}

	public int thirstLevel(Object player) throws ReflectiveOperationException {
		return (int) getLevel.invoke(getManager.invoke(player));
	}
}
