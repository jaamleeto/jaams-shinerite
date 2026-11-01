package net.jaams.shinerite.sync;

import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import net.jaams.shinerite.network.CommonConfigSyncMessage;
import net.jaams.shinerite.JaamsShineriteMod;

@Mod.EventBusSubscriber(modid = JaamsShineriteMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class JaamsShineriteConfigSyncReloader {
	@SubscribeEvent
	public static void onConfigReloading(ModConfigEvent.Reloading event) {
		if (event.getConfig().getType() != ModConfig.Type.COMMON) {
			return;
		}
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server == null || !server.isDedicatedServer()) {
			return;
		}
		byte[] data = CommonConfigSyncMessage.getServerConfigBytes();
		if (data == null) {
			return;
		}
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			JaamsShineriteMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new CommonConfigSyncMessage(data));
		}
	}
}
