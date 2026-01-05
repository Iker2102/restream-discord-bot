package bot.commands.commands.misc;

import bot.commands.Command;
import bot.core.BotContext;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.stream.Collectors;

import static net.dv8tion.jda.api.interactions.commands.build.Commands.slash;

public class HelpCommand implements Command {

    @Override public String name() { return "help"; }
    @Override public String description() { return "Muestra comandos (puedes filtrar por categoría)"; }

    @Override
    public CommandData data() {
        return slash(name(), description())
                .addOption(OptionType.STRING, "category", "Filtra por categoría", false, true);
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, BotContext ctx) {
        if (!event.getFocusedOption().getName().equals("category")) return;

        String typed = event.getFocusedOption().getValue().toLowerCase();

        var cats = ctx.commands().byCategory().keySet().stream()
                .filter(c -> c.toLowerCase().contains(typed))
                .limit(25)
                .map(c -> new net.dv8tion.jda.api.interactions.commands.Command.Choice(c, c))
                .collect(Collectors.toList());

        event.replyChoices(cats).queue();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, BotContext ctx) {
        String cat = event.getOption("category") != null ? event.getOption("category").getAsString() : null;

        var byCat = ctx.commands().byCategory();

        StringBuilder sb = new StringBuilder("📌 **Comandos:**\n");

        byCat.forEach((category, cmds) -> {
            if (cat != null && !category.equalsIgnoreCase(cat)) return;

            sb.append("\n**").append(category).append("**\n");
            for (var c : cmds) {
                sb.append("• `/").append(c.name()).append("` — ").append(c.description()).append("\n");
            }
        });

        event.reply(sb.toString()).setEphemeral(true).queue();
    }
}
