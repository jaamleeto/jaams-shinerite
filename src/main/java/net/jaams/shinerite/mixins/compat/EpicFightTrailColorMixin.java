package net.jaams.shinerite.mixins.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import net.jaams.shinerite.mixins.client.ParticleAccessor;
import net.jaams.shinerite.util.ModUtils;

import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.client.particle.AnimationTrailParticle;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

/**
 * Tints the weapon trails Epic Fight draws while swinging with the color stored on the Shinerite
 * weapon being used.
 *
 * <p>Epic Fight creates a trail particle per swing from a {@link TrailInfo}, whose
 * {@code rCol/gCol/bCol} come from the animation datapack (white for the default sword trail) and
 * are copied into the particle's color fields at the very end of the constructor. The injection
 * therefore sits at {@code RETURN} of {@link AnimationTrailParticle}'s constructors, so the weapon
 * color wins over the datapack one.
 *
 * <p>{@code owner}/{@code trailInfo} are inherited from {@link AbstractTrailParticle} and the color
 * fields come from the vanilla {@link TextureSheetParticle}, so they are read/written through
 * accessor mixins.
 *
 * <p>Untinted Shinerite weapons (no dye/imbue color) keep Epic Fight's default trail color, so a
 * base Shinerite sword still gets the plain white trail.
 */
@Mixin(value = AnimationTrailParticle.class, remap = false)
public abstract class EpicFightTrailColorMixin {

	@Inject(method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;Lyesman/epicfight/world/capabilities/entitypatch/LivingEntityPatch;Lyesman/epicfight/api/animation/Joint;Lyesman/epicfight/api/asset/AssetAccessor;Lyesman/epicfight/api/client/animation/property/TrailInfo;)V", at = @At("RETURN"), remap = false)
	private void jaams$tintTrail(ClientLevel level, LivingEntityPatch<?> owner, Joint joint,
			AssetAccessor<? extends StaticAnimation> animation, TrailInfo trailInfo, CallbackInfo ci) {
		jaams$applyWeaponColor();
	}

	@Inject(method = "<init>(Lyesman/epicfight/api/model/Armature;Lyesman/epicfight/world/capabilities/entitypatch/LivingEntityPatch;Lyesman/epicfight/api/animation/Joint;Lyesman/epicfight/api/asset/AssetAccessor;Lyesman/epicfight/api/client/animation/property/TrailInfo;)V", at = @At("RETURN"), remap = false)
	private void jaams$tintTrailLegacy(Armature armature, LivingEntityPatch<?> owner, Joint joint,
			AssetAccessor<? extends StaticAnimation> animation, TrailInfo trailInfo, CallbackInfo ci) {
		jaams$applyWeaponColor();
	}

	@Unique
	private void jaams$applyWeaponColor() {
		if (!(this instanceof AbstractTrailParticleAccessor trailAccessor) || !(this instanceof ParticleAccessor colorAccessor)) {
			return;
		}

		TrailInfo trailInfo = trailAccessor.jaams$getTrailInfo();
		Entity original = trailAccessor.jaams$getOwner() == null ? null : trailAccessor.jaams$getOwner().getOriginal();
		if (!(original instanceof LivingEntity entity)) {
			return;
		}

		InteractionHand hand = trailInfo != null && trailInfo.hand() != null ? trailInfo.hand() : InteractionHand.MAIN_HAND;
		ItemStack stack = entity.getItemInHand(hand);
		if (stack.isEmpty()) {
			stack = entity.getMainHandItem();
		}
		if (stack.isEmpty() || !ModUtils.isShineriteItem(stack)) {
			return;
		}

		int color = ModUtils.getCurrentColor(stack);
		if (color == -1) {
			return;
		}
		colorAccessor.jaams$setRedCol((color >> 16 & 255) / 255.0F);
		colorAccessor.jaams$setGreenCol((color >> 8 & 255) / 255.0F);
		colorAccessor.jaams$setBlueCol((color & 255) / 255.0F);
	}
}