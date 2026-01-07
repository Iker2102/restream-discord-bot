package bot.core;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class SettingsStore {

    private final Path file;
    private final Properties props = new Properties();

    public SettingsStore(Path file) {
        this.file = file;
        load();
    }

    private void load() {
        try {
            if (Files.exists(file)) {
                try (InputStream in = Files.newInputStream(file)) {
                    props.load(in);
                }
            } else {
                Files.createDirectories(file.getParent());
                save();
            }
        } catch (Exception e) {
            System.out.println("[SettingsStore] No se pudo cargar: " + e.getMessage());
        }
    }

    public synchronized void save() {
        try {
            Files.createDirectories(file.getParent());
            try (OutputStream out = Files.newOutputStream(file)) {
                props.store(out, "Bot settings");
            }
        } catch (Exception e) {
            System.out.println("[SettingsStore] No se pudo guardar: " + e.getMessage());
        }
    }

    public synchronized boolean getBool(String key, boolean def) {
        String v = props.getProperty(key);
        if (v == null) return def;
        return v.equalsIgnoreCase("true") || v.equals("1");
    }

    public synchronized void setBool(String key, boolean value) {
        props.setProperty(key, value ? "true" : "false");
        save();
    }
}
