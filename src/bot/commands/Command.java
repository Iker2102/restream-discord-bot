package bot.commands;

import bot.core.BotContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface Command {
    String name();
    String description();

    // Opcional: si devuelves null o "", se deduce por el paquete (carpeta)
    default String category() {
        return null;
    }

    void execute(SlashCommandInteractionEvent event, BotContext ctx) throws Exception;
}
