package net.jaams.shinerite.recipe;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Container;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.RegistryAccess;

import net.jaams.shinerite.init.ModRecipes;
import net.jaams.shinerite.util.ModUtils;

public class CopyNBTSmithingRecipe extends SmithingTransformRecipe {
	public final Ingredient template;
	public final Ingredient base;
	public final Ingredient addition;
	public final ItemStack result;

	public CopyNBTSmithingRecipe(ResourceLocation id, Ingredient template, Ingredient base, Ingredient addition, ItemStack result) {
		super(id, template, base, addition, result);
		this.template = template;
		this.base = base;
		this.addition = addition;
		this.result = result;
	}

	@Override
	public ItemStack assemble(Container container, RegistryAccess registryAccess) {
		ItemStack baseStack = container.getItem(1);
		ItemStack additionStack = container.getItem(2);
		ItemStack output = this.result.copy();
		if (baseStack.hasTag()) {
			output.setTag(baseStack.getTag().copy());
		}
		int color = -1;
		CompoundTag additionTag = additionStack.getTag();
		if (additionTag != null) {
			if (additionTag.contains("color", net.minecraft.nbt.Tag.TAG_INT)) {
				color = additionTag.getInt("color");
			} else if (additionTag.contains("display", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
				CompoundTag display = additionTag.getCompound("display");
				if (display.contains("color", net.minecraft.nbt.Tag.TAG_INT)) {
					color = display.getInt("color");
				}
			}
		}
		if (color != -1) {
			CompoundTag outputTag = output.getOrCreateTag();
			CompoundTag display = outputTag.getCompound("display");
			display.putInt("color", color);
			outputTag.put("display", display);
		}
		if (output.hasTag()) {
			ModUtils.reduceImbuementForOutput(output, output.getCount());
		}
		return output;
	}

	@Override
	public ItemStack getToastSymbol() {
		return new ItemStack(Blocks.SMITHING_TABLE);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModRecipes.COPY_NBT_SMITHING_SERIALIZER.get();
	}
}
