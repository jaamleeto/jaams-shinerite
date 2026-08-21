package net.jaams.shinerite.recipe;

import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;

import com.google.gson.JsonObject;

public class CopyNBTShapedRecipeSerializer implements RecipeSerializer<CopyNBTShapedRecipe> {
	@Override
	public CopyNBTShapedRecipe fromJson(ResourceLocation id, JsonObject json) {
		ShapedRecipe baseRecipe = RecipeSerializer.SHAPED_RECIPE.fromJson(id, json);
		return new CopyNBTShapedRecipe(id, baseRecipe.getGroup(), CraftingBookCategory.EQUIPMENT, baseRecipe.getWidth(), baseRecipe.getHeight(), baseRecipe.getIngredients(), baseRecipe.getResultItem(null));
	}

	@Override
	public CopyNBTShapedRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
		ShapedRecipe baseRecipe = RecipeSerializer.SHAPED_RECIPE.fromNetwork(id, buffer);
		return new CopyNBTShapedRecipe(id, baseRecipe.getGroup(), CraftingBookCategory.EQUIPMENT, baseRecipe.getWidth(), baseRecipe.getHeight(), baseRecipe.getIngredients(), baseRecipe.getResultItem(null));
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, CopyNBTShapedRecipe recipe) {
		RecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe);
	}
}
