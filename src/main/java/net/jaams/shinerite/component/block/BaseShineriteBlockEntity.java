package net.jaams.shinerite.component.block;

import org.joml.Vector3f;

import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.RandomSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.Connection;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.BlockPos;

import net.jaams.shinerite.configuration.common.MechanicsConfig;
import net.jaams.shinerite.particle.CustomEffectParticleData;

public class BaseShineriteBlockEntity extends BlockEntity implements BlockEntityTicker<BaseShineriteBlockEntity> {
	private static final float APPLY_SOUND_VOLUME = 0.8F;
	private static final float APPLY_SOUND_PITCH = 1.25F;

	private int color = -1;
	private boolean waxed = false;
	private int ticksUnderRain;
	private long lastWaxParticleTime = 0;
	private ListTag imbuedEffects = null;
	private int imbuedUses = 0;
	private int maxImbuedUses = 0;
	private int imbuedColor = -1;

	public BaseShineriteBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		this.ticksUnderRain = level != null ? level.random.nextInt(MechanicsConfig.WASHING_TICKS_PER_STEP.get()) : 0;
	}

	public int getColor() {
		return this.color;
	}

	public void setColor(int color) {
		if (this.color != color) {
			this.color = color;
			if (this.level != null) {
				setChanged();
				if (!this.level.isClientSide) {
					sync();
				}
			}
		}
	}

	public long getLastWaxParticleTime() {
		return lastWaxParticleTime;
	}

	public void setLastWaxParticleTime(long time) {
		this.lastWaxParticleTime = time;
	}

	public void setWaxed(boolean waxed) {
		if (this.waxed != waxed) {
			this.waxed = waxed;
			if (this.level != null) {
				setChanged();
				if (!this.level.isClientSide) {
					sync();
				}
			}
		}
	}

	public boolean isWaxed() {
		return this.waxed;
	}

	public boolean isImbued() {
		return imbuedEffects != null && !imbuedEffects.isEmpty() && imbuedUses > 0;
	}

	public ListTag getImbuedEffects() {
		return imbuedEffects;
	}

	public int getImbuedUses() {
		return imbuedUses;
	}

	public int getMaxImbuedUses() {
		return maxImbuedUses;
	}

	public int getImbuedColor() {
		return imbuedColor;
	}

	public void setImbuedState(ListTag effects, int uses, int maxUses, int imbuedColor) {
		this.imbuedEffects = effects;
		this.imbuedUses = uses;
		this.maxImbuedUses = maxUses;
		this.imbuedColor = imbuedColor;
		if (this.level != null) {
			setChanged();
			if (!this.level.isClientSide) {
				sync();
			}
		}
	}

	public void clearImbuedState() {
		this.imbuedEffects = null;
		this.imbuedUses = 0;
		this.maxImbuedUses = 0;
		this.imbuedColor = -1;
		if (this.level != null) {
			setChanged();
			if (!this.level.isClientSide) {
				sync();
			}
		}
	}

	@Override
	public void tick(Level level, BlockPos pos, BlockState state, BaseShineriteBlockEntity blockEntity) {
		if (level.isClientSide)
			return;
		tickWashing(level, pos, state);
		tickImbuedEffects(level, pos, state);
		tickImbuedAmbientParticles(level, pos);
	}

	private void tickImbuedAmbientParticles(Level level, BlockPos pos) {
		if (!MechanicsConfig.IMBUING.get() || !isImbued() || imbuedColor == -1)
			return;
		int particleInterval = MechanicsConfig.IMBUED_BLOCK_PARTICLE_INTERVAL.get();
		if (particleInterval <= 0 || level.getGameTime() % particleInterval != 0)
			return;
		if (level instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(new CustomEffectParticleData(
				((imbuedColor >> 16) & 255) / 255.0F,
				((imbuedColor >> 8) & 255) / 255.0F,
				(imbuedColor & 255) / 255.0F, 0.3F),
				pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 1, 0.3, 0.25, 0.3, 0.01);
		}
	}
	private void tickWashing(Level level, BlockPos pos, BlockState state) {
		if (!MechanicsConfig.WASHING.get() || !MechanicsConfig.WASHING_AFFECT_BLOCKS.get() || isWaxed() || getColor() == -1) {
			ticksUnderRain = 0;
			return;
		}
		boolean shouldDecay = false;
		if (MechanicsConfig.WASHING_RAIN.get() && level.isRaining()) {
			boolean exposedFromAbove = level.isRainingAt(pos.above());
			boolean exposedFromSides = MechanicsConfig.WASHING_RAIN_SIDES.get() && isExposedToRainFromSides(level, pos);
			if (exposedFromAbove || exposedFromSides) {
				shouldDecay = true;
			}
		}
		if (MechanicsConfig.WASHING_WATER.get() && hasWaterNearby(level, pos)) {
			shouldDecay = true;
		}
		if (!shouldDecay) {
			ticksUnderRain = 0;
			return;
		}
		ticksUnderRain++;
		int ticksPerStep = MechanicsConfig.WASHING_TICKS_PER_STEP.get();
		if (ticksUnderRain >= ticksPerStep) {
			if (MechanicsConfig.WASHING_THUNDERSTORM_BOOST.get() && level.isThundering()) {
				lightenColorUnderRainOrWater();
			}
			lightenColorUnderRainOrWater();
			if (isImbued()) {
				clearImbuedState();
			}
			ticksUnderRain = level.random.nextInt(Math.max(0, ticksPerStep / 5));
		}
	}

	private void tickImbuedEffects(Level level, BlockPos pos, BlockState state) {
		if (!MechanicsConfig.IMBUING.get() || !isImbued())
			return;
		VoxelShape shape = state.getShape(level, pos);
		AABB blockAABB = shape.isEmpty() ? new AABB(pos) : shape.bounds().move(pos);
		AABB searchAABB = blockAABB.inflate(0.01);
		var entities = level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, searchAABB);
		if (entities.isEmpty())
			return;
		int minTicks = MechanicsConfig.IMBUED_MIN_DURATION_TICKS.get();
		boolean appliedToAny = false;
		for (net.minecraft.world.entity.LivingEntity target : entities) {
			if (!target.getBoundingBox().intersects(searchAABB))
				continue;
			if (target instanceof net.minecraft.world.entity.player.Player && !MechanicsConfig.IMBUED_BLOCK_APPLY_TO_PLAYERS.get())
				continue;
			if (target instanceof net.minecraft.world.entity.animal.Animal && !MechanicsConfig.IMBUED_BLOCK_APPLY_TO_PASSIVE.get())
				continue;
			if (target instanceof net.minecraft.world.entity.monster.Monster && !MechanicsConfig.IMBUED_BLOCK_APPLY_TO_HOSTILE.get())
				continue;
			for (int i = 0; i < imbuedEffects.size(); i++) {
				CompoundTag effectData = imbuedEffects.getCompound(i);
				net.minecraft.world.effect.MobEffectInstance savedEffect = net.minecraft.world.effect.MobEffectInstance.load(effectData);
				if (savedEffect == null)
					continue;
				net.minecraft.world.effect.MobEffect effect = savedEffect.getEffect();
				int finalDuration = Math.max(minTicks, savedEffect.getDuration());
				if (effect.isInstantenous()) {
					effect.applyInstantenousEffect(null, null, target, savedEffect.getAmplifier(), 1.0);
				} else {
					target.addEffect(new net.minecraft.world.effect.MobEffectInstance(effect, finalDuration, savedEffect.getAmplifier(), savedEffect.isAmbient(), savedEffect.isVisible(), savedEffect.showIcon()));
				}
			}
			appliedToAny = true;
		}
		if (!appliedToAny)
			return;
		spawnApplyFeedback(level, pos);
		clearImbuedState();
	}

	private void spawnApplyFeedback(Level level, BlockPos pos) {
		level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, APPLY_SOUND_VOLUME, APPLY_SOUND_PITCH);
		if (!(level instanceof ServerLevel serverLevel) || imbuedColor == -1)
			return;
		float r = ((imbuedColor >> 16) & 255) / 255.0F;
		float g = ((imbuedColor >> 8) & 255) / 255.0F;
		float b = (imbuedColor & 255) / 255.0F;
		serverLevel.sendParticles(new CustomEffectParticleData(r, g, b, 0.3F),
			pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5, 10, 0.35, 0.25, 0.35, 0.12);
	}

	private boolean isExposedToRainFromSides(Level level, BlockPos pos) {
		BlockPos[] sidePositions = new BlockPos[]{pos.north(), pos.south(), pos.east(), pos.west()};
		for (BlockPos sidePos : sidePositions) {
			BlockState sideState = level.getBlockState(sidePos);
			if (!sideState.isSolidRender(level, sidePos) && level.isRainingAt(sidePos)) {
				return true;
			}
		}
		return false;
	}

	private boolean hasWaterNearby(Level level, BlockPos pos) {
		BlockPos[] adjacentPositions = new BlockPos[]{pos.north(), pos.south(), pos.east(), pos.west(), pos.above(), pos.below()};
		for (BlockPos adjacentPos : adjacentPositions) {
			if (level.getFluidState(adjacentPos).is(FluidTags.WATER)) {
				return true;
			}
		}
		return false;
	}

	private void lightenColorUnderRainOrWater() {
		if (color == -1)
			return;
		int red = (color >> 16) & 0xFF;
		int green = (color >> 8) & 0xFF;
		int blue = color & 0xFF;
		red = Math.min(255, red + 10);
		green = Math.min(255, green + 10);
		blue = Math.min(255, blue + 10);
		int newColor = (red << 16) | (green << 8) | blue;
		if (newColor == 0xFFFFFF) {
			setColor(-1);
		} else {
			setColor(newColor);
		}
		spawnDustParticles();
	}

	private void spawnDustParticles() {
		if (this.level == null || this.level.isClientSide || color == -1) {
			return;
		}
		ServerLevel serverLevel = (ServerLevel) this.level;
		BlockState state = getBlockState();
		VoxelShape shape = state.getShape(serverLevel, worldPosition);
		AABB aabb = shape.bounds().inflate(0.2);
		RandomSource random = serverLevel.random;
		float red = ((color >> 16) & 255) / 255.0F;
		float green = ((color >> 8) & 255) / 255.0F;
		float blue = (color & 255) / 255.0F;
		Vector3f colorVec = new Vector3f(red, green, blue);
		int particleCount = random.nextInt(20, 41);
		for (int i = 0; i < particleCount; i++) {
			double x = worldPosition.getX() + aabb.minX + random.nextDouble() * (aabb.maxX - aabb.minX);
			double y = worldPosition.getY() + aabb.minY + random.nextDouble() * (aabb.maxY - aabb.minY);
			double z = worldPosition.getZ() + aabb.minZ + random.nextDouble() * (aabb.maxZ - aabb.minZ);
			double motionY = 0.08 + random.nextDouble() * 0.12;
			double motionXZ = random.nextGaussian() * 0.02;
			serverLevel.sendParticles(new DustParticleOptions(colorVec, 1.0F), x, y, z, 1, motionXZ, motionY, motionXZ, 0.0);
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putInt("Color", this.color);
		tag.putBoolean("Waxed", this.waxed);
		tag.putInt("TicksUnderRain", this.ticksUnderRain);
		if (this.imbuedEffects != null && !this.imbuedEffects.isEmpty()) {
			tag.put("ImbuedEffects", this.imbuedEffects);
			tag.putInt("ImbuedUses", this.imbuedUses);
			tag.putInt("MaxImbuedUses", this.maxImbuedUses);
			tag.putInt("ImbuedColor", this.imbuedColor);
		}
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		if (tag.contains("Color", Tag.TAG_INT)) {
			this.color = tag.getInt("Color");
		}
		if (tag.contains("Waxed", Tag.TAG_BYTE)) {
			this.waxed = tag.getBoolean("Waxed");
		}
		if (tag.contains("TicksUnderRain", Tag.TAG_INT)) {
			this.ticksUnderRain = tag.getInt("TicksUnderRain");
		}
		if (tag.contains("ImbuedEffects", Tag.TAG_LIST)) {
			this.imbuedEffects = tag.getList("ImbuedEffects", Tag.TAG_COMPOUND);
			this.imbuedUses = tag.getInt("ImbuedUses");
			this.maxImbuedUses = tag.getInt("MaxImbuedUses");
			this.imbuedColor = tag.getInt("ImbuedColor");
		} else {
			this.imbuedEffects = null;
			this.imbuedUses = 0;
			this.maxImbuedUses = 0;
			this.imbuedColor = -1;
		}
	}

	public void sync() {
		if (this.level != null) {
			this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
		}
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		CompoundTag tag = pkt.getTag();
		if (tag != null) {
			this.load(tag);
		}
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.saveWithFullMetadata();
	}
}
