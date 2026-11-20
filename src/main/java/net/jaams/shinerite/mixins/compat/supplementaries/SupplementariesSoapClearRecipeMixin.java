package net.jaams.shinerite.mixins.compat.supplementaries;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.mehvahdjukaar.supplementaries.common.items.crafting.SoapClearRecipe;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.jaams.shinerite.compat.SupplementariesCompat;

/**
 * Extends Supplementaries' soap-clearing recipe so it also accepts Shinerite
 * items and strips their coat, color and imbuement in one craft.
 */
@Mixin(value = SoapClearRecipe.class, remap = false)
public abstract class SupplementariesSoapClearRecipeMixin {

	@Inject(method = "matches", at = @At("HEAD"), cancellable = true, remap = false)
	private void jaams$matchShineriteSoapWash(CraftingContainer container, Level level,
			CallbackInfoReturnable<Boolean> cir) {
		if (SupplementariesCompat.findSoapWashableShineriteIndex(container) != -1) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "assemble", at = @At("HEAD"), cancellable = true, remap = false)
	private void jaams$washShineriteItem(CraftingContainer container, RegistryAccess access,
			CallbackInfoReturnable<ItemStack> cir) {
		int slot = SupplementariesCompat.findSoapWashableShineriteIndex(container);
		if (slot != -1) {
			cir.setReturnValue(SupplementariesCompat.washShineriteItem(container.getItem(slot)));
		}
	}
}
