package bot.commands.commands.moderation;

import bot.commands.Command;
import bot.commands.CommandPermission;
import bot.core.BotContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.components.actionrow.ActionRow;


import static net.dv8tion.jda.api.interactions.commands.build.Commands.slash;

public class ClearCommand implements Command {

    // Para evitar colisiones + validar que solo el autor pueda pulsarlo
    private static final String BTN_CONFIRM_PREFIX = "clear:confirm:";
    private static final String BTN_CANCEL_PREFIX  = "clear:cancel:";

    @Override public String name() { return "clear"; }
    @Override public String description() { return "Borra mensajes del canal actual (con confirmación)"; }
    @Override public String category() { return "moderation"; }

    @Override
    public CommandPermission permission() {
        return CommandPermission.discord(Permission.MESSAGE_MANAGE)
                .botNeeds(Permission.MESSAGE_MANAGE);
    }

    @Override
    public CommandData data() {
        return slash(name(), description())
                .addOption(OptionType.INTEGER, "amount", "Cantidad de mensajes a borrar (1-100)", true);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, BotContext ctx) {
        var amountOpt = event.getOption("amount");
        int amount = amountOpt.getAsInt();

        if (amount < 1 || amount > 100) {
            event.reply("❌ `amount` debe estar entre **1** y **100**.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String userId = event.getUser().getId();

        event.reply(
                        "⚠️ Vas a borrar **" + amount + "** mensajes en este canal.\n" +
                                "¿Confirmas?"
                ).setEphemeral(true)
                .addComponents(
                        ActionRow.of(
                                Button.danger(BTN_CONFIRM_PREFIX + userId + ":" + amount, "✅ Confirmar"),
                                Button.secondary(BTN_CANCEL_PREFIX + userId, "✖ Cancelar")
                        )
                ).queue();

    }

    public static String confirmPrefix() { return BTN_CONFIRM_PREFIX; }
    public static String cancelPrefix()  { return BTN_CANCEL_PREFIX; }
}
