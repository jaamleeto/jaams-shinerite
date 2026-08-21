package net.jaams.shinerite.loot;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.loot.IGlobalLootModifier;

import net.jaams.shinerite.JaamsShineriteMod;

import com.mojang.serialization.Codec;

public class LootModifiers {
	public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, JaamsShineriteMod.MODID);
	public static final RegistryObject<Codec<AddItemLootModifier>> ADD_ITEM = LOOT_MODIFIER_SERIALIZERS.register("add_item", () -> AddItemLootModifier.CODEC.get());

	public static void register(IEventBus modEventBus) {
		LOOT_MODIFIER_SERIALIZERS.register(modEventBus);
	}
}
