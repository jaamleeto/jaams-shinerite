
package net.jaams.shinerite.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.dyeable.IDyeableItem;

public class ShinyPaintBrushItem extends Item implements IDyeableItem {
	public ShinyPaintBrushItem() {
		super(new Item.Properties().durability(60).rarity(Rarity.COMMON));
	}

	@Override
	public int getDefaultColor() {
		return -1;
	}

	@Override
	public int getEnchantmentValue() {
		return 16;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return false;
	}

	@Override
	public int getBarColor(ItemStack stack) {
		int color = ModUtils.getCurrentColor(stack);
		if (color != -1) {
			return color;
		}
		return super.getBarColor(stack);
	}
}
