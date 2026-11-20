package net.jaams.shinerite.compat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.fml.ModList;

import net.jaams.combat.capability.backitem.BackItemProvider;
import net.jaams.combat.capability.backitem.IBackItem;
import net.jaams.combat.keybind.BackItemSyncMessage;

/**
 * Jaam's Combat back-slot compatibility.
 *
 * <p>Bridge class following the same pattern as the other mods' compat classes:
 * no reflection is used. All direct references to Jaam's Combat live in
 * {@link BackItemImpl}, which is loaded lazily only after {@link #isAvailable()}
 * succeeds, and every public entry point is wrapped in a {@code Throwable} catch
 * so the mod can safely be absent or mismatched.</p>
 */
public final class BackItemCompat {

	public static final String MOD_ID = "jaams_combat";

	private static Boolean loaded;

	private BackItemCompat() {
	}

	/** Cheap presence check that never touches an optional class. */
	public static boolean isAvailable() {
		if (loaded == null) {
			try {
				loaded = ModList.get() != null && ModList.get().isLoaded(MOD_ID);
			} catch (Throwable t) {
				loaded = false;
			}
		}
		return loaded;
	}

	public static ItemStack getBackItem(LivingEntity entity) {
		if (!isAvailable() || entity == null) {
			return ItemStack.EMPTY;
		}
		try {
			return BackItemImpl.getBackItem(entity);
		} catch (Throwable t) {
			return ItemStack.EMPTY;
		}
	}

	public static void setBackItem(LivingEntity entity, ItemStack stack) {
		if (!isAvailable() || entity == null || stack == null) {
			return;
		}
		try {
			BackItemImpl.setBackItem(entity, stack);
		} catch (Throwable t) {
			// Optional compat: never break the caller when the back slot is unavailable.
		}
	}

	/** All direct Jaam's Combat references are isolated in this lazily loaded class. */
	private static final class BackItemImpl {

		static ItemStack getBackItem(LivingEntity entity) {
			return entity.getCapability(BackItemProvider.BACK_ITEM)
					.map(IBackItem::getBackItem)
					.orElse(ItemStack.EMPTY);
		}

		static void setBackItem(LivingEntity entity, ItemStack stack) {
			entity.getCapability(BackItemProvider.BACK_ITEM).ifPresent(backItem -> {
				backItem.setBackItem(stack);
				BackItemSyncMessage.syncFull(entity);
			});
		}
	}
}
