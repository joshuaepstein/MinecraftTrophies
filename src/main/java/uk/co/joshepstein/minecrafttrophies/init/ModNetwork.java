package uk.co.joshepstein.minecrafttrophies.init;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforgespi.language.IModInfo;
import uk.co.joshepstein.minecrafttrophies.MinecraftTrophies;
import uk.co.joshepstein.minecrafttrophies.util.ModVersion;

public class ModNetwork {
    private static final ModVersion VERSION = ModList.get().getModContainerById(MinecraftTrophies.MOD_ID).map(ModContainer::getModInfo).map(IModInfo::getVersion).map(artifactVersion -> new ModVersion(artifactVersion.getQualifier())).orElse(new ModVersion("1"));
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(MinecraftTrophies.id("network"), VERSION::toString, VERSION::accepted, VERSION::accepted);

    private static int ID = 0;

    public static void initialize() {
        MinecraftTrophies.LOGGER.info("Initializing network. Version: {}", VERSION.toString());

//        CHANNEL.registerMessage(nextId(), SyncOverSizedContentMessage.class, SyncOverSizedContentMessage::encode, SyncOverSizedContentMessage::decode, SyncOverSizedContentMessage::handle);
    }

    public static int nextId() { return ID++; }

    public static <MSG> void sendToServer(MSG message) { CHANNEL.sendToServer(message); }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void sendToClients(MSG message) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), message);
    }
}
