package bot.discord;

import bot.BotConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.EnumSet;

public class JdaFactory {

    public static JDA build(BotConfig cfg, Object... listeners) {
        JDABuilder b = JDABuilder.createDefault(cfg.discordToken)
                .enableIntents(EnumSet.allOf(GatewayIntent.class))
                .enableCache(EnumSet.allOf(CacheFlag.class));

        for (Object l : listeners) b.addEventListeners(l);

        return b.build();
    }
}
