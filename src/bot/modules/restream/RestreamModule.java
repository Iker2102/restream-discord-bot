package bot.modules.restream;

import bot.BotConfig;
import bot.CommandListener;
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

    public void start(JDA jda) {
        state = new RestreamState();
        var checker = new YouTubeLiveChecker(cfg.ytApiKey, cfg.sourceChannelId);
        manager = new RestreamManager(cfg.youtubeRtmpUrl, cfg.youtubeStreamKey, state);

        jda.addEventListener(new CommandListener(state, manager));

        service = new RestreamService(jda, state, manager, checker, cfg.notifyDiscordUserId, cfg.pollSeconds);
        service.start();
    }

    public void stop() {
        if (service != null) service.stop();
    }
}
