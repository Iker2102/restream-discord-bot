package tools;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class BinarySmokeTest {

    public static void main(String[] args) throws Exception {
        testVersion("yt-dlp", List.of("yt-dlp", "--version"));
        testVersion("ffmpeg", List.of("ffmpeg", "-version"));

        String url = "https://www.youtube.com/watch?v=hRJpiCZlLX8";
        testYtDlpGetUrl(url);
    }

    private static void testVersion(String name, List<String> cmd) throws Exception {
        System.out.println("\n== " + name + " ==");
        int code = run(cmd);
        System.out.println("Exit code: " + code);
    }

    private static void testYtDlpGetUrl(String watchUrl) throws Exception {
        System.out.println("\n== yt-dlp -g ==");
        int code = run(List.of("yt-dlp", "-g", watchUrl));
        System.out.println("Exit code: " + code);
    }

    private static int run(List<String> cmd) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process p = pb.start();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        }
        return p.waitFor();
    }
}
