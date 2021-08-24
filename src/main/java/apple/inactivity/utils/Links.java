package apple.inactivity.utils;

public class Links {
    public static final String GUILD_LIST = "https://api.wynncraft.com/public_api.php?action=guildList";
    public static final String PLAYER_STATS = "https://api.wynncraft.com/v2/player/%s/stats";
    public static final String GUILD_STATS = "https://api.wynncraft.com/public_api.php?action=guildStats&command=%s";
    public static final String GET_UUID = "https://api.mojang.com/users/profiles/minecraft/%s";

    public static String splitUUID(String uuid) {
        StringBuilder split = new StringBuilder();
        System.out.println(uuid.length());
        if (uuid.length() != 32) {
            return uuid;
        }
        split.append(uuid, 0, 8);
        split.append('-');
        split.append(uuid, 8, 12);
        split.append('-');
        split.append(uuid, 12, 16);
        split.append('-');
        split.append(uuid, 16, 20);
        split.append('-');
        split.append(uuid, 20, 32);
        System.out.println(split);
        return split.toString();
    }
}
