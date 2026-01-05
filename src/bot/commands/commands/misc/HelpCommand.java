package bot.commands.commands.misc;

import bot.commands.Command;
import bot.commands.help.HelpView;
import bot.core.BotContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class HelpCommand implements Command {

    @Override public String name() { return "help"; }
    @Override public String description() { return "Muestra la ayuda del bot"; }

    @Override
    public void execute(SlashCommandInteractionEvent event, BotContext ctx) {
        var member = event.getMember();
        if (member == null) {
            event.reply("⚠️ Úsalo dentro de un servidor.").setEphemeral(true).queue();
            return;
        }

        var visible = HelpView.visibleCommandsByCategory(ctx, member);
        String userId = event.getUser().getId();

        event.replyEmbeds(HelpView.indexEmbed(ctx, event.getUser().getName(), visible).build())
                .addComponents(HelpView.selectRow(userId, visible.keySet()))
                .setEphemeral(true)
                .queue();
    }
}
