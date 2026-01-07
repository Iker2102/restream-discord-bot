package bot.modules.restream;

import net.dv8tion.jda.api.JDA;
import restream.RestreamManager;
import restream.RestreamState;
import util.ExponentialBackoff;
import youtube.YouTubeLiveChecker;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import bot.core.BotContext;


public class RestreamService {

    private final JDA jda;
    private final RestreamState state;
    private final RestreamManager restream;
    private final YouTubeLiveChecker checker;
    private final long notifyUserId;
    private final int pollSeconds;

    private final ExponentialBackoff backoff = new ExponentialBackoff(5_000, 5 * 60_000);
    private volatile long nextAllowedStartMs = 0;

    private volatile boolean tickerEnabled = true;
    private ScheduledExecutorService scheduler;

    private final BotContext ctx;




    public RestreamService(
            JDA jda,
            BotContext ctx,
            RestreamState state,
            RestreamManager restream,
            YouTubeLiveChecker checker,
            long notifyUserId,
            int pollSeconds
    ) {
        this.jda = jda;
        this.ctx = ctx;
        this.state = state;
        this.restream = restream;
        this.checker = checker;
        this.notifyUserId = notifyUserId;
        this.pollSeconds = pollSeconds;
    }


    public void start() {
        scheduler = Executors.newScheduledThreadPool(2);

        scheduler.scheduleAtFixedRate(this::tickCheckLiveSafe, 0, pollSeconds, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(() -> {
            if (!tickerEnabled) return;
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void stop() {
        tickerEnabled = false;
        try { if (scheduler != null) scheduler.shutdownNow(); } catch (Exception ignored) {}
        try { restream.stopRestream(); } catch (Exception ignored) {}
    }

    private void tickCheckLiveSafe() {
        try {
            var live = checker.checkLive();
            state.isSourceLive = live.isLive();

            if (live.isLive()) {
                if (state.lastVideoId == null || !state.lastVideoId.equals(live.videoId())) {
                    state.lastVideoId = live.videoId();
                    sendLiveDm(live.watchUrl());
                }

                if (!state.isRestreaming) {
                    long now = System.currentTimeMillis();
                    if (now >= nextAllowedStartMs) {
                        try {
                            restream.startRestream(live.watchUrl());
                            backoff.reset();
                            nextAllowedStartMs = 0;
                        } catch (Exception ex) {
                            long delay = backoff.nextDelayMs();
                            nextAllowedStartMs = now + delay;
                        }
                    }
                }
            } else {
                state.lastVideoId = null;
                backoff.reset();
                nextAllowedStartMs = 0;

                if (state.isRestreaming) restream.stopRestream();
            }

        } catch (Exception ignored) {
        }
    }

    private void sendLiveDm(String watchUrl) {
        boolean enabled = ctx.settings().getBool("restream.notify.enabled", true);
        if (!enabled) return;

        jda.retrieveUserById(notifyUserId).queue(user ->
                user.openPrivateChannel().queue(ch ->
                        ch.sendMessage("🔴 **Directo iniciado**\n" + watchUrl).queue()
                )
        );
    }

}
