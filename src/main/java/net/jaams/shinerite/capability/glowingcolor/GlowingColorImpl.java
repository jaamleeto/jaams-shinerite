package net.jaams.shinerite.capability.glowingcolor;

import net.minecraft.nbt.CompoundTag;

public class GlowingColorImpl implements IGlowingColor {
	private int color = -1;

	@Override
	public void setColor(int color) {
		this.color = color;
	}

	@Override
	public int getColor() {
		return this.color;
	}

	@Override
	public boolean hasCustomColor() {
		return this.color != -1;
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		nbt.putInt("color", this.color);
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		this.color = nbt.getInt("color");
	}
}
