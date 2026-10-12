package net.jaams.shinerite.configuration.common;

import net.minecraftforge.common.ForgeConfigSpec;

public class WorldGenConfig {
	public static ForgeConfigSpec.BooleanValue SHINERITIA_FLOWER_PATCH;
	public static ForgeConfigSpec.DoubleValue SHINERITIA_FLOWER_PATCH_CHANCE;
	public static ForgeConfigSpec.DoubleValue SHINERITIA_FLOWER_IMBUED_CHANCE;
	public static ForgeConfigSpec.BooleanValue NETHER_SHINERITIA_FLOWER_PATCH;
	public static ForgeConfigSpec.DoubleValue NETHER_SHINERITIA_FLOWER_PATCH_CHANCE;
	public static ForgeConfigSpec.DoubleValue NETHER_SHINERITIA_FLOWER_IMBUED_CHANCE;
	public static ForgeConfigSpec.BooleanValue SHINERITE_ROOTS;
	public static ForgeConfigSpec.DoubleValue SHINERITE_ROOTS_IMBUED_CHANCE;
	public static ForgeConfigSpec.BooleanValue NETHER_SHINERITE_ROOTS;
	public static ForgeConfigSpec.DoubleValue NETHER_SHINERITE_ROOTS_IMBUED_CHANCE;
	public static ForgeConfigSpec.BooleanValue SHINERITE_COLORED_ORE;
	public static ForgeConfigSpec.BooleanValue NETHER_SHINERITE_COLORED_ORE;

	public static void register(ForgeConfigSpec.Builder builder) {
		builder.push("World Generation Handler");
		builder.comment("Configuration for Jaams Shinerite custom world generation features. Vanilla features are not included here because they cannot be configured by this mod. All of these settings require a world restart to take effect.");

		builder.push("Overworld");

		builder.push("Shineritia Flower Patch");
		SHINERITIA_FLOWER_PATCH = builder.comment("Enable the colored Shineritia flower patch feature in the overworld").worldRestart().define("Enabled", true);
		SHINERITIA_FLOWER_PATCH_CHANCE = builder.comment("Chance (0-1) that a flower patch actually generates when the feature is attempted (lower = rarer)").worldRestart().defineInRange("Generation Chance", 0.25, 0.0, 1.0);
		SHINERITIA_FLOWER_IMBUED_CHANCE = builder.comment("Chance (0-1) that a generated flower is naturally imbued with random effects (lower = rarer)").worldRestart().defineInRange("Imbued Chance", 0.04, 0.0, 1.0);
		builder.pop();

		builder.push("Shinerite Roots");
		SHINERITE_ROOTS = builder.comment("Enable the hanging Shinerite roots feature in the overworld").worldRestart().define("Enabled", true);
		SHINERITE_ROOTS_IMBUED_CHANCE = builder.comment("Chance (0-1) that a generated root segment is naturally imbued with random effects (lower = rarer)").worldRestart().defineInRange("Imbued Chance", 0.03, 0.0, 1.0);
		builder.pop();

		builder.push("Shinerite Colored Ore");
		SHINERITE_COLORED_ORE = builder.comment("Enable the colored Shinerite ore blobs feature in the overworld").worldRestart().define("Enabled", true);
		builder.pop();

		builder.pop();

		builder.push("Nether");

		builder.push("Shineritia Flower Patch");
		NETHER_SHINERITIA_FLOWER_PATCH = builder.comment("Enable the colored Shineritia flower patch feature in the nether").worldRestart().define("Enabled", true);
		NETHER_SHINERITIA_FLOWER_PATCH_CHANCE = builder.comment("Chance (0-1) that a nether flower patch actually generates when the feature is attempted (lower = rarer)").worldRestart().defineInRange("Generation Chance", 0.25, 0.0, 1.0);
		NETHER_SHINERITIA_FLOWER_IMBUED_CHANCE = builder.comment("Chance (0-1) that a generated nether flower is naturally imbued with random effects (lower = rarer)").worldRestart().defineInRange("Imbued Chance", 0.04, 0.0, 1.0);
		builder.pop();

		builder.push("Shinerite Roots");
		NETHER_SHINERITE_ROOTS = builder.comment("Enable the hanging Shinerite roots feature in the nether").worldRestart().define("Enabled", true);
		NETHER_SHINERITE_ROOTS_IMBUED_CHANCE = builder.comment("Chance (0-1) that a generated nether root segment is naturally imbued with random effects (lower = rarer)").worldRestart().defineInRange("Imbued Chance", 0.03, 0.0, 1.0);
		builder.pop();

		builder.push("Shinerite Colored Ore");
		NETHER_SHINERITE_COLORED_ORE = builder.comment("Enable the colored Shinerite ore blobs feature in the nether").worldRestart().define("Enabled", true);
		builder.pop();

		builder.pop();

		builder.pop();
	}
}
