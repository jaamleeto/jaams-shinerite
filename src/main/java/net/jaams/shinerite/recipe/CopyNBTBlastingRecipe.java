package net.jaams.shinerite.recipe;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Container;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.RegistryAccess;

import net.jaams.shinerite.init.ModRecipes;

public class CopyNBTBlastingRecipe extends BlastingRecipe {
	public CopyNBTBlastingRecipe(ResourceLocation id, String group, CookingBookCategory category, Ingredient ingredient, ItemStack result, float experience, int cookingTime) {
		super(id, group, category, ingredient, result, experience, cookingTime);
	}

	@Override
	public ItemStack assemble(Container inputContainer, RegistryAccess registryAccess) {
		ItemStack inputStack = inputContainer.getItem(0);
		ItemStack resultStack = this.getResultItem(registryAccess).copy();
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
	public ItemStack getToastSymbol() {
		return new ItemStack(Blocks.BLAST_FURNACE);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModRecipes.COPY_NBT_BLASTING_SERIALIZER.get();
	}
}
