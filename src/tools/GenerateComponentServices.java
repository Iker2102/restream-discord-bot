package tools;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Genera automáticamente:
 *   src/META-INF/services/bot.components.Component
 * Detecta componentes buscando .java bajo src/bot/components
 * que contengan: "implements Component"
 */
public class GenerateComponentServices {

    private static final Path SRC = Paths.get("src");
    private static final Path COMPONENTS_ROOT = SRC.resolve(Paths.get("bot", "components"));

    // IMPORTANTÍSIMO: el nombre del archivo debe ser el FQCN de la interfaz
    private static final Path SERVICES_FILE =
            SRC.resolve(Paths.get("META-INF", "services", "bot.components.Component"));

    private static final Pattern PACKAGE_RE = Pattern.compile("^\\s*package\\s+([a-zA-Z0-9_.]+)\\s*;\\s*$", Pattern.MULTILINE);
    private static final Pattern CLASS_RE   = Pattern.compile("\\bclass\\s+([A-Za-z0-9_]+)\\b");

    public static void main(String[] args) {
        try {
            generate();
        } catch (Exception e) {
            System.out.println("[GenerateComponentServices] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void generate() throws Exception {
        if (!Files.exists(COMPONENTS_ROOT)) {
            System.out.println("[GenerateComponentServices] No existe: " + COMPONENTS_ROOT.toAbsolutePath());
            return;
        }

        List<String> providers = new ArrayList<>();

        Files.walk(COMPONENTS_ROOT)
                .filter(p -> p.toString().endsWith(".java"))
                .forEach(p -> {
                    try {
                        String code = Files.readString(p, StandardCharsets.UTF_8);

                        if (!code.matches("(?s).*\\bimplements\\s+Component\\b.*")) return;

                        String pkg = match1(PACKAGE_RE, code);
                        String cls = match1(CLASS_RE, code);
                        if (pkg == null || cls == null) return;

                        if (code.contains("abstract class " + cls)) return;

                        providers.add(pkg + "." + cls);
                    } catch (IOException ignored) {}
                });

        Collections.sort(providers);

        Files.createDirectories(SERVICES_FILE.getParent());

        Files.writeString(
                SERVICES_FILE,
                String.join("\n", providers) + (providers.isEmpty() ? "" : "\n"),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );

        System.out.println("[GenerateComponentServices] Updated (" + providers.size() + "): " + SERVICES_FILE.toAbsolutePath());
        for (String p : providers) System.out.println("  - " + p);
    }

    private static String match1(Pattern p, String s) {
        Matcher m = p.matcher(s);
        return m.find() ? m.group(1) : null;
    }
}
