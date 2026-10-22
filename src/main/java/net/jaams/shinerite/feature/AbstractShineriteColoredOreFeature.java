package net.jaams.shinerite.feature;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import net.jaams.shinerite.component.block.BaseShineriteBlockEntity;
import net.jaams.shinerite.util.ModUtils;

import com.mojang.serialization.Codec;



public abstract class AbstractShineriteColoredOreFeature extends Feature<NoneFeatureConfiguration> {
	private static final int NEIGHBOUR_SEARCH = 2;
	private static final ThreadLocal<Direction[]> DIRECTIONS = ThreadLocal.withInitial(Direction::values);


	private static final MobEffect[] ORE_IMBUEMENT_EFFECTS = {
			MobEffects.POISON, MobEffects.DIG_SPEED, MobEffects.HARM,
			MobEffects.LUCK, MobEffects.BLINDNESS, MobEffects.NIGHT_VISION,
			MobEffects.CONFUSION, MobEffects.GLOWING
	};

	private static final double ORE_IMBUEMENT_CHANCE = 0.005D;

	private final BlockState oreBlock;
	private final int[] palette;
	private final Supplier<Boolean> enabled;
	private final Supplier<Double> chance;

	private final Supplier<Double> coloredChance;
	private final IntSupplier minSize;
	private final IntSupplier maxSize;
	private final IntSupplier spread;

	protected AbstractShineriteColoredOreFeature(Codec<NoneFeatureConfiguration> codec, BlockState oreBlock, int[] palette, Supplier<Boolean> enabled,
			Supplier<Double> chance, Supplier<Double> coloredChance, IntSupplier minSize, IntSupplier maxSize, IntSupplier spread) {
		super(codec);
		this.oreBlock = oreBlock;
		this.palette = palette;
		this.enabled = enabled;
		this.chance = chance;
		this.coloredChance = coloredChance;
		this.minSize = minSize;
		this.maxSize = maxSize;
		this.spread = spread;
	}


	protected abstract boolean isValidGround(BlockState state);

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		if (level == null || !enabled.get()) {
			return false;
		}
		RandomSource random = context.random();
		if (random.nextFloat() >= chance.get()) {
			return false;
		}
		BlockPos origin = context.origin();
		int attempts = rollInt(random, minSize.getAsInt(), maxSize.getAsInt());
		int spread = this.spread.getAsInt();
		boolean colored = random.nextDouble() < coloredChance.get();
		int color = colored ? palette[random.nextInt(palette.length)] : -1;
		BlockPos.MutableBlockPos target = new BlockPos.MutableBlockPos();
		target.set(origin);
		if (!findGround(level, target, spread)) {
			return false;
		}
		boolean placed = false;
		for (int i = 0; i < attempts; i++) {
			placed |= placeOreWithColor(level, random, target, color);
			stepToNeighbour(level, random, target, spread);
			if (!isValidGround(level.getBlockState(target))) {
				return placed;
			}
		}
		return placed;
	}


	private void stepToNeighbour(WorldGenLevel level, RandomSource random, BlockPos.MutableBlockPos target, int spread) {
		Direction[] directions = DIRECTIONS.get();
		shuffle(random, directions);
		for (Direction direction : directions) {
			BlockPos candidate = target.relative(direction);
			if (isValidGround(level.getBlockState(candidate))) {
				target.set(candidate);
				return;
			}
		}
		findGround(level, target, spread);
	}

	private static void shuffle(RandomSource random, Direction[] array) {
		for (int i = array.length - 1; i > 0; i--) {
			int j = random.nextInt(i + 1);
			Direction tmp = array[i];
			array[i] = array[j];
			array[j] = tmp;
		}
	}

	private boolean findGround(WorldGenLevel level, BlockPos.MutableBlockPos target, int spread) {
		if (isValidGround(level.getBlockState(target))) {
			return true;
		}
		int reach = Math.max(1, Math.min(spread, NEIGHBOUR_SEARCH));
		for (int dy = -reach; dy <= reach; dy++) {
			for (int dx = -reach; dx <= reach; dx++) {
				for (int dz = -reach; dz <= reach; dz++) {
					target.move(dx, dy, dz);
					boolean valid = isValidGround(level.getBlockState(target));
					target.move(-dx, -dy, -dz);
					if (valid) {
						target.move(dx, dy, dz);
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean placeOreWithColor(WorldGenLevel level, RandomSource random, BlockPos pos, int color) {
		if (level.getBlockState(pos).hasBlockEntity()) {
			return false;
		}

		if (level.getBlockState(pos.above()).isAir() || level.getBlockState(pos.below()).isAir()
				|| level.getBlockState(pos.north()).isAir() || level.getBlockState(pos.south()).isAir()
				|| level.getBlockState(pos.west()).isAir() || level.getBlockState(pos.east()).isAir()) {
			if (random.nextFloat() < 0.5F) {
				return false;
			}
		}

		if (!level.setBlock(pos, oreBlock, 2)) {
			return false;
		}

		BlockState placedState = level.getBlockState(pos);
		if (!placedState.is(oreBlock.getBlock())) {
			return false;
		}

		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity == null) {
			return false;
		}

		if (!blockEntity.getType().isValid(placedState)) {
			if (level instanceof Level realLevel) {
				realLevel.removeBlockEntity(pos);
			}
			return false;
		}

		if (blockEntity instanceof BaseShineriteBlockEntity shineriteBlockEntity) {
			if (color != -1) {
				shineriteBlockEntity.setColor(color);
			}
			ModUtils.applyNaturalImbuement(shineriteBlockEntity, random, ORE_IMBUEMENT_EFFECTS, ORE_IMBUEMENT_CHANCE);
			shineriteBlockEntity.setChanged();
			return true;
		}
		return false;
	}

	private static int rollInt(RandomSource random, int min, int max) {
		int low = Math.min(min, max);
		int high = Math.max(min, max);
		return high <= low ? low : random.nextInt(high - low + 1) + low;
	}
}
