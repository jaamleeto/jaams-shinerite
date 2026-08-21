package net.jaams.shinerite.packet;

import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.entity.Entity;
import net.minecraft.client.Minecraft;

import net.jaams.shinerite.capability.glowingcolor.GlowingColorProvider;

import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class GlowColorPacketHandler {
	public static void handle(GlowColorPacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Minecraft mc = Minecraft.getInstance();
			Entity entity = mc.level.getEntity(msg.entityId);
			if (entity == null && mc.player != null && mc.player.getId() == msg.entityId) {
				entity = mc.player;
			}
			if (entity != null) {
				entity.getCapability(GlowingColorProvider.GLOWING_COLOR).ifPresent(glowingColor -> {
					glowingColor.setColor(msg.color);
				});
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
