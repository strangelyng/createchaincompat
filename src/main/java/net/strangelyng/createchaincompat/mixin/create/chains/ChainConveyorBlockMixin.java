package net.strangelyng.createchaincompat.mixin.create.chains;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlock;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.strangelyng.createchaincompat.ChainConveyorInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChainConveyorBlock.class, remap = false)
public abstract class ChainConveyorBlockMixin {
    @Inject(method = "lambda$onSneakWrenched$1", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;placeItemBackInInventory(Lnet/minecraft/world/item/ItemStack;)V"))
    private static void chaincompat$placeItemBackInInventory(Player player, ChainConveyorBlockEntity be, CallbackInfo ci,
                                                             @Local BlockPos targetPos, @Local int chainCost) {
        ItemLike chain = ((ChainConveyorInterface) be).chaincompat$getConnectionsChain().get(targetPos);
        if (chain != null) player.getInventory().placeItemBackInInventory(new ItemStack(chain, Math.min(chainCost, 64)));
    }

    @Redirect(method = "lambda$onSneakWrenched$1", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;placeItemBackInInventory(Lnet/minecraft/world/item/ItemStack;)V"))
    private static void chaincompat$cancelPlaceItemBackInInventory(Inventory instance, ItemStack stack) {

    }
}
