package bot.modules.restream;

import bot.BotConfig;
import bot.CommandListener;
import bot.core.BotContext;
import net.dv8tion.jda.api.JDA;
import restream.RestreamManager;
import restream.RestreamState;
import youtube.YouTubeLiveChecker;

public class RestreamModule {

    private final BotConfig cfg;

    private RestreamState state;
    private RestreamManager manager;
    private RestreamService service;

    public RestreamModule(BotConfig cfg) {
        this.cfg = cfg;
    }

    public void registerListeners(JDA jda) {
        jda.addEventListener(new CommandListener(state, manager));
    }

    public void start(BotContext ctx) {
        this.state = new RestreamState();
        var checker = new YouTubeLiveChecker(cfg.ytApiKey, cfg.sourceChannelId);
        this.manager = new RestreamManager(cfg.youtubeRtmpUrl, cfg.youtubeStreamKey, state);

        ctx.jda().addEventListener(new CommandListener(state, manager));

        this.service = new RestreamService(
                ctx.jda(),
                ctx,
                state,
                manager,
                checker,
                cfg.notifyDiscordUserId,
                cfg.pollSeconds
        );
        service.start();
    }


    public void stop() {
        if (service != null) service.stop();
    }
}
