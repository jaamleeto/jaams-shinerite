
package net.jaams.shinerite.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.IEventBus;

import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.resources.ResourceLocation;

import net.jaams.shinerite.recipe.FlowerDyeRecipe;
import net.jaams.shinerite.recipe.CoatItemRecipe;
import net.jaams.shinerite.recipe.WaterBottleRecipe;
import net.jaams.shinerite.recipe.ShinyScraperRecipe;
import net.jaams.shinerite.recipe.ShinyPaintBrushRecipe;
import net.jaams.shinerite.recipe.ShineriteStewRecipeSerializer;
import net.jaams.shinerite.recipe.ShineriteStewRecipe;
import net.jaams.shinerite.recipe.LightenItemRecipe;
import net.jaams.shinerite.recipe.ImbueItemRecipe;
import net.jaams.shinerite.recipe.DyeItemRecipe;
import net.jaams.shinerite.recipe.DarkenItemRecipe;
import net.jaams.shinerite.recipe.CopyNBTStonecutterRecipeSerializer;
import net.jaams.shinerite.recipe.CopyNBTStonecutterRecipe;
import net.jaams.shinerite.recipe.CopyNBTSmithingRecipeSerializer;
import net.jaams.shinerite.recipe.CopyNBTSmithingRecipe;
import net.jaams.shinerite.recipe.ShineriteTrimSmithingRecipeSerializer;
import net.jaams.shinerite.recipe.ShineriteTrimSmithingRecipe;
import net.jaams.shinerite.recipe.CopyNBTSmeltingRecipeSerializer;
import net.jaams.shinerite.recipe.CopyNBTSmeltingRecipe;
import net.jaams.shinerite.recipe.CopyNBTShapelessRecipeSerializer;
import net.jaams.shinerite.recipe.CopyNBTShapelessRecipe;
import net.jaams.shinerite.recipe.CopyNBTShapedRecipeSerializer;
import net.jaams.shinerite.recipe.CopyNBTShapedRecipe;
import net.jaams.shinerite.recipe.CopyNBTBlastingRecipeSerializer;
import net.jaams.shinerite.recipe.CopyNBTBlastingRecipe;

@Mod.EventBusSubscriber
public class ModRecipes {
	private static final String MODID = "jaams_shinerite";
	public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, MODID);
	public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);
	public static final RegistryObject<RecipeType<CopyNBTShapedRecipe>> COPY_NBT_SHAPED_TYPE = RECIPE_TYPES.register("copy_nbt_shaped", () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(MODID, "copy_nbt_shaped")));
	public static final RegistryObject<RecipeSerializer<CopyNBTShapedRecipe>> COPY_NBT_SHAPED_SERIALIZER = RECIPE_SERIALIZERS.register("copy_nbt_shaped", CopyNBTShapedRecipeSerializer::new);
	public static final RegistryObject<RecipeType<CopyNBTShapelessRecipe>> COPY_NBT_SHAPELESS_TYPE = RECIPE_TYPES.register("copy_nbt_shapeless", () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(MODID, "copy_nbt_shapeless")));
	public static final RegistryObject<RecipeSerializer<CopyNBTShapelessRecipe>> COPY_NBT_SHAPELESS_SERIALIZER = RECIPE_SERIALIZERS.register("copy_nbt_shapeless", CopyNBTShapelessRecipeSerializer::new);
	public static final RegistryObject<RecipeType<CopyNBTSmeltingRecipe>> COPY_NBT_SMELTING_TYPE = RECIPE_TYPES.register("copy_nbt_smelting", () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(MODID, "copy_nbt_smelting")));
	public static final RegistryObject<RecipeSerializer<CopyNBTSmeltingRecipe>> COPY_NBT_SMELTING_SERIALIZER = RECIPE_SERIALIZERS.register("copy_nbt_smelting", CopyNBTSmeltingRecipeSerializer::new);
	public static final RegistryObject<RecipeType<CopyNBTBlastingRecipe>> COPY_NBT_BLASTING_TYPE = RECIPE_TYPES.register("copy_nbt_blasting", () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(MODID, "copy_nbt_blasting")));
	public static final RegistryObject<RecipeSerializer<CopyNBTBlastingRecipe>> COPY_NBT_BLASTING_SERIALIZER = RECIPE_SERIALIZERS.register("copy_nbt_blasting", CopyNBTBlastingRecipeSerializer::new);
	public static final RegistryObject<RecipeType<CopyNBTStonecutterRecipe>> COPY_NBT_STONECUTTER_TYPE = RECIPE_TYPES.register("copy_nbt_stonecutting", () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(MODID, "copy_nbt_stonecutting")));
	public static final RegistryObject<RecipeSerializer<CopyNBTStonecutterRecipe>> COPY_NBT_STONECUTTER_SERIALIZER = RECIPE_SERIALIZERS.register("copy_nbt_stonecutting", CopyNBTStonecutterRecipeSerializer::new);
	public static final RegistryObject<SimpleCraftingRecipeSerializer<DyeItemRecipe>> DYEABLE_ITEM_SERIALIZER = RECIPE_SERIALIZERS.register("custom_dyeable_item", () -> new SimpleCraftingRecipeSerializer<>(DyeItemRecipe::new));
	public static final RegistryObject<SimpleCraftingRecipeSerializer<FlowerDyeRecipe>> FLOWER_DYE_SERIALIZER = RECIPE_SERIALIZERS.register("flower_dye", () -> new SimpleCraftingRecipeSerializer<>(FlowerDyeRecipe::new));
	public static final RegistryObject<SimpleCraftingRecipeSerializer<CoatItemRecipe>> COAT_ITEM_SERIALIZER = RECIPE_SERIALIZERS.register("custom_coat_item", () -> new SimpleCraftingRecipeSerializer<>(CoatItemRecipe::new));
	public static final RegistryObject<SimpleCraftingRecipeSerializer<LightenItemRecipe>> LIGHTEN_ITEM_SERIALIZER = RECIPE_SERIALIZERS.register("custom_lighten_item", () -> new SimpleCraftingRecipeSerializer<>(LightenItemRecipe::new));
	public static final RegistryObject<SimpleCraftingRecipeSerializer<DarkenItemRecipe>> DARKEN_ITEM_SERIALIZER = RECIPE_SERIALIZERS.register("custom_darken_item", () -> new SimpleCraftingRecipeSerializer<>(DarkenItemRecipe::new));
	public static final RegistryObject<SimpleCraftingRecipeSerializer<ImbueItemRecipe>> IMBUE_ITEM_SERIALIZER = RECIPE_SERIALIZERS.register("custom_imbue_item", () -> new SimpleCraftingRecipeSerializer<>(ImbueItemRecipe::new));
	public static final RegistryObject<SimpleCraftingRecipeSerializer<ShinyPaintBrushRecipe>> SHINY_PAINT_BRUSH_SERIALIZER = RECIPE_SERIALIZERS.register("custom_paint_brush_item",
			() -> new SimpleCraftingRecipeSerializer<>(ShinyPaintBrushRecipe::new));
	public static final RegistryObject<SimpleCraftingRecipeSerializer<ShinyScraperRecipe>> SHINY_SCRAPER_SERIALIZER = RECIPE_SERIALIZERS.register("custom_scraper_item", () -> new SimpleCraftingRecipeSerializer<>(ShinyScraperRecipe::new));
	public static final RegistryObject<SimpleCraftingRecipeSerializer<WaterBottleRecipe>> WATER_BOTTLE_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("custom_water_bottle_item", () -> new SimpleCraftingRecipeSerializer<>(WaterBottleRecipe::new));
	public static final RegistryObject<RecipeType<ShineriteStewRecipe>> SHINERITE_STEW_TYPE = RECIPE_TYPES.register("shinerite_stew", () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(MODID, "shinerite_stew")));
	public static final RegistryObject<RecipeSerializer<ShineriteStewRecipe>> SHINERITE_STEW_SERIALIZER = RECIPE_SERIALIZERS.register("shinerite_stew", ShineriteStewRecipeSerializer::new);
	public static final RegistryObject<RecipeType<CopyNBTSmithingRecipe>> COPY_NBT_SMITHING_TYPE = RECIPE_TYPES.register("copy_nbt_smithing", () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(MODID, "copy_nbt_smithing")));
	public static final RegistryObject<RecipeSerializer<CopyNBTSmithingRecipe>> COPY_NBT_SMITHING_SERIALIZER = RECIPE_SERIALIZERS.register("copy_nbt_smithing", CopyNBTSmithingRecipeSerializer::new);
	public static final RegistryObject<RecipeSerializer<ShineriteTrimSmithingRecipe>> SHINERITE_TRIM_SERIALIZER = RECIPE_SERIALIZERS.register("shinerite_trim", ShineriteTrimSmithingRecipeSerializer::new);

	public static void register(IEventBus bus) {
		RECIPE_TYPES.register(bus);
		RECIPE_SERIALIZERS.register(bus);
	}
}
