package apple.inactivity.wynncraft.player;

import apple.discord.acd.MillisTimeUnits;

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

    public boolean isOld() {
        return System.currentTimeMillis() - timeRetrieved < TIME_TO_SAVE;
    }
}
