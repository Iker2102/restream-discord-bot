package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Env {
    private final Map<String, String> values;

    private Env(Map<String, String> values) {
        this.values = values;
    }

    public static Env load(String filePath) throws IOException {
        Path p = Path.of(filePath);
        if (!Files.exists(p)) {
            throw new IOException("No se encuentra el .env en: " + p.toAbsolutePath());
        }

        Map<String, String> map = new HashMap<>();
        for (String line : Files.readAllLines(p)) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            int eq = line.indexOf('=');
            if (eq <= 0) continue;

            String key = line.substring(0, eq).trim();
            String value = line.substring(eq + 1).trim();

            if ((value.startsWith("\"") && value.endsWith("\"")) ||
                    (value.startsWith("'") && value.endsWith("'"))) {
                value = value.substring(1, value.length() - 1);
            }

            map.put(key, value);
        }
        return new Env(map);
    }

    public String get(String key) {
        String sys = System.getenv(key);
        return (sys != null && !sys.isBlank()) ? sys : values.get(key);
    }

    public String require(String key) {
        String v = get(key);
        if (v == null || v.isBlank()) {
            throw new IllegalStateException("Falta variable: " + key);
        }
        return v;
    }
}
