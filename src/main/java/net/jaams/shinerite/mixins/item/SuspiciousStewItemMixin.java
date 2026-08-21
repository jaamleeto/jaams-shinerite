package net.jaams.shinerite.mixins.item;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

import org.joml.Vector3f;

import org.checkerframework.checker.units.qual.g;

import net.minecraftforge.network.PacketDistributor;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.DustParticleOptions;

import net.jaams.shinerite.packet.GlowColorPacket;
import net.jaams.shinerite.capability.glowingcolor.GlowingColorProvider;
import net.jaams.shinerite.JaamsShineriteMod;

@Mixin(SuspiciousStewItem.class)
public class SuspiciousStewItemMixin {
	@Inject(method = "finishUsingItem", at = @At("HEAD"))
	private void injectFinishUsingItem(ItemStack stack, Level world, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
		if (!world.isClientSide && entity instanceof Player player && stack.hasTag() && stack.getTag().contains("FlowerColor")) {
			int color = stack.getTag().getInt("FlowerColor");
			applyGlowingColorEffect(player, world, color);
		}
	}

	private void applyGlowingColorEffect(Player player, Level world, int color) {
		player.getCapability(GlowingColorProvider.GLOWING_COLOR).ifPresent(glowingColor -> {
			glowingColor.setColor(color);
			if (world instanceof ServerLevel) {
				JaamsShineriteMod.PACKET_HANDLER.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new GlowColorPacket(player.getId(), color));
			}
		});
		if (color != -1 && world instanceof ServerLevel serverWorld) {
			spawnColorParticles(serverWorld, player, color);
		}
	}

	private void spawnColorParticles(ServerLevel world, Player player, int color) {
		float r = ((color >> 16) & 255) / 255.0F;
		float g = ((color >> 8) & 255) / 255.0F;
		float b = (color & 255) / 255.0F;
		for (int i = 0; i < 5; i++) {
			double offsetX = player.getLookAngle().x * 0.5;
			double offsetY = player.getEyeY() - 0.2;
			double offsetZ = player.getLookAngle().z * 0.5;
			world.sendParticles(new DustParticleOptions(new Vector3f(r, g, b), 1.2F), player.getX() + offsetX, offsetY, player.getZ() + offsetZ, 1, world.random.nextGaussian() * 0.02, world.random.nextGaussian() * 0.02,
					world.random.nextGaussian() * 0.02, 0.0);
		}
	}
}
