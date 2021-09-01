package apple.inactivity.manage.listeners;

import apple.discord.acd.MillisTimeUnits;

import java.util.HashMap;
import java.util.UUID;

public class AggregatedWatchesByGuild {
    private final String guildTag;
    private final String guildName;
    private final HashMap<UUID, WatchGuild> watches = new HashMap<>();
    private long nextInactive = -1;

    public AggregatedWatchesByGuild(String guildTag, String guildName) {
        this.guildTag = guildTag;
        this.guildName = guildName;
    }

    public void put(WatchGuild watch) {
        synchronized (watches) {
            watches.put(watch.getUUID(), watch);
        }
    }

    public void runCheck(long now) {
        if (now >= nextInactive) {
            // do the check
            nextInactive = -1;
            runCheck();
        }
    }

    private void runCheck() {
        for (WatchGuild watch : watches.values()) {
            watch.watchPlayers(this);
        }
    }

    public synchronized void setNextInactive(long nextInactive) {
        if (this.nextInactive <= 0) {
            this.nextInactive = nextInactive;
        } else {
            this.nextInactive = Math.min(nextInactive, this.nextInactive);
        }
    }

    public Object getGuildTag() {
        return guildTag;
    }

    public void setNextInactiveDays(int nextInactiveDays) {
        setNextInactive(nextInactiveDays * MillisTimeUnits.DAY + System.currentTimeMillis());
    }

    public synchronized void remove(UUID uuid) {
        watches.remove(uuid);
    }
}
