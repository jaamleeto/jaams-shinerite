package net.jaams.shinerite.client.model;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

/**
 * Baked model that renders the plain armor icon and then draws the armor trim sprite on top of it.
 *
 * <p>This is produced at runtime by {@link ArmorTrimModelGenerator}, so it works for <b>any</b> trimmable
 * item, including armor from other mods that never declared a trim model of their own. Nothing has to be
 * shipped as a JSON model: the trim quads are baked from the palette sprite
 * ({@code minecraft:trims/items/<slot>_trim_<material>}) at load time.
 */
public class TrimmedArmorBakedModel implements BakedModel {

	private final BakedModel base;
	private final List<BakedQuad> trimQuads;

	public TrimmedArmorBakedModel(BakedModel base, List<BakedQuad> trimQuads) {
		this.base = base;
		this.trimQuads = ImmutableList.copyOf(trimQuads);
	}

	/**
	 * The trim is drawn as an unculled layer, exactly like a regular {@code item/generated} layer, so it may
	 * only be returned for the "no culling" pass ({@code side == null}). Returning it for the per-direction
	 * passes as well would draw it seven times.
	 */
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
		List<BakedQuad> quads = this.base.getQuads(state, side, rand);
		if (side != null || this.trimQuads.isEmpty()) {
			return quads;
		}
		return ImmutableList.<BakedQuad>builder().addAll(quads).addAll(this.trimQuads).build();
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data,
			@Nullable RenderType renderType) {
		List<BakedQuad> quads = this.base.getQuads(state, side, rand, data, renderType);
		if (side != null || renderType != null || this.trimQuads.isEmpty()) {
			return quads;
		}
		return ImmutableList.<BakedQuad>builder().addAll(quads).addAll(this.trimQuads).build();
	}

	@Override
	public List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
		return ImmutableList.of(this);
	}

	@Override
	public List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous) {
		return this.base.getRenderTypes(itemStack, fabulous);
	}

	@Override
	public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack, boolean applyLeftHandTransform) {
		this.base.applyTransform(transformType, poseStack, applyLeftHandTransform);
		return this;
	}

	@Override
	public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
		return this.base.getModelData(level, pos, state, modelData);
	}

	/**
	 * The overrides of the base model were already resolved before this model was built; returning them again
	 * would let the model resolution loop forever.
	 */
	@Override
	public ItemOverrides getOverrides() {
		return ItemOverrides.EMPTY;
	}

	@Override
	public ItemTransforms getTransforms() {
		return this.base.getTransforms();
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return this.base.getParticleIcon();
	}

	@Override
	public TextureAtlasSprite getParticleIcon(ModelData data) {
		return this.base.getParticleIcon(data);
	}

	@Override
	public boolean useAmbientOcclusion() {
		return this.base.useAmbientOcclusion();
	}

	@Override
	public boolean useAmbientOcclusion(BlockState state) {
		return this.base.useAmbientOcclusion(state);
	}

	@Override
	public boolean isGui3d() {
		return this.base.isGui3d();
	}

	@Override
	public boolean usesBlockLight() {
		return this.base.usesBlockLight();
	}

	@Override
	public boolean isCustomRenderer() {
		return this.base.isCustomRenderer();
	}

	public BakedModel getBase() {
		return this.base;
	}
}