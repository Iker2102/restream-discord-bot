package bot;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import restream.RestreamManager;
import restream.RestreamState;

public class CommandListener extends ListenerAdapter {
    private final RestreamState state;
    private final RestreamManager restream;

    public CommandListener(RestreamState state, RestreamManager restream) {
        this.state = state;
        this.restream = restream;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String msg = event.getMessage().getContentRaw().trim();

        if (msg.equalsIgnoreCase("!status")) {
            event.getChannel().sendMessage(
                    "SourceLive=" + state.isSourceLive +
                            " | Restreaming=" + state.isRestreaming +
                            " | lastVideoId=" + state.lastVideoId
            ).queue();
        }

        if (msg.equalsIgnoreCase("!stop")) {
            restream.stopRestream();
            event.getChannel().sendMessage("Parado.").queue();
        }
    }
}
