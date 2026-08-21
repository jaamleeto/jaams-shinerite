package net.jaams.shinerite.configuration.common;

import net.minecraftforge.common.ForgeConfigSpec;

public class InteractionsConfig {
	// Master switches
	public static ForgeConfigSpec.BooleanValue PROJECTILE_INTERACTIONS;
	public static ForgeConfigSpec.BooleanValue CAULDRON_INTERACTIONS;
	public static ForgeConfigSpec.BooleanValue HAND_INTERACTIONS;
	public static ForgeConfigSpec.BooleanValue DISPENSER_INTERACTIONS;
	public static ForgeConfigSpec.BooleanValue BLOCK_INTERACTIONS;
	// Projectile impact - Washing 
	public static ForgeConfigSpec.BooleanValue PROJECTILE_IMPACT_WASHING;
	public static ForgeConfigSpec.BooleanValue PROJECTILE_IMPACT_WASHING_LIVING_ENTITIES;
	public static ForgeConfigSpec.BooleanValue PROJECTILE_IMPACT_WASHING_BACK_ITEM;
	public static ForgeConfigSpec.BooleanValue PROJECTILE_IMPACT_WASHING_DROPPED_ITEMS;
	public static ForgeConfigSpec.BooleanValue PROJECTILE_IMPACT_WASHING_PROJECTILES;
	public static ForgeConfigSpec.BooleanValue PROJECTILE_IMPACT_WASHING_BLOCKS;
	// Projectile impact - Imbuing 
	public static ForgeConfigSpec.BooleanValue PROJECTILE_IMPACT_IMBUING;
	public static ForgeConfigSpec.BooleanValue PROJECTILE_IMPACT_IMBUING_DROPPED_ITEMS;
	public static ForgeConfigSpec.BooleanValue PROJECTILE_IMPACT_IMBUING_PROJECTILES;
	public static ForgeConfigSpec.DoubleValue PROJECTILE_IMPACT_IMBUING_RADIUS;
	public static ForgeConfigSpec.DoubleValue PROJECTILE_IMPACT_IMBUING_DISTANCE_FACTOR;
	public static ForgeConfigSpec.BooleanValue PROJECTILE_IMPACT_IMBUING_CONSUME_ON_ENTITY_HIT;
	public static ForgeConfigSpec.BooleanValue PROJECTILE_IMPACT_IMBUING_CONSUME_ON_BLOCK_HIT;
	public static ForgeConfigSpec.IntValue PROJECTILE_IMPACT_IMBUING_USES_PER_HIT;
	// Cauldron
	public static ForgeConfigSpec.BooleanValue CAULDRON_WASH_COLOR;
	public static ForgeConfigSpec.BooleanValue CAULDRON_WASH_IMBUEMENT;
	// Hand interactions
	public static ForgeConfigSpec.BooleanValue HAND_WAXING;
	public static ForgeConfigSpec.BooleanValue HAND_SCRAPING;
	public static ForgeConfigSpec.BooleanValue HAND_DUAL_SCRAPING;
	public static ForgeConfigSpec.BooleanValue HAND_DYEING_DYE;
	public static ForgeConfigSpec.BooleanValue HAND_DYEING_BRUSH;
	public static ForgeConfigSpec.BooleanValue HAND_LIGHTENING;
	public static ForgeConfigSpec.BooleanValue HAND_DARKENING;
	public static ForgeConfigSpec.BooleanValue HAND_IMBUING;
	public static ForgeConfigSpec.BooleanValue HAND_WASHING_WATER_BOTTLE;
	// Dispenser interactions
	public static ForgeConfigSpec.BooleanValue DISPENSER_PROJECTILE_SHINERITE_STAR;
	public static ForgeConfigSpec.BooleanValue DISPENSER_BLOCK_DYEING;
	public static ForgeConfigSpec.BooleanValue DISPENSER_BLOCK_LIGHTENING;
	public static ForgeConfigSpec.BooleanValue DISPENSER_BLOCK_DARKENING;
	public static ForgeConfigSpec.BooleanValue DISPENSER_BLOCK_WAXING;
	public static ForgeConfigSpec.BooleanValue DISPENSER_BLOCK_SCRAPING;
	public static ForgeConfigSpec.BooleanValue DISPENSER_BLOCK_WASHING;
	// Block interactions 
	public static ForgeConfigSpec.BooleanValue BLOCK_DYEING;
	public static ForgeConfigSpec.BooleanValue BLOCK_LIGHTENING;
	public static ForgeConfigSpec.BooleanValue BLOCK_DARKENING;
	public static ForgeConfigSpec.BooleanValue BLOCK_WAXING;
	public static ForgeConfigSpec.BooleanValue BLOCK_SCRAPING;
	public static ForgeConfigSpec.BooleanValue BLOCK_WASHING;
	public static ForgeConfigSpec.BooleanValue BLOCK_IMBUING;
	// Armor interactions
	public static ForgeConfigSpec.BooleanValue ARMOR_IMBUING;

	public static void register(ForgeConfigSpec.Builder builder) {
		builder.push("Interactions Handler");
		builder.push("Projectile Interactions");
		PROJECTILE_INTERACTIONS = builder.comment("Master switch: Enable or disable ALL effects when potions or projectiles impact").define("Projectile Interactions", true);
		builder.push("Water Potion Washing");
		PROJECTILE_IMPACT_WASHING = builder.define("Water Potion Washing", true);
		PROJECTILE_IMPACT_WASHING_LIVING_ENTITIES = builder.define("Water Potion Washing Living Entities", true);
		PROJECTILE_IMPACT_WASHING_BACK_ITEM = builder.define("Water Potion Washing Back Item", true);
		PROJECTILE_IMPACT_WASHING_DROPPED_ITEMS = builder.define("Water Potion Washing Dropped Items", true);
		PROJECTILE_IMPACT_WASHING_PROJECTILES = builder.define("Water Potion Washing Projectiles", true);
		PROJECTILE_IMPACT_WASHING_BLOCKS = builder.define("Water Potion Washing Blocks", true);
		builder.pop();
		builder.push("Potion Imbuing");
		PROJECTILE_IMPACT_IMBUING = builder.define("Potion Imbuing", true);
		PROJECTILE_IMPACT_IMBUING_DROPPED_ITEMS = builder.define("Potion Imbuing Dropped Items", true);
		PROJECTILE_IMPACT_IMBUING_PROJECTILES = builder.define("Potion Imbuing Projectiles", true);
		PROJECTILE_IMPACT_IMBUING_RADIUS = builder.defineInRange("Potion Imbuing Radius", 1.5, 0.5, 5.0);
		PROJECTILE_IMPACT_IMBUING_DISTANCE_FACTOR = builder.defineInRange("Potion Imbuing Max Distance Factor", 2.0, 1.0, 6.0);
		PROJECTILE_IMPACT_IMBUING_CONSUME_ON_ENTITY_HIT = builder.comment("Consume uses when the projectile hits a living entity").define("Consume Uses on Entity Hit", true);
		PROJECTILE_IMPACT_IMBUING_CONSUME_ON_BLOCK_HIT = builder.comment("Consume uses when the projectile hits a block or the ground").define("Consume Uses on Block Hit", false);
		PROJECTILE_IMPACT_IMBUING_USES_PER_HIT = builder.comment("How many uses to consume per valid hit (0 = no consumption, 1 = default behavior)").defineInRange("Consumed Uses per Hit", 1, 0, 64);
		builder.pop();
		builder.pop();
		builder.push("Cauldron Interactions");
		CAULDRON_INTERACTIONS = builder.comment("Master switch: Enable or disable ALL cauldron interactions").define("Cauldron Interactions", true);
		builder.push("Washing Options");
		CAULDRON_WASH_COLOR = builder.define("Cauldron Remove Color", true);
		CAULDRON_WASH_IMBUEMENT = builder.define("Cauldron Remove Imbuement", true);
		builder.pop();
		builder.pop();
		builder.push("Hand Interactions");
		HAND_INTERACTIONS = builder.comment("Master switch: Enable or disable ALL hand interactions").define("Hand Interactions", true);
		builder.push("Waxing");
		HAND_WAXING = builder.define("Waxing", true);
		builder.pop();
		builder.push("Scraping");
		HAND_SCRAPING = builder.define("Scraping", true);
		HAND_DUAL_SCRAPING = builder.define("Dual Scraper Cleaning", true);
		builder.pop();
		builder.push("Dyeing");
		HAND_DYEING_DYE = builder.define("Dyeing with Dyes", true);
		HAND_DYEING_BRUSH = builder.define("Dyeing with Paint Brush", true);
		builder.pop();
		builder.push("Lightening");
		HAND_LIGHTENING = builder.define("Lightening", true);
		builder.pop();
		builder.push("Darkening");
		HAND_DARKENING = builder.define("Darkening", true);
		builder.pop();
		builder.push("Imbuing");
		HAND_IMBUING = builder.define("Imbuing", true);
		builder.pop();
		builder.push("Washing");
		HAND_WASHING_WATER_BOTTLE = builder.define("Washing with Water Bottle", true);
		builder.pop();
		builder.pop(); // Hand Interactions
		builder.push("Dispenser Interactions");
		DISPENSER_INTERACTIONS = builder.comment("Master switch: Enable or disable ALL dispenser interactions").define("Dispenser Interactions", true);
		builder.push("Projectiles");
		DISPENSER_PROJECTILE_SHINERITE_STAR = builder.define("Shinerite Star Projectile", true);
		builder.pop();
		builder.push("Shinerite Blocks");
		DISPENSER_BLOCK_DYEING = builder.define("Dyeing", true);
		DISPENSER_BLOCK_LIGHTENING = builder.define("Lightening", true);
		DISPENSER_BLOCK_DARKENING = builder.define("Darkening", true);
		DISPENSER_BLOCK_WAXING = builder.define("Waxing", true);
		DISPENSER_BLOCK_SCRAPING = builder.define("Scraping", true);
		DISPENSER_BLOCK_WASHING = builder.define("Washing", true);
		builder.pop();
		builder.pop();
		builder.push("Block Interactions");
		BLOCK_INTERACTIONS = builder.comment("Master switch: Enable or disable ALL right-click interactions on Shinerite blocks").define("Block Interactions", true);
		builder.push("Shinerite Blocks");
		BLOCK_DYEING = builder.define("Dyeing", true);
		BLOCK_LIGHTENING = builder.define("Lightening", true);
		BLOCK_DARKENING = builder.define("Darkening", true);
		BLOCK_WAXING = builder.define("Waxing", true);
		BLOCK_SCRAPING = builder.define("Scraping", true);
		BLOCK_WASHING = builder.define("Washing", true);
		BLOCK_IMBUING = builder.define("Imbuing", true);
		builder.pop();
		builder.pop();
		builder.push("Armor Interactions");
		ARMOR_IMBUING = builder.comment("Allow armor to be imbued via hand interaction or anvil").define("Armor Imbuing", true);
		builder.pop();
		builder.pop();
	}
}
