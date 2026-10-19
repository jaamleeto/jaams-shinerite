package net.jaams.shinerite.configuration.common;

import net.minecraftforge.common.ForgeConfigSpec;

import net.minecraft.world.item.UseAnim;

import net.jaams.shinerite.util.ModEnums;

public class ThrowableConfig {
	// Shinerang
	public static ForgeConfigSpec.BooleanValue THROWABLE_SHINERANG;
	public static ForgeConfigSpec.IntValue THROWABLE_SHINERANG_MIN_CHARGE;
	public static ForgeConfigSpec.IntValue THROWABLE_SHINERANG_MAX_CHARGE;
	public static ForgeConfigSpec.DoubleValue THROWABLE_SHINERANG_MIN_SPEED;
	public static ForgeConfigSpec.DoubleValue THROWABLE_SHINERANG_MAX_SPEED;
	public static ForgeConfigSpec.DoubleValue THROWABLE_SHINERANG_INACCURACY;
	public static ForgeConfigSpec.DoubleValue THROWABLE_SHINERANG_CRITICAL_POWER_THRESHOLD;
	public static ForgeConfigSpec.BooleanValue THROWABLE_SHINERANG_REMEMBER_SLOT;
	public static ForgeConfigSpec.IntValue THROWABLE_SHINERANG_DAMAGE_AMOUNT_ON_THROW;
	public static ForgeConfigSpec.BooleanValue THROWABLE_SHINERANG_ALLOW_MULTISHOT;
	public static ForgeConfigSpec.DoubleValue THROWABLE_SHINERANG_MULTISHOT_SPREAD_ANGLE;
	public static ForgeConfigSpec.DoubleValue THROWABLE_SHINERANG_RECOIL;
	public static ForgeConfigSpec.DoubleValue THROWABLE_SHINERANG_RECOIL_VERTICAL_MULTIPLIER;
	public static ForgeConfigSpec.DoubleValue THROWABLE_SHINERANG_RECOIL_CROUCH_REDUCTION;
	public static ForgeConfigSpec.DoubleValue THROWABLE_SHINERANG_RECOIL_PITCH_KICK;
	public static ForgeConfigSpec.BooleanValue THROWABLE_SHINERANG_RECOIL_ONLY_FULLY_CHARGED;
	public static ForgeConfigSpec.EnumValue<ModEnums.ThrowMode> THROWABLE_SHINERANG_THROW_MODE;
	public static ForgeConfigSpec.IntValue THROWABLE_SHINERANG_COOLDOWN_TICKS_INSTANT;
	public static ForgeConfigSpec.IntValue THROWABLE_SHINERANG_USE_DURATION_TICKS;
	public static ForgeConfigSpec.IntValue THROWABLE_SHINERANG_MIN_REMAINING_DURABILITY;
	public static ForgeConfigSpec.EnumValue<UseAnim> THROWABLE_SHINERANG_USE_ANIMATION;
	// Shinerite Star 
	public static ForgeConfigSpec.BooleanValue THROWABLE_SHINERITE_STAR;
	public static ForgeConfigSpec.IntValue THROWABLE_SHINERITE_STAR_MIN_CHARGE;
	public static ForgeConfigSpec.IntValue THROWABLE_SHINERITE_STAR_MAX_CHARGE;
	public static ForgeConfigSpec.DoubleValue THROWABLE_SHINERITE_STAR_MIN_SPEED;
	public static ForgeConfigSpec.DoubleValue THROWABLE_SHINERITE_STAR_MAX_SPEED;
	public static ForgeConfigSpec.DoubleValue THROWABLE_SHINERITE_STAR_INACCURACY;
	public static ForgeConfigSpec.DoubleValue THROWABLE_SHINERITE_STAR_CRITICAL_POWER_THRESHOLD;
	public static ForgeConfigSpec.BooleanValue THROWABLE_SHINERITE_STAR_REMEMBER_SLOT;
	public static ForgeConfigSpec.IntValue THROWABLE_SHINERITE_STAR_DAMAGE_AMOUNT_ON_THROW;
	public static ForgeConfigSpec.BooleanValue THROWABLE_SHINERITE_STAR_ALLOW_MULTISHOT;
	public static ForgeConfigSpec.DoubleValue THROWABLE_SHINERITE_STAR_MULTISHOT_SPREAD_ANGLE;
	public static ForgeConfigSpec.DoubleValue THROWABLE_SHINERITE_STAR_RECOIL;
	public static ForgeConfigSpec.DoubleValue THROWABLE_SHINERITE_STAR_RECOIL_VERTICAL_MULTIPLIER;
	public static ForgeConfigSpec.DoubleValue THROWABLE_SHINERITE_STAR_RECOIL_CROUCH_REDUCTION;
	public static ForgeConfigSpec.DoubleValue THROWABLE_SHINERITE_STAR_RECOIL_PITCH_KICK;
	public static ForgeConfigSpec.BooleanValue THROWABLE_SHINERITE_STAR_RECOIL_ONLY_FULLY_CHARGED;
	public static ForgeConfigSpec.EnumValue<ModEnums.ThrowMode> THROWABLE_SHINERITE_STAR_THROW_MODE;
	public static ForgeConfigSpec.IntValue THROWABLE_SHINERITE_STAR_COOLDOWN_TICKS_INSTANT;
	public static ForgeConfigSpec.IntValue THROWABLE_SHINERITE_STAR_USE_DURATION_TICKS;
	public static ForgeConfigSpec.IntValue THROWABLE_SHINERITE_STAR_MIN_REMAINING_DURABILITY;
	public static ForgeConfigSpec.EnumValue<UseAnim> THROWABLE_SHINERITE_STAR_USE_ANIMATION;

	public static void register(ForgeConfigSpec.Builder builder) {
		builder.push("Shinerang");
		THROWABLE_SHINERANG = builder.comment("Whether the Shinerang can be thrown").define("Allow Throw", true);
		THROWABLE_SHINERANG_MIN_CHARGE = builder.comment("Minimum ticks required to start charging the Shinerang throw").defineInRange("Min Charge Time", 5, 1, 40);
		THROWABLE_SHINERANG_MAX_CHARGE = builder.comment("Maximum charge time for full power throw of the Shinerang").defineInRange("Max Charge Time", 20, 5, 60);
		THROWABLE_SHINERANG_MIN_SPEED = builder.comment("Projectile speed of the Shinerang at minimum charge").defineInRange("Min Speed", 0.5, 0.1, 2.0);
		THROWABLE_SHINERANG_MAX_SPEED = builder.comment("Projectile speed of the Shinerang at full charge").defineInRange("Max Speed", 1.8, 0.5, 4.0);
		THROWABLE_SHINERANG_INACCURACY = builder.comment("Inaccuracy/spread in degrees for the Shinerang throw (0 = perfect aim)").defineInRange("Inaccuracy", 1.0, 0.0, 50.0);
		THROWABLE_SHINERANG_CRITICAL_POWER_THRESHOLD = builder.comment("Charge percentage required for critical effects on the Shinerang throw").defineInRange("Critical Power Threshold", 0.85, 0.0, 1.0);
		THROWABLE_SHINERANG_REMEMBER_SLOT = builder.comment("Whether the Shinerang returns to the original hotbar slot after being thrown").define("Remember Original Slot", true);
		THROWABLE_SHINERANG_DAMAGE_AMOUNT_ON_THROW = builder.comment("Durability damage the Shinerang takes each time it is thrown").defineInRange("Item Damage Amount", 1, 0, 10);
		THROWABLE_SHINERANG_ALLOW_MULTISHOT = builder.comment("Whether the Multishot enchantment can be applied to the Shinerang").define("Multishot Enchantment", true);
		THROWABLE_SHINERANG_MULTISHOT_SPREAD_ANGLE = builder.comment("Spread angle in degrees for each extra projectile when using Multishot on the Shinerang").defineInRange("Multishot Spread Angle", 10.0, 0.0, 45.0);
		THROWABLE_SHINERANG_RECOIL = builder.comment("Horizontal recoil strength when throwing the Shinerang").defineInRange("Recoil", 0.0, 0.0, 10.0);
		THROWABLE_SHINERANG_RECOIL_VERTICAL_MULTIPLIER = builder.comment("Vertical multiplier applied to the recoil when throwing the Shinerang").defineInRange("Recoil Vertical Multiplier", 0.0, 0.0, 10.0);
		THROWABLE_SHINERANG_RECOIL_CROUCH_REDUCTION = builder.comment("Recoil reduction multiplier when crouching while throwing the Shinerang").defineInRange("Recoil Crouch Reduction", 0.0, 0.0, 1.0);
		THROWABLE_SHINERANG_RECOIL_PITCH_KICK = builder.comment("Additional upward pitch kick applied from recoil when throwing the Shinerang").defineInRange("Recoil Pitch Kick", 0.0, 0.0, 10.0);
		THROWABLE_SHINERANG_RECOIL_ONLY_FULLY_CHARGED = builder.comment("Whether recoil is only applied on fully charged throws of the Shinerang").define("Recoil Fully Charged", false);
		THROWABLE_SHINERANG_THROW_MODE = builder.comment("Throw mechanic style used for the Shinerang").defineEnum("Throw Mode", ModEnums.ThrowMode.CHARGE_AND_RELEASE);
		THROWABLE_SHINERANG_COOLDOWN_TICKS_INSTANT = builder.comment("Cooldown in ticks after an instant throw of the Shinerang").defineInRange("Instant Throw Cooldown Ticks", 20, 0, 200);
		THROWABLE_SHINERANG_USE_DURATION_TICKS = builder.comment("Maximum use duration in ticks for charging the Shinerang throw").defineInRange("Use Duration Ticks", 72000, 20, 72000);
		THROWABLE_SHINERANG_MIN_REMAINING_DURABILITY = builder.comment("Minimum remaining durability required to throw the Shinerang").defineInRange("Min Durability Throw", -1, -1, 100);
		THROWABLE_SHINERANG_USE_ANIMATION = builder.comment("Item use animation played when charging or throwing the Shinerang").defineEnum("Use Animation", UseAnim.SPEAR);
		builder.pop();
		builder.push("Shinerite Star");
		THROWABLE_SHINERITE_STAR = builder.comment("Whether the Shinerite Star can be thrown").define("Allow Throw", true);
		THROWABLE_SHINERITE_STAR_MIN_CHARGE = builder.comment("Minimum ticks required to start charging the Shinerite Star throw").defineInRange("Min Charge Time", 5, 1, 40);
		THROWABLE_SHINERITE_STAR_MAX_CHARGE = builder.comment("Maximum charge time for full power throw of the Shinerite Star").defineInRange("Max Charge Time", 20, 5, 60);
		THROWABLE_SHINERITE_STAR_MIN_SPEED = builder.comment("Projectile speed of the Shinerite Star at minimum charge").defineInRange("Min Speed", 0.5, 0.1, 2.0);
		THROWABLE_SHINERITE_STAR_MAX_SPEED = builder.comment("Projectile speed of the Shinerite Star at full charge").defineInRange("Max Speed", 1.8, 0.5, 4.0);
		THROWABLE_SHINERITE_STAR_INACCURACY = builder.comment("Inaccuracy/spread in degrees for the Shinerite Star throw (0 = perfect aim)").defineInRange("Inaccuracy", 1.0, 0.0, 50.0);
		THROWABLE_SHINERITE_STAR_CRITICAL_POWER_THRESHOLD = builder.comment("Charge percentage required for critical effects on the Shinerite Star throw").defineInRange("Critical Power Threshold", 0.85, 0.0, 1.0);
		THROWABLE_SHINERITE_STAR_REMEMBER_SLOT = builder.comment("Whether the Shinerite Star returns to the original hotbar slot after being thrown").define("Remember Original Slot", true);
		THROWABLE_SHINERITE_STAR_DAMAGE_AMOUNT_ON_THROW = builder.comment("Durability damage the Shinerite Star takes each time it is thrown").defineInRange("Item Damage Amount", 1, 0, 10);
		THROWABLE_SHINERITE_STAR_ALLOW_MULTISHOT = builder.comment("Whether the Multishot enchantment can be applied to the Shinerite Star").define("Multishot Enchantment", true);
		THROWABLE_SHINERITE_STAR_MULTISHOT_SPREAD_ANGLE = builder.comment("Spread angle in degrees for each extra projectile when using Multishot on the Shinerite Star").defineInRange("Multishot Spread Angle", 10.0, 0.0, 45.0);
		THROWABLE_SHINERITE_STAR_RECOIL = builder.comment("Horizontal recoil strength when throwing the Shinerite Star").defineInRange("Recoil", 0.0, 0.0, 10.0);
		THROWABLE_SHINERITE_STAR_RECOIL_VERTICAL_MULTIPLIER = builder.comment("Vertical multiplier applied to the recoil when throwing the Shinerite Star").defineInRange("Recoil Vertical Multiplier", 0.0, 0.0, 10.0);
		THROWABLE_SHINERITE_STAR_RECOIL_CROUCH_REDUCTION = builder.comment("Recoil reduction multiplier when crouching while throwing the Shinerite Star").defineInRange("Recoil Crouch Reduction", 0.0, 0.0, 1.0);
		THROWABLE_SHINERITE_STAR_RECOIL_PITCH_KICK = builder.comment("Additional upward pitch kick applied from recoil when throwing the Shinerite Star").defineInRange("Recoil Pitch Kick", 0.0, 0.0, 10.0);
		THROWABLE_SHINERITE_STAR_RECOIL_ONLY_FULLY_CHARGED = builder.comment("Whether recoil is only applied on fully charged throws of the Shinerite Star").define("Recoil Fully Charged", false);
		THROWABLE_SHINERITE_STAR_THROW_MODE = builder.comment("Throw mechanic style used for the Shinerite Star").defineEnum("Throw Mode", ModEnums.ThrowMode.CHARGE_AND_RELEASE);
		THROWABLE_SHINERITE_STAR_COOLDOWN_TICKS_INSTANT = builder.comment("Cooldown in ticks after an instant throw of the Shinerite Star").defineInRange("Instant Throw Cooldown Ticks", 20, 0, 200);
		THROWABLE_SHINERITE_STAR_USE_DURATION_TICKS = builder.comment("Maximum use duration in ticks for charging the Shinerite Star throw").defineInRange("Use Duration Ticks", 72000, 20, 72000);
		THROWABLE_SHINERITE_STAR_MIN_REMAINING_DURABILITY = builder.comment("Minimum remaining durability required to throw the Shinerite Star").defineInRange("Min Durability Throw", -1, -1, 100);
		THROWABLE_SHINERITE_STAR_USE_ANIMATION = builder.comment("Item use animation played when charging or throwing the Shinerite Star").defineEnum("Use Animation", UseAnim.SPEAR);
		builder.pop();
	}
}
