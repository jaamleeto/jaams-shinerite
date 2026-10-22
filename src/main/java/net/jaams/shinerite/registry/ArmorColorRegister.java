package net.jaams.shinerite.registry;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.jaams.shinerite.dyeable.IDyeableItem;
import net.jaams.shinerite.recipe.ShineriteTrimSmithingRecipe;
import net.jaams.shinerite.util.ShineriteTrimSupport;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ArmorColorRegister {

	private static final String SHINERITE_TRIM = "jaams_shinerite:shinerite";

	@SubscribeEvent
	public static void onColorArmorRegister(RegisterColorHandlersEvent.Item event) {
		event.register(ArmorColorRegister::getArmorTintColor,
				ShineriteTrimSupport.tintableArmor().toArray(new Item[0]));
	}

	public static int getArmorTintColor(ItemStack stack, int tintIndex) {
		if (tintIndex == 0) {
			if (stack.getItem() instanceof IDyeableItem dyeable) {
				return dyeable.getColor(stack);
			}
			if (stack.getItem() instanceof DyeableLeatherItem leather) {
				return leather.getColor(stack);
			}
			return -1;
		}
		if (tintIndex == 1) {
			return getShineriteTrimColor(stack);
		}
		return -1;
	}



	public static int getShineriteTrimColor(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		if (tag == null || !tag.contains(ShineriteTrimSmithingRecipe.TAG_TRIM_COLOR, Tag.TAG_INT)) {
			return -1;
		}
		CompoundTag trim = tag.getCompound("Trim");
		if (!SHINERITE_TRIM.equals(trim.getString("material"))) {
			return -1;
		}
		return tag.getInt(ShineriteTrimSmithingRecipe.TAG_TRIM_COLOR);
	}
}
