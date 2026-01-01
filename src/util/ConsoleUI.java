package util;

public class ConsoleUI {
    private static final String ESC = "\u001B[";
    private static boolean ansi = true;

    public static void banner(String title) {
        System.out.println("┌──────────────────────────────────────────────┐");
        System.out.printf ("│ %-44s │%n", title);
        System.out.println("└──────────────────────────────────────────────┘");
    }

    public static void renderStatus(boolean sourceLive, boolean restreaming, String videoId, String lastErr) {
        if (!ansi) {
            System.out.printf("Live=%s | Restream=%s | videoId=%s%n",
                    sourceLive ? "ON" : "OFF",
                    restreaming ? "ON" : "OFF",
                    videoId == null ? "-" : videoId
            );
            return;
        }

        System.out.print(ESC + "2J");
        System.out.print(ESC + "H");

        banner("Restream Bot");
        System.out.println();
        System.out.println("Estado");
        System.out.println("──────");
        System.out.printf("Fuente LIVE : %s%n", pill(sourceLive, "ON ", "OFF"));
        System.out.printf("Restream    : %s%n", pill(restreaming, "ON ", "OFF"));
        System.out.printf("Video ID    : %s%n", (videoId == null ? "-" : videoId));
        System.out.println();
        System.out.println("Último error");
        System.out.println("────────────");
        System.out.println(lastErr == null ? "-" : lastErr);
        System.out.println();
        System.out.println("Ctrl+C para salir");
    }

    private static String pill(boolean ok, String a, String b) {
        return ok ? (ESC + "32m" + a + ESC + "0m") : (ESC + "31m" + b + ESC + "0m");
    }
}
