package net.jaams.shinerite.recipe;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.NonNullList;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.init.ModRecipes;
import net.jaams.shinerite.item.ShinyPaintBrushItem;
import net.jaams.shinerite.configuration.common.RecipesConfig;
import net.jaams.shinerite.configuration.common.MechanicsConfig;

import java.util.List;
import java.util.ArrayList;

public class ShinyPaintBrushRecipe extends CustomRecipe {
	public ShinyPaintBrushRecipe(ResourceLocation id, CraftingBookCategory category) {
		super(id, category);
	}

	@Override
	public boolean matches(CraftingContainer container, Level level) {
		if (!RecipesConfig.SHINY_PAINT_BRUSH_RECIPE.get()) {
			return false;
		}
		if (!MechanicsConfig.DYEING.get()) {
			return false;
		}
		boolean bulkEnabled = RecipesConfig.SHINY_PAINT_BRUSH_RECIPE_3X3_MODE.get();
		boolean freeEnabled = RecipesConfig.SHINY_PAINT_BRUSH_RECIPE_FREE_MODE.get();
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
		if (!(center.getItem() instanceof ShinyPaintBrushItem)) {
			return false;
		}
		if (ModUtils.getCurrentColor(center) == -1) {
			return false;
		}
		int brushColor = ModUtils.getCurrentColor(center);
		Item referenceItem = null;
		for (int i = 0; i < 9; i++) {
			if (i == 4)
				continue;
			ItemStack stack = container.getItem(i);
			if (stack.isEmpty() || !ModUtils.isDyeableItem(stack) || stack.getItem() instanceof ShinyPaintBrushItem) {
				return false;
			}
			if (!isValidDyeableStack(stack)) {
				return false;
			}
			int stackColor = ModUtils.getCurrentColor(stack);
			if (stackColor != -1 && stackColor == brushColor) {
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
		ItemStack targetDyeable = ItemStack.EMPTY;
		int brushCount = 0;
		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (stack.isEmpty())
				continue;
			if (ModUtils.isDyeableItem(stack) && !(stack.getItem() instanceof ShinyPaintBrushItem)) {
				if (!targetDyeable.isEmpty())
					return false;
				if (!isValidDyeableStack(stack))
					return false;
				targetDyeable = stack;
			} else if (stack.getItem() instanceof ShinyPaintBrushItem) {
				if (ModUtils.getCurrentColor(stack) == -1)
					return false;
				brushCount += stack.getCount();
			} else {
				return false;
			}
		}
		if (targetDyeable.isEmpty() || brushCount == 0)
			return false;
		int targetColor = ModUtils.getCurrentColor(targetDyeable);
		if (targetColor != -1) {
			for (int i = 0; i < container.getContainerSize(); i++) {
				ItemStack stack = container.getItem(i);
				if (stack.getItem() instanceof ShinyPaintBrushItem) {
					if (targetColor == ModUtils.getCurrentColor(stack)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private boolean isValidDyeableStack(ItemStack stack) {
		if (ModUtils.isWaxed(stack) || ModUtils.isImbued(stack)) {
			return false;
		}
		return true;
	}

	@Override
	public ItemStack assemble(CraftingContainer container, RegistryAccess access) {
		boolean bulkEnabled = RecipesConfig.SHINY_PAINT_BRUSH_RECIPE_3X3_MODE.get();
		boolean freeEnabled = RecipesConfig.SHINY_PAINT_BRUSH_RECIPE_FREE_MODE.get();
		if (bulkEnabled && container.getWidth() == 3 && container.getHeight() == 3 && matchesBulkMode(container)) {
			return assembleBulkMode(container);
		}
		if (freeEnabled && matchesFreeMode(container)) {
			return assembleFreeMode(container);
		}
		return ItemStack.EMPTY;
	}

	private ItemStack assembleBulkMode(CraftingContainer container) {
		ItemStack brush = container.getItem(4);
		int brushColor = ModUtils.getCurrentColor(brush);
		ItemStack dyeableBase = ItemStack.EMPTY;
		for (int i = 0; i < 9; i++) {
			if (i == 4)
				continue;
			ItemStack stack = container.getItem(i);
			if (!stack.isEmpty()) {
				dyeableBase = stack.copy();
				break;
			}
		}
		applyBrushColor(dyeableBase, brushColor);
		dyeableBase.setCount(8);
		return dyeableBase;
	}

	private ItemStack assembleFreeMode(CraftingContainer container) {
		ItemStack dyeableTarget = ItemStack.EMPTY;
		List<Integer> brushPositions = new ArrayList<>();
		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (ModUtils.isDyeableItem(stack) && !(stack.getItem() instanceof ShinyPaintBrushItem)) {
				dyeableTarget = stack.copy();
			} else if (stack.getItem() instanceof ShinyPaintBrushItem) {
				brushPositions.add(i);
			}
		}
		ItemStack usedBrush = container.getItem(brushPositions.get(0));
		int brushColor = ModUtils.getCurrentColor(usedBrush);
		applyBrushColor(dyeableTarget, brushColor);
		dyeableTarget.setCount(1);
		return dyeableTarget;
	}

	private void applyBrushColor(ItemStack target, int brushColor) {
		int currentColor = ModUtils.getCurrentColor(target);
		if (currentColor == -1) {
			ModUtils.applyDyeColor(target, brushColor);
		} else {
			int[] combined = new int[3];
			int count = 1;
			combined[0] += (currentColor >> 16) & 255;
			combined[1] += (currentColor >> 8) & 255;
			combined[2] += currentColor & 255;
			combined[0] += (brushColor >> 16) & 255;
			combined[1] += (brushColor >> 8) & 255;
			combined[2] += brushColor & 255;
			count++;
			int finalR = combined[0] / count;
			int finalG = combined[1] / count;
			int finalB = combined[2] / count;
			int finalColor = (finalR << 16) | (finalG << 8) | finalB;
			ModUtils.applyDyeColor(target, finalColor);
		}
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
		NonNullList<ItemStack> remaining = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
		boolean bulkEnabled = RecipesConfig.SHINY_PAINT_BRUSH_RECIPE_3X3_MODE.get();
		boolean freeEnabled = RecipesConfig.SHINY_PAINT_BRUSH_RECIPE_FREE_MODE.get();
		int damageFree = RecipesConfig.SHINY_PAINT_BRUSH_RECIPE_DAMAGE_PER_USE_FREE.get();
		int damageBulk = RecipesConfig.SHINY_PAINT_BRUSH_RECIPE_DAMAGE_PER_USE_BULK.get();
		if (bulkEnabled && container.getWidth() == 3 && container.getHeight() == 3 && matchesBulkMode(container)) {
			ItemStack center = container.getItem(4).copy();
			int newDamage = Math.min(center.getDamageValue() + damageBulk, center.getMaxDamage() - 1);
			center.setDamageValue(newDamage);
			remaining.set(4, center);
			return remaining;
		}
		if (freeEnabled && matchesFreeMode(container)) {
			List<Integer> brushPositions = new ArrayList<>();
			for (int i = 0; i < container.getContainerSize(); i++) {
				ItemStack stack = container.getItem(i);
				if (stack.getItem() instanceof ShinyPaintBrushItem && stack.getCount() == 1) {
					brushPositions.add(i);
				}
			}
			if (!brushPositions.isEmpty()) {
				int pos = brushPositions.remove(0);
				ItemStack usedBrush = container.getItem(pos).copy();
				int newDamage = Math.min(usedBrush.getDamageValue() + damageFree, usedBrush.getMaxDamage() - 1);
				usedBrush.setDamageValue(newDamage);
				remaining.set(pos, usedBrush);
			}
			for (int pos : brushPositions) {
				remaining.set(pos, container.getItem(pos).copy());
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
		return ModRecipes.SHINY_PAINT_BRUSH_SERIALIZER.get();
	}
}
