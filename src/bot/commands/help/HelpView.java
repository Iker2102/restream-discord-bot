package bot.commands.help;

import bot.commands.Command;
import bot.core.BotContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.Member;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public final class HelpView {

    private HelpView() {}

    public static final String SELECT_ID_PREFIX = "help:select:";
    public static final String BACK_ID_PREFIX   = "help:back:";

    public static Map<String, List<Command>> visibleCommandsByCategory(BotContext ctx, Member member) {
        var byCat = ctx.commands().byCategory();

        Map<String, List<Command>> visible = new LinkedHashMap<>();
        for (var e : byCat.entrySet()) {
            var list = e.getValue().stream()
                    .filter(cmd -> cmd.permission().canExecute(member, ctx.ownerId()))
                    .collect(Collectors.toList());
            if (!list.isEmpty()) visible.put(e.getKey(), list);
        }
        return visible;
    }

    public static EmbedBuilder indexEmbed(BotContext ctx, String username, Map<String, List<Command>> visible) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("📚 Ayuda - Categorías");
        eb.setDescription("Selecciona una categoría en el desplegable.");
        eb.setFooter("Solicitado por " + username, null);
        eb.setColor(new Color(88, 101, 242));

        String cats = visible.keySet().stream()
                .map(c -> "• **" + c + "** (" + visible.get(c).size() + ")")
                .collect(Collectors.joining("\n"));

        if (cats.isBlank()) cats = "No hay comandos disponibles.";
        eb.addField("Categorías", cats, false);

        return eb;
    }

    public static EmbedBuilder categoryEmbed(BotContext ctx, String username, String category, List<Command> cmds) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("📂 " + category);
        eb.setDescription("Comandos disponibles:");
        eb.setFooter("Solicitado por " + username, null);
        eb.setColor(new Color(46, 204, 113));

        String body = cmds.stream()
                .map(c -> "• `/" + c.name() + "` — " + c.description())
                .collect(Collectors.joining("\n"));

        if (body.isBlank()) body = "No hay comandos disponibles.";

        if (body.length() > 1000) body = body.substring(0, 1000) + "\n…";

        eb.addField("Comandos", body, false);
        return eb;
    }

    public static ActionRow selectRow(String userId, Collection<String> categories) {
        StringSelectMenu.Builder menu = StringSelectMenu.create(SELECT_ID_PREFIX + userId)
                .setPlaceholder("Elige una categoría…")
                .setRequiredRange(1, 1);

        // Discord: max 25 opciones
        int i = 0;
        for (String c : categories) {
            if (i++ >= 25) break;
            menu.addOption(c, c);
        }

        return ActionRow.of(menu.build());
    }

    public static ActionRow backRow(String userId) {
        return ActionRow.of(Button.secondary(BACK_ID_PREFIX + userId, "⬅ Volver"));
    }
}
