package bot;

import bot.commands.CommandHandler;
import bot.commands.help.HelpUiListener;
import bot.components.ComponentHandler;
import bot.core.BotContext;
import bot.discord.JdaFactory;
import bot.events.EventHandler;
import bot.modules.restream.RestreamModule;
import net.dv8tion.jda.api.JDA;
import util.Console;
import util.Env;

public class Main {

    public static void main(String[] args) throws Exception {

        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "warn");
        System.setProperty("org.slf4j.simpleLogger.log.net.dv8tion", "warn");
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "false");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");

        Env env = Env.load(".env");
        BotConfig cfg = BotConfig.from(env);

        Console.section("Bot");
        Console.ok("DISCORD_TOKEN cargado");

        // DEV: auto-regenera el services al guardar comandos
        String dev = env.get("DEV_MODE");

        if ("true".equalsIgnoreCase(dev) || "1".equals(dev)) {
            try {
                java.nio.file.Path commandsRoot = java.nio.file.Paths.get("src", "bot", "commands");
                new tools.CommandServicesWatcher(commandsRoot).startInBackground();

                var componentsRoot = java.nio.file.Paths.get("src", "bot", "components");
                new tools.ComponentServicesWatcher(componentsRoot).startInBackground();
            } catch (Exception e) {
                System.out.println("[Main] Watcher no pudo arrancar: " + e.getMessage());
                System.out.println("[Main] Components watcher no pudo arrancar: " + e.getMessage());
            }
        }



        JDA jda = JdaFactory.build(cfg);

        jda.awaitReady();
        Console.ok("JDA listo y conectado");

        String devGuildId = env.get("DEV_GUILD_ID");
        String ownerId = env.require("OWNER_ID");

        BotContext ctx = new BotContext(jda, devGuildId, ownerId);

        CommandHandler commandHandler = new CommandHandler(ctx);
        ctx.setCommandHandler(commandHandler);

        commandHandler.loadFromServiceLoader();

        EventHandler events = new EventHandler();
        events.register(commandHandler);

        ComponentHandler componentHandler = new ComponentHandler(ctx);
        componentHandler.loadFromServiceLoader();

        events.register(componentHandler);

        events.bindTo(jda);

        commandHandler.upsertSlashCommands(jda, devGuildId);
        /*
        Cambiar el env.get a null cuando quiera hacer global
         */


        RestreamModule restreamModule = null;

        if (cfg.restreamEnabled) {
            Console.info("Restream: ENABLED");
            restreamModule = new RestreamModule(cfg);
            restreamModule.start(jda);
        } else {
            Console.info("Restream: DISABLED");
        }

        RestreamModule finalRestreamModule = restreamModule;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Console.warn("Cerrando...");
                try { if (finalRestreamModule != null) finalRestreamModule.stop(); } catch (Exception ignored) {}
                try { jda.shutdownNow(); } catch (Exception ignored) {}
                Console.ok("Apagado completo");
            } catch (Exception ignored) {}
        }));
    }
}
