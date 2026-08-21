package net.jaams.shinerite.capability.glowingcolor;

import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.Capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;

public class GlowingColorProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
	public static final Capability<IGlowingColor> GLOWING_COLOR = CapabilityManager.get(new CapabilityToken<>() {
	});
	public final IGlowingColor impl = new GlowingColorImpl();
	private final LazyOptional<IGlowingColor> optional = LazyOptional.of(() -> impl);

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return cap == GLOWING_COLOR ? optional.cast() : LazyOptional.empty();
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		nbt.putInt("color", impl.getColor());
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		impl.setColor(nbt.getInt("color"));
	}
}
