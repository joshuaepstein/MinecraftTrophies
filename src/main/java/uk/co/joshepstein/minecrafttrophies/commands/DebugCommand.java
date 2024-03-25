package uk.co.joshepstein.minecrafttrophies.commands;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.data.BlockDataAccessor;
import net.minecraft.server.commands.data.DataCommands;
import net.minecraft.server.commands.data.EntityDataAccessor;
import net.minecraft.server.commands.data.StorageDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;
import uk.co.joshepstein.minecrafttrophies.MinecraftTrophies;

import java.util.List;
import java.util.function.Function;

public class DebugCommand extends Command {
	public static final List<Function<String, DataCommands.DataProvider>> ALL_PROVIDERS = ImmutableList.of(EntityDataAccessor.PROVIDER, BlockDataAccessor.PROVIDER, StorageDataAccessor.PROVIDER);

	@Override
	public String getName() {
		return "debug";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public void build(LiteralArgumentBuilder<CommandSourceStack> builder) {
		builder.then(
				Commands.literal("dump_blockstate")
						.then(Commands.argument("block_pos", BlockPosArgument.blockPos())
								.executes(ctx -> dumpBlockstate(ctx, BlockPosArgument.getLoadedBlockPos(ctx, "block_pos")))));
		builder.then(
				Commands.literal("dump_item_nbt")
						.executes(this::dumpItemNBT));
		builder.then(
				Commands.literal("dev_world")
						.executes(this::setupDevWorld));

	}

	private int setupDevWorld(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		ServerPlayer player = (context.getSource()).getPlayerOrException();
		MinecraftServer srv = player.getServer();
		ServerLevel level = player.serverLevel();
		GameRules rules = level.getGameRules();
		(rules.getRule(GameRules.RULE_DAYLIGHT)).set(false, srv);
		(rules.getRule(GameRules.RULE_WEATHER_CYCLE)).set(false, srv);
		(rules.getRule(GameRules.RULE_DOMOBSPAWNING)).set(false, srv);
		(rules.getRule(GameRules.RULE_DOFIRETICK)).set(false, srv);
		(rules.getRule(GameRules.RULE_DO_TRADER_SPAWNING)).set(false, srv);
		level.setDayTime(6000L);
		level.setWeatherParameters(6000, 0, false, false);
		List<Entity> entities = Streams.stream(level.getEntities().getAll()).filter(entity -> !(entity instanceof Player)).toList();
		entities.forEach(entity -> entity.setRemoved(Entity.RemovalReason.DISCARDED));
		return 0;
	}

	private int dumpBlockstate(CommandContext<CommandSourceStack> context, BlockPos blockPos) throws CommandSyntaxException {
		ServerPlayer player = (context.getSource()).getPlayerOrException();
		ServerLevel world = player.serverLevel();
		BlockState blockState = world.getBlockState(blockPos);
		MinecraftTrophies.LOGGER.info("Blockstate {} = {}", blockPos, blockState);
		player.sendSystemMessage(Component.literal(blockState.toString()));
		return 0;
	}

	private int dumpItemNBT(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		ServerPlayer player = ((CommandSourceStack) context.getSource()).getPlayerOrException();
		ItemStack heldStack = player.getMainHandItem();
		MinecraftTrophies.LOGGER.info("Held Stack NBT = {}", heldStack.getTag());
		player.sendSystemMessage(Component.literal(heldStack.getTag().toString()));
		return 0;
	}

	@Override
	public boolean isDedicatedServerOnly() {
		return false;
	}
}
