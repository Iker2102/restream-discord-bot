package restream;

import util.Console;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RestreamManager {

    private final String rtmpUrl;
    private final String streamKey;
    private final RestreamState state;

    private final Deque<String> lastLines = new ArrayDeque<>(80);

    public RestreamManager(String rtmpUrl, String streamKey, RestreamState state) {
        this.rtmpUrl = rtmpUrl;
        this.streamKey = streamKey;
        this.state = state;
    }

    public synchronized void startRestream(String youtubeWatchUrl) throws Exception {
        if (state.isRestreaming) return;

        String directUrl = getDirectUrlOrThrow(youtubeWatchUrl);

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

        new Thread(() -> captureLogs("ffmpeg", p), "ffmpeg-log").start();

        new Thread(() -> waitAndHandleExit("ffmpeg", p), "ffmpeg-exit").start();

        Console.ok("Restream START → " + out);
    }

    public synchronized void stopRestream() {
        if (!state.isRestreaming) return;

        try {
            Process p = state.currentProcess;
            if (p != null && p.isAlive()) {
                p.destroy();
                if (!p.waitFor(3, TimeUnit.SECONDS)) {
                    p.destroyForcibly();
                }
            }
        } catch (Exception ignored) {}

        state.currentProcess = null;
        state.isRestreaming = false;
        Console.warn("Restream STOP");
    }


    private String getDirectUrlOrThrow(String watchUrl) throws Exception {
        List<String> cmd = List.of("yt-dlp", "-g", watchUrl);
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);

        Process p = pb.start();

        String line;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            line = br.readLine();
            String extra;
            while ((extra = br.readLine()) != null) {
                pushLine("[yt-dlp] " + extra);
            }
        }

        int code = p.waitFor();
        if (code != 0) {
            throw new RuntimeException("yt-dlp falló (exit=" + code + "). Últimas líneas: " + lastLinesSummary());
        }

        if (line == null || line.isBlank()) {
            throw new RuntimeException("yt-dlp no devolvió URL directa. Últimas líneas: " + lastLinesSummary());
        }

        return line.trim();
    }

    private void captureLogs(String name, Process p) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                pushLine("[" + name + "] " + line);

                if (line.toLowerCase().contains("error") || line.toLowerCase().contains("failed")) {
                    Console.err("[ffmpeg] " + line);
                }
            }
        } catch (Exception ignored) {}
    }

    private void waitAndHandleExit(String name, Process p) {
        try {
            int code = p.waitFor();

            synchronized (this) {
                if (state.currentProcess == p) {
                    state.currentProcess = null;
                    state.isRestreaming = false;
                }
            }

            if (code == 0) {
                Console.warn(name + " terminó (exit=0)");
            } else {
                Console.err(name + " se cayó (exit=" + code + "). Últimas líneas: " + lastLinesSummary());
            }

        } catch (Exception ignored) {}
    }

    private void pushLine(String s) {
        synchronized (lastLines) {
            if (lastLines.size() >= 80) lastLines.removeFirst();
            lastLines.addLast(s);
        }
    }

    private String lastLinesSummary() {
        synchronized (lastLines) {
            if (lastLines.isEmpty()) return "(sin logs)";
            int count = 0;
            StringBuilder sb = new StringBuilder();
            for (String s : lastLines) {
                if (++count > 10) break;
                sb.append(" | ").append(s);
            }
            return sb.toString();
        }
    }
}
