package net.jaams.shinerite.configuration.client;

import net.minecraftforge.common.ForgeConfigSpec;

public class CreativeTabConfig {
	public static ForgeConfigSpec.ConfigValue<String> CREATIVE_TAB_ICON;
	public static ForgeConfigSpec.BooleanValue ADD_ITEMS_TO_VANILLA_TABS;

	public static void register(ForgeConfigSpec.Builder builder) {
		builder.push("Creative Tab Handler");
		CREATIVE_TAB_ICON = builder.comment("The item to use as the creative tab icon (requires restart)").worldRestart().define("Creative Tab Icon", "jaams_shinerite:shinerite_ingot");
		ADD_ITEMS_TO_VANILLA_TABS = builder.comment("Add Shinerite items to the vanilla creative tabs (requires restart)").worldRestart().define("Add Items To Vanilla Tabs", false);
		builder.pop();
	}
}
