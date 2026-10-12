package net.jaams.shinerite.configuration.base;

import net.minecraftforge.common.ForgeConfigSpec;

import net.jaams.shinerite.configuration.common.AssortedCommonConfig;
import net.jaams.shinerite.configuration.common.TraitsConfig;
import net.jaams.shinerite.configuration.common.MobBehaviorConfig;
import net.jaams.shinerite.configuration.common.RecipesConfig;
import net.jaams.shinerite.configuration.common.ProjectileCommonConfig;
import net.jaams.shinerite.configuration.common.MechanicsConfig;
import net.jaams.shinerite.configuration.common.InteractionsConfig;
import net.jaams.shinerite.configuration.common.WorldGenConfig;

public class JaamsShineriteCommonConfiguration {
	public static final ForgeConfigSpec SPEC;
	static {
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		AssortedCommonConfig.register(builder);
		InteractionsConfig.register(builder);
		MechanicsConfig.register(builder);
		ProjectileCommonConfig.register(builder);
		RecipesConfig.register(builder);
		TraitsConfig.register(builder);
		MobBehaviorConfig.register(builder);
		WorldGenConfig.register(builder);
		SPEC = builder.build();
	}
}
