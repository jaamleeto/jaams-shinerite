package net.jaams.shinerite.init;

import com.mojang.serialization.Codec;
import net.jaams.shinerite.JaamsShineriteMod;
import net.jaams.shinerite.loot.AddItemLootModifier;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModLootModifiers {

    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS =
        DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, JaamsShineriteMod.MODID);

    public static final RegistryObject<Codec<AddItemLootModifier>> ADD_shinerite_ITEMS =
        LOOT_MODIFIER_SERIALIZERS.register("add_shinerite_items", () -> AddItemLootModifier.CODEC.get());

    public static void register(IEventBus modEventBus) {
        LOOT_MODIFIER_SERIALIZERS.register(modEventBus);
    }
}

