package net.jaams.shinerite.mixins.particle;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

import org.checkerframework.checker.units.qual.g;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.multiplayer.ClientLevel;

import net.jaams.shinerite.component.block.BaseShineriteStairBlock;
import net.jaams.shinerite.component.block.BaseShineriteSlabBlock;
import net.jaams.shinerite.component.block.BaseShineriteFlowerBlock;
import net.jaams.shinerite.component.block.BaseShineriteBlockEntity;
import net.jaams.shinerite.component.block.BaseShineriteBlock;

@Mixin(TerrainParticle.class)
public abstract class TerrainParticleMixin extends TextureSheetParticle {
	private boolean isTintedBlock = false;

	protected TerrainParticleMixin(ClientLevel level, double x, double y, double z) {
		super(level, x, y, z);
	}

	@Inject(method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V", at = @At("TAIL"))
	private void onInit(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, BlockState blockState, BlockPos pos, CallbackInfo ci) {
		if (blockState.getBlock() instanceof BaseShineriteBlock || blockState.getBlock() instanceof BaseShineriteFlowerBlock || blockState.getBlock() instanceof BaseShineriteSlabBlock || blockState.getBlock() instanceof BaseShineriteStairBlock) {
			this.isTintedBlock = true;
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity instanceof BaseShineriteBlockEntity shineriteBaseBlockEntity) {
				int color = shineriteBaseBlockEntity.getColor();
				if (color != -1) {
					float r = ((color >> 16) & 255) / 255.0F;
					float g = ((color >> 8) & 255) / 255.0F;
					float b = (color & 255) / 255.0F;
					this.rCol = r;
					this.gCol = g;
					this.bCol = b;
				}
			} else {
				this.alpha = 0.0F;
			}
		}
	}

	@Override
	public int getLightColor(float partialTick) {
		if (this.isTintedBlock) {
			return 15728880;
		}
		return super.getLightColor(partialTick);
	}
}
