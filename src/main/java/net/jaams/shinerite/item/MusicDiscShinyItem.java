
package net.jaams.shinerite.item;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;

import net.jaams.shinerite.dyeable.IDyeableItem;

import javax.annotation.Nullable;

import java.util.List;

public class MusicDiscShinyItem extends RecordItem implements IDyeableItem {
	public MusicDiscShinyItem() {
		super(6, () -> SoundEvents.MUSIC_DISC_PIGSTEP, new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 2400);
	}

	@Override
	public int getDefaultColor() {
		return -1;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return false;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, level, tooltip, flag);
	}
}
