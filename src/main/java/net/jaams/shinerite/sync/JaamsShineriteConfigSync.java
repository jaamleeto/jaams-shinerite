package net.jaams.shinerite.sync;

import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;

import net.minecraft.server.level.ServerPlayer;

import net.jaams.shinerite.network.CommonConfigSyncMessage;
import net.jaams.shinerite.JaamsShineriteMod;

@Mod.EventBusSubscriber(modid = JaamsShineriteMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class JaamsShineriteConfigSync {
	@SubscribeEvent
	public static void onModDataSync(OnDatapackSyncEvent event) {
		byte[] data = CommonConfigSyncMessage.getServerConfigBytes();
		if (data == null) {
			return;
		}
		for (ServerPlayer player : event.getPlayers()) {
			if (player.level().isClientSide) {
				continue;
			}
			JaamsShineriteMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new CommonConfigSyncMessage(data));
		}
	}
}
