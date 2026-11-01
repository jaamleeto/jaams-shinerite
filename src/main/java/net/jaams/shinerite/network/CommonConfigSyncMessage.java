package net.jaams.shinerite.network;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;

import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;

import net.minecraft.network.FriendlyByteBuf;

import net.jaams.shinerite.JaamsShineriteMod;
import net.jaams.shinerite.configuration.base.JaamsShineriteModConfigs;

public class CommonConfigSyncMessage {
	private static final String COMMON_CONFIG_FILE = JaamsShineriteModConfigs.CONFIG_FOLDER + "/jaams_shinerite_common.toml";
	private static volatile byte[] lastSyncedBytes;
	private static volatile CommentedFileConfig originalFileConfig;
	private static volatile boolean applying;

	public static byte[] getServerConfigBytes() {
		ModConfig config = ConfigTracker.INSTANCE.fileMap().get(COMMON_CONFIG_FILE);
		if (config == null || config.getConfigData() == null) {
			return null;
		}
		try {
			Path path = config.getFullPath();
			if (!Files.isRegularFile(path)) {
				return null;
			}
			return Files.readAllBytes(path);
		} catch (IOException | ClassCastException e) {
			JaamsShineriteMod.LOGGER.warn("Could not read common config bytes for sync", e);
			return null;
		}
	}

	public static boolean isApplying() {
		return applying;
	}

	public static byte[] lastSyncedBytes() {
		return lastSyncedBytes;
	}

	public static void clearLastSyncedBytes() {
		lastSyncedBytes = null;
	}

	private static void applyServerBytes(ModConfig config, byte[] bytes) throws Exception {
		CommentedConfig parsed = TomlFormat.instance().createParser().parse(new ByteArrayInputStream(bytes));
		CommentedFileConfig reference = originalFileConfig;
		if (reference != null) {
			Set<String> received = leafPaths(parsed);
			Set<String> expected = leafPaths(reference.unmodifiable());
			if (!received.equals(expected)) {
				Set<String> missing = new LinkedHashSet<>(expected);
				missing.removeAll(received);
				Set<String> extra = new LinkedHashSet<>(received);
				extra.removeAll(expected);
				JaamsShineriteMod.LOGGER.error("Sync aborted: server common config keys do not match the local spec (missing: {}, extra: {})", missing, extra);
				return;
			}
		}

		try {
			applying = true;
			config.getSpec().acceptConfig(new CommonConfigLockedCommented(parsed.unmodifiable()));
		} catch (RuntimeException e) {
			if (reference != null) {
				config.getSpec().acceptConfig(reference);
			}
			throw e;
		} finally {
			applying = false;
		}
	}

	private static Set<String> leafPaths(com.electronwill.nightconfig.core.UnmodifiableConfig config) {
		Set<String> paths = new LinkedHashSet<>();
		collectLeafPaths(config, "", paths);
		return paths;
	}

	private static void collectLeafPaths(com.electronwill.nightconfig.core.UnmodifiableConfig config, String prefix, Set<String> paths) {
		for (com.electronwill.nightconfig.core.UnmodifiableConfig.Entry entry : config.entrySet()) {
			String path = (prefix.isEmpty() ? "" : prefix + ".") + entry.getKey();
			Object value = entry.getRawValue();
			if (value instanceof com.electronwill.nightconfig.core.UnmodifiableConfig subConfig) {
				collectLeafPaths(subConfig, path, paths);
			} else {
				paths.add(path);
			}
		}
	}

	private final byte[] data;

	public CommonConfigSyncMessage(byte[] data) {
		this.data = data != null ? data : new byte[0];
	}

	public static void reapplyLastSync() {
		byte[] bytes = lastSyncedBytes;
		if (bytes == null || applying) {
			return;
		}
		ModConfig config = ConfigTracker.INSTANCE.fileMap().get(COMMON_CONFIG_FILE);
		if (config == null) {
			return;
		}
		try {
			applying = true;
			applyServerBytes(config, bytes);
			JaamsShineriteMod.LOGGER.info("Re-applied server common config override to the client");
		} catch (Exception e) {
			JaamsShineriteMod.LOGGER.error("Failed to re-apply synced common config", e);
		} finally {
			applying = false;
		}
	}

	public static void restoreLocalConfig() {
		CommentedFileConfig fileConfig = originalFileConfig;
		if (fileConfig == null) {
			return;
		}
		ModConfig config = ConfigTracker.INSTANCE.fileMap().get(COMMON_CONFIG_FILE);
		if (config == null) {
			return;
		}
		try {
			fileConfig.load();
			applying = true;
			config.getSpec().acceptConfig(fileConfig);
			JaamsShineriteMod.LOGGER.info("Restored local common config values after leaving the server");
		} catch (Exception e) {
			JaamsShineriteMod.LOGGER.error("Failed to restore local common config", e);
		} finally {
			applying = false;
		}
		lastSyncedBytes = null;
	}

	public static void encode(CommonConfigSyncMessage msg, FriendlyByteBuf buffer) {
		buffer.writeByteArray(msg.data);
	}

	public static CommonConfigSyncMessage decode(FriendlyByteBuf buffer) {
		return new CommonConfigSyncMessage(buffer.readByteArray());
	}

	public static void handle(CommonConfigSyncMessage msg, Supplier<NetworkEvent.Context> ctx) {
		if (!ctx.get().getDirection().getReceptionSide().isClient()) {
			ctx.get().setPacketHandled(true);
			return;
		}
		ctx.get().enqueueWork(() -> {
			net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
			if (minecraft.getSingleplayerServer() != null) {
				return;
			}
			ModConfig config = ConfigTracker.INSTANCE.fileMap().get(COMMON_CONFIG_FILE);
			if (config == null) {
				return;
			}
			if (originalFileConfig == null && config.getConfigData() instanceof CommentedFileConfig fileData) {
				originalFileConfig = fileData;
			}
			if (originalFileConfig == null) {
				return;
			}
			try {
				applying = true;
				applyServerBytes(config, msg.data);
				lastSyncedBytes = msg.data;
				JaamsShineriteMod.LOGGER.info("Applied server common config override ({})", COMMON_CONFIG_FILE);
			} catch (Exception e) {
				JaamsShineriteMod.LOGGER.error("Failed to apply synced common config from server", e);
			} finally {
				applying = false;
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
