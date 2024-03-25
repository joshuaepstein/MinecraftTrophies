package uk.co.joshepstein.minecrafttrophies.init;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import org.jetbrains.annotations.NotNull;
import uk.co.joshepstein.minecrafttrophies.MinecraftTrophies;
import uk.co.joshepstein.minecrafttrophies.client.atlas.ITextureAtlas;
import uk.co.joshepstein.minecrafttrophies.client.atlas.ResourceTextureAtlasHolder;
import uk.co.joshepstein.minecrafttrophies.util.function.Memo;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@OnlyIn(value = Dist.CLIENT)
@Mod.EventBusSubscriber(value = {Dist.CLIENT}, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModTextureAtlases {
	private static final Map<ResourceLocation, Supplier<ITextureAtlas>> REGISTRY = new HashMap<>();
	public static final Supplier<ITextureAtlas> SCREEN = ModTextureAtlases.register(MinecraftTrophies.id("textures/atlas/screen.png"), MinecraftTrophies.id("textures/gui/screen"), null);
	public static final Supplier<ITextureAtlas> ADVANCEMENTS = ModTextureAtlases.register(MinecraftTrophies.id("textures/atlas/advancements.png"), MinecraftTrophies.id("textures/gui/advancements"), null);

	@SubscribeEvent
	public static void on(@NotNull RegisterClientReloadListenersEvent event) {
		REGISTRY.values().stream().map(Supplier::get).forEach(event::registerReloadListener);
	}

	private static @NotNull Supplier<ITextureAtlas> register(ResourceLocation id, ResourceLocation resourceLocation, @Nullable Supplier<List<ResourceLocation>> validationSupplier) {
		if (REGISTRY.containsKey(id)) {
			throw new IllegalStateException("Duplicate atlas resource location registered: " + id);
		}
		Supplier<ITextureAtlas> supplier = Memo.of(() -> {
			TextureManager textureManager = Minecraft.getInstance().textureManager;
			ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
			return new ResourceTextureAtlasHolder(textureManager, resourceManager, id, resourceLocation, validationSupplier);
		});
		REGISTRY.put(id, supplier);
		return supplier;
	}
}