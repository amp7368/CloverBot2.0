package apple.inactivity.wynncraft;

import apple.discord.acd.MillisTimeUnits;
import apple.inactivity.CloverMain;
import apple.inactivity.logging.DailyStatistics;
import apple.inactivity.logging.LoggingNames;
import apple.inactivity.utils.Links;
import apple.inactivity.wynncraft.guild.WynnGuild;
import apple.inactivity.wynncraft.player.WynnPlayer;
import apple.inactivity.wynncraft.player.WynnPlayerResponse;
import apple.utilities.request.AppleJsonFromURL;
import apple.utilities.request.AppleRequestPriorityService;
import apple.utilities.request.RequestLogger;
import apple.utilities.request.SimpleExceptionHandler;
import apple.utilities.request.settings.RequestPrioritySettingsBuilder;
import apple.utilities.util.ExceptionUnpackaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.event.Level;

import java.io.IOException;
import java.util.function.Consumer;

public class WynncraftService extends AppleRequestPriorityService<WynncraftService.WynnRequestPriority> {
    private static final WynncraftService instance = new WynncraftService();
    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm'Z'").create();

    public static WynncraftService get() {
        return instance;
    }

    public static void queue(WynnRequestPriority priority, String guild, Consumer<WynnGuild> runAfter) {
        RequestLogger<WynnGuild> logger = getGuildLogger(guild);
        RequestPrioritySettingsBuilder<WynnGuild, WynnRequestPriority> settings = get()
                .<WynnGuild>getDefaultPrioritySettings()
                .withPriority(priority)
                .withPriorityExceptionHandler(new SimpleExceptionHandler(new Class[]{IOException.class}, () -> runAfter.accept(null)))
                .withPriorityRequestLogger(logger);
        get().queuePriority(new AppleJsonFromURL<>(String.format(Links.GUILD_STATS, guild),
                WynnGuild.class).withGson(GSON), runAfter, settings);
    }

    public static void queuePriority(WynnRequestPriority priority, String guildMember, Consumer<@Nullable WynnPlayer> runAfter) {
        RequestPrioritySettingsBuilder<WynnPlayer, WynnRequestPriority> settings = get()
                .<WynnPlayer>getDefaultPrioritySettings()
                .withPriority(priority)
                .withPriorityExceptionHandler(new SimpleExceptionHandler(new Class[]{IOException.class}, () -> runAfter.accept(null)))
                .withPriorityRequestLogger(getPlayerLogger(String.format(Links.PLAYER_STATS, guildMember)));
        get().queuePriority(() -> {
            @Nullable WynnPlayer player = WynnDatabase.getPlayer(guildMember);
            if (player != null) return player;
            WynnPlayerResponse response = new AppleJsonFromURL<>(String.format(Links.PLAYER_STATS, guildMember),
                    WynnPlayerResponse.class).withGson(GSON).get();
            if (response == null || response.data.length == 0)
                throw new RuntimeException(new IOException("Data does not exist"));
            DailyStatistics.requestPlayerComplete(guildMember);
            WynnDatabase.addMember(response.data[0]);
            return response.data[0];
        }, runAfter, settings);
    }

    @NotNull
    private static <T> RequestLogger<T> getPlayerLogger(String name) {
        return new RequestLogger<>() {
            @Override
            public void exceptionUncaught(Exception e) {
                CloverMain.log("Exception getting player " + name + "\n" + ExceptionUnpackaging.getStackTrace(e), Level.ERROR, LoggingNames.WYNN);
                DailyStatistics.requestPlayerError(name);
            }

            @Override
            public void finishDone(T gotten) {
                CloverMain.log("Retrieved player from url: " + name, Level.INFO, LoggingNames.WYNN);
            }
        };
    }

    @NotNull
    private static <T> RequestLogger<T> getGuildLogger(String guild) {
        return new RequestLogger<>() {
            @Override
            public void exceptionUncaught(Exception e) {
                CloverMain.log("Exception getting guild " + guild + "\n" + ExceptionUnpackaging.getStackTrace(e), Level.ERROR, LoggingNames.WYNN);
                DailyStatistics.requestGuildError(guild);
            }

            @Override
            public void finishDone(T gotten) {
                CloverMain.log("Retrieved guild " + guild, Level.INFO, LoggingNames.WYNN);
                DailyStatistics.requestGuildComplete(guild);
            }
        };
    }

    @Override
    protected WynnRequestPriority[] getPriorities() {
        return WynnRequestPriority.values();
    }

    @Override
    protected WynnRequestPriority getDefaultPriority() {
        return WynnRequestPriority.PRIMARY;
    }

    @Override
    public int getTimeUnitMillis() {
        return (int) (MillisTimeUnits.HOUR / 2);
    }

    public enum WynnRequestPriority implements AppleRequestPriority {
        IMMEDIATE(600, 750, 10),
        NOW(600, 750, 10),
        PRIMARY(600, 750, 10),
        LAZY(500, 1000, 10),
        BACKGROUND(400, 2000, 20);

        private final int requestsPerTimeUnit;
        private final int safeGuardBuffer;
        private final int failSafeGuardBuffer;

        WynnRequestPriority(int requestsPerTimeUnit, int safeGuardBuffer, int failSafeGuardBuffer) {
            this.requestsPerTimeUnit = requestsPerTimeUnit;
            this.safeGuardBuffer = safeGuardBuffer;
            this.failSafeGuardBuffer = safeGuardBuffer * failSafeGuardBuffer;
        }

        @Override
        public int getRequestsPerTimeUnit() {
            return requestsPerTimeUnit;
        }


        @Override
        public int getSafeGuardBuffer() {
            return safeGuardBuffer;
        }

        @Override
        public int getFailGuardBuffer() {
            return failSafeGuardBuffer;
        }
    }
}
