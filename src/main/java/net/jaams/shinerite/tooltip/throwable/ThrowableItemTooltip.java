package net.jaams.shinerite.tooltip.throwable;

import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.ChatFormatting;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.util.ModTooltips;
import net.jaams.shinerite.util.ModProjectiles;
import net.jaams.shinerite.util.ModEnums;
import net.jaams.shinerite.data.ThrowableItemData;
import net.jaams.shinerite.configuration.common.TraitsConfig;
import net.jaams.shinerite.configuration.common.ThrowableConfig;
import net.jaams.shinerite.configuration.common.ProjectileCommonConfig;
import net.jaams.shinerite.configuration.client.TooltipsConfig;

import java.util.Locale;
import java.util.List;

public class ThrowableItemTooltip {
    public static void add(ItemStack stack, List<Component> tooltip) {
        if (stack == null || tooltip == null || stack.isEmpty()) {
            return;
        }
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("ThrowableTrait") && !tag.getBoolean("ThrowableTrait")) {
            return;
        }
        if (!TraitsConfig.THROWABLE.get()) {
            return;
        }
        ThrowableItemData data = ThrowableItemData.getData(stack).orElse(null);
        if (!isValidJsonThrowable(data)) {
            return;
        }
        ThrowableItemData.ThrowableEntry json = data.throwable;
        ModEnums.ThrowMode throwMode = getThrowMode(stack, json);
        String projectileType = getEffectiveProjectileType(stack, json);
        addMainTrait(stack, tooltip);
        addSpecialTraits(stack, tooltip, projectileType);
        addThrowingProperties(stack, tooltip, json, throwMode, projectileType);
        addThrowingMode(stack, tooltip, throwMode);
    }

    private static String getEffectiveProjectileType(ItemStack stack, ThrowableItemData.ThrowableEntry json) {
        String nbt = getStringNBT(stack, "ThrowableProjectileType");
        if (nbt != null && !nbt.isEmpty())
            return nbt;
        if (json.projectile != null && !json.projectile.isEmpty())
            return json.projectile;
        if (isShinemerang(stack))
            return "jaams_shinerite:shinemerang_projectile";
        if (isShineriteStar(stack))
            return "jaams_shinerite:shinerite_star_projectile";
        return "GENERIC";
    }

    private static boolean isShinemerang(ItemStack stack) {
        return stack.is(net.jaams.shinerite.util.ModTags.SHINEMERANGS)
                || (stack.hasTag() && stack.getTag().getBoolean("ForceShinemerangThrowable"));
    }

    private static boolean isShineriteStar(ItemStack stack) {
        return stack.is(net.jaams.shinerite.util.ModTags.SHINERITE_STARS)
                || (stack.hasTag() && stack.getTag().getBoolean("ForceShineriteStarThrowable"));
    }

    private static boolean isValidJsonThrowable(ThrowableItemData data) {
        if (data == null || data.throwable == null)
            return false;
        ThrowableItemData.ThrowableEntry t = data.throwable;
        return Boolean.TRUE.equals(t.throw_enabled) && t.projectile != null && !t.projectile.trim().isEmpty();
    }

    private static ModEnums.ThrowMode getThrowMode(ItemStack stack, ThrowableItemData.ThrowableEntry json) {
        String nbt = getStringNBT(stack, "ThrowMode");
        if (nbt != null && !nbt.isEmpty()) {
            try {
                return ModEnums.ThrowMode.valueOf(nbt.toUpperCase(Locale.ROOT));
            } catch (Exception ignored) {
            }
        }
        if (json.throw_mode != null && !json.throw_mode.isEmpty()) {
            try {
                return ModEnums.ThrowMode.valueOf(json.throw_mode.toUpperCase(Locale.ROOT));
            } catch (Exception ignored) {
            }
        }
        if (isShinemerang(stack))
            return ThrowableConfig.THROWABLE_SHINEMERANG_THROW_MODE.get();
        if (isShineriteStar(stack))
            return ThrowableConfig.THROWABLE_SHINERITE_STAR_THROW_MODE.get();
        return ModEnums.ThrowMode.CHARGE_AND_RELEASE;
    }

    private static void addThrowingProperties(ItemStack stack, List<Component> tooltip,
            ThrowableItemData.ThrowableEntry json, ModEnums.ThrowMode throwMode, String projectileType) {
        if (!TooltipsConfig.TOOLTIP_THROWING_PROPERTIES.get())
            return;
        ModTooltips.addExtraInfo(stack, tooltip, "tooltip.jaams_shinerite.properties.throwing", ChatFormatting.YELLOW);
        addRealBaseDamage(stack, tooltip, json);
        addRealBaseKnockback(stack, tooltip, json);
        addChargeTimes(stack, tooltip, throwMode, json);
        addSpeedStats(stack, tooltip, throwMode, json);
        addInaccuracy(stack, tooltip, json);
        addPiercingLevel(stack, tooltip, json);
        addCooldownIfInstant(stack, tooltip, throwMode, json);
        addRecoil(stack, tooltip, json);
        addWaterInertia(stack, tooltip, json);
        addThrowbackPropertiesIfEnabled(stack, tooltip, throwMode, projectileType);
        addCollectorPropertiesIfEnabled(stack, tooltip, projectileType);
    }

    private static void addChargeTimes(ItemStack stack, List<Component> tooltip, ModEnums.ThrowMode throwMode,
            ThrowableItemData.ThrowableEntry json) {
        if (throwMode == ModEnums.ThrowMode.INSTANT_ON_RIGHT_CLICK)
            return;
        int minCharge = getMinChargeTicks(stack, json);
        if (minCharge > 0) {
            ModTooltips.addStat(stack, tooltip, "min_charge", ModUtils.roundToTwoDecimals(minCharge / 20.0));
        }
        int maxCharge = getMaxChargeTicks(stack, json);
        if (maxCharge != 72000 && maxCharge > 0) {
            ModTooltips.addStat(stack, tooltip, "max_charge", ModUtils.roundToTwoDecimals(maxCharge / 20.0));
        }
    }

    private static void addSpeedStats(ItemStack stack, List<Component> tooltip, ModEnums.ThrowMode throwMode,
            ThrowableItemData.ThrowableEntry json) {
        if (throwMode == ModEnums.ThrowMode.INSTANT_ON_RIGHT_CLICK) {
            float speed = getMaxSpeed(stack, json);
            if (speed > 0.0f) {
                ModTooltips.addStat(stack, tooltip, "throw_speed", ModUtils.roundToTwoDecimals(speed));
            }
        } else {
            float minSpeed = getMinSpeed(stack, json);
            float maxSpeed = getMaxSpeed(stack, json);
            if (minSpeed > 0.0f) {
                ModTooltips.addStat(stack, tooltip, "min_speed", ModUtils.roundToTwoDecimals(minSpeed));
            }
            if (maxSpeed > 0.0f) {
                ModTooltips.addStat(stack, tooltip, "max_speed", ModUtils.roundToTwoDecimals(maxSpeed));
            }
        }
    }

    private static void addInaccuracy(ItemStack stack, List<Component> tooltip, ThrowableItemData.ThrowableEntry json) {
        float inaccuracy = getInaccuracy(stack, json);
        if (inaccuracy > 0.1f) {
            ModTooltips.addStat(stack, tooltip, "inaccuracy", ModUtils.roundToTwoDecimals(inaccuracy));
        }
    }

    private static void addRecoil(ItemStack stack, List<Component> tooltip, ThrowableItemData.ThrowableEntry json) {
        float recoil = getRecoil(stack, json);
        if (recoil > 0.0f) {
            ModTooltips.addStat(stack, tooltip, "recoil", ModUtils.roundToTwoDecimals(recoil));
        }
    }

    private static void addRealBaseDamage(ItemStack stack, List<Component> tooltip,
            ThrowableItemData.ThrowableEntry json) {
        double damage = getRealBaseDamage(stack, json);
        ModTooltips.addStat(stack, tooltip, "base_damage", ModUtils.roundToTwoDecimals(damage));
    }

    private static void addRealBaseKnockback(ItemStack stack, List<Component> tooltip,
            ThrowableItemData.ThrowableEntry json) {
        double kb = getRealBaseKnockback(stack, json);
        if (kb > 0.0) {
            ModTooltips.addStat(stack, tooltip, "base_knockback", ModUtils.roundToTwoDecimals(kb));
        }
    }

    private static double getRealBaseDamage(ItemStack stack, ThrowableItemData.ThrowableEntry json) {
        if (stack.hasTag() && stack.getTag().contains("BaseDamage", 99)) {
            return stack.getTag().getDouble("BaseDamage");
        }
        if (json.base_damage != null && json.base_damage >= 0.0f) {
            return json.base_damage;
        }
        double itemDamage = ModUtils.calculateDamage(stack);
        double defaultDmg = isShinemerang(stack)
                ? ProjectileCommonConfig.SHINEMERANG_PROJECTILE_BASE_DAMAGE.get()
                : ProjectileCommonConfig.SHINERITE_STAR_PROJECTILE_BASE_DAMAGE.get();
        return itemDamage + defaultDmg;
    }

    private static double getRealBaseKnockback(ItemStack stack, ThrowableItemData.ThrowableEntry json) {
        if (stack.hasTag() && stack.getTag().contains("BaseKnockback", 99)) {
            return stack.getTag().getDouble("BaseKnockback");
        }
        if (json.base_knockback != null && json.base_knockback >= 0.0f) {
            return json.base_knockback;
        }
        return isShinemerang(stack)
                ? ProjectileCommonConfig.SHINEMERANG_PROJECTILE_BASE_KNOCKBACK.get()
                : ProjectileCommonConfig.SHINERITE_STAR_PROJECTILE_BASE_KNOCKBACK.get();
    }

    private static int getMinChargeTicks(ItemStack stack, ThrowableItemData.ThrowableEntry json) {
        Integer nbt = getIntNBT(stack, "MinChargeTicks");
        if (nbt != null && nbt >= 0)
            return nbt;
        if (json.min_charge_ticks != null && json.min_charge_ticks >= 0)
            return json.min_charge_ticks;
        return isShinemerang(stack)
                ? ThrowableConfig.THROWABLE_SHINEMERANG_MIN_CHARGE.get()
                : ThrowableConfig.THROWABLE_SHINERITE_STAR_MIN_CHARGE.get();
    }

    private static int getMaxChargeTicks(ItemStack stack, ThrowableItemData.ThrowableEntry json) {
        Integer nbt = getIntNBT(stack, "MaxChargeTicks");
        if (nbt != null && nbt >= 0)
            return nbt;
        if (json.max_charge_ticks != null && json.max_charge_ticks >= 0)
            return json.max_charge_ticks;
        return isShinemerang(stack)
                ? ThrowableConfig.THROWABLE_SHINEMERANG_MAX_CHARGE.get()
                : ThrowableConfig.THROWABLE_SHINERITE_STAR_MAX_CHARGE.get();
    }

    private static float getMinSpeed(ItemStack stack, ThrowableItemData.ThrowableEntry json) {
        Float nbt = getFloatNBT(stack, "MinSpeed");
        if (nbt != null && nbt >= 0)
            return nbt;
        if (json.min_speed != null && json.min_speed >= 0)
            return json.min_speed;
        return isShinemerang(stack)
                ? ThrowableConfig.THROWABLE_SHINEMERANG_MIN_SPEED.get().floatValue()
                : ThrowableConfig.THROWABLE_SHINERITE_STAR_MIN_SPEED.get().floatValue();
    }

    private static float getMaxSpeed(ItemStack stack, ThrowableItemData.ThrowableEntry json) {
        Float nbt = getFloatNBT(stack, "MaxSpeed");
        if (nbt != null && nbt >= 0)
            return nbt;
        if (json.max_speed != null && json.max_speed >= 0)
            return json.max_speed;
        return isShinemerang(stack)
                ? ThrowableConfig.THROWABLE_SHINEMERANG_MAX_SPEED.get().floatValue()
                : ThrowableConfig.THROWABLE_SHINERITE_STAR_MAX_SPEED.get().floatValue();
    }

    private static float getInaccuracy(ItemStack stack, ThrowableItemData.ThrowableEntry json) {
        Float nbt = getFloatNBT(stack, "InaccuracyDegrees");
        if (nbt != null && nbt >= 0)
            return nbt;
        if (json.inaccuracy != null && json.inaccuracy >= 0)
            return json.inaccuracy;
        return isShinemerang(stack)
                ? ThrowableConfig.THROWABLE_SHINEMERANG_INACCURACY.get().floatValue()
                : ThrowableConfig.THROWABLE_SHINERITE_STAR_INACCURACY.get().floatValue();
    }

    private static float getRecoil(ItemStack stack, ThrowableItemData.ThrowableEntry json) {
        Float nbt = getFloatNBT(stack, "RecoilDistance");
        if (nbt != null && nbt >= 0)
            return nbt;
        if (json.recoil != null && json.recoil >= 0)
            return json.recoil;
        return isShinemerang(stack)
                ? ThrowableConfig.THROWABLE_SHINEMERANG_RECOIL.get().floatValue()
                : ThrowableConfig.THROWABLE_SHINERITE_STAR_RECOIL.get().floatValue();
    }

    private static void addPiercingLevel(ItemStack stack, List<Component> tooltip, ThrowableItemData.ThrowableEntry json) {
        int piercing = 0;
        if (stack.hasTag() && stack.getTag().contains("PiercingLevel", 99)) {
            piercing = Math.max(0, stack.getTag().getInt("PiercingLevel"));
        } else if (json.pierce_level != null) {
            piercing = Math.max(0, json.pierce_level);
        } else {
            piercing = isShinemerang(stack)
                    ? ProjectileCommonConfig.SHINEMERANG_PROJECTILE_PIERCING_LEVEL.get()
                    : ProjectileCommonConfig.SHINERITE_STAR_PROJECTILE_PIERCING_LEVEL.get();
        }
        piercing += EnchantmentHelper.getTagEnchantmentLevel(Enchantments.PIERCING, stack);
        if (piercing > 0) {
            ModTooltips.addStat(stack, tooltip, "piercing_level", piercing);
        }
    }

    private static void addWaterInertia(ItemStack stack, List<Component> tooltip, ThrowableItemData.ThrowableEntry json) {
        if (stack.hasTag() && stack.getTag().contains("WaterInertia", 99)) {
            ModTooltips.addStat(stack, tooltip, "water_inertia", ModUtils.roundToTwoDecimals(stack.getTag().getDouble("WaterInertia")));
            return;
        }
        float def = isShinemerang(stack)
                ? ProjectileCommonConfig.SHINEMERANG_PROJECTILE_WATER_INERTIA.get().floatValue()
                : ProjectileCommonConfig.SHINERITE_STAR_PROJECTILE_WATER_INERTIA.get().floatValue();
        float inertia = ModProjectiles.getWaterInertia(stack, def);
        ModTooltips.addStat(stack, tooltip, "water_inertia", ModUtils.roundToTwoDecimals(inertia));
    }

    private static void addThrowbackPropertiesIfEnabled(ItemStack stack, List<Component> tooltip,
            ModEnums.ThrowMode throwMode, String projectileType) {
        if (!isShinemerangProjectile(projectileType)) {
            return;
        }
        if (!isThrowbackEnabled(stack) || !TooltipsConfig.TOOLTIP_THROWBACK_PROPERTIES.get()) {
            return;
        }
        double minRange = ModProjectiles.getThrowbackMinRange(stack,
                TraitsConfig.THROWBACK_SHINEMERANG_PROJECTILE_MIN_RANGE.get().doubleValue());
        float maxRange = ModProjectiles.getThrowbackMaxRange(stack,
                TraitsConfig.THROWBACK_SHINEMERANG_PROJECTILE_MAX_RANGE.get().floatValue());
        double returnSpeed = ModProjectiles.getThrowbackReturnSpeed(stack, 1.0);
        ModTooltips.addExtraInfo(stack, tooltip, "tooltip.jaams_shinerite.properties.throwback", ChatFormatting.YELLOW);
        if (throwMode == ModEnums.ThrowMode.INSTANT_ON_RIGHT_CLICK) {
            ModTooltips.addStat(stack, tooltip, "throw_range", ModUtils.roundToTwoDecimals(maxRange));
        } else {
            ModTooltips.addStat(stack, tooltip, "min_range", ModUtils.roundToTwoDecimals(minRange));
            ModTooltips.addStat(stack, tooltip, "max_range", ModUtils.roundToTwoDecimals(maxRange));
        }
        ModTooltips.addStat(stack, tooltip, "return_speed", ModUtils.roundToTwoDecimals(returnSpeed));
    }

    private static void addCollectorPropertiesIfEnabled(ItemStack stack, List<Component> tooltip, String projectileType) {
        if (!isShinemerangProjectile(projectileType)) {
            return;
        }
        if (!isCollectorEnabled(stack) || !TooltipsConfig.TOOLTIP_COLLECTOR_PROPERTIES.get()) {
            return;
        }
        int maxItems = ModProjectiles.getMaxMountedEntities(stack, true,
                TraitsConfig.RADIANT_COLLECTOR_SHINEMERANG_PROJECTILE_MAX_ITEMS.get());
        ModTooltips.addExtraInfo(stack, tooltip, "tooltip.jaams_shinerite.properties.radiant_collector",
                ChatFormatting.YELLOW);
        ModTooltips.addStat(stack, tooltip, "collector_max_items", maxItems);
    }

    private static boolean isShinemerangProjectile(String projectileType) {
        return projectileType != null && projectileType.endsWith("shinemerang_projectile");
    }

    private static void addCooldownIfInstant(ItemStack stack, List<Component> tooltip, ModEnums.ThrowMode throwMode,
            ThrowableItemData.ThrowableEntry json) {
        if (throwMode != ModEnums.ThrowMode.INSTANT_ON_RIGHT_CLICK)
            return;
        int cd = 10;
        Integer nbt = getIntNBT(stack, "InstantCooldownTicks");
        if (nbt != null)
            cd = nbt;
        else if (json.instant_cooldown_ticks != null)
            cd = json.instant_cooldown_ticks;
        ModTooltips.addStat(stack, tooltip, "cooldown", ModUtils.roundToTwoDecimals(cd / 20.0));
    }

    private static void addThrowingMode(ItemStack stack, List<Component> tooltip, ModEnums.ThrowMode throwMode) {
        if (!TooltipsConfig.TOOLTIP_THROWING_MODE.get())
            return;
        ModTooltips.addExtraInfo(stack, tooltip, "tooltip.jaams_shinerite.properties.throwing_mode",
                ChatFormatting.YELLOW);
        String key = switch (throwMode) {
            case INSTANT_ON_RIGHT_CLICK -> "tooltip.jaams_shinerite.properties.throw_mode.instant";
            case CHARGE_AND_RELEASE -> "tooltip.jaams_shinerite.properties.throw_mode.charge_release";
            case CHARGE_AND_FINISH_USING -> "tooltip.jaams_shinerite.properties.throw_mode.charge_finishing";
            case CHARGE_RELEASE_AND_FINISH -> "tooltip.jaams_shinerite.properties.throw_mode.charge_hybrid";
        };
        tooltip.add(Component.translatable(key).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    private static void addMainTrait(ItemStack stack, List<Component> tooltip) {
        ModTooltips.addProjectileTrait(stack, tooltip, "tooltip.jaams_shinerite.trait.throwable",
                "tooltip.jaams_shinerite.trait.throwable.desc");
    }

    private static void addSpecialTraits(ItemStack stack, List<Component> tooltip, String projectileType) {
        if (isShinemerangProjectile(projectileType)) {
            if (isThrowbackEnabled(stack)) {
                ModTooltips.addProjectileTrait(stack, tooltip, "tooltip.jaams_shinerite.trait.throwback",
                        "tooltip.jaams_shinerite.trait.throwback.desc");
            }
            if (isCollectorEnabled(stack)) {
                ModTooltips.addProjectileTrait(stack, tooltip, "tooltip.jaams_shinerite.trait.radiant_collector",
                        "tooltip.jaams_shinerite.trait.radiant_collector.desc");
            }
        }
    }

    private static boolean isThrowbackEnabled(ItemStack stack) {
        return ModProjectiles.getThrowbackEnabled(stack, TraitsConfig.THROWBACK.get());
    }

    private static boolean isCollectorEnabled(ItemStack stack) {
        return ModProjectiles.getRadiantCollectorEnabled(stack, TraitsConfig.RADIANT_COLLECTOR.get());
    }

    private static Integer getIntNBT(ItemStack stack, String key) {
        CompoundTag tag = stack.getTag();
        return (tag != null && tag.contains(key, Tag.TAG_INT)) ? tag.getInt(key) : null;
    }

    private static Float getFloatNBT(ItemStack stack, String key) {
        CompoundTag tag = stack.getTag();
        return (tag != null && tag.contains(key, Tag.TAG_FLOAT)) ? tag.getFloat(key) : null;
    }

    private static String getStringNBT(ItemStack stack, String key) {
        CompoundTag tag = stack.getTag();
        return (tag != null && tag.contains(key, Tag.TAG_STRING)) ? tag.getString(key) : null;
    }
}