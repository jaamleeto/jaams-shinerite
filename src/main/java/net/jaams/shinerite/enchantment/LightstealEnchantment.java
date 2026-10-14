
package net.jaams.shinerite.enchantment;

import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;

import net.jaams.shinerite.util.ModTags;
import net.jaams.shinerite.init.ModEnchantments;
import net.jaams.shinerite.configuration.common.EnchantmentsConfig;

import java.util.List;

public class LightstealEnchantment extends Enchantment {
	public LightstealEnchantment(EquipmentSlot... slots) {
		super(Enchantment.Rarity.RARE, EnchantmentCategory.WEAPON, slots);
	}

	@Override
	public int getMaxLevel() {
		return EnchantmentsConfig.LIGHTSTEAL_MAX_LEVEL.get();
	}

	@Override
	public int getMinCost(int level) {
		return EnchantmentsConfig.LIGHTSTEAL_MIN_COST.get() + (level - 1) * EnchantmentsConfig.LIGHTSTEAL_COST_PER_LEVEL.get();
	}

	@Override
	public int getMaxCost(int level) {
		return getMinCost(level) + EnchantmentsConfig.LIGHTSTEAL_COST_RANGE.get();
	}

	@Override
	protected boolean checkCompatibility(Enchantment ench) {
		return this != ench && !List.of(ModEnchantments.LIGHTBRINGER.get()).contains(ench);
	}

	@Override
	public boolean canEnchant(ItemStack stack) {
		return EnchantmentsConfig.LIGHTSTEAL.get() && stack.is(ModTags.ENCHANTABLE_LIGHTSTEAL);
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack) {
		return EnchantmentsConfig.LIGHTSTEAL.get() && stack.is(ModTags.ENCHANTABLE_LIGHTSTEAL);
	}

	@Override
	public boolean isAllowedOnBooks() {
		return EnchantmentsConfig.LIGHTSTEAL.get();
	}

	@Override
	public boolean isTradeable() {
		return false;
	}

	@Override
	public boolean isTreasureOnly() {
		return EnchantmentsConfig.LIGHTSTEAL_IS_TREASURE.get();
	}
}
