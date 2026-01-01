package tools;

import util.Env;
import youtube.YouTubeLiveChecker;

public class YouTubeLiveTest {
    public static void main(String[] args) throws Exception {
        Env env = Env.load(".env");
        var checker = new YouTubeLiveChecker(
                env.require("YOUTUBE_API_KEY"),
                env.require("YOUTUBE_CHANNEL_ID")
        );

        var live = checker.checkLive();
        System.out.println("isLive=" + live.isLive());
        System.out.println("videoId=" + live.videoId());
        System.out.println("watchUrl=" + live.watchUrl());
    }
}
