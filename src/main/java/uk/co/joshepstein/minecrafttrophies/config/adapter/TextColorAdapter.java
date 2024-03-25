package uk.co.joshepstein.minecrafttrophies.config.adapter;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.minecraft.network.chat.TextColor;
import uk.co.joshepstein.minecrafttrophies.MinecraftTrophies;

import java.io.IOException;

public class TextColorAdapter extends TypeAdapter<TextColor> {
	public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
		public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
			return (type.getRawType() == TextColor.class) ? (TypeAdapter<T>) new TextColorAdapter() : null;
		}
	};

	public void write(JsonWriter out, TextColor value) throws IOException {
		if (value == null) {
			out.nullValue();
			return;
		}
		out.value(value.toString());
	}

	public TextColor read(JsonReader in) throws IOException {
		if (in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}
		return TextColor.parseColor(in.nextString()).getOrThrow(false, (e) -> {
			MinecraftTrophies.LOGGER.error("Error parsing text color: " + e);
		});
	}
}