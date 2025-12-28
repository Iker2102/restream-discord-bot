package restream;

public class RestreamState {
    public volatile boolean isSourceLive = false;
    public volatile boolean isRestreaming = false;
    public volatile String lastVideoId = null;

    public volatile Process currentProcess = null;

    public volatile String lastError;
}
