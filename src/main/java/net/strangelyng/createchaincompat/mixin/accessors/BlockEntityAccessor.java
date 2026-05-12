package net.strangelyng.createchaincompat.mixin.accessors;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = BlockEntity.class)
public interface BlockEntityAccessor {
    @Accessor("worldPosition")
    BlockPos getWorldPosition();
}
