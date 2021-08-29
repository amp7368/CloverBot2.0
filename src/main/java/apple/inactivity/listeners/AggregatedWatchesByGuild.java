package apple.inactivity.listeners;

import java.util.HashMap;
import java.util.UUID;

public class AggregatedWatchesByGuild {
    private String guildTag;
    private String guildName;
    private long nextInactive = 0;
    private HashMap<UUID, WatchGuild> watches = new HashMap<>();

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
            runCheck();
        }
    }

    private void runCheck() {
        for (WatchGuild watch : watches.values()) {
            watch.watchPlayers();
        }
    }

    public Object getGuildTag() {
        return guildTag;
    }
}
