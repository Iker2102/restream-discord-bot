package bot;

import bot.commands.CommandHandler;
import bot.components.ComponentHandler;
import bot.core.BotContext;
import bot.discord.JdaFactory;
import bot.events.EventHandler;
import bot.modules.restream.RestreamModule;
import net.dv8tion.jda.api.JDA;
import util.Console;
import util.Env;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) throws Exception {

        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "warn");
        System.setProperty("org.slf4j.simpleLogger.log.net.dv8tion", "warn");
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "false");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");

        Console.section("Boot");

        Env env = Env.load(".env");
        BotConfig cfg = BotConfig.from(env);

        String devMode = env.get("DEV_MODE");
        boolean isDev = "true".equalsIgnoreCase(devMode) || "1".equals(devMode);

        String devGuildId = env.get("DEV_GUILD_ID");
        String ownerId = env.require("OWNER_ID");

        Console.ok("Config cargada");
        Console.info("Modo: " + (isDev ? "DEV" : "PROD") + (isDev ? " (guild)" : " (global)"));
        if (isDev) {
            Console.info("DEV_GUILD_ID: " + (devGuildId == null || devGuildId.isBlank() ? "(no definido)" : devGuildId));
        }

        if (isDev) {
            Console.section("Watchers");
            startWatchers();
        }

        Console.section("Discord");
        JDA jda = JdaFactory.build(cfg);

        jda.awaitReady();
        Console.ok("JDA conectado: " + jda.getSelfUser().getName());

        Console.section("Handlers");

        BotContext ctx = new BotContext(jda, devGuildId, ownerId);

        CommandHandler commandHandler = new CommandHandler(ctx);
        ctx.setCommandHandler(commandHandler);

        ComponentHandler componentHandler = new ComponentHandler(ctx);
        ctx.setComponentHandler(componentHandler);

        int cmdCount = commandHandler.loadFromServiceLoader();
        int compCount = componentHandler.loadFromServiceLoader();

        EventHandler events = new EventHandler();
        events.register(commandHandler);
        events.register(componentHandler);
        events.bindTo(jda);

        Console.ok("Comandos cargados: " + cmdCount);
        Console.ok("Componentes cargados: " + compCount);

        // 5) Register slash commands
        Console.section("Slash Commands");
        String target = (isDev ? devGuildId : null);

        if (isDev && (devGuildId == null || devGuildId.isBlank())) {
            Console.warn("DEV_MODE activo pero DEV_GUILD_ID no definido. Registrando GLOBAL (puede tardar).");
            target = null;
        }

        commandHandler.upsertSlashCommands(jda, target);
        Console.ok("Upsert enviado a " + (target == null ? "GLOBAL" : "GUILD"));

        Console.section("Modules");

        RestreamModule restreamModule = null;
        if (cfg.restreamEnabled) {
            Console.ok("Restream: ENABLED");
            restreamModule = new RestreamModule(cfg);
            restreamModule.start(jda);
        } else {
            Console.info("Restream: DISABLED");
        }

        // 7) Summary
        Console.section("Ready");
        Console.ok("Bot listo. Usa /help");

        RestreamModule finalRestreamModule = restreamModule;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Console.section("Shutdown");
                Console.warn("Cerrando...");
                try { if (finalRestreamModule != null) finalRestreamModule.stop(); } catch (Exception ignored) {}
                try { jda.shutdownNow(); } catch (Exception ignored) {}
                Console.ok("Apagado completo");
            } catch (Exception ignored) {}
        }));
    }

    private static void startWatchers() {
        try {
            Path commandsRoot = Paths.get("src", "bot", "commands");
            new tools.CommandServicesWatcher(commandsRoot).startInBackground();
            Console.ok("Command watcher: ON");
        } catch (Exception e) {
            Console.warn("Command watcher: OFF (" + e.getMessage() + ")");
        }

        try {
            Path componentsRoot = Paths.get("src", "bot", "components");
            new tools.ComponentServicesWatcher(componentsRoot).startInBackground();
            Console.ok("Component watcher: ON");
        } catch (Exception e) {
            Console.warn("Component watcher: OFF (" + e.getMessage() + ")");
        }
    }
}
