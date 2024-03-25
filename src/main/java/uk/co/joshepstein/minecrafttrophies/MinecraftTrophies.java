package uk.co.joshepstein.minecrafttrophies;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(MinecraftTrophies.MOD_ID)
public class MinecraftTrophies {

	public static final String MOD_ID = "minecrafttrophies";
	public static final Logger LOGGER = LogManager.getLogger();
	public static String VERSION = "Invalid Version";

	public MinecraftTrophies(IEventBus bus) {
		NeoForge.EVENT_BUS.register(this);
		ModList.get().getMods().forEach(mod -> {
			if (mod.getModId().equals(MOD_ID)) {
				VERSION = mod.getVersion().getQualifier();
			}
		});
	}

	public static ResourceLocation id(String name) {
		return id(MOD_ID, name);
	}

	public static ResourceLocation id(String namespace, String name) {
		return new ResourceLocation(namespace, name);
	}

	public static String sId(String name) {
		return MOD_ID + ":" + name;
	}

}
