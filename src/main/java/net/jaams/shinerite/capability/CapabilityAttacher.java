package net.jaams.shinerite.capability;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;

import net.minecraft.resources.ResourceLocation;

import net.jaams.shinerite.capability.glowingcolor.GlowingColorProvider;

@Mod.EventBusSubscriber(modid = "jaams_shinerite", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityAttacher {
	public static final ResourceLocation GLOWING_COLOR_CAP = new ResourceLocation("jaams_shinerite", "glowing_color");

	@SubscribeEvent
	public static void onAttachCapabilities(AttachCapabilitiesEvent<net.minecraft.world.entity.Entity> event) {
		event.addCapability(GLOWING_COLOR_CAP, new GlowingColorProvider());
	}
}
