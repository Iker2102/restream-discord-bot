package bot.commands.commands.admin;

import bot.commands.Command;
import bot.commands.CommandPermission;
import bot.core.BotContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import tools.GenerateCommandServices;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import static net.dv8tion.jda.api.interactions.commands.build.Commands.slash;

public class ReloadCommand implements Command {

    public ReloadCommand() {}

    @Override
    public String name() {
        return "reload";
    }

    @Override
    public String description() {
        return "Recarga el sistema de comandos";
    }

    @Override
    public String category() {
        return "admin";
    }

    @Override
    public CommandPermission permission() {
        return CommandPermission.ownerOnly();
    }

    @Override
    public CommandData data() {
        return slash(name(), description())
                .addSubcommands(
                        new SubcommandData("soft", "Recarga comandos sin limpiar Discord"),
                        new SubcommandData("hard", "Limpia y vuelve a subir los slash commands")
                );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, BotContext ctx) {


        String mode = event.getSubcommandName();

        if (mode == null) {
            event.reply("⚠️ Usa `/reload soft` o `/reload hard`.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        event.deferReply(true).queue();

        try {
            GenerateCommandServices.generate();

            String guildId = ctx.devGuildId();

            if (mode.equals("soft")) {
                int count = ctx.commands().reloadAndUpsert(ctx.jda(), guildId);

                event.getHook().sendMessage(
                        "🔄 **Reload soft completado**\n" +
                                "📦 Comandos recargados: **" + count + "**"
                ).queue();
                return;
            }

            if (mode.equals("hard")) {
                int count = ctx.commands().hardReloadAndUpsert(ctx.jda(), guildId);

                ctx.jda().getGuildById(guildId).retrieveCommands().queue(cmds -> {
                    event.getHook().sendMessage(
                            "💥 **Reload HARD completado**\n" +
                                    "📦 Local: **" + count + "**\n" +
                                    "☁️ Discord: **" + cmds.size() + "**"
                    ).queue();
                });
                return;
            }

            event.getHook().sendMessage("❌ Subcomando desconocido.").queue();

        } catch (Exception e) {
            e.printStackTrace();
            event.getHook().sendMessage("⚠️ Error recargando: " + e.getMessage()).queue();
        }
    }
}
