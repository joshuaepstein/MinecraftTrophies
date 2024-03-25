/*
 * Copyright (c) 2023. Joshua Epstein
 * All rights reserved.
 */

package uk.co.joshepstein.minecrafttrophies.init;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ModKeybinds {
	public static final String KEY_CATEGORY = "key.category.minecrafttrophies";
	public static KeyMapping openTrophyCrafter;

	public static void register(FMLClientSetupEvent event) {
		openTrophyCrafter = ModKeybinds.mapping(ModKeybinds.name("open_trophy_crafter"), KeyConflictContext.IN_GAME, ModKeybinds.key(InputConstants.Type.KEYSYM, InputConstants.KEY_F6));
	}

	private static KeyMapping mapping(String description, IKeyConflictContext keyConflictContext, InputConstants.Key keyCode) {
		return ModKeybinds.mapping(description, keyConflictContext, KeyModifier.NONE, keyCode, KEY_CATEGORY);
	}

	@NotNull
	private static String name(String name) {
		return "key.minecrafttrophies." + name;
	}

	private static InputConstants.Key key(InputConstants.Type inputType, int keyCode) {
		return inputType.getOrCreate(keyCode);
	}

	private static KeyMapping mapping(String description, IKeyConflictContext keyConflictContext) {
		return ModKeybinds.mapping(description, keyConflictContext, KeyModifier.NONE);
	}

	private static KeyMapping mapping(String description, IKeyConflictContext keyConflictContext, KeyModifier keyModifier, InputConstants.Key keyCode, String category) {
		KeyMapping keyMapping = new KeyMapping(description, keyConflictContext, keyCode, category);
		registerKeyBinding(keyMapping);
		return keyMapping;
	}

	public static synchronized void registerKeyBinding(KeyMapping key) {
		Minecraft.getInstance().options.keyMappings = ArrayUtils.add(Minecraft.getInstance().options.keyMappings, key);
	}

	private static KeyMapping mapping(String description, IKeyConflictContext keyConflictContext, KeyModifier keyModifier) {
		return ModKeybinds.mapping(description, keyConflictContext, keyModifier, InputConstants.UNKNOWN);
	}

	private static KeyMapping mapping(String description, IKeyConflictContext keyConflictContext, KeyModifier keyModifier, InputConstants.Key keyCode) {
		return ModKeybinds.mapping(description, keyConflictContext, keyModifier, keyCode, KEY_CATEGORY);
	}

	private static KeyMapping mapping(String description) {
		return ModKeybinds.mapping(description, KeyConflictContext.UNIVERSAL);
	}

	private static KeyMapping mapping(String description, IKeyConflictContext keyConflictContext, InputConstants.Key keyCode, String category) {
		return ModKeybinds.mapping(description, keyConflictContext, KeyModifier.NONE, keyCode, category);
	}

}
