package apple.inactivity.discord;

import apple.discord.acd.ACD;
import apple.discord.acd.permission.ACDPermissionSimple;
import net.dv8tion.jda.api.Permission;

public class CloverPermissions {
    public static final String ADMIN = "clover_admin";

    public static void addAllPermissions(ACD acd) {
        acd.getPermissions().addPermission(
                new ACDPermissionSimple(ADMIN, Permission.MANAGE_SERVER)
        );
    }
}
