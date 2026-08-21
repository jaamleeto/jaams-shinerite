package net.jaams.shinerite.util;

import net.jaams.shinerite.configuration.common.TraitsConfig;
import net.jaams.shinerite.data.TraitModifierData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeConfigSpec;

public class ModTraits {

    public static boolean isFrailglowItem(ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return false;
        if (!TraitsConfig.FRAILGLOW.get())
            return false;
        if (TraitModifierData.isTraitDisabled(stack, "frailglow"))
            return false;
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("FrailglowTrait")) {
            return tag.getBoolean("FrailglowTrait");
        }
        if (TraitModifierData.isTraitActive(stack, "frailglow")) {
            return true;
        }
        return stack.is(ModTags.FRAILGLOW);
    }

    public static boolean isThrowableItem(ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return false;
        if (!TraitsConfig.THROWABLE.get())
            return false;
        if (TraitModifierData.isTraitDisabled(stack, "throwable"))
            return false;
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("ThrowableTrait")) {
            return tag.getBoolean("ThrowableTrait");
        }
        if (TraitModifierData.isTraitActive(stack, "throwable")) {
            return true;
        }
        return false;
    }

    public static boolean isThrowbackItem(ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return false;
        if (!TraitsConfig.THROWBACK.get())
            return false;
        if (TraitModifierData.isTraitDisabled(stack, "throwback"))
            return false;
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("ThrowbackTrait")) {
            return tag.getBoolean("ThrowbackTrait");
        }
        if (TraitModifierData.isTraitActive(stack, "throwback")) {
            return true;
        }
        return false;
    }

    public static boolean isRadiantCollectorItem(ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return false;
        if (!TraitsConfig.RADIANT_COLLECTOR.get())
            return false;
        if (TraitModifierData.isTraitDisabled(stack, "radiant_collector"))
            return false;
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("RadiantCollectorTrait")) {
            return tag.getBoolean("RadiantCollectorTrait");
        }
        if (TraitModifierData.isTraitActive(stack, "radiant_collector")) {
            return true;
        }
        return false;
    }
}
