package net.jaams.shinerite.recipe;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potion;
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

public class WaterBottleRecipe extends CustomRecipe {
	public WaterBottleRecipe(ResourceLocation id, CraftingBookCategory category) {
		super(id, category);
	}

	@Override
	public boolean matches(CraftingContainer container, Level level) {
		if (!RecipesConfig.WATER_BOTTLE_RECIPE.get()) {
			return false;
		}
		if (!MechanicsConfig.WASHING.get()) {
			return false;
		}
		boolean bulkEnabled = RecipesConfig.WATER_BOTTLE_RECIPE_3X3_MODE.get();
		boolean freeEnabled = RecipesConfig.WATER_BOTTLE_RECIPE_FREE_MODE.get();
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
		if (!isValidWaterBottle(center)) {
			return false;
		}
		Item referenceItem = null;
		for (int i = 0; i < 9; i++) {
			if (i == 4)
				continue;
			ItemStack stack = container.getItem(i);
			if (stack.isEmpty() || !ModUtils.isImbueableItem(stack)) {
				return false;
			}
			if (!isValidCleanableStack(stack)) {
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
		ItemStack targetItem = ItemStack.EMPTY;
		int bottleCount = 0;
		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (stack.isEmpty())
				continue;
			if (ModUtils.isImbueableItem(stack)) {
				if (!targetItem.isEmpty())
					return false;
				if (!isValidCleanableStack(stack))
					return false;
				targetItem = stack;
			} else if (isValidWaterBottle(stack)) {
				bottleCount += stack.getCount();
			} else {
				return false;
			}
		}
		return !targetItem.isEmpty() && bottleCount >= 1;
	}

	private boolean isValidWaterBottle(ItemStack stack) {
		if (!stack.is(Items.POTION))
			return false;
		Potion potion = PotionUtils.getPotion(stack);
		return potion == Potions.WATER || potion == Potions.AWKWARD || potion == Potions.THICK || potion.getEffects().isEmpty();
	}

	private boolean isValidCleanableStack(ItemStack stack) {
		if (ModUtils.isWaxed(stack)) {
			return false;
		}
		boolean hasImbuement = ModUtils.isImbued(stack);
		int currentColor = ModUtils.getCurrentColor(stack);
		return hasImbuement || currentColor != -1;
	}

	@Override
	public ItemStack assemble(CraftingContainer container, RegistryAccess access) {
		boolean bulkEnabled = RecipesConfig.WATER_BOTTLE_RECIPE_3X3_MODE.get();
		boolean freeEnabled = RecipesConfig.WATER_BOTTLE_RECIPE_FREE_MODE.get();
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
		cleanItem(base);
		base.setCount(8);
		return base;
	}

	private ItemStack assembleFreeMode(CraftingContainer container) {
		ItemStack target = ItemStack.EMPTY;
		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (ModUtils.isImbueableItem(stack)) {
				target = stack.copy();
				break;
			}
		}
		cleanItem(target);
		target.setCount(1);
		return target;
	}

	private void cleanItem(ItemStack item) {
		if (ModUtils.isImbued(item)) {
			ModUtils.removeImbuement(item);
		}
		if (ModUtils.getCurrentColor(item) != -1) {
			ModUtils.removeColor(item);
		}
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
		NonNullList<ItemStack> remaining = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
		if (RecipesConfig.WATER_BOTTLE_RECIPE_FREE_MODE.get() && matchesFreeMode(container)) {
			int totalBottleCount = 0;
			List<Integer> bottlePositions = new ArrayList<>();
			for (int i = 0; i < container.getContainerSize(); i++) {
				ItemStack stack = container.getItem(i);
				if (isValidWaterBottle(stack)) {
					totalBottleCount += stack.getCount();
					if (stack.getCount() == 1) {
						bottlePositions.add(i);
					}
				}
			}
			int bottlesToReturn = totalBottleCount - 1;
			for (int i = 0; i < bottlesToReturn && !bottlePositions.isEmpty(); i++) {
				int pos = bottlePositions.remove(0);
				remaining.set(pos, new ItemStack(Items.GLASS_BOTTLE));
			}
		} else if (RecipesConfig.WATER_BOTTLE_RECIPE_3X3_MODE.get() && matchesBulkMode(container)) {
			remaining.set(4, new ItemStack(Items.GLASS_BOTTLE));
		}
		return remaining;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModRecipes.WATER_BOTTLE_RECIPE_SERIALIZER.get();
	}
}
