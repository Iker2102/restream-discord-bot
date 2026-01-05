package bot.commands.commands.misc;

import bot.commands.Command;
import bot.commands.CommandPermission;
import bot.core.BotContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.awt.*;
import java.util.List;
import java.util.Map;
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
                .collect(Collectors.toList());


        event.replyChoiceStrings(cats).queue();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, BotContext ctx) {
        String requestedCat = event.getOption("category") != null
                ? event.getOption("category").getAsString()
                : null;

        var member = event.getMember();
        if (member == null) {
            event.reply("⚠️ Este comando solo se puede usar dentro de un servidor.").setEphemeral(true).queue();
            return;
        }

        Map<String, List<Command>> byCat = ctx.commands().byCategory();

        Map<String, List<Command>> visible = byCat.entrySet().stream()
                .map(e -> Map.entry(
                        e.getKey(),
                        e.getValue().stream()
                                .filter(cmd -> cmd.permission().canExecute(member, ctx.ownerId()))
                                .collect(Collectors.toList())
                ))
                .filter(e -> !e.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, java.util.LinkedHashMap::new));

        if (requestedCat != null) {
            String catKey = findCategoryKey(visible, requestedCat);
            if (catKey == null) {
                event.reply("❌ No existe esa categoría (o no tienes permisos para verla).").setEphemeral(true).queue();
                return;
            }

            event.replyEmbeds(buildCategoryEmbed(ctx, member.getUser().getName(), catKey, visible.get(catKey)).build())
                    .setEphemeral(true)
                    .queue();
            return;
        }

        event.replyEmbeds(buildIndexEmbed(ctx, member.getUser().getName(), visible).build())
                .setEphemeral(true)
                .queue();
    }

    private String findCategoryKey(Map<String, List<Command>> map, String requested) {
        for (String k : map.keySet()) {
            if (k.equalsIgnoreCase(requested)) return k;
        }
        return null;
    }

    private EmbedBuilder buildIndexEmbed(BotContext ctx, String username, Map<String, List<Command>> visible) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("📚 Ayuda - Categorías");
        eb.setDescription("Usa `/help category:<nombre>` para ver los comandos de una categoría.");
        eb.setFooter("Solicitado por " + username, null);
        eb.setColor(new Color(88, 101, 242));

        String cats = visible.keySet().stream()
                .map(c -> "• **" + c + "** (" + visible.get(c).size() + ")")
                .collect(Collectors.joining("\n"));

        if (cats.isBlank()) cats = "No hay comandos disponibles.";

        eb.addField("Categorías", cats, false);

        eb.addField("Tip", "Empieza a escribir en `category` y te saldrá autocompletado.", false);

        return eb;
    }

    private EmbedBuilder buildCategoryEmbed(BotContext ctx, String username, String category, List<Command> cmds) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("📂 " + category);
        eb.setDescription("Comandos disponibles en esta categoría:");
        eb.setFooter("Solicitado por " + username, null);
        eb.setColor(new Color(46, 204, 113));

        String body = cmds.stream()
                .map(c -> formatCommandLine(c))
                .collect(Collectors.joining("\n"));

        if (body.isBlank()) body = "No hay comandos disponibles.";

        if (body.length() > 1000) {
            body = body.substring(0, 1000) + "\n…";
        }

        eb.addField("Comandos", body, false);
        eb.addField("Volver", "Usa `/help` para ver el índice.", false);

        return eb;
    }

    private String formatCommandLine(Command c) {
        String lock = permissionIcon(c.permission());
        return lock + " `/" + c.name() + "` — " + c.description();
    }

    private String permissionIcon(CommandPermission p) {
        if (p == null) return "•";
        return switch (p.type()) {
            case EVERYONE -> "•";
            case OWNER_ONLY -> "👑";
            case DISCORD_PERMISSIONS -> "🛡️";
        };
    }
}
