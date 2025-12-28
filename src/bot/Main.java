package bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import restream.RestreamManager;
import restream.RestreamState;
import util.Env;
import youtube.YouTubeLiveChecker;

import java.util.EnumSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws Exception {
        Env env = Env.load(".env");

        String discordToken = env.require("DISCORD_TOKEN");
        String ytApiKey = env.require("YOUTUBE_API_KEY");
        String channelId = env.require("YOUTUBE_CHANNEL_ID");
        String youtubeRtmpUrl = env.require("YOUTUBE_RTMP_URL");
        String youtubeStreamKey = env.require("YOUTUBE_STREAM_KEY");


        if (discordToken == null || ytApiKey == null || channelId == null || youtubeRtmpUrl == null || youtubeStreamKey == null) {
            System.out.println("Faltan variables de entorno. Necesitas:");
            System.out.println("DISCORD_TOKEN, YOUTUBE_API_KEY, YOUTUBE_CHANNEL_ID, YOUTUBE_RTMP_URL, YOUTUBE_STREAM_KEY");
            return;
        }

        RestreamState state = new RestreamState();
        YouTubeLiveChecker checker = new YouTubeLiveChecker(ytApiKey, channelId);
        RestreamManager restream = new RestreamManager(youtubeRtmpUrl, youtubeStreamKey, state);

        JDA jda = JDABuilder.createDefault(discordToken)
                .enableIntents(EnumSet.allOf(GatewayIntent.class))
                .enableCache(EnumSet.allOf(CacheFlag.class))
                .addEventListeners(new CommandListener(state, restream))
                .build();


        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                var live = checker.checkLive();
                state.isSourceLive = live.isLive();

                if (live.isLive()) {
                    if (state.lastVideoId == null || !state.lastVideoId.equals(live.videoId())) {
                        state.lastVideoId = live.videoId();
                        System.out.println("LIVE detectado: " + live.videoId());
                    }

                    if (!state.isRestreaming) {
                        restream.startRestream(live.watchUrl());
                    }
                } else {
                    state.lastVideoId = null;
                    if (state.isRestreaming) {
                        restream.stopRestream();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 20, TimeUnit.SECONDS);
    }
}
