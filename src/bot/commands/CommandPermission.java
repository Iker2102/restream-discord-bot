package bot.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

import java.util.EnumSet;
import java.util.Set;

public final class CommandPermission {

    public enum Type {
        EVERYONE,
        OWNER_ONLY,
        DISCORD_PERMISSIONS
    }

    private final Type type;
    private final Set<Permission> discordPermissions;

    private CommandPermission(Type type, Set<Permission> perms) {
        this.type = type;
        this.discordPermissions = perms;
    }

    public static CommandPermission everyone() {
        return new CommandPermission(Type.EVERYONE, Set.of());
    }

    public static CommandPermission ownerOnly() {
        return new CommandPermission(Type.OWNER_ONLY, Set.of());
    }

    public static CommandPermission discord(Permission... perms) {
        return new CommandPermission(
                Type.DISCORD_PERMISSIONS,
                EnumSet.of(perms[0], perms)
        );
    }

    public boolean canExecute(Member member, String ownerId) {
        if (member == null) return false;

        return switch (type) {
            case EVERYONE -> true;

            case OWNER_ONLY ->
                    member.getUser().getId().equals(ownerId);

            case DISCORD_PERMISSIONS ->
                    member.hasPermission(discordPermissions);
        };
    }

    public Type type() {
        return type;
    }

    public Set<Permission> discordPermissions() {
        return discordPermissions;
    }
}
