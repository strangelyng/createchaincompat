package net.strangelyng.createchaincompat.datamaps;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record ChainTexData(Optional<String> texture, Optional<String> model) {
    public static final Codec<ChainTexData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("texture").forGetter(ChainTexData::texture),
            Codec.STRING.optionalFieldOf("model").forGetter(ChainTexData::model)
    ).apply(instance, ChainTexData::new));
}
