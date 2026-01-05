package bot.components;

import bot.core.BotContext;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.*;

public class ComponentHandler extends ListenerAdapter {

    private final BotContext ctx;
    private final Map<String, Component> byPrefix = new LinkedHashMap<>();

    public ComponentHandler(BotContext ctx) {
        this.ctx = ctx;
    }

    public int loadFromServiceLoader() {
        ServiceLoader<Component> loader = ServiceLoader.load(Component.class);
        int count = 0;

        byPrefix.clear();

        for (Component c : loader) {
            String p = c.prefix();
            if (p == null || p.isBlank())
                throw new IllegalStateException("Component sin prefix: " + c.getClass().getName());

            if (byPrefix.containsKey(p))
                throw new IllegalStateException("Prefix duplicado: " + p + " (" + c.getClass().getName() + ")");

            byPrefix.put(p, c);
            count++;
        }

        System.out.println("[ComponentHandler] Componentes cargados: " + count);
        return count;
    }

    public int reload() {
        return loadFromServiceLoader();
    }


    @Override
    public void onGenericComponentInteractionCreate(GenericComponentInteractionCreateEvent event) {
        String id = event.getComponentId();

        Component best = null;
        int bestLen = -1;

        for (var e : byPrefix.entrySet()) {
            String p = e.getKey();
            if (id.startsWith(p) && p.length() > bestLen) {
                best = e.getValue();
                bestLen = p.length();
            }
        }

        if (best == null) return;

        try {
            best.handle(event, ctx);
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                event.reply("⚠️ Error: " + ex.getMessage()).setEphemeral(true).queue();
            } catch (Exception ignored) {}
        }
    }
}
