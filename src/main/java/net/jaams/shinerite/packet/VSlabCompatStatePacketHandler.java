package net.jaams.shinerite.packet;

import net.jaams.shinerite.compat.vslabcompat.VSlabCompatClientState;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VSlabCompatStatePacketHandler {
	public static void handle(VSlabCompatStatePacket msg) {
		if (msg.remove) {
			VSlabCompatClientState.remove(msg.pos);
		} else {
			VSlabCompatClientState.put(msg.pos, msg.color, msg.imbuedColor, msg.imbued);
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.level != null) {
			mc.levelRenderer.setBlocksDirty(msg.pos.getX(), msg.pos.getY(), msg.pos.getZ(), msg.pos.getX(), msg.pos.getY(), msg.pos.getZ());
		}
	}
}
