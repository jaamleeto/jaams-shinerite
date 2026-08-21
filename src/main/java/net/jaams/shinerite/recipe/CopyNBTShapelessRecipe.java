
package net.jaams.shinerite.recipe;

import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.NonNullList;

import net.jaams.shinerite.init.ModRecipes;

public class CopyNBTShapelessRecipe extends ShapelessRecipe {
	public CopyNBTShapelessRecipe(ResourceLocation id, String group, CraftingBookCategory category, NonNullList<Ingredient> ingredients, ItemStack result) {
		super(id, group, category, result, ingredients);
	}

	@Override
	public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
		ItemStack resultStack = super.assemble(container, registryAccess);
		int totalRed = 0;
		int totalGreen = 0;
		int totalBlue = 0;
		int colorCount = 0;
		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack ingredient = container.getItem(i);
			if (!ingredient.isEmpty()) {
				if (ingredient.hasTag()) {
					resultStack.setTag(ingredient.getTag().copy());
				}
				int color = -1;
				if (ingredient.hasTag()) {
					CompoundTag tag = ingredient.getTag();
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
					totalRed += (color >> 16) & 0xFF;
					totalGreen += (color >> 8) & 0xFF;
					totalBlue += color & 0xFF;
					colorCount++;
				}
			}
		}
		if (colorCount > 0) {
			int mixedRed = totalRed / colorCount;
			int mixedGreen = totalGreen / colorCount;
			int mixedBlue = totalBlue / colorCount;
			int mixedColor = (mixedRed << 16) | (mixedGreen << 8) | mixedBlue;
			CompoundTag resultTag = resultStack.getOrCreateTag();
			resultTag.putInt("color", mixedColor);
		}
		return resultStack;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModRecipes.COPY_NBT_SHAPELESS_SERIALIZER.get();
	}
}
