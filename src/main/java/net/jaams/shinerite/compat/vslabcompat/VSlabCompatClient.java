package net.jaams.shinerite.compat.vslabcompat;

import net.jaams.shinerite.JaamsShineriteMod;
import net.jaams.shinerite.util.ModUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = JaamsShineriteMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class VSlabCompatClient {
	private static final String CUT_PATH = "jaams_shinerite/cut_shinerite_vertical_slab";
	private static final String TILES_PATH = "jaams_shinerite/shinerite_tile_vertical_slab";

	@SubscribeEvent
	public static void onRegisterBlockColors(RegisterColorHandlersEvent.Block event) {
		if (!isLoaded()) return;
		registerBlockColor(event, CUT_PATH);
		registerBlockColor(event, TILES_PATH);
	}

	@SubscribeEvent
	public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
		if (!isLoaded()) return;
		registerItemColor(event, CUT_PATH);
		registerItemColor(event, TILES_PATH);
	}

	private static boolean isLoaded() {
		return ModList.get().isLoaded(VSlabCompat.VSC_NAMESPACE);
	}

	private static void registerBlockColor(RegisterColorHandlersEvent.Block event, String path) {
		ResourceLocation id = ResourceLocation.fromNamespaceAndPath(VSlabCompat.VSC_NAMESPACE, path);
		Block block = ForgeRegistries.BLOCKS.getValue(id);
		if (block == null || !id.equals(ForgeRegistries.BLOCKS.getKey(block))) {
			JaamsShineriteMod.LOGGER.warn("Vertical Slabs Compat block {} was not registered; skipping color handler", id);
			return;
		}
		event.register((state, level, pos, tintIndex) -> {
			if (tintIndex != 0 || level == null || pos == null) return -1;
			VSlabCompatClientState.Entry entry = VSlabCompatClientState.get(pos);
			if (entry == null) return -1;
			if (entry.imbued && entry.imbuedColor != -1) return entry.imbuedColor;
			return entry.color;
		}, block);
	}

	private static void registerItemColor(RegisterColorHandlersEvent.Item event, String path) {
		ResourceLocation id = ResourceLocation.fromNamespaceAndPath(VSlabCompat.VSC_NAMESPACE, path);
		Block block = ForgeRegistries.BLOCKS.getValue(id);
		if (block == null || !ForgeRegistries.BLOCKS.getKey(block).equals(id)) return;
		Item item = block.asItem();
		if (item == null) return;
		event.register((stack, tintIndex) -> tintIndex == 0 ? ModUtils.getCurrentColor(stack) : -1, item);
	}
}
