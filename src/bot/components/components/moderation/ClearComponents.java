package bot.components.components.moderation;

import bot.commands.commands.moderation.ClearCommand;
import bot.components.Component;
import bot.core.BotContext;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;

public class ClearComponents implements Component {

    @Override
    public String prefix() {
        return "clear:";
    }

    @Override
    public void handle(GenericComponentInteractionCreateEvent event, BotContext ctx) {
        if (!(event instanceof ButtonInteractionEvent btn)) return;

        String id = btn.getComponentId();

        if (id.startsWith(ClearCommand.cancelPrefix())) {
            String ownerId = id.substring(ClearCommand.cancelPrefix().length());

            if (!btn.getUser().getId().equals(ownerId)) {
                btn.reply("❌ Ese botón no es tuyo.").setEphemeral(true).queue();
                return;
            }

            btn.editMessage("✅ Cancelado.").setComponents().queue();
            return;
        }

        if (id.startsWith(ClearCommand.confirmPrefix())) {
            String rest = id.substring(ClearCommand.confirmPrefix().length()); // <userId>:<amount>
            int sep = rest.lastIndexOf(':');
            if (sep <= 0) {
                btn.reply("⚠️ ID inválido.").setEphemeral(true).queue();
                return;
            }

            String ownerId = rest.substring(0, sep);
            int amount = Integer.parseInt(rest.substring(sep + 1));

            if (!btn.getUser().getId().equals(ownerId)) {
                btn.reply("❌ Ese botón no es tuyo.").setEphemeral(true).queue();
                return;
            }

            btn.deferEdit().queue();

            var channel = btn.getChannel();
            channel.getHistory().retrievePast(amount).queue(messages -> {
                if (messages.isEmpty()) {
                    btn.getHook().editOriginal("ℹ️ No había mensajes para borrar.").setComponents().queue();
                    return;
                }
                channel.purgeMessages(messages);
                btn.getHook().editOriginal("🧹 Borrados **" + messages.size() + "** mensajes.").setComponents().queue();
            }, err -> {
                btn.getHook().editOriginal("⚠️ Error borrando: " + err.getMessage()).setComponents().queue();
            });
        }
    }
}
