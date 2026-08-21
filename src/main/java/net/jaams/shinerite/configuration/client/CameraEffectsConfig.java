package net.jaams.shinerite.configuration.client;

import net.minecraftforge.common.ForgeConfigSpec;

public class CameraEffectsConfig {
	public static ForgeConfigSpec.BooleanValue FOV_MODIFIER;

	public static void register(ForgeConfigSpec.Builder builder) {
		// Camera Effects Handler
		builder.push("Camera Effects Handler");
		builder.push("Fov Modifier");
		FOV_MODIFIER = builder.comment("Enable or disable FOV modifier").define("Fov Modifier", true);
		builder.pop();
		builder.pop();
	}
}
