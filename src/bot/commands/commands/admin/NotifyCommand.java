package bot.commands.commands.admin;

import bot.commands.Command;
import bot.commands.CommandPermission;
import bot.core.BotContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import static net.dv8tion.jda.api.interactions.commands.build.Commands.slash;

public class NotifyCommand implements Command {

    @Override public String name() { return "notify"; }
    @Override public String description() { return "Configura avisos por MD del restream"; }
    @Override public String category() { return "admin"; }

    @Override
    public CommandPermission permission() {
        return CommandPermission.ownerOnly();
    }

    @Override
    public CommandData data() {
        return slash(name(), description())
                .addSubcommands(
                        new SubcommandData("on", "Activa los avisos por MD"),
                        new SubcommandData("off", "Desactiva los avisos por MD"),
                        new SubcommandData("status", "Muestra el estado actual")
                );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, BotContext ctx) {
        String sub = event.getSubcommandName();

        if (sub == null) {
            event.reply("⚠️ Usa `/notify on`, `/notify off` o `/notify status`.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        switch (sub) {
            case "on" -> {
                ctx.settings().setBool("restream.notify.enabled", true);
                event.reply("✅ Notificaciones del restream: **ON**").setEphemeral(true).queue();
            }
            case "off" -> {
                ctx.settings().setBool("restream.notify.enabled", false);
                event.reply("✅ Notificaciones del restream: **OFF**").setEphemeral(true).queue();
            }
            case "status" -> {
                boolean enabled = ctx.settings().getBool("restream.notify.enabled", true);
                event.reply("ℹ️ Notificaciones del restream: **" + (enabled ? "ON" : "OFF") + "**")
                        .setEphemeral(true)
                        .queue();
            }
            default -> event.reply("❌ Subcomando desconocido.").setEphemeral(true).queue();
        }
    }
}
