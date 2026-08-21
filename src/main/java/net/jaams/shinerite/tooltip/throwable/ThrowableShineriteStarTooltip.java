package net.jaams.shinerite.tooltip.throwable;

import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.util.ModTooltips;
import net.jaams.shinerite.util.ModTags;
import net.jaams.shinerite.util.ModEnums;
import net.jaams.shinerite.configuration.common.TraitsConfig;
import net.jaams.shinerite.configuration.common.ThrowableConfig;
import net.jaams.shinerite.configuration.common.ProjectileCommonConfig;
import net.jaams.shinerite.configuration.client.TooltipsConfig;

import java.util.List;

public class ThrowableShineriteStarTooltip {
	public static void add(ItemStack stack, List<Component> tooltip) {
		if (stack == null || tooltip == null || stack.isEmpty()) {
			return;
		}
		if (hasJsonThrowable(stack)) {
			return;
		}
		if (!isShineriteStar(stack)) {
			return;
		}
		if (!isThrowableEnabled(stack)) {
			return;
		}
		ModEnums.ThrowMode throwMode = ModUtils.getConfigOrNbtEnum(stack, "ThrowMode", () -> ThrowableConfig.THROWABLE_SHINERITE_STAR_THROW_MODE.get());
		boolean isInstant = throwMode == ModEnums.ThrowMode.INSTANT_ON_RIGHT_CLICK;
		addMainTraits(stack, tooltip);
		addThrowingPropertiesIfEnabled(stack, tooltip, throwMode, isInstant);
		addThrowingModeIfEnabled(stack, tooltip, throwMode);
	}

	private static boolean isShineriteStar(ItemStack stack) {
		return stack.is(ModTags.SHINERITE_STARS) || (stack.hasTag() && stack.getTag().getBoolean("ForceShineriteStarThrowable"));
	}

	private static boolean hasJsonThrowable(ItemStack stack) {
		return net.jaams.shinerite.data.ThrowableItemData.getData(stack).isPresent();
	}

	private static boolean isThrowableEnabled(ItemStack stack) {
		if (!TraitsConfig.THROWABLE.get())
			return false;
		if (!isShineriteStar(stack))
			return false;
		if (!ThrowableConfig.THROWABLE_SHINERITE_STAR.get())
			return false;
		if (stack.hasTag() && stack.getTag().contains("ThrowableTrait")) {
			return stack.getTag().getBoolean("ThrowableTrait");
		}
		return true;
	}

	private static void addMainTraits(ItemStack stack, List<Component> tooltip) {
		ModTooltips.addProjectileTrait(stack, tooltip, "tooltip.jaams_shinerite.trait.throwable", "tooltip.jaams_shinerite.trait.throwable.desc");
	}

	private static void addThrowingPropertiesIfEnabled(ItemStack stack, List<Component> tooltip, ModEnums.ThrowMode throwMode, boolean isInstant) {
		if (!TooltipsConfig.TOOLTIP_THROWING_PROPERTIES.get()) {
			return;
		}
		ModTooltips.addExtraInfo(stack, tooltip, "tooltip.jaams_shinerite.properties.throwing", ChatFormatting.YELLOW);
		addBaseDamage(stack, tooltip);
		addBaseKnockback(stack, tooltip);
		addChargeTimesIfApplicable(stack, tooltip, isInstant);
		addSpeedStats(stack, tooltip, isInstant);
		addInaccuracy(stack, tooltip);
		addPiercingLevel(stack, tooltip);
		addCooldownIfInstant(stack, tooltip, isInstant);
		addRecoil(stack, tooltip);
		addWaterInertia(stack, tooltip);
	}

	private static void addBaseDamage(ItemStack stack, List<Component> tooltip) {
		double baseDamage = ModUtils.calculateDamage(stack);
		if (stack.hasTag() && stack.getTag().contains("BaseDamage", 99)) {
			baseDamage += stack.getTag().getDouble("BaseDamage");
		} else {
			baseDamage += ProjectileCommonConfig.SHINERITE_STAR_PROJECTILE_BASE_DAMAGE.get();
		}
		double rounded = ModUtils.roundToTwoDecimals(baseDamage);
		ModTooltips.addStat(stack, tooltip, "base_damage", rounded);
	}

	private static void addBaseKnockback(ItemStack stack, List<Component> tooltip) {
		double knockback = 0.0;
		if (stack.hasTag() && stack.getTag().contains("BaseKnockback", 99)) {
			knockback = stack.getTag().getDouble("BaseKnockback");
		} else {
			knockback = ProjectileCommonConfig.SHINERITE_STAR_PROJECTILE_BASE_KNOCKBACK.get();
		}
		if (knockback > 0.0) {
			double rounded = ModUtils.roundToTwoDecimals(knockback);
			ModTooltips.addStat(stack, tooltip, "base_knockback", rounded);
		}
	}

	private static void addChargeTimesIfApplicable(ItemStack stack, List<Component> tooltip, boolean isInstant) {
		if (isInstant)
			return;
		int minCharge = ModUtils.getConfigOrNbtInt(stack, "MinChargeTicks", ThrowableConfig.THROWABLE_SHINERITE_STAR_MIN_CHARGE::get);
		int maxCharge = ModUtils.getConfigOrNbtInt(stack, "MaxChargeTicks", ThrowableConfig.THROWABLE_SHINERITE_STAR_MAX_CHARGE::get);
		if (minCharge > 0) {
			double seconds = minCharge / 20.0;
			double rounded = ModUtils.roundToTwoDecimals(seconds);
			ModTooltips.addStat(stack, tooltip, "min_charge", rounded);
		}
		if (maxCharge > 0) {
			double seconds = maxCharge / 20.0;
			double rounded = ModUtils.roundToTwoDecimals(seconds);
			ModTooltips.addStat(stack, tooltip, "max_charge", rounded);
		}
	}

	private static void addSpeedStats(ItemStack stack, List<Component> tooltip, boolean isInstant) {
		double minSpeed = ModUtils.getConfigOrNbtDouble(stack, "MinSpeed", () -> ThrowableConfig.THROWABLE_SHINERITE_STAR_MIN_SPEED.get().doubleValue());
		double maxSpeed = ModUtils.getConfigOrNbtDouble(stack, "MaxSpeed", () -> ThrowableConfig.THROWABLE_SHINERITE_STAR_MAX_SPEED.get().doubleValue());
		if (isInstant) {
			if (maxSpeed > 0.0) {
				double rounded = ModUtils.roundToTwoDecimals(maxSpeed);
				ModTooltips.addStat(stack, tooltip, "throw_speed", rounded);
			}
		} else {
			if (minSpeed > 0.0) {
				double rounded = ModUtils.roundToTwoDecimals(minSpeed);
				ModTooltips.addStat(stack, tooltip, "min_speed", rounded);
			}
			if (maxSpeed > 0.0) {
				double rounded = ModUtils.roundToTwoDecimals(maxSpeed);
				ModTooltips.addStat(stack, tooltip, "max_speed", rounded);
			}
		}
	}

	private static void addInaccuracy(ItemStack stack, List<Component> tooltip) {
		double inaccuracy = ModUtils.getConfigOrNbtDouble(stack, "InaccuracyDegrees", () -> ThrowableConfig.THROWABLE_SHINERITE_STAR_INACCURACY.get().doubleValue());
		if (inaccuracy > 0.1) {
			double rounded = ModUtils.roundToTwoDecimals(inaccuracy);
			ModTooltips.addStat(stack, tooltip, "inaccuracy", rounded);
		}
	}

	private static void addPiercingLevel(ItemStack stack, List<Component> tooltip) {
		int piercingLevel = ProjectileCommonConfig.SHINERITE_STAR_PROJECTILE_PIERCING_LEVEL.get();
		if (stack.hasTag() && stack.getTag().contains("PiercingLevel", 99)) {
			piercingLevel = stack.getTag().getInt("PiercingLevel");
		}
		piercingLevel += EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, stack);
		if (piercingLevel > 0) {
			ModTooltips.addStat(stack, tooltip, "piercing_level", piercingLevel);
		}
	}

	private static void addCooldownIfInstant(ItemStack stack, List<Component> tooltip, boolean isInstant) {
		if (!isInstant)
			return;
		int cooldownTicks = ModUtils.getConfigOrNbtInt(stack, "InstantCooldownTicks", ThrowableConfig.THROWABLE_SHINERITE_STAR_COOLDOWN_TICKS_INSTANT::get);
		double seconds = cooldownTicks / 20.0;
		double rounded = ModUtils.roundToTwoDecimals(seconds);
		ModTooltips.addStat(stack, tooltip, "cooldown", rounded);
	}

	private static void addRecoil(ItemStack stack, List<Component> tooltip) {
		double recoil = ModUtils.getConfigOrNbtDouble(stack, "RecoilDistance", () -> ThrowableConfig.THROWABLE_SHINERITE_STAR_RECOIL.get().doubleValue());
		if (recoil > 0.0) {
			double rounded = ModUtils.roundToTwoDecimals(recoil);
			ModTooltips.addStat(stack, tooltip, "recoil", rounded);
		}
	}

	private static void addWaterInertia(ItemStack stack, List<Component> tooltip) {
		double waterInertia = ModUtils.getConfigOrNbtDouble(stack, "WaterInertia", () -> ProjectileCommonConfig.SHINERITE_STAR_PROJECTILE_WATER_INERTIA.get().doubleValue());
		double rounded = ModUtils.roundToTwoDecimals(waterInertia);
		ModTooltips.addStat(stack, tooltip, "water_inertia", rounded);
	}

	private static void addThrowingModeIfEnabled(ItemStack stack, List<Component> tooltip, ModEnums.ThrowMode throwMode) {
		if (!TooltipsConfig.TOOLTIP_THROWING_MODE.get())
			return;
		ModTooltips.addExtraInfo(stack, tooltip, "tooltip.jaams_shinerite.properties.throwing_mode", ChatFormatting.YELLOW);
		String modeKey = switch (throwMode) {
			case INSTANT_ON_RIGHT_CLICK -> "tooltip.jaams_shinerite.properties.throw_mode.instant";
			case CHARGE_AND_RELEASE -> "tooltip.jaams_shinerite.properties.throw_mode.charge_release";
			case CHARGE_AND_FINISH_USING -> "tooltip.jaams_shinerite.properties.throw_mode.charge_finishing";
			case CHARGE_RELEASE_AND_FINISH -> "tooltip.jaams_shinerite.properties.throw_mode.charge_hybrid";
		};
		tooltip.add(Component.translatable(modeKey).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
	}
}
