package net.jaams.shinerite.data;

import java.util.ArrayList;
import java.util.List;

import net.jaams.shinerite.condition.ConditionSource;

public class LootModifierData {
    public Boolean enabled = true;
    public float chance = 1.0f;
    public boolean replace_all = false;
    public String condition_mode = "and";
    public List<Condition> conditions = new ArrayList<>();
    public List<String> loot_tables = new ArrayList<>();
    public List<String> exclude_loot_tables = new ArrayList<>();
    public List<LootEntry> entries = new ArrayList<>();

    public static class Condition implements ConditionSource {
        public String type;
        public String mod_id;
        public String advancement;
        public String difficulty;
        public String dimension;
        public String gamestage;
        public String enchantment;
        public int level = 0;
        public String key;
        public String item;
        public String tag;
        public String rarity;
        public String nbt_key;
        public String nbt_type;
        public int nbt_int_value;
        public boolean nbt_boolean_value;
        public short nbt_short_value;
        public long nbt_long_value;
        public String nbt_string_value;
        public String component;
        public com.google.gson.JsonElement component_value;

        public String type() {
            return type;
        }

        public String modId() {
            return mod_id;
        }

        public String enchantment() {
            return enchantment;
        }

        public int level() {
            return level;
        }

        public String key() {
            return key;
        }

        public String nbtType() {
            return nbt_type != null ? nbt_type : nbt_key;
        }

        public String item() {
            return item;
        }

        public String tag() {
            return tag;
        }

        public String rarity() {
            return rarity;
        }

        public int nbtIntValue() {
            return nbt_int_value;
        }

        public boolean nbtBooleanValue() {
            return nbt_boolean_value;
        }

        public short nbtShortValue() {
            return nbt_short_value;
        }

        public long nbtLongValue() {
            return nbt_long_value;
        }

        public String nbtStringValue() {
            return nbt_string_value;
        }

        public String component() {
            return component;
        }

        public com.google.gson.JsonElement componentValue() {
            return component_value;
        }
    }

    public static class LootEntry {
        public String item;
        public int count_min = 1;
        public int count_max = 1;
        public float chance = 1.0f;
        public String nbt = "";
        public List<EnchantmentData> enchantments = new ArrayList<>();
    }

    public static class EnchantmentData {
        public String id;
        public int level = 1;
        public float chance = 1.0f;
    }
}