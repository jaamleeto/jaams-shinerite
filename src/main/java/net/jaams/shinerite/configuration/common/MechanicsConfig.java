package net.jaams.shinerite.configuration.common;

import net.minecraftforge.common.ForgeConfigSpec;

public class MechanicsConfig {
	public static ForgeConfigSpec.BooleanValue COATING;
	public static ForgeConfigSpec.BooleanValue IMBUING;
	public static ForgeConfigSpec.BooleanValue DYEING;
	public static ForgeConfigSpec.BooleanValue WASHING;
	// Washing details
	public static ForgeConfigSpec.BooleanValue WASHING_RAIN;
	public static ForgeConfigSpec.BooleanValue WASHING_WATER;
	public static ForgeConfigSpec.BooleanValue WASHING_THUNDERSTORM_BOOST;
	public static ForgeConfigSpec.BooleanValue WASHING_AFFECT_HELD_ITEMS;
	public static ForgeConfigSpec.BooleanValue WASHING_AFFECT_ARMOR;
	public static ForgeConfigSpec.BooleanValue WASHING_AFFECT_BACK_ITEM;
	public static ForgeConfigSpec.BooleanValue WASHING_AFFECT_DROPPED_ITEMS;
	public static ForgeConfigSpec.BooleanValue WASHING_AFFECT_PROJECTILES;
	public static ForgeConfigSpec.BooleanValue WASHING_AFFECT_BLOCKS;
	public static ForgeConfigSpec.BooleanValue WASHING_RAIN_SIDES;
	public static ForgeConfigSpec.IntValue WASHING_TICKS_PER_STEP;
	// Imbuing details
	public static ForgeConfigSpec.DoubleValue IMBUED_EFFECT_DECAY_EXPONENT;
	public static ForgeConfigSpec.IntValue IMBUED_MIN_DURATION_TICKS;
	public static ForgeConfigSpec.IntValue IMBUED_DAMAGE_PER_HIT;
	public static ForgeConfigSpec.BooleanValue IMBUED_CONSUME_USES_IN_CREATIVE;
	public static ForgeConfigSpec.BooleanValue IMBUED_FIRE_RESISTANCE_FIREPROOF;
	public static ForgeConfigSpec.BooleanValue IMBUED_INVISIBILITY_HIDE_ITEM;
	// Imbuable item categories
	public static ForgeConfigSpec.BooleanValue IMBUABLE_FOOD;
	public static ForgeConfigSpec.BooleanValue IMBUABLE_WEAPONS;
	public static ForgeConfigSpec.BooleanValue IMBUABLE_ARMOR;
	public static ForgeConfigSpec.BooleanValue IMBUABLE_BLOCKS;
	public static ForgeConfigSpec.BooleanValue IMBUABLE_MATERIALS;
	public static ForgeConfigSpec.BooleanValue IMBUABLE_MUSIC_DISCS;
	// Imbued armor
	public static ForgeConfigSpec.IntValue IMBUED_ARMOR_TICK_INTERVAL;
	public static ForgeConfigSpec.BooleanValue IMBUED_ARMOR_RETALIATE_ON_HIT;
	public static ForgeConfigSpec.BooleanValue IMBUED_ARMOR_RETALIATE_HOSTILE_ONLY;
	// Imbued blocks
	public static ForgeConfigSpec.IntValue IMBUED_BLOCK_PARTICLE_INTERVAL;
	public static ForgeConfigSpec.BooleanValue IMBUED_BLOCK_COATED_CAN_IMBUE;
	public static ForgeConfigSpec.BooleanValue IMBUED_BLOCK_APPLY_TO_HOSTILE;
	public static ForgeConfigSpec.BooleanValue IMBUED_BLOCK_APPLY_TO_PASSIVE;
	public static ForgeConfigSpec.BooleanValue IMBUED_BLOCK_APPLY_TO_PLAYERS;

	public static void register(ForgeConfigSpec.Builder builder) {
		builder.push("Global Mechanics Handler");
		builder.push("Dyeing");
		DYEING = builder.define("Dyeing", true);
		builder.pop();
		builder.push("Imbuing");
		IMBUING = builder.define("Imbuing", true);
		builder.push("Imbuable Item Categories");
		IMBUABLE_FOOD = builder.comment("Allow food items to be imbued").define("Imbuable Food", true);
		IMBUABLE_WEAPONS = builder.comment("Allow weapons to be imbued").define("Imbuable Weapons", true);
		IMBUABLE_ARMOR = builder.comment("Allow armor pieces to be imbued").define("Imbuable Armor", true);
		IMBUABLE_BLOCKS = builder.comment("Allow blocks to be imbued").define("Imbuable Blocks", true);
		IMBUABLE_MATERIALS = builder.comment("Allow material items (ingots, raw shinerite, nuggets) to be imbued").define("Imbuable Materials", true);
		IMBUABLE_MUSIC_DISCS = builder.comment("Allow shinerite music discs to be imbued").define("Imbuable Music Discs", true);
		builder.pop();
		builder.push("Imbued Effect Application");
		IMBUED_EFFECT_DECAY_EXPONENT = builder.defineInRange("Effect Power Decay Exponent", 0.5, 0.1, 2.0);
		IMBUED_MIN_DURATION_TICKS = builder.defineInRange("Minimum Effect Duration (Ticks)", 20, 1, 200);
		IMBUED_DAMAGE_PER_HIT = builder.defineInRange("Extra Durability Damage per Imbued Hit", 2, 0, 10);
		IMBUED_CONSUME_USES_IN_CREATIVE = builder.define("Consume Uses in Creative Mode", false);
		builder.pop();
		builder.push("Imbued Effect Behaviors");
		IMBUED_FIRE_RESISTANCE_FIREPROOF = builder.define("Fire Resistance Fireproof", true);
		IMBUED_INVISIBILITY_HIDE_ITEM = builder.define("Invisibility Hides Items", true);
		builder.pop();
		builder.push("Imbued Armor");
		IMBUED_ARMOR_TICK_INTERVAL = builder.comment("Ticks between armor imbued effect applications to attacker (20 = 1 second)").defineInRange("Armor Tick Interval", 40, 1, 200);
		IMBUED_ARMOR_RETALIATE_ON_HIT = builder.comment("Imbued armor retaliates with effects when the wearer is hit").define("Armor Retaliate on Hit", true);
		IMBUED_ARMOR_RETALIATE_HOSTILE_ONLY = builder.comment("Only apply retaliatory effects to hostile mobs (false = applies to all attackers including players)").define("Armor Retaliate Hostile Only", false);
		builder.pop();
		builder.push("Imbued Blocks");
		IMBUED_BLOCK_PARTICLE_INTERVAL = builder.comment("Ticks between ambient potion particles on imbued blocks, higher = better performance (20 = 1 second)").defineInRange("Block Particle Interval", 10, 1, 200);
		IMBUED_BLOCK_COATED_CAN_IMBUE = builder.comment("Allow coated blocks to be imbued with potions").define("Coated Blocks Can Be Imbued", true);
		IMBUED_BLOCK_APPLY_TO_HOSTILE = builder.comment("Imbued blocks apply effects to hostile mobs").define("Block Apply to Hostile", true);
		IMBUED_BLOCK_APPLY_TO_PASSIVE = builder.comment("Imbued blocks apply effects to passive animals").define("Block Apply to Passive", true);
		IMBUED_BLOCK_APPLY_TO_PLAYERS = builder.comment("Imbued blocks apply effects to players").define("Block Apply to Players", true);
		builder.pop();
		builder.pop();
		builder.push("Washing");
		WASHING = builder.define("Washing", true);
		WASHING_RAIN = builder.define("Washing Rain Decay", true);
		WASHING_WATER = builder.define("Washing Water Contact Decay", true);
		WASHING_THUNDERSTORM_BOOST = builder.define("Washing Thunderstorm Boost", true);
		WASHING_AFFECT_HELD_ITEMS = builder.define("Washing Held Items", true);
		WASHING_AFFECT_ARMOR = builder.define("Washing Armor", true);
		WASHING_AFFECT_BACK_ITEM = builder.define("Washing Back Item", true);
		WASHING_AFFECT_DROPPED_ITEMS = builder.define("Washing Dropped Items", true);
		WASHING_AFFECT_PROJECTILES = builder.define("Washing Projectiles", true);
		WASHING_AFFECT_BLOCKS = builder.define("Washing Blocks", true);
		WASHING_RAIN_SIDES = builder.define("Washing Rain From Block Sides", false);
		WASHING_TICKS_PER_STEP = builder.defineInRange("Washing Ticks Decay", 50, 10, 600);
		builder.pop();
		builder.push("Coating");
		COATING = builder.define("Coating", true);
		builder.pop();
		builder.pop(); // Global Mechanics Handler
	}
}
