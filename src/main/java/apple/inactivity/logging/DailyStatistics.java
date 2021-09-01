package apple.inactivity.logging;

import apple.discord.acd.MillisTimeUnits;
import apple.inactivity.CloverMain;
import apple.inactivity.discord.DiscordBot;
import apple.utilities.logging.LogMessage;
import apple.utilities.threading.Daemon;
import apple.utilities.util.ExceptionUnpackaging;
import org.slf4j.event.Level;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DailyStatistics implements Daemon {
    private static final DailyStatistics instance = new DailyStatistics();
    private static final Map<DailyStat, Map<String, IncrementableInt>> stats = new HashMap<>();

    static {
        for (DailyStat stat : DailyStat.values()) {
            stats.put(stat, new HashMap<>());
        }
    }

    public static DailyStatistics get() {
        return instance;
    }

    public static void incrementStat(String guild, DailyStat guildError) {
        synchronized (stats) {
            stats.get(guildError).computeIfAbsent(guild, (k) -> new IncrementableInt(0)).increment();
        }
    }

    public static void requestGuildComplete(String guild) {
        incrementStat(guild, DailyStat.GUILD);
    }

    public static void requestGuildError(String guild) {
        incrementStat(guild, DailyStat.GUILD_ERROR);
    }


    public static void requestPlayerComplete(String name) {
        incrementStat(name, DailyStat.PLAYER);
    }

    public static void requestPlayerError(String name) {
        incrementStat(name, DailyStat.PLAYER_ERROR);
    }

    public static void requestError(LogMessage logMessage) {
        incrementStat("", DailyStat.ERROR);
    }

    @Override
    public void run() {
        synchronized (stats) {
            int errors = stats.get(DailyStat.ERROR).getOrDefault("", new IncrementableInt()).get();
            Map<String, IncrementableInt> player = stats.get(DailyStat.PLAYER);
            int playerCount = sum(player.values());
            int playerErrors = sum(stats.get(DailyStat.PLAYER_ERROR).values());
            int guild = sum(stats.get(DailyStat.GUILD).values());
            int guildErrors = sum(stats.get(DailyStat.GUILD_ERROR).values());
            String msg = String.format("""
                    Daily statistics:
                    Total:
                    -- There were %d errors
                                        
                    Players:
                    -- There were %d players requested
                    -- There were %d errors when requesting players
                                        
                    Guilds:
                    -- There were %d guilds requested
                    -- There were %d errors when requesting guilds
                    """, errors, playerCount, playerErrors, guild, guildErrors);
            DiscordBot.client.getTextChannelById(DiscordBot.STATS_CHANNEL).sendMessage(msg).queue();
            for (DailyStat stat : DailyStat.values()) {
                stats.put(stat, new HashMap<>());
            }
        }
    }

    private int sum(Collection<IncrementableInt> ints) {
        int sum = 0;
        for (IncrementableInt i : ints) {
            sum += i.get();
        }
        return sum;
    }


    @Override
    public long getSleepMillis() {
        return MillisTimeUnits.DAY;
    }

    @Override
    public void onException(Exception e) {
        CloverMain.log("Daily statistics error\n" + ExceptionUnpackaging.getStackTrace(e), Level.ERROR, LoggingNames.DAEMON);
    }

    @Override
    public void onComplete() {
        CloverMain.log("EXITED daily statistics", Level.ERROR, LoggingNames.DAEMON);
    }

    @Override
    public void onStart() {
        CloverMain.log("Start daily statistics", Level.INFO, LoggingNames.DAEMON);
    }

    @Override
    public boolean isSleepFirst() {
        return true;
    }

    private enum DailyStat {
        GUILD_ERROR, PLAYER, ERROR, PLAYER_ERROR, GUILD
    }

    private static class IncrementableInt {
        private int val;

        public IncrementableInt(int initialValue) {
            val = initialValue;
        }

        public IncrementableInt() {
        }

        public void increment() {
            val++;
        }

        public void increment(int i) {
            val += i;
        }

        public void decrement() {
            val--;
        }

        public int get() {
            return val;
        }
    }
}
