package bot;

import bot.discord.JdaFactory;
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

        JDA jda = JdaFactory.build(cfg);

        jda.awaitReady();
        Console.ok("JDA listo y conectado");

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
