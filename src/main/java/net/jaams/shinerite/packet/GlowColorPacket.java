package net.jaams.shinerite.packet;

import net.minecraftforge.network.NetworkEvent;

import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Supplier;

public class GlowColorPacket {
	public final int entityId;
	public final int color;

	public GlowColorPacket(int entityId, int color) {
		this.entityId = entityId;
		this.color = color;
	}

	public static void encode(GlowColorPacket msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.entityId);
		buf.writeInt(msg.color);
	}

	public static GlowColorPacket decode(FriendlyByteBuf buf) {
		return new GlowColorPacket(buf.readInt(), buf.readInt());
	}

	public static void handle(GlowColorPacket msg, Supplier<NetworkEvent.Context> ctx) {
		GlowColorPacketHandler.handle(msg, ctx);
	}
}
