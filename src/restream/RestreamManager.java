package restream;

import util.Console;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

        String directUrl = getDirectUrlOrThrow(youtubeWatchUrl);

        String out = rtmpUrl.endsWith("/") ? (rtmpUrl + streamKey) : (rtmpUrl + "/" + streamKey);

        // Importante: ffmpeg suele loggear por stderr, pero tú lo rediriges a stdout (ok)
        List<String> cmd = List.of(
                "ffmpeg",
                "-hide_banner",
                "-loglevel", "warning",
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

        new Thread(() -> watchExit("ffmpeg", p), "ffmpeg-watch").start();

        Console.ok("Restream START -> " + out);
    }

    public synchronized void stopRestream() {
        if (!state.isRestreaming) return;

        Process p = state.currentProcess;

        state.currentProcess = null;
        state.isRestreaming = false;

        if (p == null) {
            Console.warn("Restream STOP (no process)");
            return;
        }

        try {
            if (p.isAlive()) {
                p.destroy();
                if (!p.waitFor(2, TimeUnit.SECONDS)) {
                    p.destroyForcibly();
                }
            }
        } catch (Exception ignored) {}

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
        }

        int code = p.waitFor();

        if (code != 0) {
            throw new RuntimeException("yt-dlp falló (exit=" + code + ")");
        }
        if (line == null || line.isBlank()) {
            throw new RuntimeException("yt-dlp no devolvió URL directa");
        }

        return line.trim();
    }

    private void watchExit(String name, Process p) {
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
                Console.err(name + " murió (exit=" + code + ")");
            }

        } catch (Exception e) {
            Console.err(name + " watcher error: " + e.getMessage());
            synchronized (this) {
                if (state.currentProcess == p) {
                    state.currentProcess = null;
                    state.isRestreaming = false;
                }
            }
        }
    }

    private void logProcess(String name, Process p) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println("[" + name + "] " + line);
            }
        } catch (Exception ignored) {}
    }
}
