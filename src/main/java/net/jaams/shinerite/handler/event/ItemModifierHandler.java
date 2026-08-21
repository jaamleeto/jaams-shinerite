package net.jaams.shinerite.handler.event;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.core.registries.BuiltInRegistries;

import net.jaams.shinerite.loader.ItemModifierLoader;
import net.jaams.shinerite.data.ItemModifierData;
import net.jaams.shinerite.condition.ConditionEvaluator;

import java.util.UUID;
import java.util.Map;
import java.util.List;

import java.nio.charset.StandardCharsets;

import com.google.gson.JsonPrimitive;
import com.google.gson.JsonElement;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ItemModifierHandler {
    private static final Logger LOGGER = LogManager.getLogger(ItemModifierHandler.class);

    @SubscribeEvent
    public static void onItemDataAttributeModifier(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty())
            return;
        List<ItemModifierData> datas = ItemModifierLoader.INSTANCE.getForItem(stack.getItem());
        if (datas.isEmpty())
            return;
        EquipmentSlot slot = event.getSlotType();
        for (ItemModifierData data : datas) {
            if (!checkConditions(stack, data))
                continue;
            applyNbt(stack, data);
            for (ItemModifierData.ModifierEntry modifierEntry : data.getAllModifierEntries()) {
                if (modifierEntry.slots.isEmpty() || modifierEntry.slots.contains(slot.getName())) {
                    applyAttributes(event, modifierEntry);
                }
            }
        }
    }

    private static boolean checkConditions(ItemStack stack, ItemModifierData data) {
        return ConditionEvaluator.evaluateAll(data.conditions, data.condition_mode, stack);
    }

    private static void applyAttributes(ItemAttributeModifierEvent event, ItemModifierData.ModifierEntry modifierEntry) {
        for (ItemModifierData.AttributeEntry entry : modifierEntry.attributes) {
            ResourceLocation attrLoc = ResourceLocation.tryParse(entry.attribute);
            if (attrLoc == null)
                continue;
            Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(attrLoc);
            if (attribute == null)
                continue;
            AttributeModifier.Operation operation = switch (entry.operation.toLowerCase()) {
                case "multiply_base" -> AttributeModifier.Operation.MULTIPLY_BASE;
                case "multiply_total" -> AttributeModifier.Operation.MULTIPLY_TOTAL;
                default -> AttributeModifier.Operation.ADDITION;
            };
            UUID uuid = entry.uuid != null ? entry.uuid
                    : UUID.nameUUIDFromBytes((entry.name + "|" + modifierEntry.slots.toString())
                            .getBytes(StandardCharsets.UTF_8));
            AttributeModifier modifier = new AttributeModifier(uuid, entry.name, entry.amount, operation);
            event.addModifier(attribute, modifier);
        }
    }

    private static void applyNbt(ItemStack stack, ItemModifierData data) {
        if (data.nbt.isEmpty())
            return;
        CompoundTag current = stack.getOrCreateTag();
        boolean changed = false;
        for (ItemModifierData.NbtEntry entry : data.nbt) {
            if (entry.key == null || entry.value == null)
                continue;
            Tag newTag = jsonToNbtTag(entry.value, entry.type);
            if (newTag == null)
                continue;
            if (entry.replace) {
                current.put(entry.key, newTag);
                changed = true;
            } else if (!current.contains(entry.key) || !current.get(entry.key).equals(newTag)) {
                current.put(entry.key, newTag);
                changed = true;
            }
        }
        if (changed) {
            stack.setTag(current);
        }
    }

    private static Tag jsonToNbtTag(JsonElement json, String type) {
        if (json == null)
            return null;
        type = type.toLowerCase().trim();
        try {
            switch (type) {
                case "byte":
                    return ByteTag.valueOf(json.getAsByte());
                case "short":
                    return ShortTag.valueOf(json.getAsShort());
                case "int":
                    return IntTag.valueOf(json.getAsInt());
                case "long":
                    return LongTag.valueOf(json.getAsLong());
                case "float":
                    return FloatTag.valueOf(json.getAsFloat());
                case "double":
                    return DoubleTag.valueOf(json.getAsDouble());
                case "string":
                    return StringTag.valueOf(json.getAsString());
                case "compound":
                case "auto":
                    if (json.isJsonObject()) {
                        CompoundTag compound = new CompoundTag();
                        for (Map.Entry<String, JsonElement> e : json.getAsJsonObject().entrySet()) {
                            Tag subTag = jsonToNbtTag(e.getValue(), "auto");
                            if (subTag != null)
                                compound.put(e.getKey(), subTag);
                        }
                        return compound;
                    }
                    if (json.isJsonPrimitive()) {
                        JsonPrimitive prim = json.getAsJsonPrimitive();
                        if (prim.isNumber()) {
                            double d = prim.getAsDouble();
                            if (d == Math.floor(d) && d >= Integer.MIN_VALUE && d <= Integer.MAX_VALUE) {
                                return IntTag.valueOf((int) d);
                            }
                            return DoubleTag.valueOf(d);
                        } else if (prim.isString()) {
                            return StringTag.valueOf(prim.getAsString());
                        } else if (prim.isBoolean()) {
                            return ByteTag.valueOf(prim.getAsBoolean() ? (byte) 1 : (byte) 0);
                        }
                    }
                    return null;
                case "list":
                case "array":
                    if (json.isJsonArray()) {
                        ListTag list = new ListTag();
                        for (JsonElement e : json.getAsJsonArray()) {
                            Tag elementTag = jsonToNbtTag(e, "auto");
                            if (elementTag != null)
                                list.add(elementTag);
                        }
                        return list;
                    }
                    return null;
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}