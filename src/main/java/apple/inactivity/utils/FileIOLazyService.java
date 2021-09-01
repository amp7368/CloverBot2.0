package apple.inactivity.utils;

import apple.discord.acd.MillisTimeUnits;
import apple.utilities.request.keyed.AppleRequestKeyQueue;
import apple.utilities.request.keyed.lazy.AppleRequestLazyServiceSimple;

public class FileIOLazyService {
    private static final AppleRequestKeyQueue<Boolean> wynnPlayerInactivityService = new AppleRequestLazyServiceSimple<>(
            100, 0, 0, (int) MillisTimeUnits.SECOND * 20
    );
    private static final AppleRequestKeyQueue<Boolean> wynnGuildInactivityService = new AppleRequestLazyServiceSimple<>(
            100, 0, 0, (int) MillisTimeUnits.SECOND * 20
    );


    public static AppleRequestKeyQueue<Boolean> getWynnPlayerInactivityService() {
        return wynnPlayerInactivityService;
    }

    public static AppleRequestKeyQueue<Boolean> getWynnGuildInactivityService() {
        return wynnGuildInactivityService;
    }
}
