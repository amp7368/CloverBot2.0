package apple.inactivity.manage;

import apple.inactivity.WatchGuildDaemon;
import apple.inactivity.manage.listeners.WatchGuild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class WatchGuildManager {
    private final HashMap<UUID, WatchGuild> watches = new HashMap<>();
    private long discordId;
    private transient ServerManager serverManager = null;

    // for gson
    public WatchGuildManager() {
    }

    public WatchGuildManager(long discordId) {
        this.discordId = discordId;
    }


    public void addWatch(WatchGuild watch) {
        watches.put(watch.getUUID(), watch);
        verifyServerManager();
        serverManager.save();
        WatchGuildDaemon.get().addWatch(watch);
    }

    private void verifyServerManager() {
        serverManager = Servers.getOrMake(discordId);
    }

    public List<WatchGuild> getWatches() {
        return new ArrayList<>(watches.values());
    }

    public void register() {
        verifyServerManager();
        for (WatchGuild watch : watches.values()) {
            WatchGuildDaemon.get().addWatch(watch);
        }
    }

    public void removeWatch(UUID uuid, String guildTag) {
        watches.remove(uuid);
        verifyServerManager();
        serverManager.save();
        WatchGuildDaemon.get().removeWatch(uuid, guildTag);
    }
}
