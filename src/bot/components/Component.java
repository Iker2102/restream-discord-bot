package bot.components;

import bot.core.BotContext;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;

public interface Component {

    String prefix();

    void handle(GenericComponentInteractionCreateEvent event, BotContext ctx) throws Exception;
}
