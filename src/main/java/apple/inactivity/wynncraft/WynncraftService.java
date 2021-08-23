package apple.inactivity.wynncraft;

import apple.discord.acd.MillisTimeUnits;
import apple.inactivity.utils.Links;
import apple.inactivity.wynncraft.guild.WynnGuild;
import apple.inactivity.wynncraft.guild.WynnGuildMember;
import apple.inactivity.wynncraft.player.WynnPlayer;
import apple.inactivity.wynncraft.player.WynnPlayerResponse;
import apple.utilities.request.AppleJsonFromURL;
import apple.utilities.request.AppleRequest;
import apple.utilities.request.AppleRequestPriorityService;
import apple.utilities.request.RequestLogger;
import apple.utilities.request.settings.RequestPrioritySettingsBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class WynncraftService extends AppleRequestPriorityService<WynncraftService.WynnRequestPriority> {
    private static final WynncraftService instance = new WynncraftService();
    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm'Z'").create();

    public static WynncraftService get() {
        return instance;
    }

    public static void queue(WynnRequestPriority priority, String guild, Consumer<WynnGuild> runAfter) {
        RequestLogger<WynnGuild> logger = getLogger(guild);
        RequestPrioritySettingsBuilder<WynnGuild, WynnRequestPriority> settings = get()
                .<WynnGuild>getDefaultPrioritySettings()
                .withPriority(priority)
                .withPriorityRequestLogger(logger);
        get().queuePriority(new AppleJsonFromURL<>(String.format(Links.GUILD_STATS, guild),
                WynnGuild.class).withGson(GSON), runAfter, settings);
    }

    public static void queuePriority(WynnRequestPriority priority, WynnGuildMember guildMember, Consumer<@Nullable WynnPlayer> runAfter) {
        RequestPrioritySettingsBuilder<WynnPlayerResponse, WynnRequestPriority> settings = get()
                .<WynnPlayerResponse>getDefaultPrioritySettings()
                .withPriority(priority)
                .withPriorityRequestLogger(getLogger(String.format(Links.PLAYER_STATS, guildMember.uuid)));
        get().queuePriority(new AppleJsonFromURL<>(String.format(Links.PLAYER_STATS, guildMember.uuid),
                WynnPlayerResponse.class).withGson(GSON), (WynnPlayerResponse response) -> {
            if (response == null || response.data.length == 0)
                throw new AppleRequest.AppleRuntimeRequestException("Data does not exist");
            runAfter.accept(response.data[0]);
        }, settings);
    }

    @NotNull
    private static <T> RequestLogger<T> getLogger(String guild) {
        return new RequestLogger<>() {
            @Override
            public void startRequest() {
                System.out.println("Start request " + guild);
            }

            @Override
            public void exceptionHandle(Exception e) {
                System.out.println("Exception" + guild);
            }

            @Override
            public void finishDone(T gotten) {
                System.out.println("T has been gotten " + guild);
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
        NOW(600, 750, 10),
        PRIMARY(600, 750, 10),
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
