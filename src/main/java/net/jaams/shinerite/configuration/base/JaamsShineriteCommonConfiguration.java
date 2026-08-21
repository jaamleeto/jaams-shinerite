package net.jaams.shinerite.configuration.base;

import net.minecraftforge.common.ForgeConfigSpec;

import net.jaams.shinerite.configuration.common.TraitsConfig;
import net.jaams.shinerite.configuration.common.RecipesConfig;
import net.jaams.shinerite.configuration.common.ProjectileCommonConfig;
import net.jaams.shinerite.configuration.common.MechanicsConfig;
import net.jaams.shinerite.configuration.common.InteractionsConfig;

public class JaamsShineriteCommonConfiguration {
	public static final ForgeConfigSpec SPEC;
	static {
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		InteractionsConfig.register(builder);
		MechanicsConfig.register(builder);
		ProjectileCommonConfig.register(builder);
		RecipesConfig.register(builder);
		TraitsConfig.register(builder);
		SPEC = builder.build();
	}
}
