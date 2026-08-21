package net.jaams.shinerite.configuration.common;

import net.minecraftforge.common.ForgeConfigSpec;

public class TraitsConfig {
	// Global toggles 
	public static ForgeConfigSpec.BooleanValue FRAILGLOW;
	public static ForgeConfigSpec.BooleanValue THROWABLE;
	public static ForgeConfigSpec.BooleanValue THROWBACK;
	public static ForgeConfigSpec.BooleanValue RADIANT_COLLECTOR;
	// Frailglow
	public static ForgeConfigSpec.DoubleValue FRAILGLOW_BREAK_CHANCE;
	public static ForgeConfigSpec.BooleanValue FRAILGLOW_GLOWING_ON_BREAK;
	public static ForgeConfigSpec.IntValue FRAILGLOW_GLOW_DURATION;
	public static ForgeConfigSpec.IntValue FRAILGLOW_GLOW_AMPLIFIER;
	// Throwback
	public static ForgeConfigSpec.IntValue THROWBACK_SHINEMERANG_PROJECTILE_MIN_RANGE;
	public static ForgeConfigSpec.IntValue THROWBACK_SHINEMERANG_PROJECTILE_MAX_RANGE;
	public static ForgeConfigSpec.BooleanValue THROWBACK_SHINEMERANG_PROJECTILE_ON_MAX_RANGE;
	public static ForgeConfigSpec.BooleanValue THROWBACK_SHINEMERANG_PROJECTILE_ON_HIT;
	public static ForgeConfigSpec.BooleanValue THROWBACK_SHINEMERANG_PROJECTILE_ON_BLOCK;
	// Radiant Collector
	public static ForgeConfigSpec.IntValue RADIANT_COLLECTOR_SHINEMERANG_PROJECTILE_MAX_ITEMS;
	public static ForgeConfigSpec.BooleanValue RADIANT_COLLECTOR_SHINEMERANG_PROJECTILE_GLOW;

	public static void register(ForgeConfigSpec.Builder builder) {
		builder.push("Weapon Traits Handler");
		// Global toggles
		builder.push("Traits Global Toggles");
		FRAILGLOW = builder.comment("Master switch for the mod's Frailglow trait").define("Frailglow", true);
		THROWABLE = builder.comment("Master switch for the mod's Throwable trait").define("Throwable", true);
		THROWBACK = builder.comment("Master switch for the mod's Throwback projectile trait").define("Throwback", true);
		RADIANT_COLLECTOR = builder.comment("Master switch for the mod's Radiant Collector projectile trait trait").define("Radiant Collector", true);
		builder.pop();
		// Item Traits Settings
		builder.push("Item Traits Settings");
		// Frailglow
		builder.push("Frailglow");
		FRAILGLOW_BREAK_CHANCE = builder.comment("Chance for the item to break when hitting an entity (0.0 = never, 1.0 = always)").defineInRange("Break Chance", 0.3, 0.0, 1.0);
		FRAILGLOW_GLOWING_ON_BREAK = builder.comment("Player receives Glowing effect when the item breaks").define("Glowing On Break", true);
		FRAILGLOW_GLOW_DURATION = builder.comment("Duration of the Glowing effect in ticks when Frailglow item breaks (20 ticks = 1 second)").defineInRange("Glow Duration", 60, 10, 1200);
		FRAILGLOW_GLOW_AMPLIFIER = builder.comment("Amplifier/level of the Glowing effect (0 = level I, 1 = level II, etc.)").defineInRange("Glow Amplifier", 0, 0, 10);
		builder.pop();
		// Throwable 
		builder.push("Throwable");
		ThrowableConfig.register(builder);
		builder.pop();
		builder.pop();
		// Projectile Traits Settings
		builder.push("Projectile Traits Settings");
		// Throwback
		builder.push("Throwback");
		builder.push("Shinemerang");
		THROWBACK_SHINEMERANG_PROJECTILE_MIN_RANGE = builder.comment("Minimum distance before the Shinemerang projectile starts returning").defineInRange("Min Range", 5, 1, 50);
		THROWBACK_SHINEMERANG_PROJECTILE_MAX_RANGE = builder.comment("Maximum distance the Shinemerang projectile can travel before forced return").defineInRange("Max Range", 20, 3, 100);
		THROWBACK_SHINEMERANG_PROJECTILE_ON_MAX_RANGE = builder.comment("Whether the Shinemerang returns automatically at max distance").define("Return On Max Range", true);
		THROWBACK_SHINEMERANG_PROJECTILE_ON_HIT = builder.comment("Whether the Shinemerang returns immediately after hitting an entity").define("Return On Entity Hit", true);
		THROWBACK_SHINEMERANG_PROJECTILE_ON_BLOCK = builder.comment("Whether the Shinemerang returns immediately after hitting a block").define("Return On Block Hit", true);
		builder.pop();
		builder.pop();
		// Radiant Collector
		builder.push("Radiant Collector");
		builder.push("Shinemerang");
		RADIANT_COLLECTOR_SHINEMERANG_PROJECTILE_MAX_ITEMS = builder.comment("Maximum number of items the Shinemerang can pick up and bring back").defineInRange("Max Collected Items", 3, 0, 6);
		RADIANT_COLLECTOR_SHINEMERANG_PROJECTILE_GLOW = builder.comment("Whether collected items glow while being carried by Shinemerang").define("Glow On Collected Items", true);
		builder.pop();
		builder.pop();
		builder.pop();
		builder.pop();
	}
}
