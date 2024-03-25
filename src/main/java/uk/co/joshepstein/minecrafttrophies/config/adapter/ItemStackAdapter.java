package uk.co.joshepstein.minecrafttrophies.config.adapter;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import uk.co.joshepstein.minecrafttrophies.MinecraftTrophies;

import java.io.IOException;
import java.util.Objects;

public class ItemStackAdapter extends TypeAdapter<ItemStack> {
	public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
		public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
			TypeAdapter<Item> itemTypeAdapter = gson.getDelegateAdapter(this, new TypeToken<Item>() {
			});
			return typeToken.getRawType() == ItemStack.class ? (TypeAdapter<T>) new ItemStackAdapter(itemTypeAdapter) : null;
		}
	};
	private static final String KEY_ITEM = "item";
	private static final String KEY_NBT = "nbt";
	private static final String KEY_AMOUNT = "amount";
	private final TypeAdapter<Item> itemTypeAdapter;

	public ItemStackAdapter(TypeAdapter<Item> itemTypeAdapter) {
		this.itemTypeAdapter = itemTypeAdapter;
	}

	public void write(JsonWriter out, ItemStack value) throws IOException {
		if (value == null) {
			out.nullValue();
		} else {
			out.beginObject();
			out.name("item");
			this.itemTypeAdapter.write(out, Objects.requireNonNull(value.getItem()));
			if (value.hasTag()) {
				out.name("nbt");
				out.value(value.hasTag() ? Objects.requireNonNull(value.getTag()).getAsString() : "");
			}

			if (value.getCount() > 1) {
				out.name("amount");
				out.value(value.getCount());
			}

			out.endObject();
		}
	}

	public ItemStack read(JsonReader in) throws IOException {
		if (in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		} else {
			try {
				in.beginObject();
				Item item = null;
				CompoundTag nbt = null;
				int amount = 1;

				while(in.peek() == JsonToken.NAME) {
					switch (in.nextName()) {
						case "item":
							item = this.itemTypeAdapter.read(in);
							break;
						case "nbt":
							if (in.peek() == JsonToken.NULL) {
								in.nextNull();
							} else {
								String nbtString = in.nextString();
								nbt = nbtString.isEmpty() ? new CompoundTag() : TagParser.parseTag(nbtString);
							}
							break;
						case "amount":
							amount = in.nextInt();
					}
				}

				in.endObject();
				ItemStack stack = new ItemStack(item, amount);
				stack.setTag(nbt);
				return stack;
			} catch (Exception var9) {
				MinecraftTrophies.LOGGER.error("Unable to parse NBT when reading an item from config.", var9);
				return ItemStack.EMPTY;
			}
		}
	}
}