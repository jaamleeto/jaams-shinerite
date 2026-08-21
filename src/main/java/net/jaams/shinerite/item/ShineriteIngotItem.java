
package net.jaams.shinerite.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;

import net.jaams.shinerite.configuration.common.MechanicsConfig;
import net.jaams.shinerite.dyeable.IDyeableItem;

public class ShineriteIngotItem extends Item implements IDyeableItem {
	public ShineriteIngotItem() {
		super(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON));
	}

	@Override
	public int getDefaultColor() {
		return -1;
	}

	@Override
	public boolean isPiglinCurrency(ItemStack stack) {
		return isGoldenTinted(stack);
	}

	public static boolean isGoldenTinted(ItemStack stack) {
		if (!MechanicsConfig.BARTERING.get() || !(stack.getItem() instanceof IDyeableItem dyeable) || !dyeable.hasColor(stack))
			return false;
		int color = dyeable.getColor(stack);
		float r = (color >> 16 & 255) / 255f;
		float g = (color >> 8 & 255) / 255f;
		float b = (color & 255) / 255f;
		float max = Math.max(r, Math.max(g, b));
		float min = Math.min(r, Math.min(g, b));
		if (max <= 0f || (max - min) / max < 0.3f)
			return false;
		float delta = max - min;
		float hue;
		if (max == r)
			hue = 60f * (((g - b) / delta) % 6f);
		else if (max == g)
			hue = 60f * ((b - r) / delta + 2f);
		else
			hue = 60f * ((r - g) / delta + 4f);
		if (hue < 0f)
			hue += 360f;
		return hue >= 35f && hue <= 75f;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return false;
	}
}
