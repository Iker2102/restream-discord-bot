package tools;

import java.nio.file.*;
import java.util.concurrent.atomic.AtomicLong;

import static java.nio.file.StandardWatchEventKinds.*;

public class ComponentServicesWatcher {

    private final Path root;
    private volatile boolean running = true;

    public ComponentServicesWatcher(Path root) {
        this.root = root;
    }

    public void startInBackground() {
        Thread t = new Thread(this::runLoop, "ComponentServicesWatcher");
        t.setDaemon(true);
        t.start();
    }

    private void runLoop() {
        try {
            if (!Files.exists(root)) {
                System.out.println("[Watcher] No existe: " + root.toAbsolutePath());
                return;
            }

            System.out.println("[Watcher] Watching: " + root.toAbsolutePath());

            WatchService ws = FileSystems.getDefault().newWatchService();
            registerAll(ws, root);

            AtomicLong lastTrigger = new AtomicLong(0);

            while (running) {
                WatchKey key = ws.take();

                boolean changed = false;

                for (WatchEvent<?> ev : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = ev.kind();
                    if (kind == OVERFLOW) continue;

                    Path dir = (Path) key.watchable();
                    Path full = dir.resolve((Path) ev.context());

                    if (kind == ENTRY_CREATE && Files.isDirectory(full)) {
                        registerAll(ws, full);
                    }

                    if (full.toString().endsWith(".java")) {
                        changed = true;
                    }
                }

                boolean valid = key.reset();
                if (!valid) break;

                if (changed) {
                    long now = System.currentTimeMillis();

                    if (now - lastTrigger.get() < 250) continue;
                    lastTrigger.set(now);

                    try {
                        GenerateComponentServices.generate();
                    } catch (Exception e) {
                        System.out.println("[Watcher] ERROR generando services: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[Watcher] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void registerAll(WatchService ws, Path start) throws Exception {
        Files.walk(start)
                .filter(Files::isDirectory)
                .forEach(dir -> {
                    try {
                        dir.register(ws, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
                    } catch (Exception ignored) {}
                });
    }

    public void stop() {
        running = false;
    }
}
