package net.jaams.shinerite.recipe;

import org.checkerframework.checker.units.qual.g;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.RegistryAccess;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.init.ModRecipes;
import net.jaams.shinerite.configuration.common.RecipesConfig;
import net.jaams.shinerite.configuration.common.MechanicsConfig;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

public class DyeItemRecipe extends CustomRecipe {
	public DyeItemRecipe(ResourceLocation id, CraftingBookCategory category) {
		super(id, category);
	}

	@Override
	public boolean matches(CraftingContainer container, Level level) {
		if (!RecipesConfig.DYE_ITEM_RECIPE.get()) {
			return false;
		}
		if (!MechanicsConfig.DYEING.get()) {
			return false;
		}
		boolean bulkEnabled = RecipesConfig.DYE_ITEM_RECIPE_3X3_MODE.get();
		boolean freeEnabled = RecipesConfig.DYE_ITEM_RECIPE_FREE_MODE.get();
		if (!bulkEnabled && !freeEnabled)
			return false;
		if (bulkEnabled && container.getWidth() == 3 && container.getHeight() == 3) {
			if (matchesBulkMode(container)) {
				return true;
			}
		}
		return freeEnabled && matchesFreeMode(container);
	}

	private boolean matchesBulkMode(CraftingContainer container) {
		ItemStack center = container.getItem(4);
		if (!(center.getItem() instanceof DyeItem))
			return false;
		Item referenceItem = null;
		for (int i = 0; i < 9; i++) {
			if (i == 4)
				continue;
			ItemStack stack = container.getItem(i);
			if (stack.isEmpty() || !ModUtils.isDyeableItem(stack))
				return false;
			if (!isValidShineriteStack(stack))
				return false;
			Item item = stack.getItem();
			if (referenceItem == null) {
				referenceItem = item;
				if (stack.getMaxStackSize() == 1)
					return false;
			} else if (item != referenceItem) {
				return false;
			}
		}
		return true;
	}

	private boolean matchesFreeMode(CraftingContainer container) {
		ItemStack dyeableItem = ItemStack.EMPTY;
		int totalDyeCount = 0;
		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (stack.isEmpty())
				continue;
			if (ModUtils.isDyeableItem(stack)) {
				if (!dyeableItem.isEmpty())
					return false;
				if (!isValidShineriteStack(stack))
					return false;
				dyeableItem = stack;
			} else if (stack.getItem() instanceof DyeItem) {
				totalDyeCount += stack.getCount();
			} else {
				return false;
			}
		}
		if (dyeableItem.isEmpty() || totalDyeCount == 0)
			return false;
		int maxDyes = RecipesConfig.DYE_ITEM_RECIPE_MAX_DYES.get();
		return maxDyes <= 0 || totalDyeCount <= maxDyes;
	}

	@Override
	public ItemStack assemble(CraftingContainer container, RegistryAccess access) {
		boolean bulkEnabled = RecipesConfig.DYE_ITEM_RECIPE_3X3_MODE.get();
		boolean freeEnabled = RecipesConfig.DYE_ITEM_RECIPE_FREE_MODE.get();
		if (bulkEnabled && container.getWidth() == 3 && container.getHeight() == 3 && matchesBulkMode(container)) {
			return assembleBulkMode(container);
		}
		if (freeEnabled && matchesFreeMode(container)) {
			return assembleFreeMode(container);
		}
		return ItemStack.EMPTY;
	}

	private ItemStack assembleBulkMode(CraftingContainer container) {
		DyeItem centerDye = (DyeItem) container.getItem(4).getItem();
		ItemStack base = ItemStack.EMPTY;
		for (int i = 0; i < 9; i++) {
			if (i == 4)
				continue;
			ItemStack stack = container.getItem(i);
			if (!stack.isEmpty()) {
				base = stack.copy();
				break;
			}
		}
		return dyeItem(base, List.of(centerDye), 8);
	}

	private ItemStack assembleFreeMode(CraftingContainer container) {
		ItemStack dyeableBase = ItemStack.EMPTY;
		List<DyeItem> dyes = new ArrayList<>();
		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (stack.isEmpty())
				continue;
			if (ModUtils.isDyeableItem(stack)) {
				dyeableBase = stack.copy();
			} else if (stack.getItem() instanceof DyeItem dyeItem) {
				dyes.addAll(Collections.nCopies(stack.getCount(), dyeItem));
			}
		}
		return dyeItem(dyeableBase, dyes, 1);
	}

	private boolean isValidShineriteStack(ItemStack stack) {
		if (ModUtils.isWaxed(stack) || ModUtils.isImbued(stack)) {
			return false;
		}
		int currentColor = ModUtils.getCurrentColor(stack);
		boolean hasColor = currentColor != -1;
		boolean allowOverDye = RecipesConfig.DYE_ITEM_RECIPE_ALLOW_OVER_DYEING.get();
		return !hasColor || allowOverDye;
	}

	private ItemStack dyeItem(ItemStack stack, List<DyeItem> dyes, int outputCount) {
		ItemStack result = stack.copy();
		result.setCount(outputCount);
		if (dyes.size() == 1 && outputCount == 8) {
			float[] colorComponents = dyes.get(0).getDyeColor().getTextureDiffuseColors();
			int r = (int) (colorComponents[0] * 255f);
			int g = (int) (colorComponents[1] * 255f);
			int b = (int) (colorComponents[2] * 255f);
			int finalColor = (r << 16) + (g << 8) + b;
			ModUtils.applyDyeColor(result, finalColor);
			return result;
		}
		int currentColor = ModUtils.getCurrentColor(stack);
		int[] combinedColors = new int[3];
		int colorCount = 0;
		if (currentColor != -1) {
			combinedColors[0] += (currentColor >> 16) & 255;
			combinedColors[1] += (currentColor >> 8) & 255;
			combinedColors[2] += currentColor & 255;
			colorCount++;
		}
		for (DyeItem dye : dyes) {
			float[] colorComponents = dye.getDyeColor().getTextureDiffuseColors();
			combinedColors[0] += (int) (colorComponents[0] * 255f);
			combinedColors[1] += (int) (colorComponents[1] * 255f);
			combinedColors[2] += (int) (colorComponents[2] * 255f);
			colorCount++;
		}
		int finalR = combinedColors[0] / colorCount;
		int finalG = combinedColors[1] / colorCount;
		int finalB = combinedColors[2] / colorCount;
		int finalColor = (finalR << 16) + (finalG << 8) + finalB;
		ModUtils.applyDyeColor(result, finalColor);
		return result;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModRecipes.DYEABLE_ITEM_SERIALIZER.get();
	}
}
