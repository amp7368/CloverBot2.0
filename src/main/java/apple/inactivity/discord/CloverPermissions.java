package apple.inactivity.discord;

import apple.discord.acd.ACD;
import apple.discord.acd.permission.ACDPermission;
import apple.discord.acd.permission.ACDPermissionSimple;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

public class CloverPermissions {
    public static final String ADMIN = "clover_admin";
    public static final String NOT_ADMIN = "not_clover_admin";

    public static void addAllPermissions(ACD acd) {
        ACDPermissionSimple adminPermission = new ACDPermissionSimple(ADMIN, Permission.MANAGE_SERVER);
        acd.getPermissions().addPermission(adminPermission);
        acd.getPermissions().addPermission(new ACDPermission(NOT_ADMIN, -1) {
            @Override
            public boolean hasPermission(Member member) {
                return !adminPermission.hasPermission(member);
            }
        });
    }
}
