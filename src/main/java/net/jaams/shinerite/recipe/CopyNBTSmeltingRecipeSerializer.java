package net.jaams.shinerite.recipe;

import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;

import com.google.gson.JsonObject;

public class CopyNBTSmeltingRecipeSerializer implements RecipeSerializer<CopyNBTSmeltingRecipe> {
	@Override
	public CopyNBTSmeltingRecipe fromJson(ResourceLocation id, JsonObject json) {
		SmeltingRecipe baseRecipe = RecipeSerializer.SMELTING_RECIPE.fromJson(id, json);
		return new CopyNBTSmeltingRecipe(id, baseRecipe.getGroup(), CookingBookCategory.MISC,
				baseRecipe.getIngredients().get(0), baseRecipe.getResultItem(null), baseRecipe.getExperience(), baseRecipe.getCookingTime());
	}

	@Override
	public CopyNBTSmeltingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
		SmeltingRecipe baseRecipe = RecipeSerializer.SMELTING_RECIPE.fromNetwork(id, buffer);
		return new CopyNBTSmeltingRecipe(id, baseRecipe.getGroup(), CookingBookCategory.MISC,
				baseRecipe.getIngredients().get(0), baseRecipe.getResultItem(null), baseRecipe.getExperience(), baseRecipe.getCookingTime());
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, CopyNBTSmeltingRecipe recipe) {
		RecipeSerializer.SMELTING_RECIPE.toNetwork(buffer, recipe);
	}
}
