
package net.jaams.shinerite.item;

import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;

import net.jaams.shinerite.init.ModItems;
import net.jaams.shinerite.dyeable.IDyeableItem;
import net.jaams.shinerite.configuration.common.AssortedCommonConfig;

import java.util.Random;

public abstract class ShineriteArmorItem extends ArmorItem implements IDyeableItem {
	private static final long DISGUISE_WINDOW_TICKS = 200L;

	public ShineriteArmorItem(ArmorItem.Type type, Item.Properties properties) {
		super(new ArmorMaterial() {
			@Override
			public int getDurabilityForType(ArmorItem.Type type) {
				return new int[]{36, 42, 42, 36}[type.getSlot().getIndex()] * 15;
			}

			@Override
			public int getDefenseForType(ArmorItem.Type type) {
				return new int[]{2, 4, 4, 2}[type.getSlot().getIndex()];
			}

			@Override
			public int getEnchantmentValue() {
				return 16;
			}

			@Override
			public SoundEvent getEquipSound() {
				return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("item.armor.equip_gold"));
			}

			@Override
			public Ingredient getRepairIngredient() {
				return Ingredient.of(new ItemStack(ModItems.SHINERITE_INGOT.get()));
			}

			@Override
			public String getName() {
				return "shinerite";
			}

			@Override
			public float getToughness() {
				return 0f;
			}

			@Override
			public float getKnockbackResistance() {
				return 0f;
			}
		}, type, properties);
	}

	@Override
	public int getDefaultColor() {
		return -1;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return false;
	}

	@Override
	public boolean makesPiglinsNeutral(ItemStack stack, LivingEntity wearer) {
		if (!AssortedCommonConfig.PIGLIN_DISGUISE.get() || !ShineriteIngotItem.isYellowTinted(stack))
			return false;
		int pieces = 0;
		for (ItemStack armor : wearer.getArmorSlots())
			if (ShineriteIngotItem.isYellowTinted(armor))
				pieces++;
		double chance = Math.min(1d, AssortedCommonConfig.PIGLIN_DISGUISE_CHANCE_PER_PIECE.get() * pieces);
		return disguiseRoll(wearer) < chance;
	}

	private static float disguiseRoll(LivingEntity wearer) {
		long gameTime = wearer.level() == null ? 0L : wearer.level().getGameTime();
		long seed = wearer.getUUID().getMostSignificantBits() ^ wearer.getUUID().getLeastSignificantBits() ^ (gameTime / DISGUISE_WINDOW_TICKS);
		return new Random(seed).nextFloat();
	}

	public static class Helmet extends ShineriteArmorItem {
		public Helmet() {
			super(ArmorItem.Type.HELMET, new Item.Properties());
		}

		@Override
		public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
			return "jaams_shinerite:textures/models/armor/shinerite_layer_1.png";
		}
	}

	public static class Chestplate extends ShineriteArmorItem {
		public Chestplate() {
			super(ArmorItem.Type.CHESTPLATE, new Item.Properties());
		}

		@Override
		public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
			return "jaams_shinerite:textures/models/armor/shinerite_layer_1.png";
		}
	}

	public static class Leggings extends ShineriteArmorItem {
		public Leggings() {
			super(ArmorItem.Type.LEGGINGS, new Item.Properties());
		}

		@Override
		public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
			return "jaams_shinerite:textures/models/armor/shinerite_layer_2.png";
		}
	}

	public static class Boots extends ShineriteArmorItem {
		public Boots() {
			super(ArmorItem.Type.BOOTS, new Item.Properties());
		}

		@Override
		public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
			return "jaams_shinerite:textures/models/armor/shinerite_layer_1.png";
		}
	}
}
