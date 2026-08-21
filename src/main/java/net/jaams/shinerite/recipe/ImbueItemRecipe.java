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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.RegistryAccess;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.util.ModEnums;
import net.jaams.shinerite.init.ModRecipes;
import net.jaams.shinerite.handler.mechanic.InteractionHandler;
import net.jaams.shinerite.configuration.common.RecipesConfig;
import net.jaams.shinerite.configuration.common.MechanicsConfig;

import java.util.List;

public class ImbueItemRecipe extends CustomRecipe {
	public ImbueItemRecipe(ResourceLocation id, CraftingBookCategory category) {
		super(id, category);
	}

	@Override
	public boolean matches(CraftingContainer container, Level level) {
		if (!RecipesConfig.IMBUE_ITEM_RECIPE.get()) {
			return false;
		}
		if (!MechanicsConfig.IMBUING.get()) {
			return false;
		}
		boolean bulkEnabled = RecipesConfig.IMBUE_ITEM_RECIPE_3X3_MODE.get();
		boolean freeEnabled = RecipesConfig.IMBUE_ITEM_RECIPE_FREE_MODE.get();
		ModEnums.ImbuePotionType potionType = RecipesConfig.IMBUE_ITEM_RECIPE_POTION_TYPE.get();
		if (!bulkEnabled && !freeEnabled) {
			return false;
		}
		if (bulkEnabled && container.getWidth() == 3 && container.getHeight() == 3) {
			if (matchesBulkMode(container, potionType)) {
				return true;
			}
		}
		return freeEnabled && matchesFreeMode(container, potionType);
	}

	private boolean matchesBulkMode(CraftingContainer container, ModEnums.ImbuePotionType potionType) {
		ItemStack center = container.getItem(4);
		if (!isValidPotion(center, potionType)) {
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
			if (!isValidImbueableStack(stack)) {
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

	private boolean matchesFreeMode(CraftingContainer container, ModEnums.ImbuePotionType potionType) {
		ItemStack imbueableItem = ItemStack.EMPTY;
		int potionCount = 0;
		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (stack.isEmpty())
				continue;
			if (ModUtils.isImbueableItem(stack)) {
				if (!imbueableItem.isEmpty())
					return false;
				if (!isValidImbueableStack(stack))
					return false;
				imbueableItem = stack;
			} else if (isValidPotion(stack, potionType)) {
				potionCount++;
				if (potionCount > 1)
					return false;
			} else {
				return false;
			}
		}
		return !imbueableItem.isEmpty() && potionCount == 1;
	}

	private boolean isValidPotion(ItemStack stack, ModEnums.ImbuePotionType allowedType) {
		Potion potion = PotionUtils.getPotion(stack);
		if (potion == Potions.WATER || potion == Potions.EMPTY || potion.getEffects().isEmpty()) {
			return false;
		}
		return switch (allowedType) {
			case LINGERING -> stack.is(Items.LINGERING_POTION);
			case NORMAL -> stack.is(Items.POTION);
		};
	}

	private boolean isValidImbueableStack(ItemStack stack) {
		if (ModUtils.isWaxed(stack) || ModUtils.isImbued(stack)) {
			return false;
		}
		int currentColor = ModUtils.getCurrentColor(stack);
		return currentColor == -1;
	}

	@Override
	public ItemStack assemble(CraftingContainer container, RegistryAccess access) {
		boolean bulkEnabled = RecipesConfig.IMBUE_ITEM_RECIPE_3X3_MODE.get();
		boolean freeEnabled = RecipesConfig.IMBUE_ITEM_RECIPE_FREE_MODE.get();
		ModEnums.ImbuePotionType potionType = RecipesConfig.IMBUE_ITEM_RECIPE_POTION_TYPE.get();
		if (bulkEnabled && container.getWidth() == 3 && container.getHeight() == 3 && matchesBulkMode(container, potionType)) {
			return assembleBulkMode(container);
		}
		if (freeEnabled && matchesFreeMode(container, potionType)) {
			return assembleFreeMode(container);
		}
		return ItemStack.EMPTY;
	}

	private ItemStack assembleBulkMode(CraftingContainer container) {
		ItemStack potion = container.getItem(4);
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
		return applyImbueEffects(base, potion, 8);
	}

	private ItemStack assembleFreeMode(CraftingContainer container) {
		ItemStack imbueableItem = ItemStack.EMPTY;
		ItemStack potion = ItemStack.EMPTY;
		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (stack.isEmpty())
				continue;
			if (ModUtils.isImbueableItem(stack)) {
				imbueableItem = stack.copy();
			} else if (isValidPotion(stack, RecipesConfig.IMBUE_ITEM_RECIPE_POTION_TYPE.get())) {
				potion = stack;
			}
		}
		return applyImbueEffects(imbueableItem, potion, 1);
	}

	private ItemStack applyImbueEffects(ItemStack item, ItemStack potion, int outputCount) {
		Potion potionType = PotionUtils.getPotion(potion);
		if (potionType == Potions.EMPTY || potionType.getEffects().isEmpty()) {
			return ItemStack.EMPTY;
		}
		List<MobEffectInstance> originalEffects = PotionUtils.getMobEffects(potion);
		int potionColor = PotionUtils.getColor(potion);
		int uses;
		if (item.getMaxStackSize() > 1 || item.isEdible()) {
			uses = 1;
		} else {
			MobEffectInstance main = originalEffects.get(0);
			uses = InteractionHandler.BASE_IMBUED_USES + (main.getAmplifier() * 2);
		}
		ItemStack result = item.copy();
		result.setCount(outputCount);
		CompoundTag tag = result.getOrCreateTag();
		ListTag list = new ListTag();
		for (MobEffectInstance original : originalEffects) {
			int reducedDuration = Math.max(1, original.getDuration() / 2);
			MobEffectInstance reduced = new MobEffectInstance(original.getEffect(), reducedDuration, original.getAmplifier(), original.isAmbient(), original.isVisible(), original.showIcon());
			list.add(reduced.save(new CompoundTag()));
		}
		tag.put("ImbuedEffects", list);
		tag.putInt("ImbuedUses", uses);
		tag.putInt("MaxImbuedUses", uses);
		tag.putInt("ImbuedColor", potionColor);
		ModUtils.applyDyeColor(result, potionColor);
		return result;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModRecipes.IMBUE_ITEM_SERIALIZER.get();
	}
}
