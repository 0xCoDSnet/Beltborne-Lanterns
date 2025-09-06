package net.oxcodsnet.beltborne_lanterns.common.datapack;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.Identifier;
import net.oxcodsnet.beltborne_lanterns.BLMod;
import net.oxcodsnet.beltborne_lanterns.common.config.BLClientConfig;
import net.oxcodsnet.beltborne_lanterns.common.config.BLLampConfigAccess;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Generates a runtime data pack that contributes the tag
 * data/beltborne_lanterns/tags/item/lamps.json based on the
 * current lamp config. This allows other mods (e.g. Accessories)
 * to reference our lamps purely via tags.
 */
public final class BLRuntimeDataPack {
    private BLRuntimeDataPack() {}

    private static final String PACK_FOLDER = "beltborne_lanterns_generated";

    public static boolean writeOrUpdate(MinecraftServer server) {
        try {
            Path datapacksDir = server.getSavePath(WorldSavePath.DATAPACKS);
            Path packDir = datapacksDir.resolve(PACK_FOLDER);
            Path meta = packDir.resolve("pack.mcmeta");
            Path lampsTag = packDir.resolve("data/beltborne_lanterns/tags/item/lamps.json");

            // Collect values from config (unique, keep order)
            Set<String> values = new LinkedHashSet<>();
            for (BLClientConfig.ExtraLampEntry e : BLLampConfigAccess.get().extraLampLight) {
                if (e == null || e.id == null) continue;
                Identifier id = Identifier.tryParse(e.id);
                if (id == null) continue;
                values.add(id.toString());
            }

            // Compose files
            String metaJson = "{\n" +
                    "  \"pack\": {\n" +
                    "    \"pack_format\": 61,\n" +
                    "    \"supported_formats\": { \"min_inclusive\": 1, \"max_inclusive\": 999 },\n" +
                    "    \"description\": \"Beltborne Lanterns runtime tags generated from config\"\n" +
                    "  }\n" +
                    "}\n";

            StringBuilder sb = new StringBuilder();
            sb.append("{\n  \"replace\": false,\n  \"values\": [\n");
            boolean first = true;
            for (String v : values) {
                if (!first) sb.append(",\n");
                sb.append("    \"").append(v).append("\"");
                first = false;
            }
            sb.append("\n  ]\n}\n");
            String lampsJson = sb.toString();

            boolean changed = false;
            // Ensure directories
            Files.createDirectories(lampsTag.getParent());
            if (writeIfChanged(meta, metaJson)) changed = true;
            if (writeIfChanged(lampsTag, lampsJson)) changed = true;
            if (changed) {
                BLMod.LOGGER.info("Runtime datapack written: {} ({} entries)", packDir.toAbsolutePath(), values.size());
            } else {
                BLMod.LOGGER.debug("Runtime datapack up-to-date ({} entries)", values.size());
            }
            return changed;
        } catch (Throwable t) {
            BLMod.LOGGER.warn("Failed to write runtime datapack: {}", t.toString());
            return false;
        }
    }

    private static boolean writeIfChanged(Path file, String content) throws IOException {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        if (Files.exists(file)) {
            byte[] prev = Files.readAllBytes(file);
            if (java.util.Arrays.equals(prev, bytes)) return false;
        } else {
            Files.createDirectories(file.getParent());
        }
        Files.write(file, bytes);
        return true;
    }
}
