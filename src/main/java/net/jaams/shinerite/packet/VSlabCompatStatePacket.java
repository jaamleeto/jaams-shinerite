package net.jaams.shinerite.packet;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class VSlabCompatStatePacket {
	public final BlockPos pos;
	public final int color;
	public final int imbuedColor;
	public final boolean imbued;
	public final boolean remove;

	public VSlabCompatStatePacket(BlockPos pos, int color, int imbuedColor, boolean imbued, boolean remove) {
		this.pos = pos;
		this.color = color;
		this.imbuedColor = imbuedColor;
		this.imbued = imbued;
		this.remove = remove;
	}

	public static void encode(VSlabCompatStatePacket msg, FriendlyByteBuf buf) {
		buf.writeBlockPos(msg.pos);
		buf.writeInt(msg.color);
		buf.writeInt(msg.imbuedColor);
		buf.writeBoolean(msg.imbued);
		buf.writeBoolean(msg.remove);
	}

	public static VSlabCompatStatePacket decode(FriendlyByteBuf buf) {
		return new VSlabCompatStatePacket(buf.readBlockPos(), buf.readInt(), buf.readInt(), buf.readBoolean(), buf.readBoolean());
	}

	public static void handle(VSlabCompatStatePacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> VSlabCompatStatePacketHandler.handle(msg));
		ctx.get().setPacketHandled(true);
	}
}
