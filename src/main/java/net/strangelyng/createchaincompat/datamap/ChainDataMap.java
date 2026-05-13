package net.strangelyng.createchaincompat.datamap;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

import static net.strangelyng.createchaincompat.CreateChainCompat.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public class ChainDataMap {
    public static final DataMapType<Item, ChainTexData> CHAIN_TEX_DATA = DataMapType.builder(
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "chain_tex_data"),
            Registries.ITEM,
            ChainTexData.CODEC
    ).synced(ChainTexData.CODEC, false).build();

    @SubscribeEvent
    public static void registerDataMapTypes(RegisterDataMapTypesEvent event) {
        event.register(CHAIN_TEX_DATA);
    }
}
