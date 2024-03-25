package uk.co.joshepstein.minecrafttrophies.util;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MiscUtils {
	private static final Random rand = new Random();

	public static <T> T eitherOf(Random r, T... selection) {
		if (selection.length == 0) {
			return null;
		}
		return selection[r.nextInt(selection.length)];
	}

	public static <T> List<T> concat(List<T> list1, T... elements) {
		return Stream.concat(list1.stream(), Arrays.stream(elements)).collect(Collectors.toList());
	}

	public static <T> List<T> concat(List<T> list1, List<T> list2) {
		return (List) Stream.concat((Stream) list1.stream(), (Stream) list2.stream()).collect(Collectors.toList());
	}

	public static Point2D.Float getMidpoint(Rectangle r) {
		return new Point2D.Float((float) r.x + (float) r.width / 2.0f, (float) r.y + (float) r.height / 2.0f);
	}

	public static boolean hasEmptySlot(Container inventory) {
		return MiscUtils.getRandomEmptySlot(inventory) != -1;
	}

	public static int getRandomEmptySlot(Container inventory) {
		return MiscUtils.getRandomEmptySlot(new InvWrapper(inventory));
	}

	public static int getRandomEmptySlot(IItemHandler handler) {
		List<Integer> slots = new ArrayList<>();
		for (int slot = 0; slot < handler.getSlots(); ++slot) {
			if (!handler.getStackInSlot(slot).isEmpty()) continue;
			slots.add(slot);
		}
		if (slots.isEmpty()) {
			return -1;
		}
		return MiscUtils.getRandomEntry(slots, rand);
	}

	@Nullable
	public static <T> T getRandomEntry(Collection<T> collection, Random rand) {
		if (collection.isEmpty()) {
			return null;
		}
		int randomPick = rand.nextInt(collection.size());
		return Iterables.get(collection, randomPick, null);
	}

	public static boolean hasEmptySlot(IItemHandler inventory) {
		return MiscUtils.getRandomEmptySlot(inventory) != -1;
	}

	public static int getRandomSlot(IItemHandler handler) {
		List<Integer> slots = new ArrayList<>();
		for (int slot = 0; slot < handler.getSlots(); ++slot) {
			slots.add(slot);
		}
		if (slots.isEmpty()) {
			return -1;
		}
		return MiscUtils.getRandomEntry(slots, rand);
	}

	public static List<Integer> getEmptySlots(Container inventory) {
		List<Integer> list = Lists.newArrayList();
		for (int i = 0; i < inventory.getContainerSize(); ++i) {
			if (!inventory.getItem(i).isEmpty()) continue;
			list.add(i);
		}
		return list;
	}

	public static boolean inventoryContains(Container inventory, Predicate<ItemStack> filter) {
		for (int slot = 0; slot < inventory.getContainerSize(); ++slot) {
			if (!filter.test(inventory.getItem(slot))) continue;
			return true;
		}
		return false;
	}

	public static boolean inventoryContains(IItemHandler handler, Predicate<ItemStack> filter) {
		for (int slot = 0; slot < handler.getSlots(); ++slot) {
			if (!filter.test(handler.getStackInSlot(slot))) continue;
			return true;
		}
		return false;
	}

	public static List<ItemStack> mergeItemStacks(List<ItemStack> stacks) {
		List<ItemStack> out = new ArrayList<>();
		block0:
		for (ItemStack stack : stacks) {
			if (stack.isEmpty()) continue;
			for (ItemStack existing : out) {
				if (!MiscUtils.canMerge(existing, stack)) continue;
				existing.setCount(existing.getCount() + stack.getCount());
				continue block0;
			}
			out.add(stack);
		}
		return out;
	}

	public static boolean canMerge(ItemStack stack, ItemStack other) {
		return stack.getItem() == other.getItem() && ItemStack.isSameItem(stack, other);
	}

	public static List<ItemStack> splitAndLimitStackSize(List<ItemStack> stacks) {
		List<ItemStack> out = new ArrayList<>();
		for (ItemStack stack : stacks) {
			int newCount;
			if (stack.isEmpty()) continue;
			for (int i = stack.getCount(); i > 0; i -= newCount) {
				newCount = Math.min(i, stack.getMaxStackSize());
				ItemStack copy = stack.copy();
				copy.setCount(newCount);
				out.add(copy);
			}
		}
		return out;
	}

	public static boolean canFullyMergeIntoSlot(Container inventory, int slot, ItemStack stack) {
		if (stack.isEmpty()) {
			return true;
		}
		ItemStack existing = inventory.getItem(slot);
		if (existing.isEmpty()) {
			return inventory.getMaxStackSize() >= stack.getCount();
		}
		if (!MiscUtils.canMerge(existing, stack)) {
			return false;
		}
		return inventory.getMaxStackSize() >= existing.getCount() + stack.getCount();
	}

	public static ItemStack mergeIntoInventory(IItemHandler inventory, ItemStack toAdd, boolean simulate) {
		if (toAdd.isEmpty()) {
			return ItemStack.EMPTY;
		}
		for (int slot = 0; slot < inventory.getSlots(); ++slot) {
			ItemStack inSlot = inventory.getStackInSlot(slot);
			if (inSlot.isEmpty() || !(toAdd = inventory.insertItem(slot, toAdd, simulate)).isEmpty()) continue;
			return ItemStack.EMPTY;
		}
		List<Integer> emptySlots = MiscUtils.getEmptySlots(inventory);
		for (int emptySlotId : emptySlots) {
			toAdd = inventory.insertItem(emptySlotId, toAdd, simulate);
			if (!toAdd.isEmpty()) continue;
			return ItemStack.EMPTY;
		}
		return toAdd;
	}

	public static List<Integer> getEmptySlots(IItemHandler inventory) {
		List<Integer> list = Lists.newArrayList();
		for (int i = 0; i < inventory.getSlots(); ++i) {
			if (!inventory.getStackInSlot(i).isEmpty()) continue;
			list.add(i);
		}
		return list;
	}

	public static void addStackToSlot(Container inventory, int slot, ItemStack toAdd) {
		if (toAdd.isEmpty()) {
			return;
		}
		ItemStack stack = inventory.getItem(slot);
		if (stack.isEmpty()) {
			inventory.setItem(slot, toAdd.copy());
			return;
		}
		if (MiscUtils.canMerge(stack, toAdd)) {
			stack.grow(toAdd.getCount());
		}
	}

	public static boolean addItemStack(Container inventory, ItemStack stack) {
		for (int slot = 0; slot < inventory.getContainerSize(); ++slot) {
			ItemStack contained = inventory.getItem(slot);
			if (!contained.isEmpty()) continue;
			inventory.setItem(slot, stack);
			return true;
		}
		return false;
	}

	public static <T extends Enum<T>> T getEnumEntry(Class<T> enumClass, int index) {
		Enum<T>[] constants = enumClass.getEnumConstants();
		return (T) constants[Mth.clamp(index, 0, constants.length - 1)];
	}

	public static Optional<BlockPos> getEmptyNearby(LevelReader world, BlockPos pos) {
		return BlockPos.findClosestMatch(pos, 8, 8, arg_0 -> world.isEmptyBlock(arg_0));
	}

	public static BlockPos getRandomPos(BoundingBox box, Random r) {
		return MiscUtils.getRandomPos(AABB.of(box), r);
	}

	public static BlockPos getRandomPos(AABB box, Random r) {
		int sizeX = Math.max(1, Mth.floor(box.getXsize()));
		int sizeY = Math.max(1, Mth.floor(box.getYsize()));
		int sizeZ = Math.max(1, Mth.floor(box.getZsize()));
		return new BlockPos((int) (box.minX + (double) r.nextInt(sizeX)), (int) (box.minY + (double) r.nextInt(sizeY)), (int) (box.minZ + (double) r.nextInt(sizeZ)));
	}

	public static Vec3 getRandomOffset(AABB box, Random r) {
		return new Vec3(box.minX + (double) r.nextFloat() * (box.maxX - box.minX), box.minY + (double) r.nextFloat() * (box.maxY - box.minY), box.minZ + (double) r.nextFloat() * (box.maxZ - box.minZ));
	}

	public static Vec3 getRandomOffset(BlockPos pos, Random r) {
		return new Vec3((float) pos.getX() + r.nextFloat(), (float) pos.getY() + r.nextFloat(), (float) pos.getZ() + r.nextFloat());
	}

	public static Vec3 getRandomOffset(BlockPos pos, Random r, float scale) {
		float x = (float) pos.getX() + 0.5f - scale / 2.0f + r.nextFloat() * scale;
		float y = (float) pos.getY() + 0.5f - scale / 2.0f + r.nextFloat() * scale;
		float z = (float) pos.getZ() + 0.5f - scale / 2.0f + r.nextFloat() * scale;
		return new Vec3(x, y, z);
	}

	public static Collection<ChunkPos> getChunksContaining(AABB box) {
		return MiscUtils.getChunksContaining(new Vec3i((int) box.minX, (int) box.minY, (int) box.minZ), new Vec3i((int) box.maxX, (int) box.maxY, (int) box.maxZ));
	}

	public static Collection<ChunkPos> getChunksContaining(Vec3i min, Vec3i max) {
		ArrayList affected = Lists.newArrayList();
		int maxX = max.getX() >> 4;
		int maxZ = max.getZ() >> 4;
		for (int chX = min.getX() >> 4; chX <= maxX; ++chX) {
			for (int chZ = min.getZ() >> 4; chZ <= maxZ; ++chZ) {
				affected.add(new ChunkPos(chX, chZ));
			}
		}
		return affected;
	}

	@Nullable
	public static <T> T getRandomEntry(T... entries) {
		return MiscUtils.getRandomEntry(Lists.newArrayList(entries), rand);
	}

	@Nullable
	public static <T> T getRandomEntry(Collection<T> collection) {
		return MiscUtils.getRandomEntry(collection, rand);
	}

	public static void broadcast(PlayerChatMessage message) {
		MinecraftServer srv = ServerLifecycleHooks.getCurrentServer();
		if (srv != null) {
			srv.getPlayerList().broadcastChatMessage(message, srv.createCommandSourceStack(), ChatType.bind(ChatType.CHAT, srv.createCommandSourceStack()));
		}
	}

	public static void fillContainer(AbstractContainerMenu ct, NonNullList<ItemStack> items) {
		for (int slot = 0; slot < items.size(); ++slot) {
			ct.setItem(slot, ct.getStateId(), items.get(slot));
		}
	}

	public static void giveItem(ServerPlayer player, ItemStack stack) {
		stack = stack.copy();
		if (player.getInventory().add(stack) && stack.isEmpty()) {
			stack.setCount(1);
			ItemEntity dropped = player.drop(stack, false);
			if (dropped != null) {
				dropped.makeFakeItem();
			}
			player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2f, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7f + 1.0f) * 2.0f);
			player.inventoryMenu.broadcastChanges();
		} else {
			ItemEntity dropped = player.drop(stack, false);
			if (dropped != null) {
				dropped.setNoPickUpDelay();
				dropped.setThrower(player);
			}
		}
	}

	public static Vector3f getRandomCirclePosition(Vector3f centerOffset, Vector3f axis, float radius) {
		return MiscUtils.getCirclePosition(centerOffset, axis, radius, (float) (Math.random() * 360.0));
	}

	public static Vector3f getCirclePosition(Vector3f centerOffset, Vector3f axis, float radius, float degree) {
		Vector3f circleVec = MiscUtils.normalize(MiscUtils.perpendicular(axis));
		circleVec = new Vector3f(circleVec.x() * radius, circleVec.y() * radius, circleVec.z() * radius);
//		Quaternionf rotQuat = new Quaternionf(axis, degree, true);
		Quaternionf rotQuat = new Quaternionf(axis.x(), axis.y(), axis.z(), degree);
		circleVec.rotate(rotQuat);
		return new Vector3f(circleVec.x() + centerOffset.x(), circleVec.y() + centerOffset.y(), circleVec.z() + centerOffset.z());
	}

	public static Vector3f normalize(Vector3f vec) {
		float lengthSq = vec.x() * vec.x() + vec.y() * vec.y() + vec.z() * vec.z();
		float length = (float) Math.sqrt(lengthSq);
		return new Vector3f(vec.x() / length, vec.y() / length, vec.z() / length);
	}

	public static Vector3f perpendicular(Vector3f vec) {
		if ((double) vec.z() == 0.0) {
			return new Vector3f(vec.y(), -vec.x(), 0.0f);
		}
		return new Vector3f(0.0f, vec.z(), -vec.y());
	}

	public static boolean isPlayerFakeMP(ServerPlayer player) {
		if (player instanceof FakePlayer) {
			return true;
		}
		try {
			player.getIpAddress().length();
			player.connection.connection.getRemoteAddress().toString();
			if (!player.connection.connection.channel().isOpen()) {
				return true;
			}
		} catch (Exception exc) {
			return true;
		}
		return false;
	}

	public static List<Component> splitDescriptionText(String text) {
		List<Component> tooltip = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		for (String word : text.split("\\s+")) {
			sb.append(word).append(" ");
			if (sb.length() < 30) continue;
			tooltip.add(Component.literal(sb.toString().trim()));
			sb = new StringBuilder();
		}
		if (sb.length() > 0) {
			tooltip.add(Component.literal(sb.toString().trim()));
		}
		return tooltip;
	}
}
