package net.jaams.shinerite.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.jaams.shinerite.loader.TraitModifierLoader;
import net.minecraft.world.item.ItemStack;

public class TraitModifierData {

    public List<String> target = new ArrayList<>();
    public Boolean enabled = true;
    public int priority = 0;
    public String condition_mode = "and";
    public List<Condition> conditions = new ArrayList<>();
    public List<String> active_traits = new ArrayList<>();
    public List<String> disabled_traits = new ArrayList<>();
    public TraitsEntry traits = new TraitsEntry();
    public ProjectileTraitsEntry projectile_traits = new ProjectileTraitsEntry();

    public boolean hasTrait(String traitName) {
        if (active_traits == null)
            return false;
        if (hasDisabledTrait(traitName))
            return false;
        return active_traits.stream().anyMatch((t) -> t.equalsIgnoreCase(traitName));
    }

    public boolean hasDisabledTrait(String traitName) {
        if (disabled_traits == null)
            return false;
        return disabled_traits.stream().anyMatch((t) -> t.equalsIgnoreCase(traitName));
    }

    public static class Condition {

        public String type;
        public String mod_id;
        public String enchantment;
        public int level = 0;
        public String key;
        public String nbt_key;
        public String item;
        public String tag;
        public String rarity;
        public int nbt_int_value;
        public boolean nbt_boolean_value;
        public short nbt_short_value;
        public long nbt_long_value;
        public String nbt_string_value;
    }

    public static class TraitsEntry {

        public FrailglowEntry frailglow = null;
    }

    public static class FrailglowEntry {

        public Float break_chance = null;
        public Boolean glowing_on_break = null;
        public Integer glow_duration = null;
        public Integer glow_amplifier = null;
    }

    public static class ProjectileTraitsEntry {

        public ThrowbackEntry throwback = null;
        public RadiantCollectorEntry radiant_collector = null;
    }

    public static class ThrowbackEntry {

        public Boolean enabled = null;
        public Boolean return_on_block_hit = null;
        public Boolean return_on_entity_hit = null;
        public Boolean return_on_max_range = null;
        public Float min_range = null;
        public Float max_range = null;
        public Double return_speed = null;
    }

    public static class RadiantCollectorEntry {

        public Boolean enabled = null;
        public Integer max_mounted_entities = null;
        public Boolean glow_on_collected_items = null;
    }

    public static Optional<TraitModifierData> getData(ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return Optional.empty();
        List<TraitModifierData> entries = TraitModifierLoader.INSTANCE.getForItem(stack.getItem());
        if (entries.isEmpty())
            return Optional.empty();
        TraitModifierData combinedData = null;
        for (TraitModifierData entry : entries) {
            if (TraitModifierLoader.INSTANCE.evaluateConditions(entry, stack)) {
                if (combinedData == null) {
                    combinedData = new TraitModifierData();
                }
                TraitModifierLoader.INSTANCE.mergeFrom(combinedData, entry);
            }
        }
        return Optional.ofNullable(combinedData);
    }

    public static boolean isTraitActive(ItemStack stack, String traitName) {
        return getData(stack)
                .map((d) -> d.hasTrait(traitName) && !d.hasDisabledTrait(traitName))
                .orElse(false);
    }

    public static boolean isTraitDisabled(ItemStack stack, String traitName) {
        return getData(stack)
                .map((d) -> d.hasDisabledTrait(traitName))
                .orElse(false);
    }

    public static Optional<FrailglowEntry> getFrailglow(ItemStack stack) {
        return getData(stack)
                .filter((d) -> d.hasTrait("frailglow") && d.traits.frailglow != null)
                .map((d) -> d.traits.frailglow);
    }

    public static Optional<ThrowbackEntry> getThrowback(ItemStack stack) {
        return getData(stack)
                .filter((d) -> d.hasTrait("throwback") && d.projectile_traits.throwback != null)
                .map((d) -> d.projectile_traits.throwback);
    }

    public static Optional<RadiantCollectorEntry> getRadiantCollector(ItemStack stack) {
        return getData(stack)
                .filter((d) -> d.hasTrait("radiant_collector") && d.projectile_traits.radiant_collector != null)
                .map((d) -> d.projectile_traits.radiant_collector);
    }
}
