package bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import restream.RestreamManager;
import restream.RestreamState;
import util.Console;
import util.Env;
import youtube.YouTubeLiveChecker;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final DateTimeFormatter T = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String STATUS_PREFIX = "│ ";

    private static volatile boolean tickerEnabled = true;

    public static void main(String[] args) throws Exception {

        // Silenciar JDA (para que no rompa tu línea de estado)
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "warn");
        System.setProperty("org.slf4j.simpleLogger.log.net.dv8tion", "warn");
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "false");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");

        // Cargar .env
        Env env = Env.load(".env");

        String discordToken    = env.require("DISCORD_TOKEN");
        String ytApiKey        = env.require("YOUTUBE_API_KEY");
        String channelId       = env.require("YOUTUBE_CHANNEL_ID");
        String youtubeRtmpUrl  = env.require("YOUTUBE_RTMP_URL");
        String youtubeStreamKey= env.require("YOUTUBE_STREAM_KEY");
        int pollSeconds        = Integer.parseInt(env.require("POLL_SECONDS"));

        if (discordToken == null || ytApiKey == null || channelId == null || youtubeRtmpUrl == null || youtubeStreamKey == null) {
            Console.section("Restream Bot");
            Console.fail("Faltan variables de entorno. Necesitas:");
            System.out.println("DISCORD_TOKEN, YOUTUBE_API_KEY, YOUTUBE_CHANNEL_ID, YOUTUBE_RTMP_URL, YOUTUBE_STREAM_KEY");
            return;
        }

        Console.section("Restream Bot");
        Console.ok("Variables cargadas");
        Console.info("Inicializando módulos...");

        RestreamState state = new RestreamState();
        YouTubeLiveChecker checker = new YouTubeLiveChecker(ytApiKey, channelId);
        RestreamManager restream = new RestreamManager(youtubeRtmpUrl, youtubeStreamKey, state);

        Console.info("Conectando a Discord (JDA)...");

        JDA jda = JDABuilder.createDefault(discordToken)
                .enableIntents(EnumSet.allOf(GatewayIntent.class))
                .enableCache(EnumSet.allOf(CacheFlag.class))
                .addEventListeners(new CommandListener(state, restream))
                .build();

        jda.awaitReady();
        Console.ok("JDA listo y conectado");

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

        scheduler.scheduleAtFixedRate(() -> {
            try {
                var live = checker.checkLive();
                boolean wasLive = state.isSourceLive;
                state.isSourceLive = live.isLive();

                if (live.isLive()) {
                    if (state.lastVideoId == null || !state.lastVideoId.equals(live.videoId())) {
                        state.lastVideoId = live.videoId();
                        logEvent("LIVE detectado: " + live.videoId());
                    }

                    if (!state.isRestreaming) {
                        logEvent("Iniciando restream → YouTube RTMP");
                        restream.startRestream(live.watchUrl());
                    }

                } else {
                    if (wasLive) logEvent("El canal ha dejado de estar LIVE");

                    state.lastVideoId = null;

                    if (state.isRestreaming) {
                        logEvent("Parando restream (fuente OFF)");
                        restream.stopRestream();
                    }
                }

            } catch (Exception e) {
                logEvent("ERROR en checkLive(): " + e.getMessage());
            }
        }, 0, pollSeconds, TimeUnit.SECONDS);

        scheduler.scheduleAtFixedRate(() -> {
            if (!tickerEnabled) return;
            renderStatusLine(state, pollSeconds);
        }, 0, 1, TimeUnit.SECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                tickerEnabled = false;
                System.out.println(); // baja de la línea del ticker
                Console.warn("Cerrando...");
                try { restream.stopRestream(); } catch (Exception ignored) {}
                try { jda.shutdownNow(); } catch (Exception ignored) {}
                Console.ok("Apagado completo");
            } catch (Exception ignored) {}
        }));
    }

    private static void renderStatusLine(RestreamState state, int pollSeconds) {
        synchronized (CONSOLE_LOCK) {
            String live = state.isSourceLive ? "ON " : "OFF";
            String rs   = state.isRestreaming ? "ON " : "OFF";
            String vid  = (state.lastVideoId == null ? "-" : state.lastVideoId);

            String line = String.format(
                    "│ %s | Live:%s  Restream:%s  Video:%s  Poll:%ss",
                    timeNow(), live, rs, vid, pollSeconds
            );

            int pad = Math.max(0, 120 - line.length());
            System.out.print("\r" + line + " ".repeat(pad));
            System.out.flush();
        }
    }


    private static void logEvent(String msg) {
        synchronized (CONSOLE_LOCK) {
            System.out.print("\r");

            System.out.println("[" + timeNow() + "] " + msg);
        }
    }


    private static String timeNow() {
        return LocalTime.now().format(T);
    }

    private static final Object CONSOLE_LOCK = new Object();

}
