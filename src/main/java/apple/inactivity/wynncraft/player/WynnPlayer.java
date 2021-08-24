package apple.inactivity.wynncraft.player;

import apple.discord.acd.MillisTimeUnits;
import apple.inactivity.wynncraft.guild.WynnGuildMember;

public class WynnPlayer {
    private static final long TIME_TO_SAVE = MillisTimeUnits.HOUR;
    public String username;
    public String uuid;
    public String rank;
    public WynnPlayerMeta meta;
    public WynnPlayerClass[] classes;
    public WynnPlayerGlobalData global;
    public WynnPlayerRanking ranking;
    public transient long timeRetrieved = System.currentTimeMillis();
    public transient WynnGuildMember guildMember;

    public boolean isOld() {
        return System.currentTimeMillis() - timeRetrieved > TIME_TO_SAVE;
    }

    public void addGuildMemberInfo(WynnGuildMember guildMember) {
        this.guildMember = guildMember;
    }
}
