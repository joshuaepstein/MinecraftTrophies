package uk.co.joshepstein.minecrafttrophies.init;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.commands.DebugCommand;
import uk.co.joshepstein.minecrafttrophies.commands.ReloadConfigsCommand;
import uk.co.joshepstein.minecrafttrophies.commands.TrophyCommand;
import uk.co.joshepstein.minecrafttrophies.commands.VersionCommand;

import java.util.function.Supplier;

public class ModCommands {

    public static VersionCommand VERSION_COMMAND;
    public static ReloadConfigsCommand RELOAD_CONFIGS_COMMAND;
    public static TrophyCommand TROPHY_COMMAND;
    public static DebugCommand DEBUG_COMMANDS;

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection env) {
        VERSION_COMMAND = registerCommand(VersionCommand::new, dispatcher, env);
        RELOAD_CONFIGS_COMMAND = registerCommand(ReloadConfigsCommand::new, dispatcher, env);
        TROPHY_COMMAND = registerCommand(TrophyCommand::new, dispatcher, env);
        DEBUG_COMMANDS = registerCommand(DebugCommand::new, dispatcher, env);
    }

    public static <T extends Command> T registerCommand(Supplier<T> supplier, CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection env) {
        Command command = supplier.get();
        if (!command.isDedicatedServerOnly() || env == Commands.CommandSelection.DEDICATED || env == Commands.CommandSelection.ALL) {
            command.registerCommand(dispatcher, "advancementtrophies");
        }
        return (T) command;
    }
}
