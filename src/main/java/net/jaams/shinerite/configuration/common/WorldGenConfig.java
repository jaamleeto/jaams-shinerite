package net.jaams.shinerite.configuration.common;

import net.minecraftforge.common.ForgeConfigSpec;

public class WorldGenConfig {
	public static ForgeConfigSpec.BooleanValue SHINERITIA_FLOWER_PATCH;
	public static ForgeConfigSpec.DoubleValue SHINERITIA_FLOWER_PATCH_CHANCE;
	public static ForgeConfigSpec.BooleanValue NETHER_SHINERITIA_FLOWER_PATCH;
	public static ForgeConfigSpec.DoubleValue NETHER_SHINERITIA_FLOWER_PATCH_CHANCE;
	public static ForgeConfigSpec.BooleanValue SHINERITE_ROOTS;
	public static ForgeConfigSpec.BooleanValue NETHER_SHINERITE_ROOTS;
	public static ForgeConfigSpec.BooleanValue SHINERITE_COLORED_ORE;
	public static ForgeConfigSpec.BooleanValue NETHER_SHINERITE_COLORED_ORE;

	public static void register(ForgeConfigSpec.Builder builder) {
		builder.push("World Generation");
		builder.comment("Configuration for Jaams Shinerite custom world generation features. Vanilla features are not included here because they cannot be configured by this mod. All of these settings require a world restart to take effect.");

		builder.push("Shineritia Flower Patch (Overworld)");
		SHINERITIA_FLOWER_PATCH = builder.comment("Enable the colored Shineritia flower patch feature in the overworld").worldRestart().define("Enabled", true);
		SHINERITIA_FLOWER_PATCH_CHANCE = builder.comment("Chance (0-1) that a flower patch actually generates when the feature is attempted (lower = rarer)").worldRestart().defineInRange("Generation Chance", 0.25, 0.0, 1.0);
		builder.pop();

		builder.push("Shineritia Flower Patch (Nether)");
		NETHER_SHINERITIA_FLOWER_PATCH = builder.comment("Enable the colored Shineritia flower patch feature in the nether").worldRestart().define("Enabled", true);
		NETHER_SHINERITIA_FLOWER_PATCH_CHANCE = builder.comment("Chance (0-1) that a nether flower patch actually generates when the feature is attempted (lower = rarer)").worldRestart().defineInRange("Generation Chance", 0.25, 0.0, 1.0);
		builder.pop();

		builder.push("Shinerite Roots (Overworld)");
		SHINERITE_ROOTS = builder.comment("Enable the hanging Shinerite roots feature in the overworld").worldRestart().define("Enabled", true);
		builder.pop();

		builder.push("Shinerite Roots (Nether)");
		NETHER_SHINERITE_ROOTS = builder.comment("Enable the hanging Shinerite roots feature in the nether").worldRestart().define("Enabled", true);
		builder.pop();

		builder.push("Shinerite Colored Ore (Overworld)");
		SHINERITE_COLORED_ORE = builder.comment("Enable the colored Shinerite ore blobs feature in the overworld").worldRestart().define("Enabled", true);
		builder.pop();

		builder.push("Shinerite Colored Ore (Nether)");
		NETHER_SHINERITE_COLORED_ORE = builder.comment("Enable the colored Shinerite ore blobs feature in the nether").worldRestart().define("Enabled", true);
		builder.pop();

		builder.pop();
	}
}
