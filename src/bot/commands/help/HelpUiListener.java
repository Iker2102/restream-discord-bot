package bot.commands.help;

import bot.core.BotContext;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class HelpUiListener extends ListenerAdapter {

    private final BotContext ctx;

    public HelpUiListener(BotContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        String id = event.getComponentId(); // help:select:<userId>
        if (!id.startsWith(HelpView.SELECT_ID_PREFIX)) return;

        String ownerId = id.substring(HelpView.SELECT_ID_PREFIX.length());
        if (!event.getUser().getId().equals(ownerId)) {
            event.reply("❌ Ese menú no es tuyo. Usa `/help`.").setEphemeral(true).queue();
            return;
        }

        String category = event.getValues().get(0);
        var member = event.getMember();
        if (member == null) {
            event.reply("⚠️ Úsalo dentro de un servidor.").setEphemeral(true).queue();
            return;
        }

        var visible = HelpView.visibleCommandsByCategory(ctx, member);

        var cmds = visible.get(category);
        if (cmds == null || cmds.isEmpty()) {
            event.reply("❌ Esa categoría no existe o no tienes permisos.").setEphemeral(true).queue();
            return;
        }

        event.editMessageEmbeds(HelpView.categoryEmbed(ctx, event.getUser().getName(), category, cmds).build())
                .setComponents(HelpView.backRow(ownerId))
                .queue();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getComponentId(); // help:back:<userId>
        if (!id.startsWith(HelpView.BACK_ID_PREFIX)) return;

        String ownerId = id.substring(HelpView.BACK_ID_PREFIX.length());
        if (!event.getUser().getId().equals(ownerId)) {
            event.reply("❌ Ese botón no es tuyo. Usa `/help`.").setEphemeral(true).queue();
            return;
        }

        var member = event.getMember();
        if (member == null) {
            event.reply("⚠️ Úsalo dentro de un servidor.").setEphemeral(true).queue();
            return;
        }

        var visible = HelpView.visibleCommandsByCategory(ctx, member);

        event.editMessageEmbeds(HelpView.indexEmbed(ctx, event.getUser().getName(), visible).build())
                .setComponents(HelpView.selectRow(ownerId, visible.keySet()))
                .queue();
    }
}
