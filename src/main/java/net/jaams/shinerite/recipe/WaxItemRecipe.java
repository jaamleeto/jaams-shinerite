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
import net.jaams.shinerite.configuration.common.RecipesConfig;
import net.jaams.shinerite.configuration.common.MechanicsConfig;

import java.util.List;
import java.util.ArrayList;

public class WaxItemRecipe extends CustomRecipe {
	public WaxItemRecipe(ResourceLocation id, CraftingBookCategory category) {
		super(id, category);
	}

	@Override
	public boolean matches(CraftingContainer container, Level level) {
		if (!RecipesConfig.WAX_ITEM_RECIPE.get()) {
			return false;
		}
		if (!MechanicsConfig.WAXING.get()) {
			return false;
		}
		boolean bulkEnabled = RecipesConfig.WAX_ITEM_RECIPE_3X3_MODE.get();
		boolean freeEnabled = RecipesConfig.WAX_ITEM_RECIPE_FREE_MODE.get();
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
		if (!center.is(Items.HONEYCOMB)) {
			return false;
		}
		Item referenceItem = null;
		for (int i = 0; i < 9; i++) {
			if (i == 4)
				continue;
			ItemStack stack = container.getItem(i);
			if (stack.isEmpty() || !ModUtils.isWaxableItem(stack)) {
				return false;
			}
			if (!isValidWaxableStack(stack)) {
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
		ItemStack waxableItem = ItemStack.EMPTY;
		int honeycombCount = 0;
		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (stack.isEmpty())
				continue;
			if (ModUtils.isWaxableItem(stack)) {
				if (!waxableItem.isEmpty())
					return false;
				if (!isValidWaxableStack(stack))
					return false;
				waxableItem = stack;
			} else if (stack.is(Items.HONEYCOMB)) {
				honeycombCount += stack.getCount();
			} else {
				return false;
			}
		}
		return !waxableItem.isEmpty() && honeycombCount >= 1;
	}

	private boolean isValidWaxableStack(ItemStack stack) {
		return !ModUtils.isWaxed(stack);
	}

	@Override
	public ItemStack assemble(CraftingContainer container, RegistryAccess access) {
		boolean bulkEnabled = RecipesConfig.WAX_ITEM_RECIPE_3X3_MODE.get();
		boolean freeEnabled = RecipesConfig.WAX_ITEM_RECIPE_FREE_MODE.get();
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
		base.getOrCreateTag().putBoolean("Waxed", true);
		base.setCount(8);
		return base;
	}

	private ItemStack assembleFreeMode(CraftingContainer container) {
		ItemStack waxableItem = ItemStack.EMPTY;
		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (stack.isEmpty())
				continue;
			if (ModUtils.isWaxableItem(stack)) {
				waxableItem = stack.copy();
				break;
			}
		}
		waxableItem.getOrCreateTag().putBoolean("Waxed", true);
		waxableItem.setCount(1);
		return waxableItem;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
		NonNullList<ItemStack> remaining = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
		if (RecipesConfig.WAX_ITEM_RECIPE_FREE_MODE.get() && matchesFreeMode(container)) {
			int totalHoneycombCount = 0;
			List<Integer> honeycombPositions = new ArrayList<>();
			for (int i = 0; i < container.getContainerSize(); i++) {
				ItemStack stack = container.getItem(i);
				if (stack.is(Items.HONEYCOMB)) {
					totalHoneycombCount += stack.getCount();
					if (stack.getCount() == 1) {
						honeycombPositions.add(i);
					}
				}
			}
			int honeycombsToReturn = totalHoneycombCount - 1;
			for (int i = 0; i < honeycombsToReturn && !honeycombPositions.isEmpty(); i++) {
				int pos = honeycombPositions.remove(0);
				remaining.set(pos, new ItemStack(Items.HONEYCOMB));
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
		return ModRecipes.WAX_ITEM_SERIALIZER.get();
	}
}
