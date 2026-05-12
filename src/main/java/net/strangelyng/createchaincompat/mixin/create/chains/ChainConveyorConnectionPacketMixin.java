package net.strangelyng.createchaincompat.mixin.create.chains;

import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorConnectionPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.strangelyng.createchaincompat.ChainConveyorInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = ChainConveyorConnectionPacket.class, remap = false)
public class ChainConveyorConnectionPacketMixin {
    @Shadow private ItemStack chain;
    @Shadow private BlockPos targetPos;

    @Inject(method = "applySettings(Lnet/minecraft/server/level/ServerPlayer;Lcom/simibubi/create/content/kinetics/chainConveyor/ChainConveyorBlockEntity;)V",
    at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/chainConveyor/ChainConveyorBlockEntity;addConnectionTo(Lnet/minecraft/core/BlockPos;)Z", ordinal = 0))
    private void chaincompat$beforeClbeAddConnectionTo(ServerPlayer player, ChainConveyorBlockEntity be, CallbackInfo ci) {
        if(!(be.getLevel().getBlockEntity(targetPos) instanceof ChainConveyorBlockEntity clbe)) return;
        ((ChainConveyorInterface) clbe).chaincompat$addConnectionToWithChain(be.getBlockPos(), chain.getItem());
    }

    @Inject(method = "applySettings(Lnet/minecraft/server/level/ServerPlayer;Lcom/simibubi/create/content/kinetics/chainConveyor/ChainConveyorBlockEntity;)V",
    at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/chainConveyor/ChainConveyorBlockEntity;addConnectionTo(Lnet/minecraft/core/BlockPos;)Z", ordinal = 1))
    private void chaincompat$beforeBeAddConnectionTo(ServerPlayer player, ChainConveyorBlockEntity be, CallbackInfo ci) {
        ((ChainConveyorInterface) be).chaincompat$addConnectionToWithChain(targetPos, chain.getItem());
    }

    @Inject(method = "applySettings(Lnet/minecraft/server/level/ServerPlayer;Lcom/simibubi/create/content/kinetics/chainConveyor/ChainConveyorBlockEntity;)V",
    at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;placeItemBackInInventory(Lnet/minecraft/world/item/ItemStack;)V", ordinal = 0),
    locals = LocalCapture.CAPTURE_FAILSOFT)
    private void chaincompat$beforePlaceItemsBackInInventory(ServerPlayer player, ChainConveyorBlockEntity be, CallbackInfo ci, ChainConveyorBlockEntity clbe, int chainCost) {
        ItemLike chain = ((ChainConveyorInterface) be).chaincompat$getConnectionsChain().get(targetPos.subtract(be.getBlockPos()));
        player.getInventory().placeItemBackInInventory(new ItemStack(chain == null ? Items.CHAIN : chain, Math.min(chainCost, 64)));
    }

    @Redirect(method = "applySettings(Lnet/minecraft/server/level/ServerPlayer;Lcom/simibubi/create/content/kinetics/chainConveyor/ChainConveyorBlockEntity;)V",
    at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;placeItemBackInInventory(Lnet/minecraft/world/item/ItemStack;)V", ordinal = 0))
    private void chaincompat$cancelPlaceItemsBackInInventory(Inventory instance, ItemStack stack) {

    }
}
