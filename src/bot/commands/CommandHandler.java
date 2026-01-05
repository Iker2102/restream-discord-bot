package bot.commands;

import bot.core.BotContext;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.*;

import static net.dv8tion.jda.api.interactions.commands.build.Commands.slash;

public class CommandHandler extends ListenerAdapter {

    private final BotContext ctx;
    private final Map<String, Command> commands = new HashMap<>();

    public CommandHandler(BotContext ctx) {
        this.ctx = ctx;
    }

    public void loadFromServiceLoader() {
        ServiceLoader<Command> loader = ServiceLoader.load(Command.class);
        int count = 0;

        for (Command cmd : loader) {
            register(cmd);
            count++;
        }

        if (count == 0) {
            System.out.println("[CommandHandler] No se ha cargado ningún comando (ServiceLoader vacío).");
        } else {
            System.out.println("[CommandHandler] Comandos cargados: " + count);
        }
    }

    public void register(Command cmd) {
        String key = cmd.name().toLowerCase(Locale.ROOT);
        if (commands.containsKey(key)) {
            throw new IllegalStateException("Comando duplicado: " + key + " (" + cmd.getClass().getName() + ")");
        }
        commands.put(key, cmd);
    }

    public Map<String, Command> all() {
        return Collections.unmodifiableMap(commands);
    }

    public Map<String, List<Command>> byCategory() {
        Map<String, List<Command>> map = new TreeMap<>();
        for (Command c : commands.values()) {
            String cat = resolveCategory(c);
            map.computeIfAbsent(cat, k -> new ArrayList<>()).add(c);
        }
        map.values().forEach(list -> list.sort(Comparator.comparing(Command::name)));
        return map;
    }

    private String resolveCategory(Command c) {
        if (c.category() != null && !c.category().isBlank()) return c.category();

        String pkg = c.getClass().getPackageName();
        String base = "bot.commands.commands";
        if (!pkg.startsWith(base)) return "misc";

        String rest = pkg.substring(base.length());
        if (rest.isBlank()) return "misc";

        return rest.substring(1).replace('.', '/');
    }


    public void upsertSlashCommands(JDA jda, String guildIdOrNull) {
        var update = (guildIdOrNull != null && !guildIdOrNull.isBlank())
                ? jda.getGuildById(guildIdOrNull).updateCommands()
                : jda.updateCommands();

        update.addCommands(
                net.dv8tion.jda.api.interactions.commands.build.Commands
                        .slash("reload", "Recarga el sistema de comandos")
                        .addSubcommands(
                                new net.dv8tion.jda.api.interactions.commands.build.SubcommandData(
                                        "soft", "Recarga comandos sin limpiar Discord"
                                ),
                                new net.dv8tion.jda.api.interactions.commands.build.SubcommandData(
                                        "hard", "Limpia y vuelve a subir los slash commands"
                                )
                        )
        );

        for (Command cmd : commands.values()) {
            if (cmd.name().equals("reload")) continue;
            update.addCommands(
                    net.dv8tion.jda.api.interactions.commands.build.Commands
                            .slash(cmd.name(), cmd.description())
            );
        }

        update.queue();
    }


    public synchronized int reloadFromServiceLoader() {
        commands.clear();

        ServiceLoader<Command> loader = ServiceLoader.load(Command.class, Command.class.getClassLoader());
        loader.reload();

        int count = 0;
        for (Command cmd : loader) {
            register(cmd);
            count++;
        }
        System.out.println("[CommandHandler] Reload -> " + count + " comandos");
        return count;
    }


    public synchronized int reloadAndUpsert(JDA jda, String guildIdOrNull) {
        int count = reloadFromServiceLoader();
        upsertSlashCommands(jda, guildIdOrNull);
        return count;
    }

    public synchronized int reloadFromServiceLoaderHard() {
        commands.clear();

        ServiceLoader<Command> loader = ServiceLoader.load(Command.class, Command.class.getClassLoader());
        loader.reload();

        int count = 0;
        for (Command cmd : loader) {
            register(cmd);
            count++;
        }
        return count;
    }

    public void clearSlashCommands(JDA jda, String guildIdOrNull) {
        var update = (guildIdOrNull != null && !guildIdOrNull.isBlank())
                ? Objects.requireNonNull(jda.getGuildById(guildIdOrNull), "Guild no encontrada: " + guildIdOrNull).updateCommands()
                : jda.updateCommands();

        update.queue();
    }

    /**
     * Hard reload:
     * - limpia comandos en Discord
     * - recarga comandos del ServiceLoader
     * - los vuelve a subir
     */
    public synchronized int hardReloadAndUpsert(JDA jda, String guildIdOrNull) throws InterruptedException {

        clearSlashCommands(jda, guildIdOrNull);

        Thread.sleep(600);

        int count = reloadFromServiceLoaderHard();

        upsertSlashCommands(jda, guildIdOrNull);

        return count;
    }



    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        Command cmd = commands.get(event.getName().toLowerCase(Locale.ROOT));
        if (cmd == null) {
            event.reply("❌ Comando no registrado.").setEphemeral(true).queue();
            return;
        }

        try {
            cmd.execute(event, ctx);
        } catch (Exception e) {
            event.reply("⚠️ Error: " + e.getMessage()).setEphemeral(true).queue();
            e.printStackTrace();
        }





    }
}
