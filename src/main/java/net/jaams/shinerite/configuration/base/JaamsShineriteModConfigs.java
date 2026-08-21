package net.jaams.shinerite.configuration.base;

import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.jaams.shinerite.JaamsShineriteMod;

@Mod.EventBusSubscriber(modid = JaamsShineriteMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class JaamsShineriteModConfigs {
	@SubscribeEvent
	public static void register(FMLConstructModEvent event) {
		event.enqueueWork(() -> {
			ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, JaamsShineriteClientConfiguration.SPEC, "jaams/jaams_shinerite/jaams_shinerite_client.toml");
			ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, JaamsShineriteCommonConfiguration.SPEC, "jaams/jaams_shinerite/jaams_shinerite_common.toml");
		});
	}
}
