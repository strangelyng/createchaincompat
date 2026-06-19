package net.strangelyng.createchaincompat.event;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.strangelyng.createchaincompat.CreateChainCompat;
import net.strangelyng.createchaincompat.config.CreateChainCompatConfig;

import java.util.List;

// TODO: FLESH THIS OUT
public class ClientEventHandler {
    public static void init(IEventBus bus, ModContainer mod) {
        bus.addListener(ClientEventHandler::onRegisterAdditional);
    }

    public static void onRegisterAdditional(ModelEvent.RegisterAdditional event) {
        CreateChainCompat.LOGGER.debug("CCC: onRegisterAdditional");
        CreateChainCompat.LOGGER.debug("CCC: " + CreateChainCompatConfig.MODELS.get());

        List<? extends String> modelsToRegister = CreateChainCompatConfig.MODELS.get();

        modelsToRegister.forEach(model -> {
            event.register(new ModelResourceLocation(ResourceLocation.parse(model), "standalone"));
        });
    }
}
