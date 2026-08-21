package net.jaams.shinerite.data;

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

import com.google.gson.JsonElement;

import net.jaams.shinerite.condition.ConditionSource;
import net.minecraft.world.entity.EquipmentSlot;

public class ItemModifierData {
    public List<String> target = new ArrayList<>();
    public Boolean enabled = true;
    public int priority = 0;
    public String condition_mode = "and";
    public List<Condition> conditions = new ArrayList<>();
    public ModifierEntry modifiers = new ModifierEntry();
    public List<ModifierEntry> modifiers_list = new ArrayList<>();
    public List<NbtEntry> nbt = new ArrayList<>();

    public List<ModifierEntry> getAllModifierEntries() {
        List<ModifierEntry> all = new ArrayList<>();
        if (modifiers != null && !modifiers.attributes.isEmpty()) {
            all.add(modifiers);
        }
        if (modifiers_list != null) {
            all.addAll(modifiers_list);
        }
        return all;
    }

    public boolean appliesToSlot(EquipmentSlot slot) {
        for (ModifierEntry entry : getAllModifierEntries()) {
            if (entry.slots.isEmpty() || entry.slots.contains(slot.getName())) {
                return true;
            }
        }
        return false;
    }

    public static class Condition implements ConditionSource {
        public String type;
        public String mod_id;
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
        public JsonElement component_value;

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

        public JsonElement componentValue() {
            return component_value;
        }
    }

    public static class ModifierEntry {
        public List<String> slots = new ArrayList<>();
        public List<AttributeEntry> attributes = new ArrayList<>();
    }

    public static class AttributeEntry {
        public String attribute;
        public String name;
        public double amount;
        public String operation = "addition";
        public UUID uuid;
    }

    public static class NbtEntry {
        public String key;
        public String type = "auto";
        public JsonElement value;
        public boolean replace = false;
    }

}