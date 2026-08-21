package net.jaams.shinerite.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;

public class ModEnums {
	public enum ImbuePotionType {
		LINGERING, NORMAL
	}

	public enum RepairColorMode {
		MIX, RANDOM
	}

	public enum RepairImbuementMode {
		MERGE, RANDOM
	}

	public enum ThrowMode {
		CHARGE_AND_RELEASE, INSTANT_ON_RIGHT_CLICK, CHARGE_AND_FINISH_USING, CHARGE_RELEASE_AND_FINISH
	}

	public enum KeyOption {
		ALT(InputConstants.KEY_LALT, InputConstants.KEY_RALT),
		SHIFT(InputConstants.KEY_LSHIFT, InputConstants.KEY_RSHIFT),
		CONTROL(InputConstants.KEY_LCONTROL, InputConstants.KEY_RCONTROL);

		public final int leftKey;
		public final int rightKey;

		KeyOption(int leftKey, int rightKey) {
			this.leftKey = leftKey;
			this.rightKey = rightKey;
		}

		public boolean isPressed() {
			return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), leftKey)
					|| InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), rightKey);
		}
	}
}
