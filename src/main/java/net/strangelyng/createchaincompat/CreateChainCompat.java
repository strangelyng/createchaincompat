package net.strangelyng.createchaincompat;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(CreateChainCompat.MOD_ID)
public class CreateChainCompat {
    public static final String MOD_ID = "createchaincompat";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final TagKey<Item> VALID_FOR_CHAIN_CONVEYOR = TagKey.create(
            Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "valid_for_chain_conveyor"));

    public CreateChainCompat(IEventBus bus, ModContainer mod) {

        bus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }
}
