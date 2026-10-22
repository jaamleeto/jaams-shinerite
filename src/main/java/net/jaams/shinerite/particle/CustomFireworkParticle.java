package net.jaams.shinerite.particle;


import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.multiplayer.ClientLevel;

@OnlyIn(Dist.CLIENT)
public class CustomFireworkParticle extends SimpleAnimatedParticle {
	private final float rCol, gCol, bCol;
	private final SpriteSet spriteSet;

	public static CustomFireworkParticleProvider provider(SpriteSet spriteSet) {
		return new CustomFireworkParticleProvider(spriteSet);
	}

	public static class CustomFireworkParticleProvider implements ParticleProvider<CustomFireworkParticleData> {
		private final SpriteSet spriteSet;

		public CustomFireworkParticleProvider(SpriteSet spriteSet) {
			this.spriteSet = spriteSet;
		}

		@Override
		public Particle createParticle(CustomFireworkParticleData data, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			return new CustomFireworkParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, data.getR(), data.getG(), data.getB(), data.getSize(), this.spriteSet);
		}
	}

	protected CustomFireworkParticle(ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, float r, float g, float b, float size, SpriteSet spriteSet) {
		super(world, x, y, z, spriteSet, 0.0F);
		this.rCol = r;
		this.gCol = g;
		this.bCol = b;
		this.spriteSet = spriteSet;
		this.setColor(r, g, b);
		this.setSize(size, size);
		this.quadSize *= 0.75F;
		this.lifetime = 48 + this.random.nextInt(12);
		this.gravity = 0.0F;
		this.hasPhysics = false;
		this.xd = xSpeed + (this.random.nextFloat() - 0.5F) * 0.1;
		this.yd = ySpeed + (this.random.nextFloat() - 0.5F) * 0.1;
		this.zd = zSpeed + (this.random.nextFloat() - 0.5F) * 0.1;
		this.alpha = 0.99F;
		this.setSpriteFromAge(spriteSet);
	}

	@Override
	public void tick() {
		super.tick();
		float ageProgress = this.age / (float) this.lifetime;
		this.alpha = 0.99F - ageProgress * 0.5F;
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
