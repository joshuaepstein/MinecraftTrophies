package uk.co.joshepstein.minecrafttrophies.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import uk.co.joshepstein.minecrafttrophies.MinecraftTrophies;

public class VersionCommand extends Command {

	@Override
	public String getName() {
		return "version";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public void build(LiteralArgumentBuilder<CommandSourceStack> builder) {
		builder.then(Commands.literal("current").executes((context) -> {
			sendFeedback(context, "Your running AdvancementTrophies version: " + MinecraftTrophies.VERSION, false);
			return 1;
		}));
	}

	@Override
	public boolean isDedicatedServerOnly() {
		return false;
	}
}