package net.jaams.shinerite.configuration.common;

import net.minecraftforge.common.ForgeConfigSpec;

public class EnchantmentsConfig {

	public static ForgeConfigSpec.BooleanValue LIGHTBRINGER;
	public static ForgeConfigSpec.IntValue LIGHTBRINGER_MAX_LEVEL;
	public static ForgeConfigSpec.IntValue LIGHTBRINGER_MIN_COST;
	public static ForgeConfigSpec.IntValue LIGHTBRINGER_COST_PER_LEVEL;
	public static ForgeConfigSpec.IntValue LIGHTBRINGER_COST_RANGE;
	public static ForgeConfigSpec.BooleanValue LIGHTBRINGER_IS_TREASURE;
	public static ForgeConfigSpec.IntValue LIGHTBRINGER_DURATION_PER_LEVEL;

	public static ForgeConfigSpec.BooleanValue LIGHTSTEAL;
	public static ForgeConfigSpec.IntValue LIGHTSTEAL_MAX_LEVEL;
	public static ForgeConfigSpec.IntValue LIGHTSTEAL_MIN_COST;
	public static ForgeConfigSpec.IntValue LIGHTSTEAL_COST_PER_LEVEL;
	public static ForgeConfigSpec.IntValue LIGHTSTEAL_COST_RANGE;
	public static ForgeConfigSpec.BooleanValue LIGHTSTEAL_IS_TREASURE;
	public static ForgeConfigSpec.DoubleValue LIGHTSTEAL_CHANCE_PER_LEVEL;
	public static ForgeConfigSpec.DoubleValue LIGHTSTEAL_MAX_CHANCE;
	public static ForgeConfigSpec.DoubleValue LIGHTSTEAL_HEAL_AMOUNT;
	public static ForgeConfigSpec.DoubleValue LIGHTSTEAL_HEAL_PER_LEVEL;
	public static ForgeConfigSpec.IntValue LIGHTSTEAL_STOLEN_GLOW_BASE_DURATION;
	public static ForgeConfigSpec.IntValue LIGHTSTEAL_STOLEN_GLOW_DURATION_PER_SUCCESS;

	public static ForgeConfigSpec.BooleanValue LIGHTGUARD;
	public static ForgeConfigSpec.IntValue LIGHTGUARD_MAX_LEVEL;
	public static ForgeConfigSpec.IntValue LIGHTGUARD_MIN_COST;
	public static ForgeConfigSpec.IntValue LIGHTGUARD_COST_PER_LEVEL;
	public static ForgeConfigSpec.IntValue LIGHTGUARD_COST_RANGE;
	public static ForgeConfigSpec.BooleanValue LIGHTGUARD_IS_TREASURE;
	public static ForgeConfigSpec.IntValue LIGHTGUARD_DURATION_PER_LEVEL;
	public static ForgeConfigSpec.DoubleValue LIGHTGUARD_DAMAGE_CHANCE_PER_LEVEL;
	public static ForgeConfigSpec.DoubleValue LIGHTGUARD_MAGIC_DAMAGE_PER_PIECE;
	public static ForgeConfigSpec.DoubleValue LIGHTGUARD_KNOCKBACK;
	public static ForgeConfigSpec.BooleanValue LIGHTGUARD_SPAWN_PARTICLES;
	public static ForgeConfigSpec.IntValue LIGHTGUARD_PARTICLES;
	public static ForgeConfigSpec.DoubleValue LIGHTGUARD_PARTICLE_SIZE;

	public static void register(ForgeConfigSpec.Builder builder) {
		builder.push("Custom Enchantments Handler");

		builder.push("Lightbringer");
		LIGHTBRINGER = builder.comment("Whether the Lightbringer enchantment is enabled").worldRestart().define("Lightbringer", true);
		LIGHTBRINGER_MAX_LEVEL = builder.comment("Maximum level for Lightbringer enchantment").worldRestart().defineInRange("Lightbringer Max Level", 2, 1, 5);
		LIGHTBRINGER_MIN_COST = builder.comment("Minimum XP cost for level 1").defineInRange("Lightbringer Min Cost", 10, 1, 50);
		LIGHTBRINGER_COST_PER_LEVEL = builder.comment("Additional XP cost per level above 1").defineInRange("Lightbringer Cost Per Level", 8, 1, 20);
		LIGHTBRINGER_COST_RANGE = builder.comment("XP cost range (added to min cost)").defineInRange("Lightbringer Cost Range", 15, 5, 30);
		LIGHTBRINGER_IS_TREASURE = builder.comment("Whether Lightbringer is a treasure enchantment").define("Lightbringer Is Treasure", false);
		LIGHTBRINGER_DURATION_PER_LEVEL = builder.comment("Ticks of Glowing applied per level of Lightbringer when a hit lands").defineInRange("Lightbringer Duration Per Level", 80, 1, 2000);
		builder.pop();

		builder.push("Lightsteal");
		LIGHTSTEAL = builder.comment("Whether the Lightsteal enchantment is enabled").worldRestart().define("Lightsteal", true);
		LIGHTSTEAL_MAX_LEVEL = builder.comment("Maximum level for Lightsteal enchantment").worldRestart().defineInRange("Lightsteal Max Level", 3, 1, 5);
		LIGHTSTEAL_MIN_COST = builder.comment("Minimum XP cost for level 1").defineInRange("Lightsteal Min Cost", 10, 1, 50);
		LIGHTSTEAL_COST_PER_LEVEL = builder.comment("Additional XP cost per level above 1").defineInRange("Lightsteal Cost Per Level", 8, 1, 20);
		LIGHTSTEAL_COST_RANGE = builder.comment("XP cost range (added to min cost)").defineInRange("Lightsteal Cost Range", 15, 5, 30);
		LIGHTSTEAL_IS_TREASURE = builder.comment("Whether Lightsteal is a treasure enchantment").define("Lightsteal Is Treasure", false);
		LIGHTSTEAL_CHANCE_PER_LEVEL = builder.comment("Base chance per level that Lightsteal steals health from a glowing enemy").defineInRange("Lightsteal Chance", 0.2, 0.0, 1.0);
		LIGHTSTEAL_MAX_CHANCE = builder.comment("Maximum chance cap for Lightsteal").defineInRange("Lightsteal Max Chance", 0.95, 0.0, 1.0);
		LIGHTSTEAL_HEAL_AMOUNT = builder.comment("Base health healed per successful Lightsteal roll").defineInRange("Lightsteal Heal Amount", 0.5, 0.0, 20.0);
		LIGHTSTEAL_HEAL_PER_LEVEL = builder.comment("Extra health healed per enchantment level above 1").defineInRange("Lightsteal Heal Per Level", 0.4, 0.0, 20.0);
		LIGHTSTEAL_STOLEN_GLOW_BASE_DURATION = builder.comment("Base duration of Glowing transferred to the attacker on a successful steal").defineInRange("Lightsteal Stolen Glow Base Duration", 60, 1, 2000);
		LIGHTSTEAL_STOLEN_GLOW_DURATION_PER_SUCCESS = builder.comment("Extra transferred Glowing duration added per successful roll").defineInRange("Lightsteal Stolen Glow Duration Per Success", 40, 0, 2000);
		builder.pop();

		builder.push("Lightguard");
		LIGHTGUARD = builder.comment("Whether the Lightguard enchantment is enabled").worldRestart().define("Lightguard", true);
		LIGHTGUARD_MAX_LEVEL = builder.comment("Maximum level for Lightguard enchantment").worldRestart().defineInRange("Lightguard Max Level", 4, 1, 5);
		LIGHTGUARD_MIN_COST = builder.comment("Minimum XP cost for level 1").defineInRange("Lightguard Min Cost", 10, 1, 50);
		LIGHTGUARD_COST_PER_LEVEL = builder.comment("Additional XP cost per level above 1").defineInRange("Lightguard Cost Per Level", 8, 1, 20);
		LIGHTGUARD_COST_RANGE = builder.comment("XP cost range (added to min cost)").defineInRange("Lightguard Cost Range", 15, 5, 30);
		LIGHTGUARD_IS_TREASURE = builder.comment("Whether Lightguard is a treasure enchantment").define("Lightguard Is Treasure", false);
		LIGHTGUARD_DURATION_PER_LEVEL = builder.comment("Ticks of Glowing applied to the attacker per total Lightguard level").defineInRange("Lightguard Duration Per Level", 20, 1, 2000);
		LIGHTGUARD_DAMAGE_CHANCE_PER_LEVEL = builder.comment("Chance per total level that Lightguard retaliates with magic damage").defineInRange("Lightguard Damage Chance", 0.1, 0.0, 1.0);
		LIGHTGUARD_MAGIC_DAMAGE_PER_PIECE = builder.comment("Magic damage dealt per enchanted armor piece when Lightguard retaliates").defineInRange("Lightguard Magic Damage Per Piece", 1.0, 0.0, 50.0);
		LIGHTGUARD_KNOCKBACK = builder.comment("Knockback strength applied by the Lightguard retaliation").defineInRange("Lightguard Knockback", 0.5, 0.0, 5.0);
		LIGHTGUARD_SPAWN_PARTICLES = builder.comment("Whether to spawn custom crit particles when Lightguard applies Glowing").define("Lightguard Spawn Particles", true);
		LIGHTGUARD_PARTICLES = builder.comment("Base amount of custom particles spawned per Lightguard retaliation").defineInRange("Lightguard Particles", 6, 1, 100);
		LIGHTGUARD_PARTICLE_SIZE = builder.comment("Size of the custom particles spawned by Lightguard").defineInRange("Lightguard Particle Size", 0.15, 0.01, 2.0);
		builder.pop();
		builder.pop();
	}
}