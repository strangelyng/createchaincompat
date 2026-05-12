package net.strangelyng.createchaincompat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ItemLike;

import java.util.Map;

public interface ChainConveyorInterface {
    void chaincompat$addConnectionToWithChain(BlockPos pos, ItemLike chain);

    Map<BlockPos, ItemLike> chaincompat$getConnectionsChain();
}
