package net.jaams.shinerite.mixins.particle;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

import org.checkerframework.checker.units.qual.g;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.multiplayer.ClientLevel;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.dyeable.IDyeableItem;

@Mixin(BreakingItemParticle.class)
public abstract class BreakingItemParticleMixin extends TextureSheetParticle {
	private boolean isDyeable = false;

	protected BreakingItemParticleMixin(ClientLevel level, double x, double y, double z) {
		super(level, x, y, z);
	}

	@Inject(method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDLnet/minecraft/world/item/ItemStack;)V", at = @At("TAIL"))
	private void onInit(ClientLevel level, double x, double y, double z, ItemStack stack, CallbackInfo ci) {
		Item item = stack.getItem();
		if (item instanceof IDyeableItem || ModUtils.isShineriteItem(stack)) {
			this.isDyeable = true;
			int color = ModUtils.getCurrentColor(stack);
			if (color == -1) {
				color = ModUtils.getImbuedColor(stack);
			}
			if (color != -1) {
				float r = ((color >> 16) & 255) / 255.0F;
				float g = ((color >> 8) & 255) / 255.0F;
				float b = (color & 255) / 255.0F;
				this.rCol = r;
				this.gCol = g;
				this.bCol = b;
			}
		}
	}

	@Override
	public int getLightColor(float partialTick) {
		if (this.isDyeable) {
			return 15728880;
		}
		return super.getLightColor(partialTick);
	}
}
