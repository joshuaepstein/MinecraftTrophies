package uk.co.joshepstein.minecrafttrophies.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.server.command.EnumArgument;
import uk.co.joshepstein.minecrafttrophies.blocks.TrophyBlock;
import uk.co.joshepstein.minecrafttrophies.init.ModBlocks;

import java.util.Collection;
import java.util.Date;

public class TrophyCommand extends Command {
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_ADVANCEMENTS = (p_136344_, p_136345_) -> {
		Collection<AdvancementHolder> collection = p_136344_.getSource().getServer().getAdvancements().getAllAdvancements();
		return SharedSuggestionProvider.suggestResource(collection.stream().map(AdvancementHolder::id), p_136345_);
	};
	private static final SuggestionProvider<CommandSourceStack> SUGGESTS_ITEM = (p_136344_, p_136345_) -> {
		Collection<Item> collection = BuiltInRegistries.ITEM.stream().toList();
		return SharedSuggestionProvider.suggestResource(collection.stream().map(BuiltInRegistries.ITEM::getKey), p_136345_);
	};


	@Override
	public String getName() {
		return "trophy";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public void build(LiteralArgumentBuilder<CommandSourceStack> builder) {
		builder.then(Commands.literal("type").then(Commands.argument("type", EnumArgument.enumArgument(TrophyBlock.TrophyData.Type.class)).executes(this::setType)));
		builder.then(Commands.literal("advancement").then(Commands.argument("advancement", ResourceLocationArgument.id()).suggests(SUGGEST_ADVANCEMENTS).executes(this::setAdvancement)));
		builder.then(Commands.literal("givento").then(Commands.argument("player", EntityArgument.player()).executes(this::giveTo)));
		builder.then(Commands.literal("item").then(Commands.argument("item", ResourceLocationArgument.id()).suggests(SUGGESTS_ITEM).executes(this::setItem)));
		builder.then(Commands.literal("all_advancements").executes(context -> {
			context.getSource().sendSuccess(() -> Component.literal("All advancements: ").withStyle(ChatFormatting.GREEN).withStyle(ChatFormatting.GREEN), true);
			ServerAdvancementManager manager = context.getSource().getServer().getAdvancements();
			manager.getAllAdvancements().forEach(advancement -> {
				System.out.println(advancement.id());
			});

			return 1;
		}));
	}

	public int setItem(CommandContext<CommandSourceStack> command) throws CommandSyntaxException {
		if (command.getSource().getPlayerOrException().getMainHandItem().getItem() != ModBlocks.TROPHY.get().asItem()) {
			command.getSource().sendSuccess(() -> (Component.literal("You must be holding a trophy!")).withStyle(ChatFormatting.RED), true);
			return 0;
		}
		ResourceLocation item = ResourceLocationArgument.getId(command, "item");
		ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(item));
		ItemStack hand = command.getSource().getPlayerOrException().getMainHandItem();
		TrophyBlock.TrophyData data = new TrophyBlock.TrophyData(hand);
		data.setItemResourceLocation(item);
		data.updateDelegate();
		CompoundTag blockEntityTag = new CompoundTag();
		blockEntityTag.put("TrophyData", data.serializeNBT());
		hand.getOrCreateTag().put("BlockEntityTag", blockEntityTag);
		command.getSource().sendSuccess(() -> Component.literal("Set trophy item to: ").withStyle(ChatFormatting.GREEN).append(Component.translatable(stack.getItem().getDescriptionId())).withStyle(ChatFormatting.GREEN), true);
		return 1;
	}

	public int giveTo(CommandContext<CommandSourceStack> command) throws CommandSyntaxException {
		if (command.getSource().getPlayerOrException().getMainHandItem().getItem() != ModBlocks.TROPHY.get().asItem()) {
			command.getSource().sendSuccess(() -> (Component.literal("You must be holding a trophy!")).withStyle(ChatFormatting.RED), true);
			return 0;
		}
		Player type = EntityArgument.getPlayer(command, "player");
		ItemStack stack = command.getSource().getPlayerOrException().getMainHandItem();
		TrophyBlock.TrophyData data = new TrophyBlock.TrophyData(stack);
		data.setGivenPlayerName(type.getName().getString());
		data.updateDelegate();
		CompoundTag blockEntityTag = new CompoundTag();
		blockEntityTag.put("TrophyData", data.serializeNBT());
		stack.getOrCreateTag().put("BlockEntityTag", blockEntityTag);
		command.getSource().sendSuccess(() -> Component.literal("Set trophy given to: ").withStyle(ChatFormatting.GREEN).append(Component.translatable(type.getName().getString())).withStyle(ChatFormatting.GREEN), true);
		return 1;
	}

	public int setType(CommandContext<CommandSourceStack> command) throws CommandSyntaxException {
		if (command.getSource().getPlayerOrException().getMainHandItem().getItem() != ModBlocks.TROPHY.get().asItem()) {
			command.getSource().sendSuccess(() -> (Component.literal("You must be holding a trophy!")).withStyle(ChatFormatting.RED), true);
			return 0;
		}
		TrophyBlock.TrophyData.Type type = command.getArgument("type", TrophyBlock.TrophyData.Type.class);
		ItemStack stack = command.getSource().getPlayerOrException().getMainHandItem();
		TrophyBlock.TrophyData data = new TrophyBlock.TrophyData(stack);
		data.setType(type.getRegistryName());
		data.updateDelegate();
		CompoundTag blockEntityTag = new CompoundTag();
		blockEntityTag.put("TrophyData", data.serializeNBT());
		stack.getOrCreateTag().put("BlockEntityTag", blockEntityTag);
		stack.setHoverName(stack.getHoverName().copy().withStyle(style -> style.withColor(type.getColor())));
		command.getSource().sendSuccess(() -> Component.literal("Set trophy type to ").withStyle(ChatFormatting.WHITE).append(type.getColor() + type.getName().toUpperCase()), true);
		return 1;
	}

	public int setAdvancement(CommandContext<CommandSourceStack> command) throws CommandSyntaxException {
		if (command.getSource().getPlayerOrException().getMainHandItem().getItem() != ModBlocks.TROPHY.get().asItem()) {
			command.getSource().sendSuccess(() -> (Component.literal("You must be holding a trophy!")).withStyle(ChatFormatting.RED), true);
			return 0;
		}
		ItemStack stack = command.getSource().getPlayerOrException().getMainHandItem();
		TrophyBlock.TrophyData data = new TrophyBlock.TrophyData(stack);
		data.setAdvName(ResourceLocationArgument.getAdvancement(command, "advancement").value().display().orElseThrow().getTitle().getString());
		data.setItemResourceLocation(BuiltInRegistries.ITEM.getKey(ResourceLocationArgument.getAdvancement(command, "advancement").value().display().orElseThrow().getIcon().getItem()));
		data.setDate(new Date());
		data.setGivenPlayerName(command.getSource().getPlayerOrException().getDisplayName().getString());
		data.updateDelegate();
		CompoundTag blockEntityTag = new CompoundTag();
		blockEntityTag.put("TrophyData", data.serializeNBT());
		stack.getOrCreateTag().put("BlockEntityTag", blockEntityTag);
		stack.setHoverName(Component.literal(ResourceLocationArgument.getAdvancement(command, "advancement").value().display().orElseThrow().getTitle().getString() + " Trophy").withStyle(Style.EMPTY.withColor(TrophyBlock.TrophyData.Type.fromRegistryName(data.getType()).getColor()).withItalic(false)));
		command.getSource().sendSuccess(() -> Component.literal("Set trophy advancement to ").withStyle(ChatFormatting.WHITE).append(ChatFormatting.GREEN + data.getAdvName()), true);
		return 1;
	}

	@Override
	public boolean isDedicatedServerOnly() {
		return false;
	}
}
