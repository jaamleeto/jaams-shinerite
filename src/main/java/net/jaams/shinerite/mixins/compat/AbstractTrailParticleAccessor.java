package net.jaams.shinerite.mixins.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.client.particle.AbstractTrailParticle;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;

/**
 * Gives access to the trail particle's {@code owner} and {@code trailInfo}, both declared in
 * {@link AbstractTrailParticle}. The trail mixin targets the subclass, so they cannot be shadowed
 * from there.
 */
@Mixin(value = AbstractTrailParticle.class, remap = false)
public interface AbstractTrailParticleAccessor {

	@Accessor("owner")
	EntityPatch<?> jaams$getOwner();

	@Accessor("trailInfo")
	TrailInfo jaams$getTrailInfo();
}