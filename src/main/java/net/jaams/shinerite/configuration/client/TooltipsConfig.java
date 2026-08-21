package net.jaams.shinerite.configuration.client;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class TooltipsConfig {
	// Tooltips System 
	public static ForgeConfigSpec.BooleanValue TOOLTIPS;
	public static ForgeConfigSpec.BooleanValue CONTROL_TOOLTIPS;
	public static ForgeConfigSpec.BooleanValue ALT_TOOLTIPS;
	public static ForgeConfigSpec.ConfigValue<List<? extends String>> EXCLUDED_TOOLTIPS_ITEMS;
	public static ForgeConfigSpec.ConfigValue<List<? extends String>> EXCLUDED_CONTROL_TOOLTIPS_ITEMS;
	public static ForgeConfigSpec.ConfigValue<List<? extends String>> EXCLUDED_ALT_TOOLTIPS_ITEMS;
	// Dyeing
	public static ForgeConfigSpec.BooleanValue TOOLTIP_DYED_STATUS;
	public static ForgeConfigSpec.BooleanValue TOOLTIP_COLOR_HEX_COLORED;
	// Waxing
	public static ForgeConfigSpec.BooleanValue TOOLTIP_WAXED_STATUS;
	public static ForgeConfigSpec.BooleanValue TOOLTIP_WAXEABLE_TRAIT;
	public static ForgeConfigSpec.BooleanValue TOOLTIP_WAXEABLE_TRAIT_ADVANCED_ONLY;
	// Imbuing
	public static ForgeConfigSpec.BooleanValue TOOLTIP_IMBUABLE_TRAIT;
	public static ForgeConfigSpec.BooleanValue TOOLTIP_IMBUABLE_TRAIT_ADVANCED_ONLY;
	public static ForgeConfigSpec.BooleanValue TOOLTIP_IMBUED_EFFECTS;
	public static ForgeConfigSpec.BooleanValue TOOLTIP_IMBUED_USES;
	// Throwing
	public static ForgeConfigSpec.BooleanValue TOOLTIP_THROWING_PROPERTIES;
	public static ForgeConfigSpec.BooleanValue TOOLTIP_THROWING_MODE;
	public static ForgeConfigSpec.BooleanValue TOOLTIP_THROWBACK_PROPERTIES;
	public static ForgeConfigSpec.BooleanValue TOOLTIP_COLLECTOR_PROPERTIES;
	// Frailglow
	public static ForgeConfigSpec.BooleanValue TOOLTIP_FRAILGLOW_PROPERTIES;

	public static void register(ForgeConfigSpec.Builder builder) {
		// Tooltips System Handler
		builder.push("Tooltips System Handler");
		builder.push("Tooltips Base Settings");
		TOOLTIPS = builder.comment("Enable or disable ALL custom tooltips added by the mod").define("Enable Tooltips", true);
		CONTROL_TOOLTIPS = builder.comment("Enable or disable tooltips shown when holding the Control key added by the mod").define("Enable Extra Info Tooltips", true);
		ALT_TOOLTIPS = builder.comment("Enable or disable tooltips shown when holding the Alt key added by the mod").define("Enable Descriptions Tooltips", true);
		EXCLUDED_TOOLTIPS_ITEMS = builder.comment("List of item IDs excluded from ALL custom tooltips added by the mod").defineList("Excluded Tooltips Items", List.of(), obj -> obj instanceof String);
		EXCLUDED_CONTROL_TOOLTIPS_ITEMS = builder.comment("List of item IDs excluded from Control key tooltips added by the mod").defineList("Excluded Extra Info Tooltips Items", List.of(), obj -> obj instanceof String);
		EXCLUDED_ALT_TOOLTIPS_ITEMS = builder.comment("List of item IDs excluded from Alt key tooltips added by the mod").defineList("Excluded Descriptions Tooltips Items", List.of(), obj -> obj instanceof String);
		builder.pop();
		builder.push("Tooltips Misc Settings");
		builder.push("Dyeing Tooltips");
		TOOLTIP_DYED_STATUS = builder.comment("Show 'Dyed' text in normal tooltips when the item has color").define("Show Dyed Status", true);
		TOOLTIP_COLOR_HEX_COLORED = builder.comment("true = The #RRGGBB code is colored with the item's actual color\nfalse = always gray").define("Color Hex With Item Color", true);
		builder.pop();
		builder.push("Waxing Tooltips");
		TOOLTIP_WAXED_STATUS = builder.comment("Show 'Waxed' text when the item is waxed").define("Show Waxed Status", true);
		TOOLTIP_WAXEABLE_TRAIT = builder.comment("Show the 'Waxeable' trait in tooltips").define("Show Waxeable Trait", true);
		TOOLTIP_WAXEABLE_TRAIT_ADVANCED_ONLY = builder.comment("If true: 'Waxeable' trait only appears in advanced tooltips\nIf false: appears in normal tooltips too").define("Waxeable Trait Advanced Only", true);
		builder.pop();
		builder.push("Imbuing Tooltips");
		TOOLTIP_IMBUABLE_TRAIT = builder.comment("Show the 'Imbuable' trait in tooltips").define("Show Imbuable Trait", true);
		TOOLTIP_IMBUABLE_TRAIT_ADVANCED_ONLY = builder.comment("If true: 'Imbuable' trait only appears in advanced tooltips\nIf false: appears in normal tooltips too").define("Imbuable Trait Advanced Only", true);
		TOOLTIP_IMBUED_EFFECTS = builder.comment("Show the detailed list of imbued potion effects").define("Show Imbued Effects List", true);
		TOOLTIP_IMBUED_USES = builder.comment("Show the remaining/max imbued uses counter").define("Show Imbued Uses Counter", true);
		builder.pop();
		builder.pop();
		builder.push("Weapon Traits Tooltips");
		builder.push("Throwable");
		TOOLTIP_THROWING_PROPERTIES = builder.comment("Show the 'Throwing Properties' section in tooltips added by the mod").define("Show Throwing Properties", true);
		TOOLTIP_THROWING_MODE = builder.comment("Show the 'Throwing Mode' section in tooltips added by the mod").define("Show Throwing Mode", true);
		TOOLTIP_THROWBACK_PROPERTIES = builder.comment("Show the 'Throwback' properties section in tooltips").define("Show Throwback Properties", true);
		TOOLTIP_COLLECTOR_PROPERTIES = builder.comment("Show the 'Radiant Collector' properties section in tooltips").define("Show Radiant Collector Properties", true);
		builder.pop();
		builder.push("Frailglow");
		TOOLTIP_FRAILGLOW_PROPERTIES = builder.comment("Show the 'Frailglow Properties' section in tooltips").define("Show Frailglow Properties", true);
		builder.pop();
		builder.pop();
		builder.pop();
	}
}
