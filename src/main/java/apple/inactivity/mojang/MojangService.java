package apple.inactivity.mojang;

import apple.discord.acd.MillisTimeUnits;
import apple.inactivity.utils.Links;
import apple.utilities.request.AppleJsonFromURL;
import apple.utilities.request.AppleRequestPriorityService;
import apple.utilities.request.AppleRequestService;
import apple.utilities.request.settings.RequestPrioritySettingsBuilder;

import java.util.function.Consumer;

public class MojangService extends AppleRequestPriorityService<MojangService.MojangPriority> {
    private static final MojangService instance = new MojangService();

    public static MojangService get() {
        return instance;
    }

    public static AppleRequestService.RequestHandler<ResponseUUID> getUUID(String playerName, Consumer<String> uuidConsumer, RequestPrioritySettingsBuilder<ResponseUUID, MojangPriority> settings) {
        return get().queue(new AppleJsonFromURL<>(String.format(Links.GET_UUID, playerName), ResponseUUID.class),
                responseUUID -> uuidConsumer.accept(responseUUID.id),
                settings);
    }

    @Override
    protected MojangPriority[] getPriorities() {
        return MojangPriority.values();
    }

    @Override
    protected MojangPriority getDefaultPriority() {
        return MojangPriority.HIGH;
    }

    @Override
    protected int getTimeUnitMillis() {
        return (int) MillisTimeUnits.MINUTE_15;
    }

    public enum MojangPriority implements AppleRequestPriority {
        HIGH(500, 1250),
        LOW(200, 2500);

        private final int requests;
        private final int safeguard;

        MojangPriority(int requests, int safeguard) {
            this.requests = requests;
            this.safeguard = safeguard;
        }

        @Override
        public int getRequestsPerTimeUnit() {
            return requests;
        }

        @Override
        public int getSafeGuardBuffer() {
            return safeguard;
        }

    }

    private static class ResponseUUID {
        public String name;
        public String id;
    }
}
