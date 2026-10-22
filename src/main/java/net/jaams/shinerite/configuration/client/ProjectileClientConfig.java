package net.jaams.shinerite.configuration.client;

import net.minecraftforge.common.ForgeConfigSpec;

public class ProjectileClientConfig {
	public static ForgeConfigSpec.DoubleValue SHINERANG_PROJECTILE_SCALE;
	public static ForgeConfigSpec.BooleanValue SHINERANG_PROJECTILE_TRAIL;
	public static ForgeConfigSpec.IntValue SHINERANG_PROJECTILE_TRAIL_SPAWN_RATE;
	public static ForgeConfigSpec.DoubleValue SHINERITE_STAR_PROJECTILE_SCALE;
	public static ForgeConfigSpec.BooleanValue SHINERITE_STAR_PROJECTILE_TRAIL;
	public static ForgeConfigSpec.IntValue SHINERITE_STAR_PROJECTILE_TRAIL_SPAWN_RATE;

	public static void register(ForgeConfigSpec.Builder builder) {
		builder.push("Projectile Client Handler");
		builder.push("Shinerang Projectile");
		SHINERANG_PROJECTILE_SCALE = builder.comment("Visual size scale of the Shinerang projectile").defineInRange("Projectile Visual Size", 1.9, 0.1, 10.0);
		SHINERANG_PROJECTILE_TRAIL = builder.comment("Enable or disable trail particles for Shinerang projectile").define("Projectile Trail", true);
		SHINERANG_PROJECTILE_TRAIL_SPAWN_RATE = builder.comment("Shinerang projectile trail particle spawn frequency").defineInRange("Projectile Trail Spawn Rate", 2, 1, 20);
		builder.pop();
		builder.push("Shinerite Star Projectile");
		SHINERITE_STAR_PROJECTILE_SCALE = builder.comment("Visual size scale of the Shinerite Star projectile").defineInRange("Projectile Visual Size", 1.5, 0.1, 10.0);
		SHINERITE_STAR_PROJECTILE_TRAIL = builder.comment("Enable or disable trail particles for Shinerite Star projectile").define("Projectile Trail", true);
		SHINERITE_STAR_PROJECTILE_TRAIL_SPAWN_RATE = builder.comment("Shinerite Star projectile trail particle spawn frequency").defineInRange("Projectile Trail Spawn Rate", 3, 1, 20);
		builder.pop();
		builder.pop();
	}
}
