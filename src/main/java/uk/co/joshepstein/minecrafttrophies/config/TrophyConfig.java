package uk.co.joshepstein.minecrafttrophies.config;

import com.google.gson.annotations.Expose;

import java.util.List;

public class TrophyConfig extends Config {

    @Expose private ParticleSettings particles;
    @Expose private boolean showNames;
    @Expose private List<String> excludeNamespaces;
    @Expose private List<String> excludeAdvancementResourceNames;
    @Expose private boolean showOutdatedMessageOnJoin;

    @Override
    public String getName() {
        return "trophies";
    }

    @Override
    public void reset() {
        particles = new ParticleSettings();
        showNames = true;

        excludeNamespaces = List.of("vanillatweaks");

        excludeAdvancementResourceNames = List.of("example:category/name");

        showOutdatedMessageOnJoin = true;
    }

    public ParticleSettings getParticles() {
        return particles;
    }

    public boolean isShowNames() {
        return showNames;
    }

    public List<String> getExcludeNamespaces() {
        return excludeNamespaces;
    }

    public boolean isExcludedNamespace(String namespace) {
        return excludeNamespaces.contains(namespace);
    }

    public List<String> getExcludeAdvancementResourceNames() {
        return excludeAdvancementResourceNames;
    }

    public boolean isExcludedAdvancementResourceName(String resourceName) {
        return excludeAdvancementResourceNames.contains(resourceName);
    }

    public boolean isShowOutdatedMessageOnJoin() {
        return showOutdatedMessageOnJoin;
    }

    public static class ParticleSettings {
        @Expose private boolean enabled;
        @Expose private int lifetime;
        @Expose
        private boolean colourfromtrophycolor;

        public ParticleSettings() {
            enabled = true;
            lifetime = 60;
            colourfromtrophycolor = true;
        }

        public boolean getEnabled() {
            return enabled;
        }

        public int getLifetime() {
            return lifetime;
        }

        public boolean getColourFromTrophyColor() {
            return colourfromtrophycolor;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public void setLifetime(int lifetime) {
            this.lifetime = lifetime;
        }

        public void setColourFromTrophyColor(boolean colourfromtrophycolor) {
            this.colourfromtrophycolor = colourfromtrophycolor;
        }
    }
}
