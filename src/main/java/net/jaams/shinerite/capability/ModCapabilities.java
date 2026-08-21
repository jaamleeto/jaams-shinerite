package net.jaams.shinerite.capability;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

import net.jaams.shinerite.capability.glowingcolor.IGlowingColor;

@Mod.EventBusSubscriber(modid = "jaams_shinerite", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCapabilities {
	@SubscribeEvent
	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.register(IGlowingColor.class);
	}
}
