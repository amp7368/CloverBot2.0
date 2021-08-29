package apple.inactivity.utils;

import apple.utilities.request.AppleRequest;
import apple.utilities.request.AppleRequestService;
import apple.utilities.request.RequestLogger;
import apple.utilities.request.settings.RequestSettingsBuilder;

import java.util.function.Consumer;

public class FileIOService extends AppleRequestService {
    private final static FileIOService instance = new FileIOService();

    public static FileIOService get() {
        return instance;
    }

    @Override
    public <T> RequestHandler<T> queue(AppleRequest<T> request, Consumer<T> runAfter, RequestSettingsBuilder<T> builder) {
        builder.addRequestLogger(new RequestLogger<T>() {
            @Override
            public void exceptionHandle(Exception e) {
                e.printStackTrace();
            }
        });
        return super.queue(request, runAfter, builder);
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
