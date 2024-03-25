package uk.co.joshepstein.minecrafttrophies.config.adapter;

import com.google.gson.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import uk.co.joshepstein.minecrafttrophies.MinecraftTrophies;

import java.lang.reflect.Type;

public class CompoundTagAdapter implements JsonSerializer<CompoundTag>, JsonDeserializer<CompoundTag> {
	public static CompoundTagAdapter INSTANCE = new CompoundTagAdapter();

	public CompoundTag deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		try {
			return TagParser.parseTag(json.getAsString());
		} catch (CommandSyntaxException e) {
			MinecraftTrophies.LOGGER.error("Error parsing compound tag: ", e);
			return null;
		}
	}

	public JsonElement serialize(CompoundTag src, Type typeOfSrc, JsonSerializationContext context) {
		return new JsonPrimitive(src.toString());
	}
}