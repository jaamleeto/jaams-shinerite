package net.jaams.shinerite.mixins.compat.moonlight;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;

/**
 * Moonlight 2.8.57+ throws an AssertionError in dev when a config value is
 * defined outside a category. v_slab_compat 2.5 does exactly that for its
 * common config, which prevents the mod from loading in a dev workspace.
 *
 * This keeps the original comment handling but removes the dev-only assertion.
 */
@Mixin(value = ConfigBuilder.class, remap = false)
public abstract class MoonlightConfigBuilderMixin {

	@Shadow
	private String currentComment;

	@Shadow
	private String currentKey;

	@Shadow
	protected Map<String, String> comments;

	@Shadow
	public abstract String tooltipKey(String name);

	@Inject(method = "maybeAddTranslationString", at = @At("HEAD"), cancellable = true, remap = false)
	private void jaams$skipDevCategoryAssertion(String name, CallbackInfo ci) {
		this.currentKey = this.tooltipKey(name);
		if (this.currentComment != null && this.currentKey != null) {
			this.comments.put(this.currentKey, this.currentComment);
			this.currentComment = null;
			this.currentKey = null;
		}
		ci.cancel();
	}
}
