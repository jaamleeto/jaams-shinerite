package net.jaams.shinerite.recipe;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Container;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.RegistryAccess;

import net.jaams.shinerite.init.ModRecipes;

public class CopyNBTStonecutterRecipe extends StonecutterRecipe {
	public CopyNBTStonecutterRecipe(ResourceLocation id, String group, Ingredient ingredient, ItemStack result) {
		super(id, group, ingredient, result);
	}

	@Override
	public ItemStack assemble(Container inputContainer, RegistryAccess registryAccess) {
		ItemStack inputStack = inputContainer.getItem(0);
		ItemStack resultStack = this.result.copy();
		if (inputStack.hasTag()) {
			resultStack.setTag(inputStack.getTag().copy());
		}
		int color = -1;
		if (inputStack.hasTag()) {
			CompoundTag tag = inputStack.getTag();
			if (tag.contains("color", Tag.TAG_INT)) {
				color = tag.getInt("color");
			} else if (tag.contains("display", Tag.TAG_COMPOUND)) {
				CompoundTag display = tag.getCompound("display");
				if (display.contains("color", Tag.TAG_INT)) {
					color = display.getInt("color");
				}
			}
		}
		if (color != -1) {
			CompoundTag resultTag = resultStack.getOrCreateTag();
			resultTag.putInt("color", color);
		}
		return resultStack;
	}

	@Override
	public boolean matches(Container container, Level level) {
		return this.ingredient.test(container.getItem(0));
	}

	@Override
	public ItemStack getToastSymbol() {
		return new ItemStack(Blocks.STONECUTTER);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModRecipes.COPY_NBT_STONECUTTER_SERIALIZER.get();
	}
}
