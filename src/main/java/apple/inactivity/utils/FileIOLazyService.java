package apple.inactivity.utils;

import apple.discord.acd.MillisTimeUnits;
import apple.utilities.request.RequestLogger;
import apple.utilities.request.keyed.AppleRequestKeyQueue;
import apple.utilities.request.keyed.lazy.AppleRequestLazyServiceSimple;
import apple.utilities.request.settings.RequestSettingsBuilder;

public class FileIOLazyService {
    private static final AppleRequestKeyQueue<Boolean> wynnPlayerInactivityService = new AppleRequestLazyServiceSimple<>(
            100, 0, 0, (int) MillisTimeUnits.SECOND * 20
    );
    private static RequestSettingsBuilder<Boolean> settings;
    private static final AppleRequestKeyQueue<Boolean> wynnGuildInactivityService = new AppleRequestLazyServiceSimple<>(
            100, 0, 0, (int) MillisTimeUnits.SECOND * 20, settings
    );

    static {
        settings = new RequestSettingsBuilder<>();
        settings.withRequestLogger(
                new RequestLogger<>() {
                    @Override
                    public void startRequest() {
                        System.out.println("lazy start");
                    }

                    @Override
                    public void exceptionHandle(Exception e) {
                        System.out.println("lazy exception");
                    }

                    @Override
                    public void finishDone(Boolean gotten) {
                        System.out.println("lazy gotten " + gotten);
                    }
                }
        );
    }

    public static AppleRequestKeyQueue<Boolean> getWynnPlayerInactivityService() {
        return wynnPlayerInactivityService;
    }

    public static AppleRequestKeyQueue<Boolean> getWynnGuildInactivityService() {
        return wynnGuildInactivityService;
    }
}
