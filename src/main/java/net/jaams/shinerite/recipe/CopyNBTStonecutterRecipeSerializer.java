package net.jaams.shinerite.recipe;

import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;

import com.google.gson.JsonObject;

public class CopyNBTStonecutterRecipeSerializer implements RecipeSerializer<CopyNBTStonecutterRecipe> {
	@Override
	public CopyNBTStonecutterRecipe fromJson(ResourceLocation id, JsonObject json) {
		StonecutterRecipe baseRecipe = RecipeSerializer.STONECUTTER.fromJson(id, json);
		return new CopyNBTStonecutterRecipe(id, baseRecipe.getGroup(), baseRecipe.getIngredients().get(0), baseRecipe.getResultItem(null));
	}

	@Override
	public CopyNBTStonecutterRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
		StonecutterRecipe baseRecipe = RecipeSerializer.STONECUTTER.fromNetwork(id, buffer);
		return new CopyNBTStonecutterRecipe(id, baseRecipe.getGroup(), baseRecipe.getIngredients().get(0), baseRecipe.getResultItem(null));
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, CopyNBTStonecutterRecipe recipe) {
		RecipeSerializer.STONECUTTER.toNetwork(buffer, recipe);
	}
}
