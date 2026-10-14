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
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import net.jaams.shinerite.configuration.common.MechanicsConfig;
import net.jaams.shinerite.configuration.common.InteractionsConfig;
import net.jaams.shinerite.particle.CustomEffectParticleData;
import net.jaams.shinerite.util.ModUtils;

public class BaseShineriteBlockEntity extends BlockEntity implements BlockEntityTicker<BaseShineriteBlockEntity> {
	private static final float APPLY_SOUND_VOLUME = 0.8F;
	private static final float APPLY_SOUND_PITCH = 1.25F;

	private int color = -1;
	private boolean coated = false;
	private int ticksUnderRain;
	private long lastCoatedParticleTime = 0;
	private ListTag imbuedEffects = null;
	private int imbuedUses = 0;
	private int maxImbuedUses = 0;
	private int imbuedColor = -1;

	public BaseShineriteBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		this.ticksUnderRain = 0;
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

	public long getLastCoatedParticleTime() {
		return lastCoatedParticleTime;
	}

	public void setLastCoatedParticleTime(long time) {
		this.lastCoatedParticleTime = time;
	}

	public void setCoated(boolean coated) {
		if (this.coated != coated) {
			this.coated = coated;
			if (this.level != null) {
				setChanged();
				if (!this.level.isClientSide) {
					sync();
				}
			}
		}
	}

	public boolean isCoated() {
		return this.coated;
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
		if (level == null || level.isClientSide || state.isAir()) {
			return;
		}
		tickWashing(level, pos, state);
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
		boolean hasColor = getColor() != -1;
		boolean imbued = MechanicsConfig.IMBUING.get() && isImbued();
		if (!MechanicsConfig.WASHING.get() || !MechanicsConfig.WASHING_AFFECT_BLOCKS.get() || isCoated() || (!hasColor && !imbued)) {
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
			if (imbued) {
				decayImbuementUnderRainOrWater();
			}
			if (hasColor) {
				if (MechanicsConfig.WASHING_THUNDERSTORM_BOOST.get() && level.isThundering()) {
					lightenColorUnderRainOrWater();
				}
				lightenColorUnderRainOrWater();
			}
			ticksUnderRain = level.random.nextInt(Math.max(0, ticksPerStep / 5));
		}
	}

	private void decayImbuementUnderRainOrWater() {
		int uses = imbuedUses - 1;
		if (uses <= 0) {
			clearImbuedState();
			spawnImbuementRemovalFeedback();
			return;
		}
		int newImbuedColor = imbuedColor;
		if (newImbuedColor != -1) {
			float progress = uses / (float) Math.max(1, maxImbuedUses);
			newImbuedColor = lightenImbuedColor(newImbuedColor, 1.0F - progress);
		}
		setImbuedState(imbuedEffects, uses, maxImbuedUses, newImbuedColor);
	}

	private void spawnImbuementRemovalFeedback() {
		if (this.level == null)
			return;
		this.level.playSound(null, worldPosition, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 0.8F);
		if (this.level instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(new DustParticleOptions(new Vector3f(1.0F, 1.0F, 1.0F), 1.0F),
				worldPosition.getX() + 0.5, worldPosition.getY() + 0.6, worldPosition.getZ() + 0.5, 10, 0.3, 0.25, 0.3, 0.05);
		}
	}

	private static int lightenImbuedColor(int color, float fraction) {
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;
		int minR = r / 2;
		int minG = g / 2;
		int minB = b / 2;
		r = (int) (r + (255 - r) * fraction * 0.15f);
		g = (int) (g + (255 - g) * fraction * 0.15f);
		b = (int) (b + (255 - b) * fraction * 0.15f);
		r = Math.max(minR, Math.min(255, r));
		g = Math.max(minG, Math.min(255, g));
		b = Math.max(minB, Math.min(255, b));
		return (r << 16) | (g << 8) | b;
	}

	public boolean registerDirectContact(net.minecraft.world.entity.LivingEntity target) {
		return registerDirectContact(target, false);
	}

	public boolean registerDirectContact(net.minecraft.world.entity.LivingEntity target, boolean fromLanding) {
		if (this.level == null || this.level.isClientSide || !MechanicsConfig.IMBUING.get() || !isImbued())
			return false;
		if (!isEligibleTarget(target))
			return false;
		if (fromLanding && this.level instanceof ServerLevel serverLevel) {
			ModUtils.generateImbuedBurstParticles(serverLevel, target.getX(), target.getY(), target.getZ(), imbuedColor, 50);
		}
		applyImbuedEffectsTo(target);
		consumeAppliedImbuement(this.level, worldPosition);
		return true;
	}

	/**
	 * Hands the stored effects over to the player that broke this block. Shared by every Shinerite
	 * block type (solid blocks, flowers, roots, potted plants) so the behavior is identical everywhere.
	 *
	 * <p>Duration is scaled by {@link MechanicsConfig#IMBUED_BLOCK_BREAK_DURATION_FACTOR} so breaking is
	 * deliberately weaker than the sneak right-click transfer ({@link #registerDirectContact}), which
	 * always uses the full stored duration. Mining has no skill component, so it should not be the best
	 * way to get a buff.
	 */
	public void transferImbuementToBreaker(Level level, BlockPos pos, Player player) {
		if (level.isClientSide || !isImbued()) {
			return;
		}
		if (!MechanicsConfig.IMBUING.get() || !InteractionsConfig.BLOCK_BREAK_TRANSFER_IMBUEMENT.get()) {
			return;
		}
		if (ModUtils.hasRestrictedEffect(player)) {
			return;
		}
		ListTag effects = getImbuedEffects();
		if (effects == null || effects.isEmpty()) {
			return;
		}
		float factor = MechanicsConfig.IMBUED_BLOCK_BREAK_DURATION_FACTOR.get().floatValue();
		int minTicks = MechanicsConfig.IMBUED_MIN_DURATION_TICKS.get();
		for (int i = 0; i < effects.size(); i++) {
			CompoundTag effectData = effects.getCompound(i);
			MobEffectInstance savedEffect = MobEffectInstance.load(effectData);
			if (savedEffect == null) {
				continue;
			}
			MobEffect effect = savedEffect.getEffect();
			if (effect.isInstantenous()) {
				effect.applyInstantenousEffect(null, null, player, savedEffect.getAmplifier(), 1.0D);
				continue;
			}
			int duration = Math.max(minTicks, (int) (savedEffect.getDuration() * factor));
			player.addEffect(new MobEffectInstance(effect, duration, savedEffect.getAmplifier(), savedEffect.isAmbient(),
					savedEffect.isVisible(), savedEffect.showIcon()));
		}
		if (level instanceof ServerLevel serverLevel && getImbuedColor() != -1) {
			ModUtils.generateImbuedBurstParticles(serverLevel, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
					getImbuedColor(), 30);
		}
	}

	private boolean isEligibleTarget(net.minecraft.world.entity.LivingEntity target) {
		if (target instanceof net.minecraft.world.entity.player.Player && !MechanicsConfig.IMBUED_BLOCK_APPLY_TO_PLAYERS.get())
			return false;
		if (target instanceof net.minecraft.world.entity.animal.Animal && !MechanicsConfig.IMBUED_BLOCK_APPLY_TO_PASSIVE.get())
			return false;
		if (target instanceof net.minecraft.world.entity.monster.Monster && !MechanicsConfig.IMBUED_BLOCK_APPLY_TO_HOSTILE.get())
			return false;
		return true;
	}

	private void applyImbuedEffectsTo(net.minecraft.world.entity.LivingEntity target) {
		int minTicks = MechanicsConfig.IMBUED_MIN_DURATION_TICKS.get();
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
	}

	private void consumeAppliedImbuement(Level level, BlockPos pos) {
		spawnApplyFeedback(level, pos);
		clearImbuedState();
		setColor(-1);
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
		tag.putBoolean("Coated", this.coated);
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
		if (tag.contains("Coated", Tag.TAG_BYTE)) {
			this.coated = tag.getBoolean("Coated");
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
		requestTintRefresh();
	}

	@Override
	public void handleUpdateTag(CompoundTag tag) {
		super.handleUpdateTag(tag);
		requestTintRefresh();
	}

	private void requestTintRefresh() {
		if (this.level instanceof net.minecraft.client.multiplayer.ClientLevel) {
			net.minecraft.client.Minecraft.getInstance().levelRenderer.setSectionDirtyWithNeighbors(
				this.worldPosition.getX() >> 4, this.worldPosition.getY() >> 4, this.worldPosition.getZ() >> 4);
		}
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.saveWithFullMetadata();
	}
}
