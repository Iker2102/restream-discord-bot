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

    private final Set<Permission> userPerms;
    private final boolean userAny;

    private final Set<Permission> botPerms;
    private final boolean botAny;

    private CommandPermission(Type type,
                              Set<Permission> userPerms, boolean userAny,
                              Set<Permission> botPerms, boolean botAny) {
        this.type = type;
        this.userPerms = userPerms;
        this.userAny = userAny;
        this.botPerms = botPerms;
        this.botAny = botAny;
    }

    // ---------- Factories ----------

    public static CommandPermission everyone() {
        return new CommandPermission(Type.EVERYONE, Set.of(), false, Set.of(), false);
    }

    public static CommandPermission ownerOnly() {
        return new CommandPermission(Type.OWNER_ONLY, Set.of(), false, Set.of(), false);
    }

    /** Requiere que el usuario tenga TODOS los permisos indicados */
    public static CommandPermission discord(Permission... perms) {
        return new CommandPermission(Type.DISCORD_PERMISSIONS, enumSet(perms), false, Set.of(), false);
    }

    /** Requiere que el usuario tenga CUALQUIERA de los permisos indicados */
    public static CommandPermission discordAny(Permission... perms) {
        return new CommandPermission(Type.DISCORD_PERMISSIONS, enumSet(perms), true, Set.of(), false);
    }

    // ---------- Builder ----------

    /** Requiere que el BOT tenga TODOS los permisos indicados */
    public CommandPermission botNeeds(Permission... perms) {
        return new CommandPermission(type, userPerms, userAny, enumSet(perms), false);
    }

    /** Requiere que el BOT tenga CUALQUIERA de los permisos indicados */
    public CommandPermission botNeedsAny(Permission... perms) {
        return new CommandPermission(type, userPerms, userAny, enumSet(perms), true);
    }

    // ---------- Checks ----------

    public boolean canUserExecute(Member member, String ownerId) {
        if (member == null) return false;

        return switch (type) {
            case EVERYONE -> true;

            case OWNER_ONLY -> member.getUser().getId().equals(ownerId);

            case DISCORD_PERMISSIONS -> {
                if (userPerms.isEmpty()) yield true;
                yield userAny
                        ? hasAny(member, userPerms)
                        : member.hasPermission(userPerms);
            }
        };
    }

    public boolean botHasNeeds(Member member) {
        if (botPerms.isEmpty()) return true;
        if (member == null) return false;

        var self = member.getGuild().getSelfMember();
        return botAny
                ? hasAny(self, botPerms)
                : self.hasPermission(botPerms);
    }

    public Type type() { return type; }
    public Set<Permission> userPerms() { return userPerms; }
    public Set<Permission> botPerms() { return botPerms; }
    public boolean userAny() { return userAny; }
    public boolean botAny() { return botAny; }

    // ---------- Helpers ----------

    private static boolean hasAny(Member member, Set<Permission> perms) {
        for (Permission p : perms) {
            if (member.hasPermission(p)) {
                return true;
            }
        }
        return false;
    }

    private static Set<Permission> enumSet(Permission... perms) {
        if (perms == null || perms.length == 0) {
            return Set.of();
        }
        return EnumSet.of(perms[0], perms);
    }
}
