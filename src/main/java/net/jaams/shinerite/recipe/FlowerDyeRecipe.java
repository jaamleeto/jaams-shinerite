package net.jaams.shinerite.recipe;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.init.ModItems;
import net.jaams.shinerite.init.ModRecipes;
import net.jaams.shinerite.configuration.common.RecipesConfig;

public class FlowerDyeRecipe extends CustomRecipe {
	public FlowerDyeRecipe(ResourceLocation id, CraftingBookCategory category) {
		super(id, category);
	}

	@Override
	public boolean matches(CraftingContainer container, Level level) {
		if (!RecipesConfig.FLOWER_DYE_RECIPE.get()) {
			return false;
		}
		ItemStack flower = ItemStack.EMPTY;
		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (stack.isEmpty()) {
				continue;
			}
			if (!flower.isEmpty()) {
				return false;
			}
			if (!isDyedShineritiaFlower(stack)) {
				return false;
			}
			flower = stack;
		}
		return !flower.isEmpty();
	}

	private boolean isDyedShineritiaFlower(ItemStack stack) {
		if (stack.getItem() != ModItems.SHINERITIA.get()) {
			return false;
		}
		if (ModUtils.isImbued(stack) || ModUtils.isCoated(stack)) {
			return false;
		}
		return true;
	}

	@Override
	public ItemStack assemble(CraftingContainer container, RegistryAccess access) {
		ItemStack flower = ItemStack.EMPTY;
		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (!stack.isEmpty()) {
				flower = stack;
				break;
			}
		}
		if (flower.isEmpty()) {
			return ItemStack.EMPTY;
		}
		int color = ModUtils.getCurrentColor(flower);
		DyeColor dyeColor;
		if (color == -1) {
			dyeColor = DyeColor.WHITE;
		} else {
			dyeColor = findClosestDyeColor(color);
		}
		if (dyeColor == null) {
			return ItemStack.EMPTY;
		}
		Item dyeItem = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("minecraft", dyeColor.getSerializedName() + "_dye"));
		if (dyeItem == Items.AIR) {
			return ItemStack.EMPTY;
		}
		return new ItemStack(dyeItem, RecipesConfig.FLOWER_DYE_RECIPE_OUTPUT_COUNT.get());
	}

	private static DyeColor findClosestDyeColor(int color) {
		if (color == -1) {
			return null;
		}
		int red = (color >> 16) & 0xFF;
		int green = (color >> 8) & 0xFF;
		int blue = color & 0xFF;
		DyeColor best = null;
		long bestDistance = Long.MAX_VALUE;
		for (DyeColor candidate : DyeColor.values()) {
			float[] components = candidate.getTextureDiffuseColors();
			int cr = (int) (components[0] * 255f);
			int cg = (int) (components[1] * 255f);
			int cb = (int) (components[2] * 255f);
			long dr = red - cr;
			long dg = green - cg;
			long db = blue - cb;
			long distance = dr * dr + dg * dg + db * db;
			if (distance < bestDistance) {
				bestDistance = distance;
				best = candidate;
			}
		}
		return best;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 1;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModRecipes.FLOWER_DYE_SERIALIZER.get();
	}
}
