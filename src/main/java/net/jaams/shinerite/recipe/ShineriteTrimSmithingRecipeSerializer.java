package net.jaams.shinerite.recipe;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class ShineriteTrimSmithingRecipeSerializer implements RecipeSerializer<ShineriteTrimSmithingRecipe> {
	@Override
	public ShineriteTrimSmithingRecipe fromJson(ResourceLocation id, JsonObject json) {
		Ingredient template = Ingredient.fromJson(GsonHelper.getNonNull(json, "template"));
		Ingredient base = Ingredient.fromJson(GsonHelper.getNonNull(json, "base"));
		Ingredient addition = Ingredient.fromJson(GsonHelper.getNonNull(json, "addition"));
		return new ShineriteTrimSmithingRecipe(id, template, base, addition);
	}

	@Override
	public @Nullable ShineriteTrimSmithingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
		Ingredient template = Ingredient.fromNetwork(buffer);
		Ingredient base = Ingredient.fromNetwork(buffer);
		Ingredient addition = Ingredient.fromNetwork(buffer);
		return new ShineriteTrimSmithingRecipe(id, template, base, addition);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, ShineriteTrimSmithingRecipe recipe) {
		recipe.template.toNetwork(buffer);
		recipe.base.toNetwork(buffer);
		recipe.addition.toNetwork(buffer);
	}
}
