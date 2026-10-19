package net.jaams.shinerite.capability;

import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.server.level.ServerPlayer;

import net.jaams.shinerite.packet.GlowColorPacket;
import net.jaams.shinerite.network.SyncModDataMessage;
import net.jaams.shinerite.capability.glowingcolor.GlowingColorProvider;
import net.jaams.shinerite.JaamsShineriteMod;

@Mod.EventBusSubscriber(modid = "jaams_shinerite", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityEvents {
	private static void syncGlowColor(LivingEntity entity, int color) {
		if (entity instanceof ServerPlayer player) {
			JaamsShineriteMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new GlowColorPacket(entity.getId(), color));
			JaamsShineriteMod.PACKET_HANDLER.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), new GlowColorPacket(entity.getId(), color));
		} else {
			JaamsShineriteMod.PACKET_HANDLER.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), new GlowColorPacket(entity.getId(), color));
		}
	}

	@SubscribeEvent
	public static void onPotionEffectChange(MobEffectEvent.Expired event) {
		Entity entity = event.getEntity();
		if (entity.level().isClientSide) {
			return;
		}
		MobEffectInstance effectInstance = event.getEffectInstance();
		if (effectInstance != null && effectInstance.getEffect() == MobEffects.GLOWING) {
			entity.getCapability(GlowingColorProvider.GLOWING_COLOR).ifPresent(glowingColor -> {
				JaamsShineriteMod.queueServerWork(5, () -> {
					glowingColor.setColor(-1);
					syncGlowColor((LivingEntity) entity, -1);
				});
			});
		}
	}

	@SubscribeEvent
	public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
		if (!(event.getEntity() instanceof LivingEntity entity) || entity.level().isClientSide) {
			return;
		}
		entity.getCapability(GlowingColorProvider.GLOWING_COLOR).ifPresent(glowingColor -> {
			if (glowingColor.hasCustomColor() && entity.hasEffect(MobEffects.GLOWING)) {
				syncGlowColor(entity, glowingColor.getColor());
			}
		});
	}

	@SubscribeEvent
	public static void onStartTracking(PlayerEvent.StartTracking event) {
		if (!(event.getTarget() instanceof LivingEntity target) || target.level().isClientSide) {
			return;
		}
		target.getCapability(GlowingColorProvider.GLOWING_COLOR).ifPresent(glowingColor -> {
			if (glowingColor.hasCustomColor() && target.hasEffect(MobEffects.GLOWING)) {
				JaamsShineriteMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) event.getEntity()), new GlowColorPacket(target.getId(), glowingColor.getColor()));
			}
		});
	}

	@SubscribeEvent
	public static void onModDataSync(OnDatapackSyncEvent event) {
		for (ServerPlayer player : event.getPlayers()) {
			if (player.level().isClientSide) {
				continue;
			}
			JaamsShineriteMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), SyncModDataMessage.fromServerState());
		}
	}

	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		if (player.level().isClientSide) {
			return;
		}
		player.getCapability(GlowingColorProvider.GLOWING_COLOR).ifPresent(glowingColor -> {
			if (glowingColor.hasCustomColor() && player.hasEffect(MobEffects.GLOWING)) {
				syncGlowColor(player, glowingColor.getColor());
			}
		});
	}
}
