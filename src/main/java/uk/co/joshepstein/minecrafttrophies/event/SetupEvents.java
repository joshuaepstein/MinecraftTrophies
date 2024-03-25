package uk.co.joshepstein.minecrafttrophies.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class SetupEvents {

	@SubscribeEvent
	public static void setupCommon(FMLCommonSetupEvent event) {} //TODO: Register network and configs

	@SubscribeEvent
	public static void registerModel(ModelEvent.RegisterAdditional event) {} // TODO: Register item models

}
