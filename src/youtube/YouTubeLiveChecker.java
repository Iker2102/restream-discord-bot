package youtube;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class YouTubeLiveChecker {

    private final String apiKey;
    private final String channelId;

    public YouTubeLiveChecker(String apiKey, String channelId) {
        this.apiKey = apiKey;
        this.channelId = channelId;
    }

    public LiveStatus checkLive() throws Exception {

        String endpoint =
                "https://www.googleapis.com/youtube/v3/search"
                        + "?part=snippet"
                        + "&channelId=" + channelId
                        + "&eventType=live"
                        + "&type=video"
                        + "&maxResults=1"
                        + "&key=" + apiKey;

        HttpURLConnection con = (HttpURLConnection) new URL(endpoint).openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(10_000);
        con.setReadTimeout(10_000);

        int code = con.getResponseCode();
        if (code != 200) {
            throw new RuntimeException("YouTube API HTTP " + code);
        }

        JsonObject root = JsonParser.parseReader(
                new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)
        ).getAsJsonObject();

        JsonArray items = root.getAsJsonArray("items");
        if (items == null || items.isEmpty()) {
            return new LiveStatus(false, null, null);
        }

        JsonObject first = items.get(0).getAsJsonObject();
        JsonObject id = first.getAsJsonObject("id");
        String videoId = id.get("videoId").getAsString();

        String watchUrl = "https://www.youtube.com/watch?v=" + videoId;
        return new LiveStatus(true, videoId, watchUrl);
    }
}
