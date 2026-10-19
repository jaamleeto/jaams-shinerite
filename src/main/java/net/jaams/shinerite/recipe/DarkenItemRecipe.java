package net.jaams.shinerite.recipe;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.NonNullList;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.init.ModRecipes;
import net.jaams.shinerite.handler.mechanic.InteractionHandler;
import net.jaams.shinerite.configuration.common.RecipesConfig;
import net.jaams.shinerite.configuration.common.MechanicsConfig;

import java.util.List;
import java.util.ArrayList;

public class DarkenItemRecipe extends CustomRecipe {
	private static final int DEFAULT_DARKEN_START_COLOR = 0xCDCDCD;

	public DarkenItemRecipe(ResourceLocation id, CraftingBookCategory category) {
		super(id, category);
	}

	@Override
	public boolean matches(CraftingContainer container, Level level) {
		if (!RecipesConfig.DARKEN_ITEM_RECIPE.get()) {
			return false;
		}
		if (!MechanicsConfig.DYEING.get()) {
			return false;
		}
		boolean bulkEnabled = RecipesConfig.DARKEN_ITEM_RECIPE_3X3_MODE.get();
		boolean freeEnabled = RecipesConfig.DARKEN_ITEM_RECIPE_FREE_MODE.get();
		if (!bulkEnabled && !freeEnabled) {
			return false;
		}
		if (bulkEnabled && container.getWidth() == 3 && container.getHeight() == 3) {
			if (matchesBulkMode(container)) {
				return true;
			}
		}
		return freeEnabled && matchesFreeMode(container);
	}

	private boolean matchesBulkMode(CraftingContainer container) {
		ItemStack center = container.getItem(4);
		if (!center.is(Items.INK_SAC)) {
			return false;
		}
		Item referenceItem = null;
		for (int i = 0; i < 9; i++) {
			if (i == 4)
				continue;
			ItemStack stack = container.getItem(i);
			if (stack.isEmpty() || !ModUtils.isDyeableItem(stack)) {
				return false;
			}
			if (!isValidDyeableStackForDarken(stack)) {
				return false;
			}
			Item item = stack.getItem();
			if (referenceItem == null) {
				referenceItem = item;
				if (stack.getMaxStackSize() == 1) {
					return false;
				}
			} else if (item != referenceItem) {
				return false;
			}
		}
		return true;
	}

	private boolean matchesFreeMode(CraftingContainer container) {
		ItemStack dyeableItem = ItemStack.EMPTY;
		int inkCount = 0;
		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (stack.isEmpty())
				continue;
			if (ModUtils.isDyeableItem(stack)) {
				if (!dyeableItem.isEmpty())
					return false;
				if (!isValidDyeableStackForDarken(stack))
					return false;
				dyeableItem = stack;
			} else if (stack.is(Items.INK_SAC)) {
				inkCount += stack.getCount();
			} else {
				return false;
			}
		}
		return !dyeableItem.isEmpty() && inkCount > 0;
	}

	private boolean isValidDyeableStackForDarken(ItemStack stack) {
		if (ModUtils.isCoated(stack) || ModUtils.isImbued(stack)) {
			return false;
		}
		int currentColor = ModUtils.getCurrentColor(stack);
		if (currentColor == -1) {
			return true;
		}
		if (currentColor == 0x000000) {
			return false;
		}
		int darker = InteractionHandler.darkenColor(currentColor);
		return darker != currentColor;
	}

	@Override
	public ItemStack assemble(CraftingContainer container, RegistryAccess access) {
		boolean bulkEnabled = RecipesConfig.DARKEN_ITEM_RECIPE_3X3_MODE.get();
		boolean freeEnabled = RecipesConfig.DARKEN_ITEM_RECIPE_FREE_MODE.get();
		if (bulkEnabled && container.getWidth() == 3 && container.getHeight() == 3 && matchesBulkMode(container)) {
			return assembleBulkMode(container);
		}
		if (freeEnabled && matchesFreeMode(container)) {
			return assembleFreeMode(container);
		}
		return ItemStack.EMPTY;
	}

	private ItemStack assembleBulkMode(CraftingContainer container) {
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
		int currentColor = ModUtils.getCurrentColor(base);
		int colorToDarken = (currentColor == -1) ? DEFAULT_DARKEN_START_COLOR : currentColor;
		int darkened = InteractionHandler.darkenColor(colorToDarken);
		ModUtils.applyDyeColor(base, darkened);
		base.setCount(8);
		return base;
	}

	private ItemStack assembleFreeMode(CraftingContainer container) {
		ItemStack dyeableBase = ItemStack.EMPTY;
		int totalInkCount = 0;
		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (stack.isEmpty())
				continue;
			if (ModUtils.isDyeableItem(stack)) {
				dyeableBase = stack.copy();
			} else if (stack.is(Items.INK_SAC)) {
				totalInkCount += stack.getCount();
			}
		}
		int currentColor = ModUtils.getCurrentColor(dyeableBase);
		int targetColor = (currentColor == -1) ? DEFAULT_DARKEN_START_COLOR : currentColor;
		for (int i = 0; i < totalInkCount; i++) {
			int next = InteractionHandler.darkenColor(targetColor);
			if (next == targetColor)
				break;
			targetColor = next;
		}
		ModUtils.applyDyeColor(dyeableBase, targetColor);
		dyeableBase.setCount(1);
		return dyeableBase;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
		NonNullList<ItemStack> remaining = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
		if (RecipesConfig.DARKEN_ITEM_RECIPE_FREE_MODE.get() && matchesFreeMode(container)) {
			ItemStack dyeable = ItemStack.EMPTY;
			int totalInkCount = 0;
			List<Integer> inkPositions = new ArrayList<>();
			for (int i = 0; i < container.getContainerSize(); i++) {
				ItemStack stack = container.getItem(i);
				if (ModUtils.isDyeableItem(stack)) {
					dyeable = stack;
				} else if (stack.is(Items.INK_SAC)) {
					totalInkCount += stack.getCount();
					if (stack.getCount() == 1) {
						inkPositions.add(i);
					}
				}
			}
			int currentColor = ModUtils.getCurrentColor(dyeable);
			int targetColor = (currentColor == -1) ? DEFAULT_DARKEN_START_COLOR : currentColor;
			int inksUsed = 0;
			while (inksUsed < totalInkCount) {
				int next = InteractionHandler.darkenColor(targetColor);
				if (next == targetColor)
					break;
				targetColor = next;
				inksUsed++;
			}
			int inksToReturn = totalInkCount - inksUsed;
			for (int i = 0; i < inksToReturn && !inkPositions.isEmpty(); i++) {
				int pos = inkPositions.remove(0);
				remaining.set(pos, new ItemStack(Items.INK_SAC));
			}
		}
		return remaining;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModRecipes.DARKEN_ITEM_SERIALIZER.get();
	}
}
