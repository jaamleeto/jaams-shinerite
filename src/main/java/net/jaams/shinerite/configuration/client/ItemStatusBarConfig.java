package net.jaams.shinerite.configuration.client;

import net.minecraftforge.common.ForgeConfigSpec;

public class ItemStatusBarConfig {
	public static ForgeConfigSpec.BooleanValue IMBUED_BAR;
	public static ForgeConfigSpec.IntValue IMBUED_BAR_POS_X;
	public static ForgeConfigSpec.IntValue IMBUED_BAR_POS_Y;

	public static void register(ForgeConfigSpec.Builder builder) {
		builder.push("Item Status Bar Handler");
		builder.push("Imbued Item Bar");
		IMBUED_BAR = builder.comment("Enable or disable the imbued item status bar overlay").define("Imbued Bar", true);
		IMBUED_BAR_POS_X = builder.comment("Horizontal position of the imbued item status bar").defineInRange("Imbued Bar X Pos", 2, -20, 200);
		IMBUED_BAR_POS_Y = builder.comment("Vertical position of the imbued item status bar").defineInRange("Imbued Bar Y Pos", 13, -20, 200);
		builder.pop();
		builder.pop();
	}
}
