package net.jaams.shinerite.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public final class BackItemCompat {
	private static final String PROVIDER_CLASS = "net.jaams.combat.capability.backitem.BackItemProvider";
	private static final String BACK_ITEM_CLASS = "net.jaams.combat.capability.backitem.IBackItem";
	private static final String SYNC_CLASS = "net.jaams.combat.keybind.BackItemSyncMessage";

	private static boolean resolved;
	private static Capability<?> capability;
	private static Method getBackItemMethod;
	private static Method setBackItemMethod;
	private static Method syncFullMethod;

	private BackItemCompat() {
	}

	public static boolean isAvailable() {
		resolve();
		return capability != null;
	}

	public static ItemStack getBackItem(LivingEntity entity) {
		if (entity == null) {
			return ItemStack.EMPTY;
		}
		Object impl = getImpl(entity);
		if (impl == null) {
			return ItemStack.EMPTY;
		}
		try {
			Object result = getBackItemMethod.invoke(impl);
			return result instanceof ItemStack stack ? stack : ItemStack.EMPTY;
		} catch (ReflectiveOperationException e) {
			return ItemStack.EMPTY;
		}
	}

	public static void setBackItem(LivingEntity entity, ItemStack stack) {
		if (entity == null || stack == null) {
			return;
		}
		Object impl = getImpl(entity);
		if (impl == null) {
			return;
		}
		try {
			setBackItemMethod.invoke(impl, stack);
			syncToClients(entity);
		} catch (ReflectiveOperationException ignored) {
		}
	}

	@SuppressWarnings("unchecked")
	private static Object getImpl(LivingEntity entity) {
		resolve();
		if (capability == null) {
			return null;
		}
		LazyOptional<Object> optional = entity.getCapability((Capability<Object>) capability);
		return optional.orElse(null);
	}

	private static void syncToClients(LivingEntity entity) {
		if (syncFullMethod == null) {
			return;
		}
		try {
			syncFullMethod.invoke(null, entity);
		} catch (ReflectiveOperationException ignored) {
		}
	}

	private static void resolve() {
		if (resolved) {
			return;
		}
		resolved = true;
		try {
			Class<?> providerClass = Class.forName(PROVIDER_CLASS);
			Field capabilityField = providerClass.getField("BACK_ITEM");
			capability = (Capability<?>) capabilityField.get(null);

			Class<?> backItemInterface = Class.forName(BACK_ITEM_CLASS);
			getBackItemMethod = backItemInterface.getMethod("getBackItem");
			setBackItemMethod = backItemInterface.getMethod("setBackItem", ItemStack.class);
		} catch (ReflectiveOperationException | LinkageError e) {
			capability = null;
			getBackItemMethod = null;
			setBackItemMethod = null;
		}
		try {
			Class<?> syncClass = Class.forName(SYNC_CLASS);
			syncFullMethod = syncClass.getMethod("syncFull", LivingEntity.class);
		} catch (ReflectiveOperationException | LinkageError e) {
			syncFullMethod = null;
		}
	}
}
