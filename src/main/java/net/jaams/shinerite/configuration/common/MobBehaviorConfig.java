package net.jaams.shinerite.configuration.common;

import net.minecraftforge.common.ForgeConfigSpec;

public class MobBehaviorConfig {

	public static ForgeConfigSpec.IntValue THROWABLE_MOB_BEHAVIOR_COOLDOWN_MIN_TICKS;
	public static ForgeConfigSpec.IntValue THROWABLE_MOB_BEHAVIOR_COOLDOWN_MAX_TICKS;
	public static ForgeConfigSpec.IntValue THROWABLE_MOB_BEHAVIOR_INITIAL_COOLDOWN_MIN_TICKS;
	public static ForgeConfigSpec.IntValue THROWABLE_MOB_BEHAVIOR_INITIAL_COOLDOWN_MAX_TICKS;
	public static ForgeConfigSpec.DoubleValue THROWABLE_MOB_BEHAVIOR_COOLDOWN_EASY_MULTIPLIER;
	public static ForgeConfigSpec.DoubleValue THROWABLE_MOB_BEHAVIOR_COOLDOWN_NORMAL_MULTIPLIER;
	public static ForgeConfigSpec.DoubleValue THROWABLE_MOB_BEHAVIOR_COOLDOWN_HARD_MULTIPLIER;
	public static ForgeConfigSpec.DoubleValue THROWABLE_MOB_BEHAVIOR_MIN_THROW_DISTANCE;
	public static ForgeConfigSpec.DoubleValue THROWABLE_MOB_BEHAVIOR_MELEE_DISTANCE;
	public static ForgeConfigSpec.BooleanValue THROWABLE_MOB_BEHAVIOR_CHECK_CLEAR_SHOT;
	public static ForgeConfigSpec.DoubleValue THROWABLE_MOB_BEHAVIOR_CRITICAL_CHANCE;
	public static ForgeConfigSpec.DoubleValue THROWABLE_MOB_BEHAVIOR_INACCURACY_MULTIPLIER;
	public static ForgeConfigSpec.BooleanValue THROWABLE_MOBS_THROW;

	public static void register(ForgeConfigSpec.Builder builder) {
		builder.push("Mob Behaviors Handler");

		builder.push("Mob Throwable Behavior");
		THROWABLE_MOB_BEHAVIOR_COOLDOWN_MIN_TICKS = builder
				.comment(
						"Minimum cooldown (in ticks) between throws. Each throw picks a random value between Min and Max (20 ticks = 1 second).")
				.defineInRange("Throwable Cooldown Min Ticks", 60, 5, 1200);
		THROWABLE_MOB_BEHAVIOR_COOLDOWN_MAX_TICKS = builder
				.comment(
						"Maximum cooldown (in ticks) between throws. Each throw picks a random value between Min and Max.")
				.defineInRange("Throwable Cooldown Max Ticks", 120, 5, 1200);
		THROWABLE_MOB_BEHAVIOR_INITIAL_COOLDOWN_MIN_TICKS = builder
				.comment("Minimum initial cooldown (in ticks) before first throw after acquiring a target")
				.defineInRange("Throwable Initial Cooldown Min Ticks", 40, 0, 800);
		THROWABLE_MOB_BEHAVIOR_INITIAL_COOLDOWN_MAX_TICKS = builder
				.comment("Maximum initial cooldown (in ticks) before first throw after acquiring a target")
				.defineInRange("Throwable Initial Cooldown Max Ticks", 120, 0, 800);
		THROWABLE_MOB_BEHAVIOR_COOLDOWN_EASY_MULTIPLIER = builder
						.comment("Cooldown multiplier on Easy difficulty (higher = slower throws)")
						.defineInRange("Throwable Cooldown Easy Multiplier", 3.5, 0.1, 5.0);
		THROWABLE_MOB_BEHAVIOR_COOLDOWN_NORMAL_MULTIPLIER = builder
						.comment("Cooldown multiplier on Normal difficulty")
						.defineInRange("Throwable Cooldown Normal Multiplier", 2.5, 0.1, 5.0);
		THROWABLE_MOB_BEHAVIOR_COOLDOWN_HARD_MULTIPLIER = builder
						.comment("Cooldown multiplier on Hard difficulty (lower = faster throws)")
						.defineInRange("Throwable Cooldown Hard Multiplier", 1.5, 0.1, 5.0);
		THROWABLE_MOB_BEHAVIOR_MIN_THROW_DISTANCE = builder
				.comment(
						"Minimum distance (in blocks) for the mob to throw. If the target is closer, the mob will prefer melee if within melee range, or wait/advance. Set to 0 to disable.")
				.defineInRange("Throwable Min Throw Distance", 3.0, 0.0, 10.0);
		THROWABLE_MOB_BEHAVIOR_MELEE_DISTANCE = builder
				.comment(
						"Distance (in blocks) at which the mob will perform a melee attack with the throwable instead of throwing.")
				.defineInRange("Throwable Melee Distance", 2.5, 1.0, 5.0);
		THROWABLE_MOB_BEHAVIOR_CHECK_CLEAR_SHOT = builder
				.comment(
						"If true, mobs will raytrace from their eyes to the target's center and only throw if no solid blocks are in the way. If false, uses the vanilla line-of-sight check.")
				.define("Throwable Check Clear Shot", true);
		THROWABLE_MOB_BEHAVIOR_CRITICAL_CHANCE = builder
				.comment(
						"Probability (0.0 to 1.0) that a mob's throw will be a critical hit (deals extra damage). 0 = never critical, 1 = always critical.")
				.defineInRange("Throwable Critical Chance", 0.15, 0.0, 1.0);
		THROWABLE_MOB_BEHAVIOR_INACCURACY_MULTIPLIER = builder
				.comment(
						"Multiplier for the throwable's inaccuracy when thrown by a mob. "
								+ "1.0 = normal accuracy (as defined by the item). "
								+ "Higher values = less accurate (more spread). "
								+ "0.0 = perfect aim.")
				.defineInRange("Throwable Inaccuracy Multiplier", 1.0, 0.0, 10.0);
		THROWABLE_MOBS_THROW = builder.comment("Allow mobs to throw throwable items when equipped")
				.define("Mobs Throw Throwables", true);
		builder.pop();

		builder.pop();
	}
}
