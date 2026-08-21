package net.jaams.shinerite.configuration.client;

import net.minecraftforge.common.ForgeConfigSpec;

public class ProjectileClientConfig {
	// Shinemerang
	public static ForgeConfigSpec.DoubleValue SHINEMERANG_PROJECTILE_SCALE;
	public static ForgeConfigSpec.BooleanValue SHINEMERANG_PROJECTILE_TRAIL;
	public static ForgeConfigSpec.IntValue SHINEMERANG_PROJECTILE_TRAIL_SPAWN_RATE;
	// Shinerite Star
	public static ForgeConfigSpec.DoubleValue SHINERITE_STAR_PROJECTILE_SCALE;
	public static ForgeConfigSpec.BooleanValue SHINERITE_STAR_PROJECTILE_TRAIL;
	public static ForgeConfigSpec.IntValue SHINERITE_STAR_PROJECTILE_TRAIL_SPAWN_RATE;

	public static void register(ForgeConfigSpec.Builder builder) {
		builder.push("Projectile Client Handler");
		builder.push("Shinemerang Projectile");
		SHINEMERANG_PROJECTILE_SCALE = builder.comment("Visual size scale of the Shinemerang projectile").defineInRange("Projectile Visual Size", 1.9, 0.1, 10.0);
		SHINEMERANG_PROJECTILE_TRAIL = builder.comment("Enable or disable trail particles for Shinemerang projectile").define("Projectile Trail", true);
		SHINEMERANG_PROJECTILE_TRAIL_SPAWN_RATE = builder.comment("Shinemerang projectile trail particle spawn frequency").defineInRange("Projectile Trail Spawn Rate", 2, 1, 20);
		builder.pop();
		builder.push("Shinerite Star Projectile");
		SHINERITE_STAR_PROJECTILE_SCALE = builder.comment("Visual size scale of the Shinerite Star projectile").defineInRange("Projectile Visual Size", 1.5, 0.1, 10.0);
		SHINERITE_STAR_PROJECTILE_TRAIL = builder.comment("Enable or disable trail particles for Shinerite Star projectile").define("Projectile Trail", true);
		SHINERITE_STAR_PROJECTILE_TRAIL_SPAWN_RATE = builder.comment("Shinerite Star projectile trail particle spawn frequency").defineInRange("Projectile Trail Spawn Rate", 3, 1, 20);
		builder.pop();
		builder.pop();
	}
}
