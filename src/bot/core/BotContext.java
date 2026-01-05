package bot.core;

import bot.commands.CommandHandler;
import net.dv8tion.jda.api.JDA;

public class BotContext {
    private final JDA jda;
    private CommandHandler commandHandler; // se setea después

    public BotContext(JDA jda) {
        this.jda = jda;
    }

    public JDA jda() {
        return jda;
    }

    public CommandHandler commands() {
        return commandHandler;
    }

    public void setCommandHandler(CommandHandler handler) {
        this.commandHandler = handler;
    }
}
