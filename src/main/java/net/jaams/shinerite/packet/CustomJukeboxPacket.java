package net.jaams.shinerite.packet;

import net.minecraftforge.network.NetworkEvent;

import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

import java.util.function.Supplier;

public class CustomJukeboxPacket {
	private final BlockPos pos;
	private final ItemStack item;
	private final long sequenceId;

	public CustomJukeboxPacket(BlockPos pos, ItemStack item, long sequenceId) {
		this.pos = pos;
		this.item = item.copy();
		this.sequenceId = sequenceId;
	}

	public static CustomJukeboxPacket decode(FriendlyByteBuf buf) {
		BlockPos pos = buf.readBlockPos();
		ItemStack item = buf.readItem();
		long sequenceId = buf.readLong();
		return new CustomJukeboxPacket(pos, item, sequenceId);
	}

	public static void encode(CustomJukeboxPacket packet, FriendlyByteBuf buf) {
		buf.writeBlockPos(packet.pos);
		buf.writeItem(packet.item);
		buf.writeLong(packet.sequenceId);
	}

	public void handle(Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context ctx = supplier.get();
		if (ctx.getDirection().getReceptionSide().isClient()) {
			ctx.enqueueWork(() -> CustomJukeboxPacketHandler.handleJukeboxSync(this.pos, this.item, this.sequenceId));
		}
		ctx.setPacketHandled(true);
	}
}
