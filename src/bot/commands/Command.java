package bot.commands;

import bot.core.BotContext;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import static net.dv8tion.jda.api.interactions.commands.build.Commands.slash;

public interface Command {
    String name();
    String description();

    default String category() { return null; }

    default CommandPermission permission() {
        return CommandPermission.everyone();
    }

    default CommandData data() {
        return slash(name(), description());
    }

    default void onAutoComplete(CommandAutoCompleteInteractionEvent event, BotContext ctx) {}

    void execute(SlashCommandInteractionEvent event, BotContext ctx) throws Exception;
}
