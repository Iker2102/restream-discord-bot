package restream;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class RestreamManager {
    private final String rtmpUrl;
    private final String streamKey;
    private final RestreamState state;

    public RestreamManager(String rtmpUrl, String streamKey, RestreamState state) {
        this.rtmpUrl = rtmpUrl;
        this.streamKey = streamKey;
        this.state = state;
    }

    public synchronized void startRestream(String youtubeWatchUrl) throws Exception {
        if (state.isRestreaming) return;

        String directUrl = getDirectUrl(youtubeWatchUrl);
        if (directUrl == null || directUrl.isBlank()) {
            System.out.println("No se pudo obtener URL directa con yt-dlp");
            return;
        }

        String out = rtmpUrl.endsWith("/") ? (rtmpUrl + streamKey) : (rtmpUrl + "/" + streamKey);

        List<String> cmd = List.of(
                "ffmpeg",
                "-re",
                "-i", directUrl,
                "-c", "copy",
                "-f", "flv",
                out
        );

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);

        Process p = pb.start();
        state.currentProcess = p;
        state.isRestreaming = true;

        new Thread(() -> logProcess("ffmpeg", p), "ffmpeg-log").start();
        System.out.println("Restream START -> " + out);
    }

    public synchronized void stopRestream() {
        if (!state.isRestreaming) return;

        try {
            if (state.currentProcess != null && state.currentProcess.isAlive()) {
                state.currentProcess.destroy();
            }
        } catch (Exception ignored) {}

        state.currentProcess = null;
        state.isRestreaming = false;
        System.out.println("Restream STOP");
    }

    private String getDirectUrl(String watchUrl) throws Exception {
        List<String> cmd = List.of("yt-dlp", "-g", watchUrl);
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);

        Process p = pb.start();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line = br.readLine();
            p.waitFor();
            return line;
        }
    }

    private void logProcess(String name, Process p) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println("[" + name + "] " + line);
            }
        } catch (Exception ignored) {}
    }
}
