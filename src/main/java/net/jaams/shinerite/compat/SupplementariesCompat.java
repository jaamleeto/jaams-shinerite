package net.jaams.shinerite.compat;

import com.mojang.datafixers.util.Pair;

import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.FluidContainerList.Category;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.supplementaries.common.block.blocks.CannonBlock;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.IFireItemBehavior;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundParticlePacket;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

import org.joml.Vector3f;

import net.jaams.shinerite.component.block.BaseShineriteBlockEntity;
import net.jaams.shinerite.component.projectile.BaseWeaponProjectileEntity;
import net.jaams.shinerite.configuration.common.InteractionsConfig;
import net.jaams.shinerite.configuration.common.MechanicsConfig;
import net.jaams.shinerite.configuration.common.ProjectileCommonConfig;
import net.jaams.shinerite.dyeable.IDyeableItem;
import net.jaams.shinerite.entity.ShinerangProjectileEntity;
import net.jaams.shinerite.entity.ShineriteStarProjectileEntity;
import net.jaams.shinerite.init.ModEntities;
import net.jaams.shinerite.init.ModItems;
import net.jaams.shinerite.util.ModProjectiles;
import net.jaams.shinerite.util.ModUtils;

/**
 * Shinerite compatibility with Supplementaries.
 *
 * <p>Only referenced from the compat mixins, so this class is never loaded
 * unless Supplementaries is installed. Soap washing is routed through Moonlight's
 * {@code IWashable} contract for blocks and through Supplementaries' soap-clearing
 * recipe for items. Soft Fluids (faucets) and the cannon are handled here too.</p>
 */
public final class SupplementariesCompat {

	private static final ResourceLocation SOAP_ID = ResourceLocation.fromNamespaceAndPath("supplementaries", "soap");
	private static final String COLOR_TAG = "color";
	private static final String COATED_TAG = "Coated";

	private SupplementariesCompat() {
	}

	/**
	 * Cleans every removable Shinerite state from a placed block: coating (wax),
	 * imbuement and dye color. This is the {@code IWashable} entry point used by
	 * soap; Supplementaries plays the generic soap feedback, so only the specific
	 * wax/color-removal effects are emitted here.
	 *
	 * @return true when something was actually cleaned
	 */
	public static boolean tryWashBlock(Level level, BlockPos pos, BlockState state) {
		BaseShineriteBlockEntity blockEntity = getWashableBlockEntity(level, pos, state);
		return blockEntity != null && washBlock(level, pos, state, blockEntity).changed();
	}

	@Nullable
	private static BaseShineriteBlockEntity getWashableBlockEntity(Level level, BlockPos pos, BlockState state) {
		if (level == null || pos == null || state == null
				|| !InteractionsConfig.BLOCK_INTERACTIONS.get()
				|| state.is(ModTags.SOAP_BLACKLIST_BLOCK)) {
			return null;
		}
		return level.getBlockEntity(pos) instanceof BaseShineriteBlockEntity blockEntity ? blockEntity : null;
	}

	private static WashResult washBlock(Level level, BlockPos pos, BlockState state,
			BaseShineriteBlockEntity blockEntity) {
		boolean removedCoating = blockEntity.isCoated();
		int removedColor = blockEntity.getColor();
		boolean changed = false;

		if (removedCoating) {
			blockEntity.setCoated(false);
			ModUtils.generateCoatedRemovalParticles(level, pos, state, level.random);
			level.playSound(null, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
			changed = true;
		}
		if (blockEntity.isImbued()) {
			blockEntity.clearImbuedState();
			changed = true;
		}
		if (removedColor != -1) {
			blockEntity.setColor(-1);
			ModUtils.generateDustParticles(level, pos, removedColor);
			changed = true;
		}

		if (changed) {
			blockEntity.setChanged();
			level.sendBlockUpdated(pos, state, state, 3);
			if (level instanceof ServerLevel serverLevel) {
				serverLevel.getChunkSource().blockChanged(pos);
			}
		}
		return new WashResult(changed);
	}

	/**
	 * Audiovisual feedback for a faucet fluid that actually changed a block. Water
	 * from a faucet must feel like emptying a water bottle on the block: splashing
	 * and dye-stain sounds plus splash and dust particles around it. Soap keeps
	 * Supplementaries' own soap effects and is deliberately not routed here.
	 */
	private static void playFluidWashEffects(Level level, BlockPos pos, BlockState state) {
		if (level.isClientSide()) {
			return;
		}
		level.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 1.0F, 1.0F);
		level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}
		serverLevel.sendParticles(ParticleTypes.SPLASH, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
				10, 0.2, 0.2, 0.2, 0.0);
		VoxelShape shape = state.getShape(level, pos);
		AABB bounds = shape.bounds().inflate(0.25);
		RandomSource random = serverLevel.random;
		for (int i = 0; i < 50; i++) {
			double x = pos.getX() + bounds.minX + random.nextDouble() * (bounds.maxX - bounds.minX);
			double y = pos.getY() + bounds.minY + random.nextDouble() * (bounds.maxY - bounds.minY);
			double z = pos.getZ() + bounds.minZ + random.nextDouble() * (bounds.maxZ - bounds.minZ);
			serverLevel.sendParticles(new DustParticleOptions(new Vector3f(1.0F, 1.0F, 1.0F), 1.0F),
					x, y, z, 1, 0.0, 0.1, 0.0, 0.0);
		}
	}

	private record WashResult(boolean changed) {
	}

	/**
	 * Plays Supplementaries' own soap-washing sound and suds particles around the
	 * player, matching {@code SoapItem.playEffectsAndConsume}. Called by the hand
	 * interaction handler only when a supplementaries:soap stack is involved.
	 */
	public static void playHandSoapEffects(Player player) {
		Level level = player.level();
		level.playSound(null, player, ModSounds.SOAP_WASH.get(), SoundSource.PLAYERS,
				1.0F, 0.9F + level.random.nextFloat() * 0.3F);
		if (!level.isClientSide()) {
			ModNetwork.CHANNEL.sentToAllClientPlayersTrackingEntityAndSelf(player,
					new ClientBoundParticlePacket(player, ClientBoundParticlePacket.Type.BUBBLE_CLEAN_ENTITY));
		}
	}

	/**
	 * Handles fluid delivered by a Supplementaries faucet. Water washes a Shinerite
	 * block and potions imbue it, with the same config switches as the mod's own
	 * water-bottle mechanics. A coated block is sealed and rejects both fluids.
	 *
	 * @return true only when the fluid actually changed the block
	 */
	public static boolean tryAcceptSoftFluid(Level level, BlockPos pos, BlockState state, SoftFluidStack fluid) {
		if (fluid == null || fluid.isEmpty()) {
			return false;
		}
		BaseShineriteBlockEntity blockEntity = getWashableBlockEntity(level, pos, state);
		if (blockEntity == null || blockEntity.isCoated()) {
			return false;
		}
		if (fluid.is(BuiltInSoftFluids.WATER)) {
			return tryWashWithFluid(level, pos, state, blockEntity);
		}
		if (fluid.is(BuiltInSoftFluids.POTION)) {
			return tryImbueWithPotionFluid(level, pos, blockEntity, fluid);
		}
		return false;
	}

	private static boolean tryWashWithFluid(Level level, BlockPos pos, BlockState state,
			BaseShineriteBlockEntity blockEntity) {
		if (!MechanicsConfig.WASHING.get() || !InteractionsConfig.BLOCK_WASHING.get()) {
			return false;
		}
		WashResult result = washBlock(level, pos, state, blockEntity);
		if (!result.changed()) {
			return false;
		}
		playFluidWashEffects(level, pos, state);
		return true;
	}

	private static boolean tryImbueWithPotionFluid(Level level, BlockPos pos,
			BaseShineriteBlockEntity blockEntity, SoftFluidStack fluid) {
		if (!MechanicsConfig.IMBUING.get() || !InteractionsConfig.BLOCK_IMBUING.get()) {
			return false;
		}
		// toItem expects the *empty* container, not an already filled potion bottle.
		// With Items.POTION Moonlight cannot resolve a container category and returns
		// null, which used to crash the faucet tick.
		Pair<ItemStack, Category> filled = fluid.toItem(new ItemStack(Items.GLASS_BOTTLE));
		if (filled == null) {
			return false;
		}
		ItemStack potionStack = filled.getFirst();
		return !potionStack.isEmpty()
				&& ModUtils.imbueBlockWithThrownPotion(level, pos, blockEntity, potionStack);
	}

	/**
	 * Locates a crafting grid holding exactly one soap and one Shinerite item
	 * with a washable state.
	 *
	 * @return the slot index of the Shinerite item, or -1 when the recipe should not apply
	 */
	public static int findSoapWashableShineriteIndex(CraftingContainer container) {
		int shineriteSlot = -1;
		int soapSlot = -1;
		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (stack.isEmpty()) {
				continue;
			}
			if (stack.is(ModTags.SOAP_BLACKLIST_ITEM)) {
				return -1;
			}
			Item item = stack.getItem();
			if (isSoap(item)) {
				if (soapSlot != -1) {
					return -1;
				}
				soapSlot = i;
			} else if (item instanceof IDyeableItem && hasWashableState(stack)) {
				if (shineriteSlot != -1) {
					return -1;
				}
				shineriteSlot = i;
			} else {
				return -1;
			}
		}
		return soapSlot != -1 && shineriteSlot != -1 ? shineriteSlot : -1;
	}

	/** Returns a copy of the stack stripped of coat, color and imbuement. */
	public static ItemStack washShineriteItem(ItemStack source) {
		ItemStack result = source.copy();
		result.setCount(1);
		CompoundTag tag = result.getTag();
		if (tag != null) {
			tag.remove(COLOR_TAG);
			tag.remove(COATED_TAG);
		}
		ModUtils.removeImbuement(result);
		return result;
	}

	/** Registers the Shinerite projectiles that Supplementaries' cannon can fire. */
	public static void registerCannonBehaviors() {
		CannonBlock.registerBehavior(ModItems.SHINERITE_STAR.get(), new ShineriteStarFireBehavior());
		CannonBlock.registerBehavior(ModItems.SHINERANG.get(), new ShinerangFireBehavior());
	}

	private static boolean hasWashableState(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		if (tag == null) {
			return false;
		}
		return tag.contains(COLOR_TAG, Tag.TAG_INT) || tag.getBoolean(COATED_TAG) || ModUtils.isImbued(stack);
	}

	private static boolean isSoap(Item item) {
		return BuiltInRegistries.ITEM.getKey(item).equals(SOAP_ID);
	}

	private static void initializeFromStack(BaseWeaponProjectileEntity projectile, ItemStack stack,
			double baseDamage, double baseKnockback, int pierceLevel) {
		projectile.setWeaponItem(stack);
		projectile.setWeaponBaseDamage(ModProjectiles.getBaseDamage(stack, baseDamage));
		projectile.setWeaponBaseKnockback(ModProjectiles.getBaseKnockback(stack, baseKnockback));
		projectile.setPierceLevel(ModProjectiles.getPiercingLevel(stack, pierceLevel));
	}

	private static final class ShineriteStarFireBehavior implements IFireItemBehavior {

		@Override
		public boolean fire(ItemStack stack, ServerLevel level, Vec3 firePos, Vec3 direction,
				float power, int inaccuracy, @Nullable Player owner) {
			ShineriteStarProjectileEntity star;
			if (owner != null) {
				star = new ShineriteStarProjectileEntity(level, owner, stack);
			} else {
				star = new ShineriteStarProjectileEntity(ModEntities.SHINERITE_STAR_PROJECTILE.get(),
						firePos.x(), firePos.y(), firePos.z(), level);
				initializeFromStack(star, stack,
						ProjectileCommonConfig.SHINERITE_STAR_PROJECTILE_BASE_DAMAGE.get(),
						ProjectileCommonConfig.SHINERITE_STAR_PROJECTILE_BASE_KNOCKBACK.get(),
						ProjectileCommonConfig.SHINERITE_STAR_PROJECTILE_PIERCING_LEVEL.get());
			}
			star.setPos(firePos);
			star.shoot(direction.x(), direction.y(), direction.z(), power, inaccuracy);
			level.addFreshEntity(star);
			return true;
		}
	}

	private static final class ShinerangFireBehavior implements IFireItemBehavior {

		@Override
		public boolean fire(ItemStack stack, ServerLevel level, Vec3 firePos, Vec3 direction,
				float power, int inaccuracy, @Nullable Player owner) {
			ShinerangProjectileEntity shinerang;
			if (owner != null) {
				shinerang = new ShinerangProjectileEntity(level, owner, stack);
			} else {
				shinerang = new ShinerangProjectileEntity(ModEntities.SHINERANG_PROJECTILE.get(),
						firePos.x(), firePos.y(), firePos.z(), level);
				initializeFromStack(shinerang, stack,
						ProjectileCommonConfig.SHINERANG_PROJECTILE_BASE_DAMAGE.get(),
						ProjectileCommonConfig.SHINERANG_PROJECTILE_BASE_KNOCKBACK.get(),
						ProjectileCommonConfig.SHINERANG_PROJECTILE_PIERCING_LEVEL.get());
			}
			shinerang.setPos(firePos);
			shinerang.shoot(direction.x(), direction.y(), direction.z(), power, inaccuracy);
			level.addFreshEntity(shinerang);
			return true;
		}
	}
}
