package net.jaams.shinerite.configuration.base;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.stream.Stream;

import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.jaams.shinerite.JaamsShineriteMod;

@Mod.EventBusSubscriber(modid = JaamsShineriteMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class JaamsShineriteModConfigs {
	public static final String CONFIG_FOLDER = "jaams/shinerite";
	public static final String OBSOLETE_CONFIG_FOLDER = "jaams/jaams_shinerite";

	@SubscribeEvent
	public static void register(FMLConstructModEvent event) {
		event.enqueueWork(() -> {
			migrateObsoleteConfig();
			ModLoadingContext context = JaamsShineriteMod.MOD_LOADING_CONTEXT;
			if (context == null) {
				JaamsShineriteMod.LOGGER.error("Mod loading context is not available, configs will not be registered");
				return;
			}
			context.registerConfig(ModConfig.Type.CLIENT, JaamsShineriteClientConfiguration.SPEC, CONFIG_FOLDER + "/jaams_shinerite_client.toml");
			context.registerConfig(ModConfig.Type.COMMON, JaamsShineriteCommonConfiguration.SPEC, CONFIG_FOLDER + "/jaams_shinerite_common.toml");
		});
	}

	private static void migrateObsoleteConfig() {
		Path configDir = FMLPaths.CONFIGDIR.get();
		Path obsoleteDir = configDir.resolve(OBSOLETE_CONFIG_FOLDER).normalize();
		Path currentDir = configDir.resolve(CONFIG_FOLDER).normalize();

		if (!Files.isDirectory(obsoleteDir) || obsoleteDir.equals(currentDir)) {
			return;
		}

		try {
			Files.createDirectories(currentDir);
			try (Stream<Path> files = Files.list(obsoleteDir)) {
				files.filter(Files::isRegularFile).forEach(file -> {
					try {
						Path target = currentDir.resolve(file.getFileName().toString());
						if (!Files.exists(target)) {
							Files.move(file, target, StandardCopyOption.REPLACE_EXISTING);
						}
					} catch (IOException e) {
						JaamsShineriteMod.LOGGER.warn("Could not migrate obsolete config file {}", file, e);
					}
				});
			}

			deleteRecursively(obsoleteDir);
			JaamsShineriteMod.LOGGER.info("Removed obsolete config folder {}", obsoleteDir);
		} catch (IOException e) {
			JaamsShineriteMod.LOGGER.warn("Could not clean up obsolete config folder {}", obsoleteDir, e);
		}
	}

	private static void deleteRecursively(Path path) throws IOException {
		try (Stream<Path> paths = Files.walk(path)) {
			paths.sorted(Comparator.reverseOrder()).forEach(p -> {
				try {
					Files.deleteIfExists(p);
				} catch (IOException e) {
					throw new java.io.UncheckedIOException(e);
				}
			});
		} catch (java.io.UncheckedIOException e) {
			throw e.getCause();
		}
	}
}
