
package net.jaams.shinerite.enchantment;

import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;

import net.jaams.shinerite.util.ModTags;
import net.jaams.shinerite.configuration.common.EnchantmentsConfig;

import java.util.List;

public class LightguardEnchantment extends Enchantment {
	public LightguardEnchantment(EquipmentSlot... slots) {
		super(Enchantment.Rarity.COMMON, EnchantmentCategory.ARMOR, slots);
	}

	@Override
	public int getMaxLevel() {
		return EnchantmentsConfig.LIGHTGUARD_MAX_LEVEL.get();
	}

	@Override
	public int getMinCost(int level) {
		return EnchantmentsConfig.LIGHTGUARD_MIN_COST.get() + (level - 1) * EnchantmentsConfig.LIGHTGUARD_COST_PER_LEVEL.get();
	}

	@Override
	public int getMaxCost(int level) {
		return getMinCost(level) + EnchantmentsConfig.LIGHTGUARD_COST_RANGE.get();
	}

	@Override
	protected boolean checkCompatibility(Enchantment ench) {
		return List.of(Enchantments.THORNS).contains(ench);
	}

	@Override
	public boolean canEnchant(ItemStack stack) {
		return EnchantmentsConfig.LIGHTGUARD.get() && stack.is(ModTags.ENCHANTABLE_LIGHTGUARD);
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack) {
		return EnchantmentsConfig.LIGHTGUARD.get() && stack.is(ModTags.ENCHANTABLE_LIGHTGUARD);
	}

	@Override
	public boolean isAllowedOnBooks() {
		return EnchantmentsConfig.LIGHTGUARD.get();
	}

	@Override
	public boolean isTradeable() {
		return false;
	}

	@Override
	public boolean isTreasureOnly() {
		return EnchantmentsConfig.LIGHTGUARD_IS_TREASURE.get();
	}
}
