package bot.core;

import bot.commands.CommandHandler;
import net.dv8tion.jda.api.JDA;
import bot.components.ComponentHandler;


public class BotContext {
    private final JDA jda;
    private final String devGuildId;
    private final String ownerId;


    private CommandHandler commandHandler;
    private ComponentHandler componentHandler;


    public ComponentHandler components() {
        return componentHandler;
    }

    public void setComponentHandler(ComponentHandler componentHandler) {
        this.componentHandler = componentHandler;
    }

    public BotContext(JDA jda, String devGuildId, String ownerId) {
        this.jda = jda;
        this.devGuildId = devGuildId;
        this.ownerId = ownerId;
    }

    public JDA jda() { return jda; }
    public String devGuildId() { return devGuildId; }
    public String ownerId() { return ownerId; }

    public CommandHandler commands() { return commandHandler; }
    public void setCommandHandler(CommandHandler handler) { this.commandHandler = handler; }
}
