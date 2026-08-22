package net.jaams.shinerite.configuration.common;

import net.minecraftforge.common.ForgeConfigSpec;

public class AssortedCommonConfig {
	public static ForgeConfigSpec.BooleanValue BARTERING;
	public static ForgeConfigSpec.DoubleValue BARTERING_BETRAY_CHANCE;
	public static ForgeConfigSpec.BooleanValue PIGLIN_DISGUISE;
	public static ForgeConfigSpec.DoubleValue PIGLIN_DISGUISE_CHANCE_PER_PIECE;

	public static void register(ForgeConfigSpec.Builder builder) {
		builder.push("Assorted Common Handler");
		builder.push("Piglin Shinerite Behavior");
		builder.push("Piglin Bartering");
		BARTERING = builder.comment("Piglins treat yellow-dyed Shinerite Ingots as gold currency for bartering").define("Bartering", true);
		BARTERING_BETRAY_CHANCE = builder.comment("Chance that a Piglin realizes the deception when bartering a yellow-dyed Shinerite Ingot and turns hostile instead of giving items (0 = never, 1 = always)").defineInRange("Betrayal Chance", 0.15, 0.0, 1.0);
		builder.pop();
		builder.push("Piglin Disguise");
		PIGLIN_DISGUISE = builder.comment("Wearing yellow-dyed Shinerite armor can make Piglins treat the wearer as if wearing gold armor").define("Yellow Armor Disguise", true);
		PIGLIN_DISGUISE_CHANCE_PER_PIECE = builder.comment("Chance per yellow-dyed Shinerite armor piece that Piglins ignore the wearer on sight (4 pieces at 0.25 = always disguised)").defineInRange("Disguise Chance per Piece", 0.25, 0.0, 1.0);
		builder.pop();
		builder.pop();
		builder.pop();
	}
}
