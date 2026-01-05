package bot.commands.commands.misc;

import bot.commands.Command;
import bot.commands.CommandHandler;
import bot.commands.CommandPermission;
import bot.core.BotContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;
import java.util.Map;

public class HelpCommand implements Command {

    @Override public String name() { return "help"; }
    @Override public String description() { return "Lista comandos por categorías"; }

    @Override
    public void execute(SlashCommandInteractionEvent event, BotContext ctx) {
        CommandHandler handler = ctx.commands();

        Map<String, List<Command>> byCat = handler.byCategory();
        StringBuilder sb = new StringBuilder("📌 **Comandos:**\n");

        byCat.forEach((cat, cmds) -> {
            sb.append("\n**").append(cat).append("**\n");
            for (Command c : cmds) {
                sb.append("• `/").append(c.name()).append("` — ").append(c.description()).append("\n");
            }
        });

        event.reply(sb.toString()).setEphemeral(true).queue();
    }
}
