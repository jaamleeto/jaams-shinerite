package net.jaams.shinerite.component.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;

import net.jaams.shinerite.dyeable.IDyeableItem;

public class BaseShineriteBlockItem extends BlockItem implements IDyeableItem {
	public BaseShineriteBlockItem(Block block, Item.Properties properties) {
		super(block, properties);
	}

	@Override
	public int getDefaultColor() {
		return -1;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return false;
	}

	public boolean isCoated(ItemStack stack) {
		return stack.getOrCreateTag().getBoolean("Coated");
	}

	public void setCoated(ItemStack stack, boolean coated) {
		stack.getOrCreateTag().putBoolean("Coated", coated);
	}
}
