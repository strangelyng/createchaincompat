package net.strangelyng.createchaincompat.mixin.create.chains;

import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorConnectionHandler;
import net.minecraft.world.item.ItemStack;
import net.strangelyng.createchaincompat.CreateChainCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ChainConveyorConnectionHandler.class, remap = false)
public class ChainConveyorConnectionHandlerMixin {
    @Inject(method = "isChain", at = @At("HEAD"), cancellable = true, remap = false)
    private static void chaincompat$isChain(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        if (itemStack.is(CreateChainCompat.VALID_FOR_CHAIN_CONVEYOR)) {
            cir.setReturnValue(true);
        }
    }
}
