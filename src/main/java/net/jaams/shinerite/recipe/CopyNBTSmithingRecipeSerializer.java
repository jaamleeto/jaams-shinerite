package net.jaams.shinerite.recipe;

import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;

public class CopyNBTSmithingRecipeSerializer implements RecipeSerializer<CopyNBTSmithingRecipe> {
	@Override
	public CopyNBTSmithingRecipe fromJson(ResourceLocation id, JsonObject json) {
		Ingredient template = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "template"));
		Ingredient base = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "base"));
		Ingredient addition = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "addition"));
		JsonObject resultJson = GsonHelper.getAsJsonObject(json, "result");
		ItemStack result = ItemStack.EMPTY;
		if (resultJson.has("item")) {
			ResourceLocation itemId = new ResourceLocation(GsonHelper.getAsString(resultJson, "item"));
			int count = GsonHelper.getAsInt(resultJson, "count", 1);
			result = new ItemStack(ForgeRegistries.ITEMS.getValue(itemId), count);
			if (resultJson.has("nbt")) {
				try {
					String nbtString = GsonHelper.getAsString(resultJson, "nbt");
					result.setTag(net.minecraft.nbt.TagParser.parseTag(nbtString));
				} catch (Exception ignored) {
				}
			}
		}
		return new CopyNBTSmithingRecipe(id, template, base, addition, result);
	}

	@Override
	public @Nullable CopyNBTSmithingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
		Ingredient template = Ingredient.fromNetwork(buffer);
		Ingredient base = Ingredient.fromNetwork(buffer);
		Ingredient addition = Ingredient.fromNetwork(buffer);
		ItemStack result = buffer.readItem();
		return new CopyNBTSmithingRecipe(id, template, base, addition, result);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, CopyNBTSmithingRecipe recipe) {
		recipe.template.toNetwork(buffer);
		recipe.base.toNetwork(buffer);
		recipe.addition.toNetwork(buffer);
		buffer.writeItem(recipe.result);
	}
}
