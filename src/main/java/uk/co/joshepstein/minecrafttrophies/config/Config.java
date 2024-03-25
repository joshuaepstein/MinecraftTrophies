package uk.co.joshepstein.minecrafttrophies.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.nbt.CompoundTag;
import uk.co.joshepstein.minecrafttrophies.MinecraftTrophies;
import uk.co.joshepstein.minecrafttrophies.config.adapter.CompoundTagAdapter;
import uk.co.joshepstein.minecrafttrophies.config.adapter.IdentifierAdapter;
import uk.co.joshepstein.minecrafttrophies.config.adapter.ItemStackAdapter;
import uk.co.joshepstein.minecrafttrophies.config.adapter.TextColorAdapter;
import uk.co.joshepstein.minecrafttrophies.init.ModConfigs;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;

public abstract class Config {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapterFactory(IdentifierAdapter.FACTORY)
            .registerTypeAdapterFactory(ItemStackAdapter.FACTORY)
            .registerTypeAdapterFactory(TextColorAdapter.FACTORY)
            .registerTypeAdapter(CompoundTag.class, CompoundTagAdapter.INSTANCE)
            .excludeFieldsWithoutExposeAnnotation()
            .enableComplexMapKeySerialization()
            .setPrettyPrinting()
            .create();

    protected String root = "config%s%s%s".formatted(File.separator, MinecraftTrophies.MOD_ID, File.separator);
    protected String extension = ".json";

    public void generateConfig() {
        this.reset();

        try {
            this.writeConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getConfigFile() {
        return new File(this.root + this.getName() + this.extension);
    }

    public abstract String getName();

    public String toString() {
        return getName();
    }

    public <T extends Config> T readConfig() {
        Main.LOGGER.info("Reading config: " + getName());
        try {
            FileReader reader = new FileReader(getConfigFile());
            try {
                Config config1 = GSON.fromJson(reader, getClass());
                config1.onLoad(this);
                if (!config1.isValid()) {
                    MinecraftTrophies.LOGGER.error("Invalid config {}, using defaults", this);
                    ModConfigs.INVALID_CONFIGS.add(getConfigFile().toString());
                    config1.reset();
                }
                Config config2 = config1;
                reader.close();
                return (T) config2;
            } catch (Exception e) {
                MinecraftTrophies.LOGGER.warn("Invalid config {}, using defaults", this, e);
                reset();
                ModConfigs.INVALID_CONFIGS.add(getConfigFile().toString());
                Config config = this;
                reader.close();
                return (T) config;
            } catch (Throwable throwable) {
                try {
                    reader.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
                throw throwable;
            }
        } catch (Exception e) {
            MinecraftTrophies.LOGGER.warn("Config file {} not found, generating new", this);
            generateConfig();
            return (T) this;
        }
    }

    public boolean isValid() {
        return true;
    }

    protected void onLoad(Config oldConfigInstance) {}


    public static boolean checkAllFieldsAreNotNull(Object o) throws IllegalAccessException {
        for (Field v : o.getClass().getDeclaredFields()) {
            if (v.canAccess(o)) {
                Object field = v.get(o);
                if (field == null)
                    return false;
                if (!field.getClass().isPrimitive()) {
                    boolean b = checkAllFieldsAreNotNull(field);
                    if (!b)
                        return false;
                }
            }
        }
        return true;
    }

    public abstract void reset();

    public void writeConfig() throws IOException {
        File cfgFile = getConfigFile();
        File dir = cfgFile.getParentFile();
        if (!dir.exists() && !dir.mkdirs()) return;
        if (!cfgFile.exists() && !cfgFile.createNewFile()) return;
        FileWriter writer = new FileWriter(cfgFile);
        GSON.toJson(this, writer);
        writer.flush();
        writer.close();
    }
}
