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
	public static ForgeConfigSpec.BooleanValue NETHER_SHINERITIA_FLOWER_PATCH;
	public static ForgeConfigSpec.DoubleValue NETHER_SHINERITIA_FLOWER_PATCH_CHANCE;
	public static ForgeConfigSpec.IntValue NETHER_SHINERITIA_FLOWER_MIN_EXTRA;
	public static ForgeConfigSpec.IntValue NETHER_SHINERITIA_FLOWER_MAX_EXTRA;
	public static ForgeConfigSpec.IntValue NETHER_SHINERITIA_FLOWER_MIN_RADIUS;
	public static ForgeConfigSpec.IntValue NETHER_SHINERITIA_FLOWER_MAX_RADIUS;
	public static ForgeConfigSpec.IntValue NETHER_SHINERITIA_FLOWER_SEARCH_DEPTH;
	public static ForgeConfigSpec.DoubleValue NETHER_SHINERITIA_FLOWER_IMBUED_CHANCE;
	public static ForgeConfigSpec.BooleanValue SHINERITE_ROOTS;
	public static ForgeConfigSpec.DoubleValue SHINERITE_ROOTS_CHANCE;
	public static ForgeConfigSpec.IntValue SHINERITE_ROOTS_MIN_LENGTH;
	public static ForgeConfigSpec.IntValue SHINERITE_ROOTS_MAX_LENGTH;
	public static ForgeConfigSpec.DoubleValue SHINERITE_ROOTS_IMBUED_CHANCE;
	public static ForgeConfigSpec.BooleanValue NETHER_SHINERITE_ROOTS;
	public static ForgeConfigSpec.DoubleValue NETHER_SHINERITE_ROOTS_CHANCE;
	public static ForgeConfigSpec.IntValue NETHER_SHINERITE_ROOTS_MIN_LENGTH;
	public static ForgeConfigSpec.IntValue NETHER_SHINERITE_ROOTS_MAX_LENGTH;
	public static ForgeConfigSpec.DoubleValue NETHER_SHINERITE_ROOTS_IMBUED_CHANCE;
	public static ForgeConfigSpec.BooleanValue SHINERITE_COLORED_ORE;
	public static ForgeConfigSpec.DoubleValue SHINERITE_COLORED_ORE_CHANCE;
	public static ForgeConfigSpec.IntValue SHINERITE_COLORED_ORE_MIN_SIZE;
	public static ForgeConfigSpec.IntValue SHINERITE_COLORED_ORE_MAX_SIZE;
	public static ForgeConfigSpec.IntValue SHINERITE_COLORED_ORE_SPREAD;
	public static ForgeConfigSpec.BooleanValue NETHER_SHINERITE_COLORED_ORE;
	public static ForgeConfigSpec.DoubleValue NETHER_SHINERITE_COLORED_ORE_CHANCE;
	public static ForgeConfigSpec.IntValue NETHER_SHINERITE_COLORED_ORE_MIN_SIZE;
	public static ForgeConfigSpec.IntValue NETHER_SHINERITE_COLORED_ORE_MAX_SIZE;
	public static ForgeConfigSpec.IntValue NETHER_SHINERITE_COLORED_ORE_SPREAD;

	private static final int DEFAULT_FLOWER_MIN_EXTRA = 1;
	private static final int DEFAULT_FLOWER_MAX_EXTRA = 4;
	private static final int DEFAULT_FLOWER_MIN_RADIUS = 1;
	private static final int DEFAULT_FLOWER_MAX_RADIUS = 2;
	private static final int DEFAULT_FLOWER_SEARCH_DEPTH = 8;
	private static final int DEFAULT_ROOTS_MIN_LENGTH = 3;
	private static final int DEFAULT_ROOTS_MAX_LENGTH = 7;
	private static final int DEFAULT_ORE_MIN_SIZE = 6;
	private static final int DEFAULT_ORE_MAX_SIZE = 10;
	private static final int DEFAULT_ORE_SPREAD = 1;

	public static void register(ForgeConfigSpec.Builder builder) {
		builder.push("World Generation Handler");
		builder.comment("Configuration for Jaams Shinerite custom world generation features. Vanilla features are not included here because they cannot be configured by this mod.");
		builder.comment("'Generation Chance' controls how often the feature actually produces something, on top of the per-chunk attempts defined by its datapack placement.");
		builder.comment("Sizes, lengths, radii and spread values shape how big each individual generation is. All of these settings require a world restart to take effect.");

		builder.push("Overworld Generation");
		pushFlowerPatch(builder, "Shineritia Patch", false);
		pushRoots(builder, "Shinerite Roots", false);
		pushColoredOre(builder, "Shinerite Ore", false);
		builder.pop();

		builder.push("Nether Generation");
		pushFlowerPatch(builder, "Shineritia Patch", true);
		pushRoots(builder, "Shinerite Roots", true);
		pushColoredOre(builder, "Shinerite Ore", true);
		builder.pop();

		builder.pop();
	}

	private static void pushFlowerPatch(ForgeConfigSpec.Builder builder, String section, boolean nether) {
		builder.push(section);
		if (nether) {
			NETHER_SHINERITIA_FLOWER_PATCH = builder.comment("Enable the colored Shineritia flower patch feature in the nether").worldRestart().define("Enabled", true);
			NETHER_SHINERITIA_FLOWER_PATCH_CHANCE = builder.comment("Chance that a nether flower patch actually generates when the feature is attempted. Lower = rarer").worldRestart().defineInRange("Generation Chance", 0.25, 0.0, 1.0);
			NETHER_SHINERITIA_FLOWER_MIN_EXTRA = builder.comment("Minimum number of extra flowers spawned around the centre flower of a nether patch").worldRestart().defineInRange("Minimum Extra Flowers", DEFAULT_FLOWER_MIN_EXTRA, 0, 64);
			NETHER_SHINERITIA_FLOWER_MAX_EXTRA = builder.comment("Maximum number of extra flowers spawned around the centre flower of a nether patch").worldRestart().defineInRange("Maximum Extra Flowers", DEFAULT_FLOWER_MAX_EXTRA, 0, 64);
			NETHER_SHINERITIA_FLOWER_MIN_RADIUS = builder.comment("Minimum horizontal radius in blocks used to scatter the extra nether flowers").worldRestart().defineInRange("Minimum Radius", DEFAULT_FLOWER_MIN_RADIUS, 0, 16);
			NETHER_SHINERITIA_FLOWER_MAX_RADIUS = builder.comment("Maximum horizontal radius in blocks used to scatter the extra nether flowers").worldRestart().defineInRange("Maximum Radius", DEFAULT_FLOWER_MAX_RADIUS, 0, 16);
			NETHER_SHINERITIA_FLOWER_SEARCH_DEPTH = builder.comment("How many blocks below the candidate position the feature scans for valid ground before giving up").worldRestart().defineInRange("Ground Search Depth", DEFAULT_FLOWER_SEARCH_DEPTH, 1, 64);
			NETHER_SHINERITIA_FLOWER_IMBUED_CHANCE = builder.comment("Chance that a generated nether flower is naturally imbued with random effects. Lower = rarer").worldRestart().defineInRange("Imbued Chance", 0.04, 0.0, 1.0);
		} else {
			SHINERITIA_FLOWER_PATCH = builder.comment("Enable the colored Shineritia flower patch feature in the overworld").worldRestart().define("Enabled", true);
			SHINERITIA_FLOWER_PATCH_CHANCE = builder.comment("Chance that a flower patch actually generates when the feature is attempted. Lower = rarer").worldRestart().defineInRange("Generation Chance", 0.25, 0.0, 1.0);
			SHINERITIA_FLOWER_MIN_EXTRA = builder.comment("Minimum number of extra flowers spawned around the centre flower of a patch").worldRestart().defineInRange("Minimum Extra Flowers", DEFAULT_FLOWER_MIN_EXTRA, 0, 64);
			SHINERITIA_FLOWER_MAX_EXTRA = builder.comment("Maximum number of extra flowers spawned around the centre flower of a patch").worldRestart().defineInRange("Maximum Extra Flowers", DEFAULT_FLOWER_MAX_EXTRA, 0, 64);
			SHINERITIA_FLOWER_MIN_RADIUS = builder.comment("Minimum horizontal radius in blocks used to scatter the extra flowers").worldRestart().defineInRange("Minimum Radius", DEFAULT_FLOWER_MIN_RADIUS, 0, 16);
			SHINERITIA_FLOWER_MAX_RADIUS = builder.comment("Maximum horizontal radius in blocks used to scatter the extra flowers").worldRestart().defineInRange("Maximum Radius", DEFAULT_FLOWER_MAX_RADIUS, 0, 16);
			SHINERITIA_FLOWER_SEARCH_DEPTH = builder.comment("How many blocks below the candidate position the feature scans for valid ground before giving up").worldRestart().defineInRange("Ground Search Depth", DEFAULT_FLOWER_SEARCH_DEPTH, 1, 64);
			SHINERITIA_FLOWER_IMBUED_CHANCE = builder.comment("Chance that a generated flower is naturally imbued with random effects. Lower = rarer").worldRestart().defineInRange("Imbued Chance", 0.04, 0.0, 1.0);
		}
		builder.pop();
	}

	private static void pushRoots(ForgeConfigSpec.Builder builder, String section, boolean nether) {
		builder.push(section);
		if (nether) {
			NETHER_SHINERITE_ROOTS = builder.comment("Enable the hanging Shinerite roots feature in the nether").worldRestart().define("Enabled", true);
			NETHER_SHINERITE_ROOTS_CHANCE = builder.comment("Chance that a nether root strand actually generates when the feature is attempted. Lower = rarer").worldRestart().defineInRange("Generation Chance", 0.35, 0.0, 1.0);
			NETHER_SHINERITE_ROOTS_MIN_LENGTH = builder.comment("Minimum number of blocks in a generated nether root strand").worldRestart().defineInRange("Minimum Length", DEFAULT_ROOTS_MIN_LENGTH, 1, 32);
			NETHER_SHINERITE_ROOTS_MAX_LENGTH = builder.comment("Maximum number of blocks in a generated nether root strand").worldRestart().defineInRange("Maximum Length", DEFAULT_ROOTS_MAX_LENGTH, 1, 32);
			NETHER_SHINERITE_ROOTS_IMBUED_CHANCE = builder.comment("Chance that a generated nether root segment is naturally imbued with random effects. Lower = rarer").worldRestart().defineInRange("Imbued Chance", 0.03, 0.0, 1.0);
		} else {
			SHINERITE_ROOTS = builder.comment("Enable the hanging Shinerite roots feature in the overworld").worldRestart().define("Enabled", true);
			SHINERITE_ROOTS_CHANCE = builder.comment("Chance that a root strand actually generates when the feature is attempted. Lower = rarer").worldRestart().defineInRange("Generation Chance", 0.35, 0.0, 1.0);
			SHINERITE_ROOTS_MIN_LENGTH = builder.comment("Minimum number of blocks in a generated root strand").worldRestart().defineInRange("Minimum Length", DEFAULT_ROOTS_MIN_LENGTH, 1, 32);
			SHINERITE_ROOTS_MAX_LENGTH = builder.comment("Maximum number of blocks in a generated root strand").worldRestart().defineInRange("Maximum Length", DEFAULT_ROOTS_MAX_LENGTH, 1, 32);
			SHINERITE_ROOTS_IMBUED_CHANCE = builder.comment("Chance that a generated root segment is naturally imbued with random effects. Lower = rarer").worldRestart().defineInRange("Imbued Chance", 0.03, 0.0, 1.0);
		}
		builder.pop();
	}

	private static void pushColoredOre(ForgeConfigSpec.Builder builder, String section, boolean nether) {
		builder.push(section);
		if (nether) {
			NETHER_SHINERITE_COLORED_ORE = builder.comment("Enable the colored Shinerite ore blobs feature in the nether").worldRestart().define("Enabled", true);
			NETHER_SHINERITE_COLORED_ORE_CHANCE = builder.comment("Chance that a nether colored ore blob actually generates when the feature is attempted. Lower = rarer").worldRestart().defineInRange("Generation Chance", 0.4, 0.0, 1.0);
			NETHER_SHINERITE_COLORED_ORE_MIN_SIZE = builder.comment("Minimum number of blocks replaced by a generated nether colored ore blob").worldRestart().defineInRange("Minimum Blob Size", DEFAULT_ORE_MIN_SIZE, 1, 64);
			NETHER_SHINERITE_COLORED_ORE_MAX_SIZE = builder.comment("Maximum number of blocks replaced by a generated nether colored ore blob").worldRestart().defineInRange("Maximum Blob Size", DEFAULT_ORE_MAX_SIZE, 1, 64);
			NETHER_SHINERITE_COLORED_ORE_SPREAD = builder.comment("How far from the origin a block of a nether colored ore blob can be placed, in blocks. 0 = a single block").worldRestart().defineInRange("Spread Radius", DEFAULT_ORE_SPREAD, 0, 8);
		} else {
			SHINERITE_COLORED_ORE = builder.comment("Enable the colored Shinerite ore blobs feature in the overworld").worldRestart().define("Enabled", true);
			SHINERITE_COLORED_ORE_CHANCE = builder.comment("Chance that a colored ore blob actually generates when the feature is attempted. Lower = rarer").worldRestart().defineInRange("Generation Chance", 0.4, 0.0, 1.0);
			SHINERITE_COLORED_ORE_MIN_SIZE = builder.comment("Minimum number of blocks replaced by a generated colored ore blob").worldRestart().defineInRange("Minimum Blob Size", DEFAULT_ORE_MIN_SIZE, 1, 64);
			SHINERITE_COLORED_ORE_MAX_SIZE = builder.comment("Maximum number of blocks replaced by a generated colored ore blob").worldRestart().defineInRange("Maximum Blob Size", DEFAULT_ORE_MAX_SIZE, 1, 64);
			SHINERITE_COLORED_ORE_SPREAD = builder.comment("How far from the origin a block of a colored ore blob can be placed, in blocks. 0 = a single block").worldRestart().defineInRange("Spread Radius", DEFAULT_ORE_SPREAD, 0, 8);
		}
		builder.pop();
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