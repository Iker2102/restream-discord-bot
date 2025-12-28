package util;

public class Console {
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String CYAN = "\u001B[36m";
    public static final String GRAY = "\u001B[90m";

    public static void ok(String msg) { System.out.println(GREEN + "✔ " + msg + RESET); }
    public static void info(String msg) { System.out.println(CYAN + "ℹ " + msg + RESET); }
    public static void warn(String msg) { System.out.println(YELLOW + "⚠ " + msg + RESET); }
    public static void err(String msg) { System.out.println(RED + "✖ " + msg + RESET); }
    public static void fail(String msg) {System.out.println(RED + "✖ " + msg + RESET); }

    public static void section(String title) {
        System.out.println(GRAY + "──────────── " + title + " ────────────" + RESET);
    }
}
