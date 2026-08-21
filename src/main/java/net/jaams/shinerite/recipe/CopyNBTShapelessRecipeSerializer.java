package net.jaams.shinerite.recipe;

import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;

import com.google.gson.JsonObject;

public class CopyNBTShapelessRecipeSerializer implements RecipeSerializer<CopyNBTShapelessRecipe> {
	@Override
	public CopyNBTShapelessRecipe fromJson(ResourceLocation id, JsonObject json) {
		ShapelessRecipe baseRecipe = RecipeSerializer.SHAPELESS_RECIPE.fromJson(id, json);
		return new CopyNBTShapelessRecipe(id, baseRecipe.getGroup(), CraftingBookCategory.MISC, baseRecipe.getIngredients(), baseRecipe.getResultItem(null));
	}

	@Override
	public CopyNBTShapelessRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
		ShapelessRecipe baseRecipe = RecipeSerializer.SHAPELESS_RECIPE.fromNetwork(id, buffer);
		return new CopyNBTShapelessRecipe(id, baseRecipe.getGroup(), CraftingBookCategory.MISC, baseRecipe.getIngredients(), baseRecipe.getResultItem(null));
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, CopyNBTShapelessRecipe recipe) {
		RecipeSerializer.SHAPELESS_RECIPE.toNetwork(buffer, recipe);
	}
}
