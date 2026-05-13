package net.strangelyng.createchaincompat.datamap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ChainTexData(String texture) {
    public static final Codec<ChainTexData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("texture").forGetter(ChainTexData::texture)
    ).apply(instance, ChainTexData::new));
}
