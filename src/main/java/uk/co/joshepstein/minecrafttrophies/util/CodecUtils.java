package uk.co.joshepstein.minecrafttrophies.util;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import org.apache.logging.log4j.Logger;
import uk.co.joshepstein.minecrafttrophies.MinecraftTrophies;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class CodecUtils {
	public CodecUtils() {
	}

	public static <T> Optional<T> readJson(Codec<T> codec, JsonElement jsonElement) {
		DataResult<T> var10000 = codec.parse(JsonOps.INSTANCE, jsonElement);
		Logger var10001 = MinecraftTrophies.LOGGER;
		Objects.requireNonNull(var10001);
		return var10000.resultOrPartial(var10001::error);
	}

	public static <T> void writeJson(Codec<T> codec, T value, Consumer<JsonElement> successConsumer) {
		DataResult<JsonElement> var10000 = codec.encodeStart(JsonOps.INSTANCE, value);
		Logger var10001 = MinecraftTrophies.LOGGER;
		Objects.requireNonNull(var10001);
		var10000.resultOrPartial(var10001::error).ifPresent(successConsumer);
	}

	public static <T> JsonElement writeJson(Codec<T> codec, T value) {
		DataResult<JsonElement> var10000 = codec.encodeStart(JsonOps.INSTANCE, value);
		Logger var10002 = MinecraftTrophies.LOGGER;
		Objects.requireNonNull(var10002);
		return var10000.getOrThrow(false, var10002::error);
	}

	public static <T> T readNBT(Codec<T> codec, CompoundTag tag, String targetKey, T defaultValue) {
		return readNBT(codec, tag.get(targetKey)).orElse(defaultValue);
	}

	public static <T> T readNBT(Codec<T> codec, Tag nbt, T defaultValue) {
		return readNBT(codec, nbt).orElse(defaultValue);
	}

	public static <T> Optional<T> readNBT(Codec<T> codec, Tag nbt) {
		DataResult<T> var10000 = codec.parse(NbtOps.INSTANCE, nbt);
		Logger var10001 = MinecraftTrophies.LOGGER;
		Objects.requireNonNull(var10001);
		return var10000.resultOrPartial(var10001::error);
	}

	public static <T> void writeNBT(Codec<T> codec, T value, CompoundTag targetTag, String targetKey) {
		writeNBT(codec, value, (nbt) -> {
			targetTag.put(targetKey, nbt);
		});
	}

	public static <T> void writeNBT(Codec<T> codec, T value, Consumer<Tag> successConsumer) {
		DataResult<Tag> var10000 = codec.encodeStart(NbtOps.INSTANCE, value);
		Logger var10001 = MinecraftTrophies.LOGGER;
		Objects.requireNonNull(var10001);
		var10000.resultOrPartial(var10001::error).ifPresent(successConsumer);
	}

	public static <T> Tag writeNBT(Codec<T> codec, T value) {
		DataResult<Tag> var10000 = codec.encodeStart(NbtOps.INSTANCE, value);
		Logger var10002 = MinecraftTrophies.LOGGER;
		Objects.requireNonNull(var10002);
		return var10000.getOrThrow(false, var10002::error);
	}
}