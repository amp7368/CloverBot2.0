package apple.inactivity;

import apple.discord.acd.MillisTimeUnits;
import apple.inactivity.manage.listeners.AggregatedWatchesByGuild;
import apple.inactivity.manage.listeners.WatchGuild;

import java.util.HashMap;
import java.util.UUID;

public class WatchGuildDaemon {
    private static WatchGuildDaemon instance = new WatchGuildDaemon();
    private final HashMap<UUID, WatchGuild> allWatches = new HashMap<>();

    private final HashMap<String, AggregatedWatchesByGuild> watchesByGuild = new HashMap<>();

    public static WatchGuildDaemon get() {
        return instance;
    }

    public void start() {
        new Thread(this::watch).start();
    }

    private void watch() {
        while (true) {
            try {
                synchronized (this) {
                    long now = System.currentTimeMillis();
                    for (AggregatedWatchesByGuild watch : watchesByGuild.values()) {
                        watch.runCheck(now);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(MillisTimeUnits.MINUTE);
            } catch (InterruptedException e) {
                e.printStackTrace(); // todo tell appleptr16
                // if we can't sleep, abort trying to
                break;
            }
        }
    }

    public void addWatch(WatchGuild watch) {
        synchronized (this) {
            allWatches.put(watch.getUUID(), watch);
            watchesByGuild.computeIfAbsent(watch.getGuildTag(), (k) -> new AggregatedWatchesByGuild(watch.getGuildTag(), watch.getGuildName())).put(watch);
            watch.save();
        }
    }

    public void removeWatch(UUID uuid,String guildTag) {
        synchronized (this) {
            allWatches.remove(uuid);
            watchesByGuild.get(guildTag).remove(uuid);
        }
    }
}
