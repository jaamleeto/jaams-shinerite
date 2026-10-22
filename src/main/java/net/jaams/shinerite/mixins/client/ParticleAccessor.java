package net.jaams.shinerite.mixins.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.particle.Particle;



@Mixin(Particle.class)
public interface ParticleAccessor {

	@Accessor("rCol")
	float jaams$getRedCol();

	@Accessor("gCol")
	float jaams$getGreenCol();

	@Accessor("bCol")
	float jaams$getBlueCol();

	@Accessor("rCol")
	void jaams$setRedCol(float color);

	@Accessor("gCol")
	void jaams$setGreenCol(float color);

	@Accessor("bCol")
	void jaams$setBlueCol(float color);
}
