package apple.inactivity.manage;

import apple.inactivity.listeners.WatchGuild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WatchGuildManager {
    private final HashMap<String, WatchGuild> watches = new HashMap<>();
    private long discordId;

    public WatchGuildManager() {
    }

    public WatchGuildManager(long discordId) {
        this.discordId = discordId;
    }


    public void addWatch(WatchGuild watchGuild) {
        watches.put(watchGuild.getUuid(), watchGuild);
        Servers.getOrMake(discordId).save();
    }

    public List<WatchGuild> getWatches() {
        return new ArrayList<>(watches.values());
    }
}
