package uk.co.joshepstein.minecrafttrophies.init;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import uk.co.joshepstein.minecrafttrophies.MinecraftTrophies;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MinecraftTrophies.MOD_ID);

    public static final CreativeModeTab TAB = CreativeModeTab.builder().icon(() -> {
        return new ItemStack(ModBlocks.TROPHY.get());
    }).build();

    public static void register(IEventBus eventBus) {

        ITEMS.register(eventBus);
    }
}
