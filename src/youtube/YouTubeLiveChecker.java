package youtube;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class YouTubeLiveChecker {
    private final String apiKey;
    private final String channelId;
    private final HttpClient http = HttpClient.newHttpClient();

    public YouTubeLiveChecker(String apiKey, String channelId) {
        this.apiKey = apiKey;
        this.channelId = channelId;
    }

    public LiveStatus checkLive() throws Exception {
        // Busca eventos live del canal
        String url = "https://www.googleapis.com/youtube/v3/search"
                + "?part=snippet"
                + "&channelId=" + channelId
                + "&eventType=live"
                + "&type=video"
                + "&maxResults=1"
                + "&key=" + apiKey;

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

        JsonObject root = JsonParser.parseString(res.body()).getAsJsonObject();
        JsonArray items = root.getAsJsonArray("items");

        if (items == null || items.isEmpty()) {
            return new LiveStatus(false, null, null);
        }

        JsonObject first = items.get(0).getAsJsonObject();
        String videoId = first.getAsJsonObject("id").get("videoId").getAsString();
        String watchUrl = "https://www.youtube.com/watch?v=" + videoId;

        return new LiveStatus(true, videoId, watchUrl);
    }
}
