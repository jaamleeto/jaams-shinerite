package net.jaams.shinerite.configuration.client;

import net.minecraftforge.common.ForgeConfigSpec;

public class ItemStatusBarConfig {
	public static ForgeConfigSpec.BooleanValue SHOW_IMBUED_BAR;
	public static ForgeConfigSpec.IntValue IMBUED_BAR_X;
	public static ForgeConfigSpec.IntValue IMBUED_BAR_Y;

	public static void register(ForgeConfigSpec.Builder builder) {
		builder.push("Item Status Bars Handler");
		builder.push("Imbued Bar Settings");
		SHOW_IMBUED_BAR = builder.comment("Show or hide the Imbued status bar").define("Show Imbued Bar", true);
		IMBUED_BAR_X = builder.comment("X offset of the Imbued bar").defineInRange("Bar X Offset", 2, -20, 200);
		IMBUED_BAR_Y = builder.comment("Y offset of the Imbued bar").defineInRange("Bar Y Offset", 13, -20, 200);
		builder.pop();
		builder.pop();
	}
}
