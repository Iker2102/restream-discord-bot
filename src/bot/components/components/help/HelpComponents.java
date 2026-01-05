package bot.components.components.help;

import bot.commands.help.HelpView;
import bot.components.Component;
import bot.core.BotContext;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

public class HelpComponents implements Component {

    @Override
    public String prefix() {
        return "help:";
    }

    @Override
    public void handle(GenericComponentInteractionCreateEvent event, BotContext ctx) {
        String id = event.getComponentId();

        // Select: help:select:<userId>
        if (event instanceof StringSelectInteractionEvent select) {
            if (!id.startsWith(HelpView.SELECT_ID_PREFIX)) return;

            String ownerId = id.substring(HelpView.SELECT_ID_PREFIX.length());
            if (!select.getUser().getId().equals(ownerId)) {
                select.reply("❌ Ese menú no es tuyo. Usa `/help`.").setEphemeral(true).queue();
                return;
            }

            var member = select.getMember();
            if (member == null) return;

            String category = select.getValues().get(0);

            var visible = HelpView.visibleCommandsByCategory(ctx, member);
            var cmds = visible.get(category);

            select.editMessageEmbeds(
                    HelpView.categoryEmbed(ctx, select.getUser().getName(), category, cmds).build()
            ).setComponents(
                    HelpView.backRow(ownerId)
            ).queue();

            return;
        }

        // Button: help:back:<userId>
        if (event instanceof ButtonInteractionEvent btn) {
            if (!id.startsWith(HelpView.BACK_ID_PREFIX)) return;

            String ownerId = id.substring(HelpView.BACK_ID_PREFIX.length());
            if (!btn.getUser().getId().equals(ownerId)) {
                btn.reply("❌ Ese botón no es tuyo.").setEphemeral(true).queue();
                return;
            }

            var member = btn.getMember();
            if (member == null) return;

            var visible = HelpView.visibleCommandsByCategory(ctx, member);

            btn.editMessageEmbeds(
                    HelpView.indexEmbed(ctx, btn.getUser().getName(), visible).build()
            ).setComponents(
                    HelpView.selectRow(ownerId, visible.keySet())
            ).queue();
        }
    }
}
