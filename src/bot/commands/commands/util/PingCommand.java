package bot.commands.commands.util;

import bot.commands.Command;
import bot.core.BotContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class PingCommand implements Command {

    @Override
    public String name() {
        return "ping";
    }

    @Override
    public String description() {
        return "Te contesta Pong!";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, BotContext ctx) throws Exception {
        event.reply("Pong!").setEphemeral(true).queue();
    }


}
