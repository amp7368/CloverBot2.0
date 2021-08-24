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
    private transient ProfessionLevel[] maxProfs = null;

    public boolean isOld() {
        return System.currentTimeMillis() - timeRetrieved > TIME_TO_SAVE;
    }

    public void addGuildMemberInfo(WynnGuildMember guildMember) {
        this.guildMember = guildMember;
    }

    public int inactivity() {
        return (int) ((timeRetrieved - meta.lastJoin.getTime()) / MillisTimeUnits.DAY);
    }

    public int hoursPlayed() {
        return (int) (this.meta.playtime / 60);
    }

    public ProfessionLevel getProf(ProfessionType professionType) {
        if (maxProfs == null) {
            maxProfs = new ProfessionLevel[ProfessionType.values().length];
            int i = 0;
            for (ProfessionType prof : ProfessionType.values()) {
                maxProfs[i++] = calculateMaxProf(prof);
            }
        }
        return maxProfs[professionType.ordinal()];
    }

    private ProfessionLevel calculateMaxProf(ProfessionType prof) {
        ProfessionLevel level = null;
        for (WynnPlayerClass wynnClass : classes) {
            ProfessionLevel level1 = prof.get(wynnClass.professions);
            if (level1 == null) continue;
            if (level1.isThisGreater(level)) {
                level = level1;
            }
        }
        if(level == null){
            level = new ProfessionLevel(0,0);
        }
        return level;
    }
}
