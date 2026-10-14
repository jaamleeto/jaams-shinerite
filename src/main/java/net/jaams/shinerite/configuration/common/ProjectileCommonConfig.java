package net.jaams.shinerite.configuration.common;

import net.minecraftforge.common.ForgeConfigSpec;

public class ProjectileCommonConfig {
	// Shinemerang projectile
	public static ForgeConfigSpec.DoubleValue SHINEMERANG_PROJECTILE_BASE_DAMAGE;
	public static ForgeConfigSpec.DoubleValue SHINEMERANG_PROJECTILE_BASE_KNOCKBACK;
	public static ForgeConfigSpec.IntValue SHINEMERANG_PROJECTILE_PIERCING_LEVEL;
	public static ForgeConfigSpec.IntValue SHINEMERANG_PROJECTILE_IGNORE_TICKS;
	public static ForgeConfigSpec.BooleanValue SHINEMERANG_PROJECTILE_ALLOW_CRITICALS;
	public static ForgeConfigSpec.DoubleValue SHINEMERANG_PROJECTILE_WATER_INERTIA;
	public static ForgeConfigSpec.DoubleValue SHINEMERANG_PROJECTILE_HITBOX_WIDTH;
	public static ForgeConfigSpec.DoubleValue SHINEMERANG_PROJECTILE_HITBOX_HEIGHT;
	public static ForgeConfigSpec.BooleanValue SHINEMERANG_PROJECTILE_DESPAWN_AS_ITEM;
	public static ForgeConfigSpec.IntValue SHINEMERANG_PROJECTILE_DESPAWN_TIME;
	public static ForgeConfigSpec.BooleanValue SHINEMERANG_PROJECTILE_SWOOSH_SOUND;
	public static ForgeConfigSpec.IntValue SHINEMERANG_PROJECTILE_SWOOSH_INTERVAL;
	// Shinerite Star projectile 
	public static ForgeConfigSpec.DoubleValue SHINERITE_STAR_PROJECTILE_BASE_DAMAGE;
	public static ForgeConfigSpec.DoubleValue SHINERITE_STAR_PROJECTILE_BASE_KNOCKBACK;
	public static ForgeConfigSpec.IntValue SHINERITE_STAR_PROJECTILE_PIERCING_LEVEL;
	public static ForgeConfigSpec.IntValue SHINERITE_STAR_PROJECTILE_IGNORE_TICKS;
	public static ForgeConfigSpec.BooleanValue SHINERITE_STAR_PROJECTILE_ALLOW_CRITICALS;
	public static ForgeConfigSpec.DoubleValue SHINERITE_STAR_PROJECTILE_WATER_INERTIA;
	public static ForgeConfigSpec.DoubleValue SHINERITE_STAR_PROJECTILE_HITBOX_WIDTH;
	public static ForgeConfigSpec.DoubleValue SHINERITE_STAR_PROJECTILE_HITBOX_HEIGHT;
	public static ForgeConfigSpec.BooleanValue SHINERITE_STAR_PROJECTILE_DESPAWN_AS_ITEM;
	public static ForgeConfigSpec.IntValue SHINERITE_STAR_PROJECTILE_DESPAWN_TIME;

	public static void register(ForgeConfigSpec.Builder builder) {
		builder.push("Projectile Common Handler");
		// Shinemerang Projectile
		builder.push("Shinemerang Projectile");
		SHINEMERANG_PROJECTILE_BASE_DAMAGE = builder.comment("Base damage dealt by the Shinemerang projectile").defineInRange("Base Damage", 2.0, 0.5, 64.0);
		SHINEMERANG_PROJECTILE_BASE_KNOCKBACK = builder.comment("Base knockback strength applied by the Shinemerang projectile to hit entities").defineInRange("Base Knockback", 0.4, 0.0, 4.0);
		SHINEMERANG_PROJECTILE_PIERCING_LEVEL = builder.comment("Number of additional entities the Shinemerang projectile can pierce through").defineInRange("Piercing Level", 0, 0, 72000);
		SHINEMERANG_PROJECTILE_IGNORE_TICKS = builder.comment("Number of initial ticks during which the Shinemerang projectile ignores collisions and damage").defineInRange("Ignore Ticks", 10, 0, 72000);
		SHINEMERANG_PROJECTILE_ALLOW_CRITICALS = builder.comment("Whether the Shinemerang projectile can deal critical hits").define("Critical Hits", true);
		SHINEMERANG_PROJECTILE_WATER_INERTIA = builder.comment("Water inertia factor for the Shinemerang projectile").defineInRange("Water Inertia", 0.6, 0.0, 1.0);
		SHINEMERANG_PROJECTILE_HITBOX_WIDTH = builder.comment("Hitbox width of the Shinemerang projectile").defineInRange("Hitbox Width", 0.6, 0.1, 2.0);
		SHINEMERANG_PROJECTILE_HITBOX_HEIGHT = builder.comment("Hitbox height of the Shinemerang projectile").defineInRange("Hitbox Height", 0.6, 0.1, 2.0);
		SHINEMERANG_PROJECTILE_DESPAWN_AS_ITEM = builder.comment("Whether the Shinemerang projectile drops as an item when it despawns").define("Despawn As Item", false);
		SHINEMERANG_PROJECTILE_DESPAWN_TIME = builder.comment("Despawn time in ticks for the Shinemerang projectile").defineInRange("Despawn Time", 1200, 100, 72000);
		SHINEMERANG_PROJECTILE_SWOOSH_SOUND = builder.comment("Whether the Shinemerang projectile plays a swoosh sound while flying").define("Swoosh Sound", true);
		SHINEMERANG_PROJECTILE_SWOOSH_INTERVAL = builder.comment("Number of ticks between each swoosh sound played by the Shinemerang projectile").defineInRange("Swoosh Interval", 5, 1, 200);
		builder.pop();
		// Shinerite Star Projectile
		builder.push("Shinerite Star Projectile");
		SHINERITE_STAR_PROJECTILE_BASE_DAMAGE = builder.comment("Base damage dealt by the Shinerite Star projectile").defineInRange("Base Damage", 1.5, 0.5, 32.0);
		SHINERITE_STAR_PROJECTILE_BASE_KNOCKBACK = builder.comment("Base knockback strength applied by the Shinerite Star projectile to hit entities").defineInRange("Base Knockback", 0.3, 0.0, 3.0);
		SHINERITE_STAR_PROJECTILE_PIERCING_LEVEL = builder.comment("Number of additional entities the Shinerite Star projectile can pierce through").defineInRange("Piercing Level", 0, 0, 72000);
		SHINERITE_STAR_PROJECTILE_IGNORE_TICKS = builder.comment("Number of initial ticks during which the Shinerite Star projectile ignores collisions and damage").defineInRange("Ignore Ticks", 10, 0, 72000);
		SHINERITE_STAR_PROJECTILE_ALLOW_CRITICALS = builder.comment("Whether the Shinerite Star projectile can deal critical hits").define("Critical Hits", true);
		SHINERITE_STAR_PROJECTILE_WATER_INERTIA = builder.comment("Water inertia factor for the Shinerite Star projectile").defineInRange("Water Inertia", 0.4, 0.0, 1.0);
		SHINERITE_STAR_PROJECTILE_HITBOX_WIDTH = builder.comment("Hitbox width of the Shinerite Star projectile").defineInRange("Hitbox Width", 0.25, 0.1, 1.0);
		SHINERITE_STAR_PROJECTILE_HITBOX_HEIGHT = builder.comment("Hitbox height of the Shinerite Star projectile").defineInRange("Hitbox Height", 0.25, 0.1, 1.0);
		SHINERITE_STAR_PROJECTILE_DESPAWN_AS_ITEM = builder.comment("Whether the Shinerite Star projectile drops as an item when it despawns").define("Despawn As Item", false);
		SHINERITE_STAR_PROJECTILE_DESPAWN_TIME = builder.comment("Despawn time in ticks for the Shinerite Star projectile").defineInRange("Despawn Time", 800, 100, 72000);
		builder.pop();
		builder.pop();
	}
}
