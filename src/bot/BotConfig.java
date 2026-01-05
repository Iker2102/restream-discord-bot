package bot;

import util.Env;

public class BotConfig {
    public final String discordToken;

    public final boolean restreamEnabled;
    public final String ytApiKey;
    public final String sourceChannelId;
    public final String youtubeRtmpUrl;
    public final String youtubeStreamKey;
    public final Long notifyDiscordUserId;
    public final Integer pollSeconds;

    private BotConfig(
            String discordToken,
            boolean restreamEnabled,
            String ytApiKey,
            String sourceChannelId,
            String youtubeRtmpUrl,
            String youtubeStreamKey,
            Long notifyDiscordUserId,
            Integer pollSeconds
    ) {
        this.discordToken = discordToken;
        this.restreamEnabled = restreamEnabled;
        this.ytApiKey = ytApiKey;
        this.sourceChannelId = sourceChannelId;
        this.youtubeRtmpUrl = youtubeRtmpUrl;
        this.youtubeStreamKey = youtubeStreamKey;
        this.notifyDiscordUserId = notifyDiscordUserId;
        this.pollSeconds = pollSeconds;
    }

    public static BotConfig from(Env env) {
        String token = env.require("DISCORD_TOKEN");

        boolean enabled = parseBool(opt(env, "RESTREAM_ENABLED"), false);

        String ytApiKey   = opt(env, "YOUTUBE_API_KEY");
        String channelId  = opt(env, "YOUTUBE_CHANNEL_ID");
        String rtmpUrl    = opt(env, "YOUTUBE_RTMP_URL");
        String streamKey  = opt(env, "YOUTUBE_STREAM_KEY");

        Long notifyId = parseLongOrNull(opt(env, "NOTIFY_DISCORD_USER_ID"));
        Integer poll  = parseIntOrDefault(opt(env, "POLL_SECONDS"), 10);

        if (enabled) {
            boolean ok = ytApiKey != null && channelId != null && rtmpUrl != null && streamKey != null && notifyId != null;
            if (!ok) enabled = false;
        }

        return new BotConfig(token, enabled, ytApiKey, channelId, rtmpUrl, streamKey, notifyId, poll);
    }

    // ---- helpers ----
    private static String opt(Env env, String key) {
        try {
            String v = env.get(key);
            if (v == null) return null;
            v = v.trim();
            return v.isEmpty() ? null : v;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static boolean parseBool(String v, boolean def) {
        if (v == null) return def;
        v = v.trim();
        return v.equalsIgnoreCase("true") || v.equals("1") || v.equalsIgnoreCase("yes") || v.equalsIgnoreCase("y");
    }

    private static Long parseLongOrNull(String v) {
        if (v == null) return null;
        try {
            return Long.parseLong(v.toString().trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Integer parseIntOrDefault(String v, int def) {
        if (v == null) return def;
        try {
            return Integer.parseInt(v.toString().trim());
        } catch (Exception ignored) {
            return def;
        }
    }
}
