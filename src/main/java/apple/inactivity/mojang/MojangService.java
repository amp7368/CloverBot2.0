package apple.inactivity.mojang;

import apple.discord.acd.MillisTimeUnits;
import apple.inactivity.utils.Links;
import apple.utilities.request.AppleJsonFromURL;
import apple.utilities.request.AppleRequest;
import apple.utilities.request.AppleRequestPriorityService;
import apple.utilities.request.AppleRequestService;
import apple.utilities.request.settings.RequestPrioritySettingsBuilder;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MojangService extends AppleRequestPriorityService<MojangService.MojangPriority> {
    private static final MojangService instance = new MojangService();

    public static MojangService get() {
        return instance;
    }

    public static AppleRequestService.RequestHandler<ResponseUUID> getUUID(String playerName, BiConsumer<String, String> uuidAndNameConsumer, RequestPrioritySettingsBuilder<ResponseUUID, MojangPriority> settings) {
        return get().queuePriority(new AppleJsonFromURL<>(String.format(Links.GET_UUID, playerName), ResponseUUID.class),
                responseUUID -> {
                    if (responseUUID == null || responseUUID.id == null)
                        throw new AppleRequest.AppleRuntimeRequestException("no uuid for username");
                    uuidAndNameConsumer.accept(responseUUID.id, responseUUID.name);
                },
                settings);
    }

    public static AppleRequestService.RequestHandler<ResponseMinecraftUsername[]> getPlayerName(UUID uuid, Consumer<String[]> uuidAndNameConsumer, RequestPrioritySettingsBuilder<ResponseMinecraftUsername[], MojangPriority> settings) {
        return get().queuePriority(new AppleJsonFromURL<>(String.format(Links.GET_USERNAME, uuid.toString().replace("-", "")), ResponseMinecraftUsername[].class),
                responseUUID -> {
                    if (responseUUID == null || responseUUID.length == 0)
                        throw new AppleRequest.AppleRuntimeRequestException("no username for uuid");
                    String[] names = new String[responseUUID.length];
                    for (int i = 0; i < responseUUID.length; i++) {
                        names[i] = responseUUID[i].name;
                    }
                    uuidAndNameConsumer.accept(names);
                },
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

    public static class ResponseUUID {
        public String name;
        public String id;
    }

    public static class ResponseMinecraftUsername {
        public String name;
    }
}
