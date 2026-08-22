package net.jaams.shinerite.configuration.common;

import net.minecraftforge.common.ForgeConfigSpec;

import net.jaams.shinerite.util.ModEnums;

public class RecipesConfig {
	// Coat Recipe
	public static ForgeConfigSpec.ConfigValue<Boolean> COAT_ITEM_RECIPE;
	public static ForgeConfigSpec.ConfigValue<Boolean> COAT_ITEM_RECIPE_3X3_MODE;
	public static ForgeConfigSpec.ConfigValue<Boolean> COAT_ITEM_RECIPE_FREE_MODE;
	// Water Bottle Recipe
	public static ForgeConfigSpec.ConfigValue<Boolean> WATER_BOTTLE_RECIPE;
	public static ForgeConfigSpec.ConfigValue<Boolean> WATER_BOTTLE_RECIPE_3X3_MODE;
	public static ForgeConfigSpec.ConfigValue<Boolean> WATER_BOTTLE_RECIPE_FREE_MODE;
	// Scraper Recipe
	public static ForgeConfigSpec.ConfigValue<Boolean> SHINY_SCRAPER_RECIPE;
	public static ForgeConfigSpec.ConfigValue<Boolean> SHINY_SCRAPER_RECIPE_3X3_MODE;
	public static ForgeConfigSpec.ConfigValue<Boolean> SHINY_SCRAPER_RECIPE_FREE_MODE;
	public static ForgeConfigSpec.IntValue SHINY_SCRAPER_RECIPE_DAMAGE_PER_USE_FREE;
	public static ForgeConfigSpec.IntValue SHINY_SCRAPER_RECIPE_DAMAGE_PER_USE_BULK;
	// Paint Brush Recipe
	public static ForgeConfigSpec.ConfigValue<Boolean> SHINY_PAINT_BRUSH_RECIPE;
	public static ForgeConfigSpec.ConfigValue<Boolean> SHINY_PAINT_BRUSH_RECIPE_3X3_MODE;
	public static ForgeConfigSpec.ConfigValue<Boolean> SHINY_PAINT_BRUSH_RECIPE_FREE_MODE;
	public static ForgeConfigSpec.IntValue SHINY_PAINT_BRUSH_RECIPE_DAMAGE_PER_USE_FREE;
	public static ForgeConfigSpec.IntValue SHINY_PAINT_BRUSH_RECIPE_DAMAGE_PER_USE_BULK;
	// Shineritia Stew Recipe
	public static ForgeConfigSpec.ConfigValue<Boolean> SHINERITIA_STEW_RECIPE;
	// Dye Recipe
	public static ForgeConfigSpec.ConfigValue<Boolean> DYE_ITEM_RECIPE;
	public static ForgeConfigSpec.ConfigValue<Boolean> DYE_ITEM_RECIPE_3X3_MODE;
	public static ForgeConfigSpec.ConfigValue<Boolean> DYE_ITEM_RECIPE_FREE_MODE;
	public static ForgeConfigSpec.ConfigValue<Integer> DYE_ITEM_RECIPE_MAX_DYES;
	public static ForgeConfigSpec.ConfigValue<Boolean> DYE_ITEM_RECIPE_ALLOW_OVER_DYEING;
	// Imbue Recipe
	public static ForgeConfigSpec.ConfigValue<Boolean> IMBUE_ITEM_RECIPE;
	public static ForgeConfigSpec.ConfigValue<Boolean> IMBUE_ITEM_RECIPE_3X3_MODE;
	public static ForgeConfigSpec.ConfigValue<Boolean> IMBUE_ITEM_RECIPE_FREE_MODE;
	public static ForgeConfigSpec.EnumValue<ModEnums.ImbuePotionType> IMBUE_ITEM_RECIPE_POTION_TYPE;
	// Lighten Recipe
	public static ForgeConfigSpec.ConfigValue<Boolean> LIGHTEN_ITEM_RECIPE;
	public static ForgeConfigSpec.ConfigValue<Boolean> LIGHTEN_ITEM_RECIPE_3X3_MODE;
	public static ForgeConfigSpec.ConfigValue<Boolean> LIGHTEN_ITEM_RECIPE_FREE_MODE;
	public static ForgeConfigSpec.ConfigValue<Integer> LIGHTEN_ITEM_RECIPE_MAX_GLOW_INKS;
	// Darken Recipe
	public static ForgeConfigSpec.ConfigValue<Boolean> DARKEN_ITEM_RECIPE;
	public static ForgeConfigSpec.ConfigValue<Boolean> DARKEN_ITEM_RECIPE_3X3_MODE;
	public static ForgeConfigSpec.ConfigValue<Boolean> DARKEN_ITEM_RECIPE_FREE_MODE;
	public static ForgeConfigSpec.ConfigValue<Integer> DARKEN_ITEM_RECIPE_MAX_INKS;
	// Anvil Item Recipe
	public static ForgeConfigSpec.ConfigValue<Boolean> ANVIL_ITEM_RECIPE;
	public static ForgeConfigSpec.ConfigValue<Boolean> ANVIL_DYE_ONLY_MIX;
	public static ForgeConfigSpec.ConfigValue<Boolean> ANVIL_IMBUING_MERGE_EFFECTS;
	public static ForgeConfigSpec.ConfigValue<Boolean> ANVIL_IMBUING_TRANSFER_COLOR;
	public static ForgeConfigSpec.ConfigValue<Boolean> ANVIL_IMBUING_PRESERVE_USES;
	public static ForgeConfigSpec.IntValue ANVIL_IMBUE_COST_LEVEL;
	// Repair Item Recipe
	public static ForgeConfigSpec.ConfigValue<Boolean> REPAIR_ITEM_RECIPE;
	public static ForgeConfigSpec.ConfigValue<Boolean> REPAIR_DYEING_ACTIVE;
	public static ForgeConfigSpec.EnumValue<ModEnums.RepairColorMode> REPAIR_DYE_MODE;
	public static ForgeConfigSpec.ConfigValue<Boolean> REPAIR_IMBUING_ACTIVE;
	public static ForgeConfigSpec.EnumValue<ModEnums.RepairImbuementMode> REPAIR_IMBUING_MODE;

	public static void register(ForgeConfigSpec.Builder builder) {
		builder.push("Custom Recipes Handler");
		builder.push("Anvil Item Recipe");
		ANVIL_ITEM_RECIPE = builder.comment("Master switch: Enable Shinerite-specific anvil combinations (color & imbuement)").define("Anvil Item Recipe", true);
		builder.push("Dyeing");
		ANVIL_DYE_ONLY_MIX = builder.comment("Allow averaging colors when combining two non-imbued but dyed items").define("Anvil Item Recipe Dye Mix", true);
		builder.pop();
		builder.push("Imbuing");
		ANVIL_IMBUING_MERGE_EFFECTS = builder.comment("Allow merging potion effects when combining two imbued items").define("Anvil Item Recipe Merge Effects", true);
		ANVIL_IMBUING_TRANSFER_COLOR = builder.comment("Allow averaging/transferring color when at least one item is imbued").define("Anvil Item Recipe Imbued Color Transfer", true);
		ANVIL_IMBUING_PRESERVE_USES = builder.comment("Allow preserving/averaging ImbuedUses and MaxImbuedUses when combining imbued items").define("Anvil Item Recipe Average Uses", true);
		ANVIL_IMBUE_COST_LEVEL = builder.comment("General cost level for anvil combinations that involve imbued items:\n" + "1 = Very cheap   2 = Cheap   3 = Balanced   4 = Expensive   5 = Very expensive")
				.defineInRange("Anvil Item Recipe Imbue Cost Level", 3, 1, 5);
		builder.pop();
		builder.pop();
		builder.push("Repair Item Recipe");
		REPAIR_ITEM_RECIPE = builder.comment("Master switch: Enable Shinerite-specific logic in vanilla repair recipe (2 identical items)").define("Repair Item Recipe", true);
		builder.push("Dyeing");
		REPAIR_DYEING_ACTIVE = builder.comment("Enable any dyeing/color logic during repair (if disabled, color is never preserved or changed)").define("Repair Item Recipe Dyeing", true);
		REPAIR_DYE_MODE = builder.comment("How to handle color when repairing two dyed items:\nMIX = average RGB colors\nRANDOM = randomly choose one of the two colors").defineEnum("Repair Item Recipe Color Mode", ModEnums.RepairColorMode.MIX);
		builder.pop();
		builder.push("Imbuing");
		REPAIR_IMBUING_ACTIVE = builder.comment("Enable any imbuement-related logic during repair (if disabled, imbuement is never preserved or modified)").define("Repair Item Recipe Imbuement", true);
		REPAIR_IMBUING_MODE = builder.comment("How to handle imbuement when repairing two imbued items:\nMERGE = combine effects (increase amplifier, average duration)\nRANDOM = keep effects from one of the two items randomly")
				.defineEnum("Repair Item Recipe Imbuement Mode", ModEnums.RepairImbuementMode.MERGE);
		builder.pop();
		builder.pop();
		builder.push("Coat Item Recipe");
		COAT_ITEM_RECIPE = builder.comment("Master switch: Enable or disable the Shinerite coating crafting recipe entirely").define("Coating Item Recipe", true);
		COAT_ITEM_RECIPE_3X3_MODE = builder.comment("Enable the 3x3 recipe").define("Coat Item Recipe Bulk Mode", true);
		COAT_ITEM_RECIPE_FREE_MODE = builder.comment("Enable the free-form recipe").define("Coat Item Recipe Free Mode", true);
		builder.pop();
		builder.push("Water Bottle Recipe");
		WATER_BOTTLE_RECIPE = builder.comment("Master switch: Enable or disable the special water bottle cleaning recipe entirely").define("Water Bottle Recipe", true);
		WATER_BOTTLE_RECIPE_3X3_MODE = builder.comment("Enable the 3x3 recipe").define("Water Bottle Recipe Bulk Mode", true);
		WATER_BOTTLE_RECIPE_FREE_MODE = builder.comment("Enable the free-form recipe").define("Water Bottle Recipe Free Mode", true);
		builder.pop();
		builder.push("Shiny Scraper Recipe");
		SHINY_SCRAPER_RECIPE = builder.comment("Master switch: Enable or disable the Shiny Scraper cleaning crafting recipe entirely").define("Shiny Scraper Recipe", true);
		SHINY_SCRAPER_RECIPE_3X3_MODE = builder.comment("Enable the 3x3 recipe: 8 identical cleanable Shinerite items around + 1 Shiny Scraper in center → 8 cleaned items").define("Shiny Scraper Recipe Bulk Mode", true);
		SHINY_SCRAPER_RECIPE_FREE_MODE = builder.comment("Enable the free-form recipe: 1 cleanable Shinerite item + one or more Shiny Scrapers anywhere → 1 cleaned item").define("Shiny Scraper Recipe Free Mode", true);
		SHINY_SCRAPER_RECIPE_DAMAGE_PER_USE_FREE = builder.comment("Damage applied to the used Shiny Scraper in free-form mode (per cleaned item)").defineInRange("Shiny Scraper Damage (Free Mode)", 1, 0, 64);
		SHINY_SCRAPER_RECIPE_DAMAGE_PER_USE_BULK = builder.comment("Damage applied to the center Shiny Scraper in bulk mode (for cleaning 8 items at once)").defineInRange("Shiny Scraper Damage (Bulk Mode)", 8, 0, 64);
		builder.pop();
		builder.push("Shiny Paint Brush Recipe");
		SHINY_PAINT_BRUSH_RECIPE = builder.comment("Master switch: Enable or disable the Shiny Paint Brush dyeing crafting recipe entirely").define("Shiny Paint Brush Recipe", true);
		SHINY_PAINT_BRUSH_RECIPE_3X3_MODE = builder.comment("Enable the 3x3 recipe").define("Shiny Paint Brush Recipe Bulk Mode", true);
		SHINY_PAINT_BRUSH_RECIPE_FREE_MODE = builder.comment("Enable the free-form recipe").define("Shiny Paint Brush Recipe Free Mode", true);
		SHINY_PAINT_BRUSH_RECIPE_DAMAGE_PER_USE_FREE = builder.comment("Damage applied to the used Shiny Paint Brush in free-form mode").defineInRange("Shiny Paint Brush Recipe Damage (Free Mode)", 1, 0, 64);
		SHINY_PAINT_BRUSH_RECIPE_DAMAGE_PER_USE_BULK = builder.comment("Damage applied to the center Shiny Paint Brush in bulk mode").defineInRange("Shiny Paint Brush Recipe Damage (Bulk Mode)", 8, 0, 64);
		builder.pop();
		builder.push("Shineritia Stew Recipe");
		SHINERITIA_STEW_RECIPE = builder.comment("Master switch: Enable or disable the Shineritia Stew (Suspicious Stew with Glowing) crafting recipe").define("Shineritia Stew Recipe", true);
		builder.pop();
		builder.push("Dye Item Recipe");
		DYE_ITEM_RECIPE = builder.comment("Master switch: Enable or disable the Shinerite dyeing crafting recipe entirely").define("Dye Item Recipe", true);
		DYE_ITEM_RECIPE_3X3_MODE = builder.comment("Enable the 3x3 recipe").define("Dye Item Recipe Bulk Mode", true);
		DYE_ITEM_RECIPE_FREE_MODE = builder.comment("Enable the free-form recipe").define("Dye Item Recipe Free Mode", true);
		DYE_ITEM_RECIPE_MAX_DYES = builder.comment("Maximum number of dye items allowed in free-form mode").defineInRange("Dye Item Recipe Max Dyes", 8, 0, 8);
		DYE_ITEM_RECIPE_ALLOW_OVER_DYEING = builder.comment("Allow applying dyes to already dyed items (mixes with existing color)").define("Dye Item Recipe Over-Dyeing", true);
		builder.pop();
		builder.push("Imbue Item Recipe");
		IMBUE_ITEM_RECIPE = builder.comment("Master switch: Enable or disable the Shinerite imbuing crafting recipe entirely").define("Imbue Item Recipe", true);
		IMBUE_ITEM_RECIPE_3X3_MODE = builder.comment("Enable the 3x3 recipe").define("Imbue Item Recipe Bulk Mode", true);
		IMBUE_ITEM_RECIPE_FREE_MODE = builder.comment("Enable the free-form").define("Imbue Item Recipe Free Mode", true);
		IMBUE_ITEM_RECIPE_POTION_TYPE = builder.comment("Choose which type of potion can be used for imbuing").defineEnum("Imbue Item Recipe Potion Type", ModEnums.ImbuePotionType.LINGERING);
		builder.pop();
		builder.push("Lighten Item Recipe");
		LIGHTEN_ITEM_RECIPE = builder.comment("Master switch: Enable or disable the Shinerite lightening crafting recipe entirely").define("Lighten Item Recipe", true);
		LIGHTEN_ITEM_RECIPE_3X3_MODE = builder.comment("Enable the 3x3 recipe").define("Lighten Item Recipe Bulk Mode", true);
		LIGHTEN_ITEM_RECIPE_FREE_MODE = builder.comment("Enable the free-form recipe").define("Lighten Item Recipe Free Mode", true);
		LIGHTEN_ITEM_RECIPE_MAX_GLOW_INKS = builder.comment("Maximum number of Glow Ink Sacs allowed in free-form mode").defineInRange("Lighten Item Recipe Max Glow Inks", 8, 0, 8);
		builder.pop();
		builder.push("Darken Item Recipe");
		DARKEN_ITEM_RECIPE = builder.comment("Master switch: Enable or disable the Shinerite darkening crafting recipe entirely").define("Darken Item Recipe", true);
		DARKEN_ITEM_RECIPE_3X3_MODE = builder.comment("Enable the 3x3 recipe").define("Darken Item Recipe Bulk Mode", true);
		DARKEN_ITEM_RECIPE_FREE_MODE = builder.comment("Enable the free-form recipe").define("Darken Item Recipe Free Mode", true);
		DARKEN_ITEM_RECIPE_MAX_INKS = builder.comment("Maximum number of Ink Sacs allowed in free-form mode").defineInRange("Darken Item Recipe Max Inks", 8, 0, 8);
		builder.pop();
		builder.pop();
	}
}
