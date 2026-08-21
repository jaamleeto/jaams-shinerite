
package net.jaams.shinerite.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;

import net.jaams.shinerite.dyeable.IDyeableItem;

public class ShinyScraperItem extends Item implements IDyeableItem {
	public ShinyScraperItem() {
		super(new Item.Properties().durability(160).rarity(Rarity.COMMON));
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
}
