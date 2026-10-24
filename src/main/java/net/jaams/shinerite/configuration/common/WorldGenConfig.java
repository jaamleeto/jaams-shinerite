package net.jaams.shinerite.configuration.common;

import net.minecraft.util.RandomSource;

import net.minecraftforge.common.ForgeConfigSpec;

public class WorldGenConfig {
	public static ForgeConfigSpec.BooleanValue SHINERITIA_FLOWER_PATCH;
	public static ForgeConfigSpec.DoubleValue SHINERITIA_FLOWER_PATCH_CHANCE;
	public static ForgeConfigSpec.IntValue SHINERITIA_FLOWER_MIN_EXTRA;
	public static ForgeConfigSpec.IntValue SHINERITIA_FLOWER_MAX_EXTRA;
	public static ForgeConfigSpec.IntValue SHINERITIA_FLOWER_MIN_RADIUS;
	public static ForgeConfigSpec.IntValue SHINERITIA_FLOWER_MAX_RADIUS;
	public static ForgeConfigSpec.IntValue SHINERITIA_FLOWER_SEARCH_DEPTH;
	public static ForgeConfigSpec.DoubleValue SHINERITIA_FLOWER_IMBUED_CHANCE;
	public static ForgeConfigSpec.BooleanValue SHINERITIA_FLOWER_COLORED_GENERATION;
	public static ForgeConfigSpec.BooleanValue SHINERITE_COLORED_ORE;
	public static ForgeConfigSpec.DoubleValue SHINERITE_COLORED_ORE_CHANCE;
	public static ForgeConfigSpec.DoubleValue SHINERITE_COLORED_ORE_IMBUED_CHANCE;
	public static ForgeConfigSpec.IntValue SHINERITE_COLORED_ORE_MIN_SIZE;
	public static ForgeConfigSpec.IntValue SHINERITE_COLORED_ORE_MAX_SIZE;
	public static ForgeConfigSpec.IntValue SHINERITE_COLORED_ORE_SPREAD;
	public static ForgeConfigSpec.BooleanValue SHINERITE_COLORED_ORE_COLORED_GENERATION;
	public static ForgeConfigSpec.BooleanValue SHINERITE_ROOTS_PATCH;
	public static ForgeConfigSpec.DoubleValue SHINERITE_ROOTS_PATCH_CHANCE;
	public static ForgeConfigSpec.IntValue SHINERITE_ROOTS_MIN_PATCH_SIZE;
	public static ForgeConfigSpec.IntValue SHINERITE_ROOTS_MAX_PATCH_SIZE;
	public static ForgeConfigSpec.IntValue SHINERITE_ROOTS_MIN_RADIUS;
	public static ForgeConfigSpec.IntValue SHINERITE_ROOTS_MAX_RADIUS;
	public static ForgeConfigSpec.BooleanValue SHINERITE_ROOTS_COLORED_GENERATION;
	public static ForgeConfigSpec.DoubleValue SHINERITE_ROOTS_IMBUED_CHANCE;
	public static ForgeConfigSpec.BooleanValue NETHER_SHINERITIA_FLOWER_PATCH;
	public static ForgeConfigSpec.DoubleValue NETHER_SHINERITIA_FLOWER_PATCH_CHANCE;
	public static ForgeConfigSpec.IntValue NETHER_SHINERITIA_FLOWER_MIN_EXTRA;
	public static ForgeConfigSpec.IntValue NETHER_SHINERITIA_FLOWER_MAX_EXTRA;
	public static ForgeConfigSpec.IntValue NETHER_SHINERITIA_FLOWER_MIN_RADIUS;
	public static ForgeConfigSpec.IntValue NETHER_SHINERITIA_FLOWER_MAX_RADIUS;
	public static ForgeConfigSpec.IntValue NETHER_SHINERITIA_FLOWER_SEARCH_DEPTH;
	public static ForgeConfigSpec.DoubleValue NETHER_SHINERITIA_FLOWER_IMBUED_CHANCE;
	public static ForgeConfigSpec.BooleanValue NETHER_SHINERITIA_FLOWER_COLORED_GENERATION;
	public static ForgeConfigSpec.BooleanValue NETHER_SHINERITE_COLORED_ORE;
	public static ForgeConfigSpec.DoubleValue NETHER_SHINERITE_COLORED_ORE_CHANCE;
	public static ForgeConfigSpec.DoubleValue NETHER_SHINERITE_COLORED_ORE_IMBUED_CHANCE;
	public static ForgeConfigSpec.IntValue NETHER_SHINERITE_COLORED_ORE_MIN_SIZE;
	public static ForgeConfigSpec.IntValue NETHER_SHINERITE_COLORED_ORE_MAX_SIZE;
	public static ForgeConfigSpec.BooleanValue NETHER_SHINERITE_COLORED_ORE_COLORED_GENERATION;
	public static ForgeConfigSpec.IntValue NETHER_SHINERITE_COLORED_ORE_SPREAD;
	public static ForgeConfigSpec.BooleanValue NETHER_SHINERITE_ROOTS_PATCH;
	public static ForgeConfigSpec.DoubleValue NETHER_SHINERITE_ROOTS_PATCH_CHANCE;
	public static ForgeConfigSpec.IntValue NETHER_SHINERITE_ROOTS_MIN_PATCH_SIZE;
	public static ForgeConfigSpec.IntValue NETHER_SHINERITE_ROOTS_MAX_PATCH_SIZE;
	public static ForgeConfigSpec.IntValue NETHER_SHINERITE_ROOTS_MIN_RADIUS;
	public static ForgeConfigSpec.IntValue NETHER_SHINERITE_ROOTS_MAX_RADIUS;
	public static ForgeConfigSpec.DoubleValue NETHER_SHINERITE_ROOTS_IMBUED_CHANCE;
	public static ForgeConfigSpec.BooleanValue NETHER_SHINERITE_ROOTS_COLORED_GENERATION;

	private static final int DEFAULT_FLOWER_MIN_EXTRA = 1;
	private static final int DEFAULT_FLOWER_MAX_EXTRA = 4;
	private static final int DEFAULT_FLOWER_MIN_RADIUS = 1;
	private static final int DEFAULT_FLOWER_MAX_RADIUS = 2;
	private static final int DEFAULT_FLOWER_SEARCH_DEPTH = 8;
	private static final int DEFAULT_ROOTS_MIN_PATCH_SIZE = 3;
	private static final int DEFAULT_ROOTS_MAX_PATCH_SIZE = 4;
	private static final int DEFAULT_ROOTS_MIN_RADIUS = 1;
	private static final int DEFAULT_ROOTS_MAX_RADIUS = 2;
	private static final int DEFAULT_ORE_MIN_SIZE = 4;
	private static final int DEFAULT_ORE_MAX_SIZE = 9;

	private static final int DEFAULT_NETHER_ORE_MIN_SIZE = 6;
	private static final int DEFAULT_NETHER_ORE_MAX_SIZE = 18;

	private static final double DEFAULT_ORE_CHANCE = 0.48D;
	private static final double DEFAULT_NETHER_ORE_CHANCE = 0.6D;
	private static final double DEFAULT_ORE_IMBUED_CHANCE = 0.005D;
	private static final int DEFAULT_ORE_SPREAD = 1;

	public static void register(ForgeConfigSpec.Builder builder) {
		builder.push("World Gen Handler");
		builder.comment("Configuration for Jaams Shinerite custom world generation features. Vanilla features are not included here because they cannot be configured by this mod.");
	 builder.comment("'Generation Chance' controls how often the feature actually produces something, on top of the per-chunk attempts defined by its datapack placement.");
		builder.comment("'Imbued Chance' controls how often a generated block is naturally imbued with a random effect. Lower = rarer.");
		builder.comment("Sizes, radii and spread values shape how big each individual generation is. All of these settings require a world restart to take effect.");
		builder.comment("'Colored Generation' controls whether blocks generated by the feature are randomly colored. Setting it to false makes them generate with their default color.");

		builder.push("Overworld Generation");
		pushFlowerPatch(builder, "Shineritia Patch");
		pushRootsPatch(builder, "Shinerite Roots", false);
		pushColoredOre(builder, "Shinerite Ore", false);
		builder.pop();

		builder.push("Nether Generation");
		pushNetherFlowerPatch(builder, "Shineritia Patch");
		pushRootsPatch(builder, "Shinerite Roots", true);
		pushColoredOre(builder, "Shinerite Ore", true);
		builder.pop();

		builder.pop();
	}

	private static void pushFlowerPatch(ForgeConfigSpec.Builder builder, String section) {
		builder.push(section);
		SHINERITIA_FLOWER_PATCH = builder.comment("Enable the colored Shineritia flower patch feature in the overworld").worldRestart().define("Enabled Feature", true);
		SHINERITIA_FLOWER_PATCH_CHANCE = builder.comment("Chance that a flower patch actually generates when the feature is attempted. Lower = rarer").worldRestart().defineInRange("Generation Chance", 0.25D, 0.0D, 1.0D);
		SHINERITIA_FLOWER_MIN_EXTRA = builder.comment("Minimum number of extra flowers spawned around the centre flower of a patch").worldRestart().defineInRange("Minimum Extra Flowers", DEFAULT_FLOWER_MIN_EXTRA, 0, 64);
		SHINERITIA_FLOWER_MAX_EXTRA = builder.comment("Maximum number of extra flowers spawned around the centre flower of a patch").worldRestart().defineInRange("Maximum Extra Flowers", DEFAULT_FLOWER_MAX_EXTRA, 0, 64);
		SHINERITIA_FLOWER_MIN_RADIUS = builder.comment("Minimum horizontal radius in blocks used to scatter the extra flowers").worldRestart().defineInRange("Minimum Radius", DEFAULT_FLOWER_MIN_RADIUS, 0, 16);
		SHINERITIA_FLOWER_MAX_RADIUS = builder.comment("Maximum horizontal radius in blocks used to scatter the extra flowers").worldRestart().defineInRange("Maximum Radius", DEFAULT_FLOWER_MAX_RADIUS, 0, 16);
		SHINERITIA_FLOWER_SEARCH_DEPTH = builder.comment("How many blocks below the candidate position the feature scans for valid ground before giving up").worldRestart().defineInRange("Ground Search Depth", DEFAULT_FLOWER_SEARCH_DEPTH, 1, 64);
		SHINERITIA_FLOWER_IMBUED_CHANCE = builder.comment("Chance that a generated flower is naturally imbued with ONE random effect out of regeneration, poison, speed, luck and levitation. Lower = rarer").worldRestart().defineInRange("Imbued Chance", 0.01D, 0.0D, 1.0D);
		SHINERITIA_FLOWER_COLORED_GENERATION = builder.comment("Whether generated flowers are randomly colored. Disable to make them always generate with their default color").worldRestart().define("Colored Generation", true);
		builder.pop();
	}

	private static void pushNetherFlowerPatch(ForgeConfigSpec.Builder builder, String section) {
		builder.push(section);
		NETHER_SHINERITIA_FLOWER_PATCH = builder.comment("Enable the Shineritia flower patch feature in the nether. Only crimson and warped forests generate them. Disabled by default, enable it to make them generate.").worldRestart().define("Enabled Feature", false);
		NETHER_SHINERITIA_FLOWER_PATCH_CHANCE = builder.comment("Chance that a nether flower patch actually generates when the feature is attempted. Lower = rarer").worldRestart().defineInRange("Generation Chance", 0.2D, 0.0D, 1.0D);
		NETHER_SHINERITIA_FLOWER_MIN_EXTRA = builder.comment("Minimum number of extra flowers spawned around the centre flower of a nether patch").worldRestart().defineInRange("Minimum Extra Flowers", DEFAULT_FLOWER_MIN_EXTRA, 0, 64);
		NETHER_SHINERITIA_FLOWER_MAX_EXTRA = builder.comment("Maximum number of extra flowers spawned around the centre flower of a nether patch").worldRestart().defineInRange("Maximum Extra Flowers", DEFAULT_FLOWER_MAX_EXTRA, 0, 64);
		NETHER_SHINERITIA_FLOWER_MIN_RADIUS = builder.comment("Minimum horizontal radius in blocks used to scatter the extra nether flowers").worldRestart().defineInRange("Minimum Radius", DEFAULT_FLOWER_MIN_RADIUS, 0, 16);
		NETHER_SHINERITIA_FLOWER_MAX_RADIUS = builder.comment("Maximum horizontal radius in blocks used to scatter the extra nether flowers").worldRestart().defineInRange("Maximum Radius", DEFAULT_FLOWER_MAX_RADIUS, 0, 16);
		NETHER_SHINERITIA_FLOWER_SEARCH_DEPTH = builder.comment("How many blocks below the candidate position the feature scans for valid ground before giving up").worldRestart().defineInRange("Ground Search Depth", DEFAULT_FLOWER_SEARCH_DEPTH, 1, 64);
		NETHER_SHINERITIA_FLOWER_IMBUED_CHANCE = builder.comment("Chance that a generated nether flower (crimson or warped) is naturally imbued with ONE random effect out of its respective effect pool. Lower = rarer").worldRestart().defineInRange("Imbued Chance", 0.02D, 0.0D, 1.0D);
		NETHER_SHINERITIA_FLOWER_COLORED_GENERATION = builder.comment("Whether generated nether flowers are randomly colored. Disable to make them always generate with their default color").worldRestart().define("Colored Generation", true);
		builder.pop();
	}

	private static void pushRootsPatch(ForgeConfigSpec.Builder builder, String section, boolean nether) {
		builder.push(section);
		if (nether) {
			NETHER_SHINERITE_ROOTS_PATCH = builder.comment("Enable the hanging Shinerite Roots patches in the nether").worldRestart().define("Enabled Feature", true);
			NETHER_SHINERITE_ROOTS_PATCH_CHANCE = builder.comment("Chance that a nether roots patch actually generates when the feature is attempted. Lower = rarer").worldRestart().defineInRange("Generation Chance", 0.25D, 0.0D, 1.0D);
			NETHER_SHINERITE_ROOTS_MIN_PATCH_SIZE = builder.comment("Minimum number of roots placed in a nether patch").worldRestart().defineInRange("Minimum Patch Size", DEFAULT_ROOTS_MIN_PATCH_SIZE, 1, 64);
			NETHER_SHINERITE_ROOTS_MAX_PATCH_SIZE = builder.comment("Maximum number of roots placed in a nether patch").worldRestart().defineInRange("Maximum Patch Size", DEFAULT_ROOTS_MAX_PATCH_SIZE, 1, 64);
			NETHER_SHINERITE_ROOTS_MIN_RADIUS = builder.comment("Minimum horizontal radius in blocks used to scatter a nether roots patch").worldRestart().defineInRange("Minimum Radius", DEFAULT_ROOTS_MIN_RADIUS, 0, 16);
			NETHER_SHINERITE_ROOTS_MAX_RADIUS = builder.comment("Maximum horizontal radius in blocks used to scatter a nether roots patch").worldRestart().defineInRange("Maximum Radius", DEFAULT_ROOTS_MAX_RADIUS, 0, 16);
			NETHER_SHINERITE_ROOTS_IMBUED_CHANCE = builder.comment("Chance that a generated nether root is naturally imbued with ONE random effect out of fire resistance, strength, glowing and wither. Lower = rarer").worldRestart().defineInRange("Imbued Chance", 0.015D, 0.0D, 1.0D);
			NETHER_SHINERITE_ROOTS_COLORED_GENERATION = builder.comment("Whether generated nether roots are colored (inheriting the color of the Shinerite ore they hang from, or a biome-based palette). Disable to make them always generate with their default color").worldRestart().define("Colored Generation", true);
		} else {
			SHINERITE_ROOTS_PATCH = builder.comment("Enable the hanging Shinerite Roots patches in the overworld, like lush cave vines but for lichens").worldRestart().define("Enabled Feature", true);
			SHINERITE_ROOTS_PATCH_CHANCE = builder.comment("Chance that a roots patch actually generates when the feature is attempted. Lower = rarer").worldRestart().defineInRange("Generation Chance", 0.25D, 0.0D, 1.0D);
			SHINERITE_ROOTS_MIN_PATCH_SIZE = builder.comment("Minimum number of roots placed in a patch").worldRestart().defineInRange("Minimum Patch Size", DEFAULT_ROOTS_MIN_PATCH_SIZE, 1, 64);
			SHINERITE_ROOTS_MAX_PATCH_SIZE = builder.comment("Maximum number of roots placed in a patch").worldRestart().defineInRange("Maximum Patch Size", DEFAULT_ROOTS_MAX_PATCH_SIZE, 1, 64);
			SHINERITE_ROOTS_MIN_RADIUS = builder.comment("Minimum horizontal radius in blocks used to scatter a roots patch").worldRestart().defineInRange("Minimum Radius", DEFAULT_ROOTS_MIN_RADIUS, 0, 16);
			SHINERITE_ROOTS_MAX_RADIUS = builder.comment("Maximum horizontal radius in blocks used to scatter a roots patch").worldRestart().defineInRange("Maximum Radius", DEFAULT_ROOTS_MAX_RADIUS, 0, 16);
			SHINERITE_ROOTS_IMBUED_CHANCE = builder.comment("Chance that a generated root is naturally imbued with ONE random effect out of poison, regeneration, night vision and haste. Lower = rarer").worldRestart().defineInRange("Imbued Chance", 0.015D, 0.0D, 1.0D);
			SHINERITE_ROOTS_COLORED_GENERATION = builder.comment("Whether generated roots are colored (inheriting the color of the Shinerite ore they hang from, or a biome-based palette). Disable to make them always generate with their default color").worldRestart().define("Colored Generation", true);
		}
		builder.pop();
	}

	private static void pushColoredOre(ForgeConfigSpec.Builder builder, String section, boolean nether) {
		builder.push(section);
		if (nether) {
			NETHER_SHINERITE_COLORED_ORE = builder.comment("Enable the colored Shinerite ore blobs feature in the nether").worldRestart().define("Enabled Feature", true);
			NETHER_SHINERITE_COLORED_ORE_CHANCE = builder.comment("Chance that a nether colored ore blob actually generates when the feature is attempted. Lower = rarer").worldRestart().defineInRange("Generation Chance", DEFAULT_NETHER_ORE_CHANCE, 0.0D, 1.0D);
			NETHER_SHINERITE_COLORED_ORE_IMBUED_CHANCE = builder.comment("Chance that a generated nether ore is naturally imbued with ONE random effect out of poison, haste, harm, luck, blindness, night vision, nausea and glowing. Lower = rarer").worldRestart().defineInRange("Imbued Chance", DEFAULT_ORE_IMBUED_CHANCE, 0.0D, 1.0D);
			NETHER_SHINERITE_COLORED_ORE_MIN_SIZE = builder.comment("Minimum number of blocks replaced by a generated nether colored ore blob").worldRestart().defineInRange("Minimum Blob Size", DEFAULT_NETHER_ORE_MIN_SIZE, 1, 64);
			NETHER_SHINERITE_COLORED_ORE_MAX_SIZE = builder.comment("Maximum number of blocks replaced by a generated nether colored ore blob").worldRestart().defineInRange("Maximum Blob Size", DEFAULT_NETHER_ORE_MAX_SIZE, 1, 64);
			NETHER_SHINERITE_COLORED_ORE_SPREAD = builder.comment("How far from the origin a block of a nether colored ore blob can be placed, in blocks. 0 = a single block").worldRestart().defineInRange("Spread Radius", DEFAULT_ORE_SPREAD, 0, 8);
			NETHER_SHINERITE_COLORED_ORE_COLORED_GENERATION = builder.comment("Whether generated nether colored ore blobs are randomly colored. Disable to make them always generate with their default color").worldRestart().define("Colored Generation", true);
		} else {
			SHINERITE_COLORED_ORE = builder.comment("Enable the colored Shinerite ore blobs feature in the overworld").worldRestart().define("Enabled Feature", true);
			SHINERITE_COLORED_ORE_CHANCE = builder.comment("Chance that a colored ore blob actually generates when the feature is attempted. Lower = rarer").worldRestart().defineInRange("Generation Chance", DEFAULT_ORE_CHANCE, 0.0D, 1.0D);
			SHINERITE_COLORED_ORE_IMBUED_CHANCE = builder.comment("Chance that a generated ore is naturally imbued with ONE random effect out of poison, haste, harm, luck, blindness, night vision, nausea and glowing. Lower = rarer").worldRestart().defineInRange("Imbued Chance", DEFAULT_ORE_IMBUED_CHANCE, 0.0D, 1.0D);
			SHINERITE_COLORED_ORE_MIN_SIZE = builder.comment("Minimum number of blocks replaced by a generated colored ore blob").worldRestart().defineInRange("Minimum Blob Size", DEFAULT_ORE_MIN_SIZE, 1, 64);
			SHINERITE_COLORED_ORE_MAX_SIZE = builder.comment("Maximum number of blocks replaced by a generated colored ore blob").worldRestart().defineInRange("Maximum Blob Size", DEFAULT_ORE_MAX_SIZE, 1, 64);
			SHINERITE_COLORED_ORE_SPREAD = builder.comment("How far from the origin a block of a colored ore blob can be placed, in blocks. 0 = a single block").worldRestart().defineInRange("Spread Radius", DEFAULT_ORE_SPREAD, 0, 8);
			SHINERITE_COLORED_ORE_COLORED_GENERATION = builder.comment("Whether generated colored ore blobs are randomly colored. Disable to make them always generate with their default color").worldRestart().define("Colored Generation", true);
		}
		builder.pop();
	}

	public static int rollInt(RandomSource random, int min, int max) {
		int low = Math.min(min, max);
		int high = Math.max(min, max);
		if (high <= low) {
			return low;
		}
		return random.nextInt(high - low + 1) + low;
	}

	public static int rollInt(RandomSource random, ForgeConfigSpec.IntValue min, ForgeConfigSpec.IntValue max) {
		int low = Math.min(min.get(), max.get());
		int high = Math.max(min.get(), max.get());
		if (high <= low) {
			return low;
		}
		return random.nextInt(high - low + 1) + low;
	}
}
