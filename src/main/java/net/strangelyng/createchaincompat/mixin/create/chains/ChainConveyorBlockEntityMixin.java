package net.strangelyng.createchaincompat.mixin.create.chains;

import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.strangelyng.createchaincompat.ChainConveyorInterface;
import net.strangelyng.createchaincompat.mixin.accessors.BlockEntityAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Mixin(value = ChainConveyorBlockEntity.class, remap = false)
public abstract class ChainConveyorBlockEntityMixin implements ChainConveyorInterface {
    @Shadow public abstract boolean forPointsAlongChains(BlockPos connection, int positions, Consumer<Vec3> callback);

    @Unique
    public final Map<BlockPos, ItemLike> chaincompat$connectionsChain = new HashMap<>();

    public Map<BlockPos, ItemLike> chaincompat$getConnectionsChain() {
        return chaincompat$connectionsChain;
    }

    @Unique
    private static final ThreadLocal<ItemLike> CURRENT_CHAIN = new ThreadLocal<>();

    // Chain Destroyed
    @Inject(method = "chainDestroyed", at = @At("HEAD"))
    public void chaincompat$chainDestroyed(BlockPos target, boolean spawnDrops, boolean sendEffect, CallbackInfo ci) {
        CURRENT_CHAIN.set(chaincompat$connectionsChain.get(target));
    }

    @Redirect(method = "chainDestroyed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V"))
    private void chaincompat$redirectPopResource(Level pLevel, BlockPos pPos, ItemStack pStack) {
        ItemLike replacement = CURRENT_CHAIN.get() != null ? CURRENT_CHAIN.get() : pStack.getItem();
        Block.popResource(pLevel, pPos, new ItemStack(replacement, pStack.getCount()));
    }

    @Redirect(method = "chainDestroyed", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/chainConveyor/ChainConveyorBlockEntity;forPointsAlongChains(Lnet/minecraft/core/BlockPos;ILjava/util/function/Consumer;)Z"))
    private boolean chaincompat$redirectLambdaChain(ChainConveyorBlockEntity instance, BlockPos target, int firstChain, Consumer<Vec3> vec3Consumer) {
        Level level = instance.getLevel();

        ItemLike replacement = CURRENT_CHAIN.get() != null ? CURRENT_CHAIN.get() : Items.CHAIN;
        return forPointsAlongChains(target, firstChain, vec -> level.addFreshEntity(new ItemEntity(level, vec.x, vec.y, vec.z, new ItemStack(replacement))));
    }

    public void chaincompat$addConnectionToWithChain(BlockPos target, ItemLike chain) {
        BlockPos localTarget = target.subtract(((BlockEntityAccessor)this).getWorldPosition());
        chaincompat$connectionsChain.put(localTarget, chain);
    }

    @Inject(method = "removeConnectionTo", at = @At("HEAD"))
    public void chaincompat$removeConnectionTo(BlockPos target, CallbackInfoReturnable<Boolean> cir) {
        BlockPos localTarget = target.subtract(((BlockEntityAccessor)this).getWorldPosition());
        chaincompat$connectionsChain.remove(localTarget);
    }

    // NBT
    @Inject(method = "write", at = @At("TAIL"))
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        ListTag list = new ListTag();
        for (Map.Entry<BlockPos, ItemLike> entry : chaincompat$connectionsChain.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.put("Pos", NbtUtils.writeBlockPos(entry.getKey()));
            ResourceLocation itemRS = BuiltInRegistries.ITEM.getKey(entry.getValue().asItem());
            if (itemRS != null) entryTag.putString("Item", itemRS.toString());
            list.add(entryTag);
        }
        compound.put("ChainConnections", list);
    }

    @Inject(method = "read", at = @At("TAIL"))
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        chaincompat$connectionsChain.clear();
        ListTag list = compound.getList("ChainConnections", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = list.getCompound(i);
            BlockPos pos = NbtUtils.readBlockPos(entryTag, "Pos").get();
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(entryTag.getString("Item")));
            if (item != null) chaincompat$connectionsChain.put(pos, item);
        }
    }
}
