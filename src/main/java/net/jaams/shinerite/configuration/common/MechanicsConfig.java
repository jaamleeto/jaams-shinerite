package net.jaams.shinerite.configuration.common;

import net.minecraftforge.common.ForgeConfigSpec;

public class MechanicsConfig {
	public static ForgeConfigSpec.BooleanValue DYEING;
	public static ForgeConfigSpec.BooleanValue IMBUING;
	public static ForgeConfigSpec.BooleanValue IMBUABLE_FOOD;
	public static ForgeConfigSpec.BooleanValue IMBUABLE_WEAPONS;
	public static ForgeConfigSpec.BooleanValue IMBUABLE_ARMOR;
	public static ForgeConfigSpec.BooleanValue IMBUABLE_BLOCKS;
	public static ForgeConfigSpec.BooleanValue IMBUABLE_MATERIALS;
	public static ForgeConfigSpec.BooleanValue IMBUABLE_MUSIC_DISCS;
	public static ForgeConfigSpec.DoubleValue IMBUED_EFFECT_DECAY_EXPONENT;
	public static ForgeConfigSpec.IntValue IMBUED_MIN_DURATION_TICKS;
	public static ForgeConfigSpec.IntValue IMBUED_DAMAGE_PER_HIT;
	public static ForgeConfigSpec.BooleanValue IMBUED_CONSUME_USES_IN_CREATIVE;
	public static ForgeConfigSpec.BooleanValue IMBUED_FIRE_RESISTANCE_FIREPROOF;
	public static ForgeConfigSpec.BooleanValue IMBUED_INVISIBILITY_HIDE_ITEM;
	public static ForgeConfigSpec.IntValue IMBUED_ARMOR_TICK_INTERVAL;
	public static ForgeConfigSpec.BooleanValue IMBUED_ARMOR_RETALIATE_ON_HIT;
	public static ForgeConfigSpec.BooleanValue IMBUED_ARMOR_RETALIATE_HOSTILE_ONLY;
	public static ForgeConfigSpec.IntValue IMBUED_BLOCK_PARTICLE_INTERVAL;
	public static ForgeConfigSpec.BooleanValue IMBUED_BLOCK_COATED_CAN_IMBUE;
	public static ForgeConfigSpec.BooleanValue IMBUED_BLOCK_APPLY_TO_HOSTILE;
	public static ForgeConfigSpec.BooleanValue IMBUED_BLOCK_APPLY_TO_PASSIVE;
	public static ForgeConfigSpec.BooleanValue IMBUED_BLOCK_APPLY_TO_PLAYERS;
	public static ForgeConfigSpec.DoubleValue IMBUED_BLOCK_BREAK_DURATION_FACTOR;
	public static ForgeConfigSpec.BooleanValue WASHING;
	public static ForgeConfigSpec.BooleanValue WASHING_RAIN;
	public static ForgeConfigSpec.BooleanValue WASHING_WATER;
	public static ForgeConfigSpec.BooleanValue WASHING_THUNDERSTORM_BOOST;
	public static ForgeConfigSpec.BooleanValue WASHING_RAIN_SIDES;
	public static ForgeConfigSpec.BooleanValue WASHING_AFFECT_HELD_ITEMS;
	public static ForgeConfigSpec.BooleanValue WASHING_AFFECT_ARMOR;
	public static ForgeConfigSpec.BooleanValue WASHING_AFFECT_BACK_ITEM;
	public static ForgeConfigSpec.BooleanValue WASHING_AFFECT_DROPPED_ITEMS;
	public static ForgeConfigSpec.BooleanValue WASHING_AFFECT_PROJECTILES;
	public static ForgeConfigSpec.BooleanValue WASHING_AFFECT_BLOCKS;
	public static ForgeConfigSpec.IntValue WASHING_TICKS_PER_STEP;
	public static ForgeConfigSpec.BooleanValue COATING;

	public static void register(ForgeConfigSpec.Builder builder) {
		builder.push("Global Mechanics Handler");
		builder.comment("Configuration for the custom mechanics added by Jaams Shinerite. Each mechanic can be toggled globally and fine-tuned with its own sub-settings.");

		builder.push("Dyeing");
		DYEING = builder.comment("Enable or disable the dyeing mechanic entirely").define("Dyeing", true);
		builder.pop();

		builder.push("Imbuing");
		IMBUING = builder.comment("Enable or disable the imbuing mechanic entirely").define("Imbuing", true);
		builder.push("Imbuable Item Categories");
		IMBUABLE_FOOD = builder.comment("Allow food items to be imbued").define("Imbuable Food", true);
		IMBUABLE_WEAPONS = builder.comment("Allow weapons to be imbued").define("Imbuable Weapons", true);
		IMBUABLE_ARMOR = builder.comment("Allow armor pieces to be imbued").define("Imbuable Armor", true);
		IMBUABLE_BLOCKS = builder.comment("Allow blocks to be imbued").define("Imbuable Blocks", true);
		IMBUABLE_MATERIALS = builder.comment("Allow material items (ingots, raw shinerite, nuggets) to be imbued").define("Imbuable Materials", true);
		IMBUABLE_MUSIC_DISCS = builder.comment("Allow shinerite music discs to be imbued").define("Imbuable Music Discs", true);
		builder.pop();
		builder.push("Imbued Effect Application");
		IMBUED_EFFECT_DECAY_EXPONENT = builder.comment("Exponent that controls how the effect power decays as the imbued item loses uses, higher = power drops faster").defineInRange("Effect Power Decay Exponent", 0.5D, 0.1D, 2.0D);
		IMBUED_MIN_DURATION_TICKS = builder.comment("Minimum effect duration in ticks applied when transferring effects, even when the item is almost depleted (20 = 1 second)").defineInRange("Minimum Effect Duration Ticks", 20, 1, 200);
		IMBUED_DAMAGE_PER_HIT = builder.comment("Extra durability damage the item takes per hit while its imbued effect is active").defineInRange("Extra Durability Damage per Imbued Hit", 2, 0, 10);
		IMBUED_CONSUME_USES_IN_CREATIVE = builder.comment("Whether imbued items still consume uses when used by a creative player").define("Consume Uses in Creative Mode", false);
		builder.pop();
		builder.push("Imbued Effect Behaviors");
		IMBUED_FIRE_RESISTANCE_FIREPROOF = builder.comment("Items imbued with fire resistance become fireproof, like netherite gear").define("Fire Resistance Fireproof", true);
		IMBUED_INVISIBILITY_HIDE_ITEM = builder.comment("Items imbued with invisibility stop rendering on the player").define("Invisibility Hides Items", true);
		builder.pop();
		builder.push("Imbued Armor Settings");
		IMBUED_ARMOR_TICK_INTERVAL = builder.comment("Ticks between armor imbued effect applications to attacker (20 = 1 second)").defineInRange("Armor Tick Interval", 40, 1, 200);
		IMBUED_ARMOR_RETALIATE_ON_HIT = builder.comment("Imbued armor retaliates with effects when the wearer is hit").define("Armor Retaliate on Hit", true);
		IMBUED_ARMOR_RETALIATE_HOSTILE_ONLY = builder.comment("Only apply retaliatory effects to hostile mobs (false = applies to all attackers including players)").define("Armor Retaliate Hostile Only", false);
		builder.pop();
		builder.push("Imbued Blocks Settings");
		IMBUED_BLOCK_PARTICLE_INTERVAL = builder.comment("Ticks between ambient potion particles on imbued blocks, higher = better performance (20 = 1 second)").defineInRange("Block Particle Interval", 10, 1, 200);
		IMBUED_BLOCK_COATED_CAN_IMBUE = builder.comment("Allow coated blocks to be imbued with potions").define("Coated Blocks Can Be Imbued", true);
		IMBUED_BLOCK_APPLY_TO_HOSTILE = builder.comment("Imbued blocks apply effects to hostile mobs").define("Block Apply to Hostile", true);
		IMBUED_BLOCK_APPLY_TO_PASSIVE = builder.comment("Imbued blocks apply effects to passive animals").define("Block Apply to Passive", true);
		IMBUED_BLOCK_APPLY_TO_PLAYERS = builder.comment("Imbued blocks apply effects to players").define("Block Apply to Players", true);
		IMBUED_BLOCK_BREAK_DURATION_FACTOR = builder.comment("Duration multiplier for the effects transferred when a player BREAKS an imbued block. The sneak right-click transfer always uses the full stored duration, so keeping this below 1.0 makes breaking the weaker option").defineInRange("Block Break Duration Factor", 0.5D, 0.0D, 1.0D);
		builder.pop();
		builder.pop();

		builder.push("Washing");
		WASHING = builder.comment("Enable or disable the washing mechanic entirely").define("Washing", true);
		builder.push("Washing Decay Sources");
		WASHING_RAIN = builder.comment("Whether colored or imbued effects decay when exposed to rain").define("Rain Decay", true);
		WASHING_WATER = builder.comment("Whether colored or coated effects decay when in contact with water").define("Water Contact Decay", true);
		WASHING_THUNDERSTORM_BOOST = builder.comment("Decay progresses faster during thunderstorms when rain decay is enabled").define("Thunderstorm Boost", true);
		WASHING_RAIN_SIDES = builder.comment("Whether blocks also decay when rain reaches them from the sides, not only from above").define("Rain From Block Sides", false);
		builder.pop();
		builder.push("Washing Affected Items");
		WASHING_AFFECT_HELD_ITEMS = builder.comment("Washing decay applies to items held in the player's hands").define("Held Items", true);
		WASHING_AFFECT_ARMOR = builder.comment("Washing decay applies to equipped armor").define("Armor", true);
		WASHING_AFFECT_BACK_ITEM = builder.comment("Washing decay applies to back items (from curios cosmetic slots)").define("Back Item", true);
		WASHING_AFFECT_DROPPED_ITEMS = builder.comment("Washing decay applies to dropped item entities on the ground").define("Dropped Items", true);
		WASHING_AFFECT_PROJECTILES = builder.comment("Washing decay applies to arrows and other projectiles in flight").define("Projectiles", true);
		WASHING_AFFECT_BLOCKS = builder.comment("Washing decay applies to placed blocks").define("Blocks", true);
		builder.pop();
		builder.push("Washing Decay Rate");
		WASHING_TICKS_PER_STEP = builder.comment("Ticks between each washing decay step, lower = faster decay (20 = 1 second)").defineInRange("Ticks per Decay Step", 50, 10, 600);
		builder.pop();
		builder.pop();

		builder.push("Coating");
		COATING = builder.comment("Enable or disable the coating mechanic entirely").define("Coating", true);
		builder.pop();
		builder.pop();
	}
}
