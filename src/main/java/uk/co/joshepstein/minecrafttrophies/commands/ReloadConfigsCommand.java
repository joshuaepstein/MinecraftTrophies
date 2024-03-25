package uk.co.joshepstein.minecrafttrophies.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PlayNetworkDirection;
import uk.co.joshepstein.minecrafttrophies.init.ModConfigs;
import uk.co.joshepstein.minecrafttrophies.init.ModNetwork;
import uk.co.joshepstein.minecrafttrophies.network.message.InvalidConfigsMessage;

public class ReloadConfigsCommand extends Command {
	@Override
	public String getName() {
		return "reloadcfg";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public void build(LiteralArgumentBuilder<CommandSourceStack> builder) {
		builder.executes(this::reloadCfg);
	}

	public int reloadCfg(CommandContext<CommandSourceStack> command) throws CommandSyntaxException {
		try {
			ModConfigs.register();
		} catch (Exception var4) {
			var4.printStackTrace();
			throw var4;
		}

		if (!ModConfigs.INVALID_CONFIGS.isEmpty()) {
			(command.getSource()).sendSuccess(() -> (Component.literal("Configs reloaded, with errors!")).withStyle(ChatFormatting.RED), true);

			try {
				ModNetwork.CHANNEL.sendTo(new InvalidConfigsMessage(ModConfigs.INVALID_CONFIGS), (command.getSource()).getPlayerOrException().connection.connection, PlayNetworkDirection.PLAY_TO_CLIENT);
			} catch (CommandSyntaxException var3) {
				var3.printStackTrace();
				throw var3;
			}
		} else {
			(command.getSource()).sendSuccess(() -> (Component.literal("Configs reloaded!")).withStyle(ChatFormatting.GREEN), true);
		}

		return 0;
	}

	@Override
	public boolean isDedicatedServerOnly() {
		return false;
	}
}