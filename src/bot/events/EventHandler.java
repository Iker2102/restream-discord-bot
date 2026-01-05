package bot.events;

import net.dv8tion.jda.api.JDA;

import java.util.ArrayList;
import java.util.List;

public class EventHandler {
    private final List<Object> listeners = new ArrayList<>();

    public void register(Object listener) {
        listeners.add(listener);
    }

    public void bindTo(JDA jda) {
        for (Object l : listeners) {
            jda.addEventListener(l);
        }
    }
}
