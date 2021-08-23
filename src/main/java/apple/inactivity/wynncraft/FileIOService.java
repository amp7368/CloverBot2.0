package apple.inactivity.wynncraft;

import apple.utilities.request.AppleRequestService;

public class FileIOService extends AppleRequestService {
    private final static FileIOService instance = new FileIOService();

    public static FileIOService get() {
        return instance;
    }

    @Override
    public int getRequestsPerTimeUnit() {
        return 100;
    }

    @Override
    public int getTimeUnitMillis() {
        return 0;
    }

    @Override
    public int getSafeGuardBuffer() {
        return 0;
    }
}
