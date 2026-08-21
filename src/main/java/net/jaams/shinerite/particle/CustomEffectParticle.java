package net.jaams.shinerite.particle;

import org.checkerframework.checker.units.qual.g;

import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.multiplayer.ClientLevel;

@OnlyIn(Dist.CLIENT)
public class CustomEffectParticle extends SimpleAnimatedParticle {
	private final float rCol, gCol, bCol;

	public static CustomEffectParticleProvider provider(SpriteSet spriteSet) {
		return new CustomEffectParticleProvider(spriteSet);
	}

	public static class CustomEffectParticleProvider implements ParticleProvider<CustomEffectParticleData> {
		private final SpriteSet spriteSet;

		public CustomEffectParticleProvider(SpriteSet spriteSet) {
			this.spriteSet = spriteSet;
		}

		@Override
		public Particle createParticle(CustomEffectParticleData data, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			return new CustomEffectParticle(worldIn, x, y, z, data.getR(), data.getG(), data.getB(), data.getSize(), this.spriteSet);
		}
	}

	protected CustomEffectParticle(ClientLevel world, double x, double y, double z, float r, float g, float b, float size, SpriteSet spriteSet) {
		super(world, x, y, z, spriteSet, 0.0F);
		this.rCol = r;
		this.gCol = g;
		this.bCol = b;
		this.setColor(r, g, b);
		this.setSpriteFromAge(spriteSet);
		this.quadSize = size;
		this.scale(size + this.random.nextFloat() * 0.2F);
		this.lifetime = 16 + this.random.nextInt(8);
		this.gravity = 0.0F;
		this.hasPhysics = false;
		this.xd = 0.0;
		this.yd = 0.04;
		this.zd = 0.0;
		this.alpha = 0.8F;
	}

	@Override
	public void tick() {
		super.tick();
		float ageProgress = this.age / (float) this.lifetime;
		this.xd = Math.sin(this.age * 0.3 + this.random.nextFloat()) * 0.03;
		this.yd = 0.04 + ageProgress * 0.02;
		this.zd = Math.cos(this.age * 0.3 + this.random.nextFloat()) * 0.03;
		this.alpha = 0.8F - ageProgress * 0.6F;
	}

	@Override
	public int getLightColor(float partialTick) {
		return 15728880;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}
}
