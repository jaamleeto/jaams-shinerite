package net.jaams.shinerite;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;

import net.jaams.shinerite.init.ModTabs;
import net.jaams.shinerite.init.ModItems;
import net.jaams.shinerite.init.ModBlocks;
import net.jaams.shinerite.init.ModBlockEntities;
import net.jaams.shinerite.init.ModLootModifiers;
import net.jaams.shinerite.init.ModRecipes;
import net.jaams.shinerite.init.ModParticles;
import net.jaams.shinerite.packet.GlowColorPacket;
import net.jaams.shinerite.packet.CustomJukeboxPacket;
import net.jaams.shinerite.network.SyncModDataMessage;

import net.jaams.shinerite.init.ModSounds;
import net.jaams.shinerite.init.ModItems;
import net.jaams.shinerite.init.ModEntities;
import net.jaams.shinerite.init.ModEnchantments;

import net.jaams.shinerite.dispenser.ShineriteBlockDispenser;
import net.jaams.shinerite.registry.ShineriteFeaturesRegister;
import net.jaams.shinerite.util.ShineriteTrimCompat;

import java.util.function.Supplier;
import java.util.function.Function;
import java.util.function.BiConsumer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.AbstractMap;

@Mod("jaams_shinerite")
public class JaamsShineriteMod {
	public static final Logger LOGGER = LogManager.getLogger(JaamsShineriteMod.class);
	public static final String MODID = "jaams_shinerite";
	public static ModLoadingContext MOD_LOADING_CONTEXT;

	public static boolean isOwnNamespace(String location) {
		int i = location.indexOf(':');
		String ns = i >= 0 ? location.substring(0, i) : "";
		return MODID.equals(ns);
	}

	public JaamsShineriteMod(FMLJavaModLoadingContext context) {


		MinecraftForge.EVENT_BUS.register(this);
		MOD_LOADING_CONTEXT = context;
		IEventBus bus = context.getModEventBus();
		ModSounds.REGISTRY.register(bus);
		ModEntities.REGISTRY.register(bus);
		ModEnchantments.REGISTRY.register(bus);
		ModTabs.REGISTRY.register(bus);
		ModItems.REGISTRY.register(bus);
		ModBlockEntities.REGISTRY.register(bus);
		ModBlocks.REGISTRY.register(bus);
		ModLootModifiers.register(bus);
		ModRecipes.register(bus);
		ModParticles.register(bus);
		ShineriteFeaturesRegister.FEATURES.register(bus);
		bus.addListener(this::commonSetup);
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			ShineriteTrimCompat.excludeNamespace("oreganized");
		});
	}

	private void commonSetup(final FMLCommonSetupEvent event) {
		((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(ForgeRegistries.BLOCKS.getKey(ModBlocks.SHINERITIA.get()), () -> ModBlocks.POTTED_SHINERITIA.get());
		for (DyeColor dyeColor : DyeColor.values()) {
			Item dyeItem = DyeItem.byColor(dyeColor);
			DispenserBlock.registerBehavior(dyeItem, new ShineriteBlockDispenser());
		}
		DispenserBlock.registerBehavior(Items.HONEYCOMB, new ShineriteBlockDispenser());
		DispenserBlock.registerBehavior(Items.GLOW_INK_SAC, new ShineriteBlockDispenser());
		DispenserBlock.registerBehavior(Items.INK_SAC, new ShineriteBlockDispenser());
		DispenserBlock.registerBehavior(ModItems.SHINY_SCRAPER.get(), new ShineriteBlockDispenser());
		DispenserBlock.registerBehavior(ModItems.SHINY_PAINT_BRUSH.get(), new ShineriteBlockDispenser());
		DispenserBlock.registerBehavior(Items.POTION, new ShineriteBlockDispenser());
		addNetworkMessage(GlowColorPacket.class, GlowColorPacket::encode, GlowColorPacket::decode, GlowColorPacket::handle);
		addNetworkMessage(CustomJukeboxPacket.class, CustomJukeboxPacket::encode, CustomJukeboxPacket::decode, CustomJukeboxPacket::handle);
		addNetworkMessage(SyncModDataMessage.class, SyncModDataMessage::encode, SyncModDataMessage::decode, SyncModDataMessage::handle);
	}

	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(ResourceLocation.fromNamespaceAndPath(MODID, MODID), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	private static int messageID = 0;

	public static <T> void addNetworkMessage(Class<T> messageType, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer) {
		PACKET_HANDLER.registerMessage(messageID, messageType, encoder, decoder, messageConsumer);
		messageID++;
	}

	private static final Collection<AbstractMap.SimpleEntry<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();

	public static void queueServerWork(int tick, Runnable action) {
		if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER)
			workQueue.add(new AbstractMap.SimpleEntry<>(action, tick));
	}

	@SubscribeEvent
	public void tick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			List<AbstractMap.SimpleEntry<Runnable, Integer>> actions = new ArrayList<>();
			workQueue.forEach(work -> {
				work.setValue(work.getValue() - 1);
				if (work.getValue() == 0)
					actions.add(work);
			});
			actions.forEach(e -> e.getKey().run());
			workQueue.removeAll(actions);
		}
	}
}
