package tools;

import java.nio.file.*;
import java.util.*;

import static java.nio.file.StandardWatchEventKinds.*;

public class CommandServicesWatcher {

    private final Path root;
    private WatchService watchService;

    private volatile long lastGenMs = 0;

    public CommandServicesWatcher(Path commandsRoot) {
        this.root = commandsRoot;
    }

    public void startInBackground() throws Exception {
        watchService = FileSystems.getDefault().newWatchService();

        registerAllDirs(root);

        Thread t = new Thread(this::loop, "CommandServicesWatcher");
        t.setDaemon(true);
        t.start();

        System.out.println("[Watcher] Watching: " + root.toAbsolutePath());
    }

    private void loop() {
        while (true) {
            try {
                WatchKey key = watchService.take();
                Path dir = (Path) key.watchable();

                boolean relevant = false;

                for (WatchEvent<?> ev : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = ev.kind();
                    if (kind == OVERFLOW) continue;

                    Path changed = dir.resolve((Path) ev.context());

                    if (kind == ENTRY_CREATE) {
                        try {
                            if (Files.isDirectory(changed)) registerAllDirs(changed);
                        } catch (Exception ignored) {}
                    }

                    if (changed.toString().endsWith(".java")) relevant = true;
                }

                key.reset();

                if (relevant) {
                    debounceGenerate();
                }
            } catch (Exception e) {
                System.out.println("[Watcher] ERROR: " + e.getMessage());
            }
        }
    }

    private void debounceGenerate() {
        long now = System.currentTimeMillis();
        if (now - lastGenMs < 500) return;
        lastGenMs = now;

        try {
            GenerateCommandServices.generate();
        } catch (Exception e) {
            System.out.println("[Watcher] Generate ERROR: " + e.getMessage());
        }
    }

    private void registerAllDirs(Path start) throws Exception {
        Files.walk(start)
                .filter(Files::isDirectory)
                .forEach(d -> {
                    try {
                        d.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                    } catch (Exception ignored) {}
                });
    }
}
