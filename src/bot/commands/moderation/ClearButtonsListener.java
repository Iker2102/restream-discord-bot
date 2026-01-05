package bot.commands.moderation;

import bot.commands.commands.moderation.ClearCommand;
import bot.core.BotContext;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.concurrent.TimeUnit;

public class ClearButtonsListener extends ListenerAdapter {

    private final BotContext ctx;

    public ClearButtonsListener(BotContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getComponentId();

        if (id.startsWith(ClearCommand.confirmPrefix())) {
            handleConfirm(event, id);
            return;
        }

        if (id.startsWith(ClearCommand.cancelPrefix())) {
            handleCancel(event, id);
        }
    }

    private void handleCancel(ButtonInteractionEvent event, String id) {
        String ownerId = id.substring(ClearCommand.cancelPrefix().length());

        if (!event.getUser().getId().equals(ownerId)) {
            event.reply("❌ Ese botón no es tuyo.").setEphemeral(true).queue();
            return;
        }

        event.editMessage("✅ Cancelado.").setComponents().queue();
    }

    private void handleConfirm(ButtonInteractionEvent event, String id) {
        // id: clear:confirm:<userId>:<amount>
        String rest = id.substring(ClearCommand.confirmPrefix().length()); // <userId>:<amount>
        int sep = rest.lastIndexOf(':');
        if (sep <= 0) {
            event.reply("⚠️ ID inválido.").setEphemeral(true).queue();
            return;
        }

        String ownerId = rest.substring(0, sep);
        String amountStr = rest.substring(sep + 1);

        if (!event.getUser().getId().equals(ownerId)) {
            event.reply("❌ Ese botón no es tuyo.").setEphemeral(true).queue();
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            event.reply("⚠️ Cantidad inválida.").setEphemeral(true).queue();
            return;
        }

        var channel = event.getChannel();

        event.deferEdit().queue();

        channel.getHistory().retrievePast(amount).queue(messages -> {
            if (messages.isEmpty()) {
                event.getHook().editOriginal("ℹ️ No había mensajes para borrar.").setComponents().queue();
                return;
            }

            channel.purgeMessages(messages);

            event.getHook().editOriginal("🧹 Borrados **" + messages.size() + "** mensajes.")
                    .setComponents()
                    .queue();

            event.getHook().deleteOriginal().queueAfter(5, TimeUnit.SECONDS);

        }, err -> {
            event.getHook().editOriginal("⚠️ Error borrando: " + err.getMessage())
                    .setComponents()
                    .queue();
        });
    }
}
