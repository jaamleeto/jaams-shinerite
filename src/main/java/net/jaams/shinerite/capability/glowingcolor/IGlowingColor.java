package net.jaams.shinerite.capability.glowingcolor;

import net.minecraft.nbt.CompoundTag;

public interface IGlowingColor {
	void setColor(int color);

	int getColor();

	boolean hasCustomColor();

	CompoundTag serializeNBT();

	void deserializeNBT(CompoundTag nbt);
}
