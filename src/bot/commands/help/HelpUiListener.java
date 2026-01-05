package bot.commands.help;

import bot.core.BotContext;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class HelpUiListener extends ListenerAdapter {

    private final BotContext ctx;

    public HelpUiListener(BotContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        String id = event.getComponentId();
        if (!id.startsWith(HelpView.SELECT_ID_PREFIX)) return;

        String ownerId = id.substring(HelpView.SELECT_ID_PREFIX.length());

        if (!event.getUser().getId().equals(ownerId)) {
            event.reply("❌ Ese menú no es tuyo. Usa `/help`.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        var member = event.getMember();
        if (member == null) return;

        String category = event.getValues().get(0);

        var visible = HelpView.visibleCommandsByCategory(ctx, member);
        var cmds = visible.get(category);

        if (cmds == null) {
            event.reply("❌ Categoría inválida.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        event.editMessageEmbeds(
                HelpView.categoryEmbed(ctx, event.getUser().getName(), category, cmds).build()
        ).setComponents(
                HelpView.backRow(ownerId)
        ).queue();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getComponentId();
        if (!id.startsWith(HelpView.BACK_ID_PREFIX)) return;

        String ownerId = id.substring(HelpView.BACK_ID_PREFIX.length());

        if (!event.getUser().getId().equals(ownerId)) {
            event.reply("❌ Ese botón no es tuyo.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        var member = event.getMember();
        if (member == null) return;

        var visible = HelpView.visibleCommandsByCategory(ctx, member);

        event.editMessageEmbeds(
                HelpView.indexEmbed(ctx, event.getUser().getName(), visible).build()
        ).setComponents(
                HelpView.selectRow(ownerId, visible.keySet())
        ).queue();
    }
}
