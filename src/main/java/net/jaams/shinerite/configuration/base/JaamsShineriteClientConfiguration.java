package net.jaams.shinerite.configuration.base;

import net.minecraftforge.common.ForgeConfigSpec;

import net.jaams.shinerite.configuration.client.TooltipsConfig;
import net.jaams.shinerite.configuration.client.ProjectileClientConfig;
import net.jaams.shinerite.configuration.client.ItemStatusBarConfig;
import net.jaams.shinerite.configuration.client.CreativeTabConfig;
import net.jaams.shinerite.configuration.client.AssortedClientConfig;

public class JaamsShineriteClientConfiguration {
	public static final ForgeConfigSpec SPEC;
	static {
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		AssortedClientConfig.register(builder);
		CreativeTabConfig.register(builder);
		ItemStatusBarConfig.register(builder);
		ProjectileClientConfig.register(builder);
		TooltipsConfig.register(builder);
		SPEC = builder.build();
	}
}
