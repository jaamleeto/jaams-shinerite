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
import net.jaams.shinerite.item.ShinyScraperItem;
import net.jaams.shinerite.configuration.common.RecipesConfig;

import java.util.List;
import java.util.ArrayList;

public class ShinyScraperRecipe extends CustomRecipe {
	public ShinyScraperRecipe(ResourceLocation id, CraftingBookCategory category) {
		super(id, category);
	}

	@Override
	public boolean matches(CraftingContainer container, Level level) {
		if (!RecipesConfig.SHINY_SCRAPER_RECIPE.get()) {
			return false;
		}
		boolean bulkEnabled = RecipesConfig.SHINY_SCRAPER_RECIPE_3X3_MODE.get();
		boolean freeEnabled = RecipesConfig.SHINY_SCRAPER_RECIPE_FREE_MODE.get();
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
		if (!(center.getItem() instanceof ShinyScraperItem)) {
			return false;
		}
		Item referenceItem = null;
		for (int i = 0; i < 9; i++) {
			if (i == 4)
				continue;
			ItemStack stack = container.getItem(i);
			if (stack.isEmpty()) {
				return false;
			}
			if (!isValidCleanableItem(stack)) {
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
		ItemStack targetCleanable = ItemStack.EMPTY;
		int scraperCount = 0;
		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (stack.isEmpty())
				continue;
			if (isValidCleanableItem(stack)) {
				if (!targetCleanable.isEmpty())
					return false;
				targetCleanable = stack;
			} else if (stack.getItem() instanceof ShinyScraperItem) {
				scraperCount += stack.getCount();
			} else {
				return false;
			}
		}
		return !targetCleanable.isEmpty() && scraperCount >= 1;
	}

	private boolean isValidCleanableItem(ItemStack stack) {
		if (stack.getItem() instanceof ShinyScraperItem) {
			return false;
		}
		if (!ModUtils.isCoatableItem(stack) && !ModUtils.isImbueableItem(stack)) {
			return false;
		}
		if (ModUtils.isCoated(stack)) {
			return true;
		}
		if (ModUtils.isImbued(stack)) {
			return true;
		}
		return ModUtils.getCurrentColor(stack) != -1;
	}

	@Override
	public ItemStack assemble(CraftingContainer container, RegistryAccess access) {
		boolean bulkEnabled = RecipesConfig.SHINY_SCRAPER_RECIPE_3X3_MODE.get();
		boolean freeEnabled = RecipesConfig.SHINY_SCRAPER_RECIPE_FREE_MODE.get();
		if (bulkEnabled && container.getWidth() == 3 && container.getHeight() == 3 && matchesBulkMode(container)) {
			return assembleBulkMode(container);
		}
		if (freeEnabled && matchesFreeMode(container)) {
			return assembleFreeMode(container);
		}
		return ItemStack.EMPTY;
	}

	private ItemStack assembleBulkMode(CraftingContainer container) {
		ItemStack cleanableBase = ItemStack.EMPTY;
		for (int i = 0; i < 9; i++) {
			if (i == 4)
				continue;
			ItemStack stack = container.getItem(i);
			if (!stack.isEmpty()) {
				cleanableBase = stack.copy();
				break;
			}
		}
		cleanItem(cleanableBase);
		cleanableBase.setCount(8);
		return cleanableBase;
	}

	private ItemStack assembleFreeMode(CraftingContainer container) {
		ItemStack cleanableTarget = ItemStack.EMPTY;
		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (isValidCleanableItem(stack)) {
				cleanableTarget = stack.copy();
				break;
			}
		}
		cleanItem(cleanableTarget);
		cleanableTarget.setCount(1);
		return cleanableTarget;
	}

	private void cleanItem(ItemStack item) {
		if (ModUtils.isCoated(item)) {
			item.removeTagKey("Coated");
		} else if (ModUtils.isImbued(item)) {
			ModUtils.removeImbuement(item);
			ModUtils.removeColor(item);
		} else if (ModUtils.getCurrentColor(item) != -1) {
			ModUtils.removeColor(item);
		}
		if (item.hasTag() && item.getTag().isEmpty()) {
			item.setTag(null);
		}
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
		NonNullList<ItemStack> remaining = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
		boolean bulkEnabled = RecipesConfig.SHINY_SCRAPER_RECIPE_3X3_MODE.get();
		boolean freeEnabled = RecipesConfig.SHINY_SCRAPER_RECIPE_FREE_MODE.get();
		int damageFree = RecipesConfig.SHINY_SCRAPER_RECIPE_DAMAGE_PER_USE_FREE.get();
		int damageBulk = RecipesConfig.SHINY_SCRAPER_RECIPE_DAMAGE_PER_USE_BULK.get();
		if (bulkEnabled && container.getWidth() == 3 && container.getHeight() == 3 && matchesBulkMode(container)) {
			ItemStack center = container.getItem(4).copy();
			int newDamage = Math.min(center.getDamageValue() + damageBulk, center.getMaxDamage() - 1);
			center.setDamageValue(newDamage);
			remaining.set(4, center);
			return remaining;
		}
		if (freeEnabled && matchesFreeMode(container)) {
			List<Integer> scraperPositions = new ArrayList<>();
			for (int i = 0; i < container.getContainerSize(); i++) {
				ItemStack stack = container.getItem(i);
				if (stack.getItem() instanceof ShinyScraperItem && stack.getCount() == 1) {
					scraperPositions.add(i);
				}
			}
			if (!scraperPositions.isEmpty()) {
				int pos = scraperPositions.remove(0);
				ItemStack usedScraper = container.getItem(pos).copy();
				int newDamage = Math.min(usedScraper.getDamageValue() + damageFree, usedScraper.getMaxDamage() - 1);
				usedScraper.setDamageValue(newDamage);
				remaining.set(pos, usedScraper);
			}
			for (int pos : scraperPositions) {
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
		return ModRecipes.SHINY_SCRAPER_SERIALIZER.get();
	}
}
