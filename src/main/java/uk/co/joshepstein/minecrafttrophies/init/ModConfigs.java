package uk.co.joshepstein.minecrafttrophies.init;

import uk.co.joshepstein.minecrafttrophies.MinecraftTrophies;
import uk.co.joshepstein.minecrafttrophies.config.TrophyConfig;

import java.util.HashSet;
import java.util.Set;

public class ModConfigs {
    private static boolean initialized = false;

    public static Set<String> INVALID_CONFIGS = new HashSet<>();
    public static TrophyConfig TROPHY_CONFIG;

    public static void register() {
        INVALID_CONFIGS.clear();
        TROPHY_CONFIG = new TrophyConfig().readConfig();

        initialized = true;
        MinecraftTrophies.LOGGER.info("Successfully loaded MinecraftTrophies internal configs.");
    }
}
