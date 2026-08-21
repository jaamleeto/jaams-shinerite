package net.jaams.shinerite.recipe;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;

import com.google.gson.JsonObject;

public class CopyNBTBlastingRecipeSerializer implements RecipeSerializer<CopyNBTBlastingRecipe> {
	@Override
	public CopyNBTBlastingRecipe fromJson(ResourceLocation id, JsonObject json) {
		BlastingRecipe baseRecipe = RecipeSerializer.BLASTING_RECIPE.fromJson(id, json);
		return new CopyNBTBlastingRecipe(id, baseRecipe.getGroup(), CookingBookCategory.MISC, // Usa una categoría predeterminada.
				baseRecipe.getIngredients().get(0), baseRecipe.getResultItem(null), baseRecipe.getExperience(), baseRecipe.getCookingTime());
	}

	@Override
	public CopyNBTBlastingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
		BlastingRecipe baseRecipe = RecipeSerializer.BLASTING_RECIPE.fromNetwork(id, buffer);
		return new CopyNBTBlastingRecipe(id, baseRecipe.getGroup(), CookingBookCategory.MISC, // Usa una categoría predeterminada.
				baseRecipe.getIngredients().get(0), baseRecipe.getResultItem(null), baseRecipe.getExperience(), baseRecipe.getCookingTime());
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, CopyNBTBlastingRecipe recipe) {
		RecipeSerializer.BLASTING_RECIPE.toNetwork(buffer, recipe);
	}
}
