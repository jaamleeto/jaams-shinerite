package net.jaams.shinerite.recipe;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;

import com.google.gson.JsonObject;

public class ShineriteStewRecipeSerializer implements RecipeSerializer<ShineriteStewRecipe> {
	@Override
	public ShineriteStewRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
		return new ShineriteStewRecipe(recipeId, CraftingBookCategory.MISC);
	}

	@Override
	public ShineriteStewRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
		return new ShineriteStewRecipe(recipeId, CraftingBookCategory.MISC);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, ShineriteStewRecipe recipe) {
	}
}
