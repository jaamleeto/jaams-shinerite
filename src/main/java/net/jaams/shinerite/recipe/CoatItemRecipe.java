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

public class CoatItemRecipe extends CustomRecipe {
	public CoatItemRecipe(ResourceLocation id, CraftingBookCategory category) {
		super(id, category);
	}

	@Override
	public boolean matches(CraftingContainer container, Level level) {
		if (!RecipesConfig.COAT_ITEM_RECIPE.get()) {
			return false;
		}
		if (!MechanicsConfig.COATING.get()) {
			return false;
		}
		boolean bulkEnabled = RecipesConfig.COAT_ITEM_RECIPE_3X3_MODE.get();
		boolean freeEnabled = RecipesConfig.COAT_ITEM_RECIPE_FREE_MODE.get();
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
			if (stack.isEmpty() || !ModUtils.isCoatableItem(stack)) {
				return false;
			}
			if (!isValidCoatableStack(stack)) {
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
		ItemStack coatableItem = ItemStack.EMPTY;
		int honeycombCount = 0;
		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (stack.isEmpty())
				continue;
			if (ModUtils.isCoatableItem(stack)) {
				if (!coatableItem.isEmpty())
					return false;
				if (!isValidCoatableStack(stack))
					return false;
				coatableItem = stack;
			} else if (stack.is(Items.HONEYCOMB)) {
				honeycombCount += stack.getCount();
			} else {
				return false;
			}
		}
		return !coatableItem.isEmpty() && honeycombCount >= 1;
	}

	private boolean isValidCoatableStack(ItemStack stack) {
		return !ModUtils.isCoated(stack);
	}

	@Override
	public ItemStack assemble(CraftingContainer container, RegistryAccess access) {
		boolean bulkEnabled = RecipesConfig.COAT_ITEM_RECIPE_3X3_MODE.get();
		boolean freeEnabled = RecipesConfig.COAT_ITEM_RECIPE_FREE_MODE.get();
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
		base.getOrCreateTag().putBoolean("Coated", true);
		base.setCount(8);
		return base;
	}

	private ItemStack assembleFreeMode(CraftingContainer container) {
		ItemStack coatableItem = ItemStack.EMPTY;
		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (stack.isEmpty())
				continue;
			if (ModUtils.isCoatableItem(stack)) {
				coatableItem = stack.copy();
				break;
			}
		}
		coatableItem.getOrCreateTag().putBoolean("Coated", true);
		coatableItem.setCount(1);
		return coatableItem;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
		NonNullList<ItemStack> remaining = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
		if (RecipesConfig.COAT_ITEM_RECIPE_FREE_MODE.get() && matchesFreeMode(container)) {
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
		return ModRecipes.COAT_ITEM_SERIALIZER.get();
	}
}
