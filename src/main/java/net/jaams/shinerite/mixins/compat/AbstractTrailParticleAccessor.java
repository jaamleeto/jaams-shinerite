package net.jaams.shinerite.mixins.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.client.particle.AbstractTrailParticle;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;



@Mixin(value = AbstractTrailParticle.class, remap = false)
public interface AbstractTrailParticleAccessor {

	@Accessor("owner")
	EntityPatch<?> jaams$getOwner();

	@Accessor("trailInfo")
	TrailInfo jaams$getTrailInfo();
}
