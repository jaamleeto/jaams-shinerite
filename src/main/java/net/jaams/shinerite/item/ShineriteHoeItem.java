
package net.jaams.shinerite.item;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.HoeItem;

import net.jaams.shinerite.init.ModItems;
import net.jaams.shinerite.dyeable.IDyeableItem;

public class ShineriteHoeItem extends HoeItem implements IDyeableItem {
	public ShineriteHoeItem() {
		super(new Tier() {
			public int getUses() {
				return 632;
			}

			public float getSpeed() {
				return 6f;
			}

			public float getAttackDamageBonus() {
				return 0f;
			}

			public int getLevel() {
				return 2;
			}

			public int getEnchantmentValue() {
				return 16;
			}

			public Ingredient getRepairIngredient() {
				return Ingredient.of(new ItemStack(ModItems.SHINERITE_INGOT.get()));
			}
		}, 0, -0.8f, new Item.Properties());
	}

	@Override
	public int getDefaultColor() {
		return -1;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return false;
	}
}
