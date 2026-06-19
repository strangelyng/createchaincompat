package net.strangelyng.createchaincompat.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class CreateChainCompatConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // TODO: JSON-based config?
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MODELS = BUILDER
            .comment("A list of models")
            .defineListAllowEmpty("models", List.of("minecraft:block/oak_fence_post"), () -> "", CreateChainCompatConfig::validateModelName);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validateModelName(final Object obj) {
        return obj instanceof String modelName && ((String) obj).contains(":block/");
    }
}
