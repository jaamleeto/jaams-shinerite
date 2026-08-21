package net.jaams.shinerite.recipe;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.RegistryAccess;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.util.ModTags;
import net.jaams.shinerite.init.ModRecipes;
import net.jaams.shinerite.configuration.common.RecipesConfig;

public class ShineriteStewRecipe extends CustomRecipe {
	public ShineriteStewRecipe(ResourceLocation id, CraftingBookCategory category) {
		super(id, category);
	}

	@Override
	public boolean matches(CraftingContainer container, Level level) {
		if (!RecipesConfig.SHINERITIA_STEW_RECIPE.get()) {
			return false;
		}
		boolean hasBrownMushroom = false;
		boolean hasRedMushroom = false;
		boolean hasShineriteFlower = false;
		boolean hasBowl = false;
		for (int i = 0; i < container.getContainerSize(); ++i) {
			ItemStack stack = container.getItem(i);
			if (!stack.isEmpty()) {
				if (stack.is(Blocks.BROWN_MUSHROOM.asItem())) {
					if (hasBrownMushroom)
						return false;
					hasBrownMushroom = true;
				} else if (stack.is(Blocks.RED_MUSHROOM.asItem())) {
					if (hasRedMushroom)
						return false;
					hasRedMushroom = true;
				} else if (stack.is(ModTags.SHINERITE_FLOWERS)) {
					if (hasShineriteFlower)
						return false;
					hasShineriteFlower = true;
				} else if (stack.is(Items.BOWL)) {
					if (hasBowl)
						return false;
					hasBowl = true;
				} else {
					return false;
				}
			}
		}
		return hasBrownMushroom && hasRedMushroom && hasShineriteFlower && hasBowl;
	}

	@Override
	public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
		ItemStack result = new ItemStack(Items.SUSPICIOUS_STEW);
		for (int i = 0; i < container.getContainerSize(); ++i) {
			ItemStack stack = container.getItem(i);
			if (!stack.isEmpty() && stack.is(ModTags.SHINERITE_FLOWERS)) {
				int color = ModUtils.getCurrentColor(stack);
				SuspiciousStewItem.saveMobEffect(result, MobEffects.GLOWING, 100);
				if (color != -1) {
					result.getOrCreateTag().putInt("FlowerColor", color);
				}
				break;
			}
		}
		return result;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width >= 2 && height >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModRecipes.SHINERITE_STEW_SERIALIZER.get();
	}
}
