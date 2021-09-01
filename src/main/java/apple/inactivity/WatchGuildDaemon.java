package apple.inactivity;

import apple.discord.acd.MillisTimeUnits;
import apple.inactivity.logging.LoggingNames;
import apple.inactivity.manage.listeners.AggregatedWatchesByGuild;
import apple.inactivity.manage.listeners.WatchGuild;
import apple.utilities.util.ExceptionUnpackaging;
import org.slf4j.event.Level;

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
        CloverMain.log("Watch Guild Daemon started", Level.INFO, LoggingNames.CLOVER);
        while (true) {
            try {
                synchronized (this) {
                    long now = System.currentTimeMillis();
                    for (AggregatedWatchesByGuild watch : watchesByGuild.values()) {
                        watch.runCheck(now);
                    }
                }
            } catch (Exception e) {
                CloverMain.log("Exception in watch guild daemon" + "\n" + ExceptionUnpackaging.getStackTrace(e), Level.ERROR, LoggingNames.DAEMON);
            }
            try {
                Thread.sleep(MillisTimeUnits.MINUTE);
            } catch (InterruptedException e) {
                CloverMain.log("Exception sleeping in guild daemon" + "\n" + ExceptionUnpackaging.getStackTrace(e), Level.ERROR, LoggingNames.DAEMON);
                // if we can't sleep, abort trying to
                break;
            }
        }
        CloverMain.log("Watch Guild Daemon ended", Level.ERROR, LoggingNames.DAEMON);
    }

    public void addWatch(WatchGuild watch) {
        synchronized (this) {
            allWatches.put(watch.getUUID(), watch);
            watchesByGuild.computeIfAbsent(watch.getGuildTag(), (k) -> new AggregatedWatchesByGuild(watch.getGuildTag(), watch.getGuildName())).put(watch);
            watch.save();
        }
    }

    public void removeWatch(UUID uuid, String guildTag) {
        synchronized (this) {
            allWatches.remove(uuid);
            watchesByGuild.get(guildTag).remove(uuid);
        }
    }
}
