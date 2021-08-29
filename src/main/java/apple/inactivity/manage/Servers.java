package apple.inactivity.manage;

import apple.inactivity.CloverMain;
import apple.inactivity.listeners.InactivityListener;
import apple.inactivity.listeners.WatchGuild;
import apple.inactivity.utils.FileIOLazyService;
import apple.inactivity.utils.FileIOService;
import apple.utilities.database.AppleJsonDatabase;
import apple.utilities.database.keyed.AppleJsonDatabaseManagerKey;
import apple.utilities.file.FileFormatting;
import apple.utilities.request.AppleRequestQueue;
import apple.utilities.request.keyed.AppleRequestKeyQueue;
import apple.utilities.request.settings.RequestSettingsBuilder;
import apple.utilities.request.settings.RequestSettingsBuilderVoid;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;

public class Servers implements AppleJsonDatabaseManagerKey<ServerManager> {
    private static final HashMap<Long, ServerManager> servers = new HashMap<>();
    private static final Servers instance = new Servers();
    private final transient Gson gson = new GsonBuilder()
            .registerTypeAdapter(InactivityListener.class, new InactivityListener.ListenerDeserializer())
            .registerTypeAdapter(InactivityListener.class, new InactivityListener.ListenerSerializer())
            .create();

    public static Servers get() {
        return instance;
    }

    public static void loadNow() {
        File folder = get().getDBFolder();
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                if (fileName.length() > 5) {
                    fileName = fileName.substring(0, fileName.length() - 5);
                }
                long id;
                try {
                    id = Long.parseLong(fileName);
                } catch (NumberFormatException e) {
                    continue;
                }
                ServerManager manager = get().loadNow(ServerManager.class, ServerManager.getFileName(id));
                if (manager != null)
                    servers.put(manager.getId(), manager);
            }
        }
        for (ServerManager server : servers.values()) {
            server.register();
        }
    }

    @NotNull
    public static ServerManager getOrMake(long serverId) {
        return servers.computeIfAbsent(serverId, (k) -> new ServerManager(serverId));
    }

    @NotNull
    public File getDBFolder() {
        return FileFormatting.fileWithChildren(AppleJsonDatabase.getDBFolder(CloverMain.class), "clover", "watches");
    }

    @Override
    public AppleRequestKeyQueue<Boolean> getSavingService() {
        return FileIOLazyService.getWynnGuildInactivityService();
    }

    @Override
    public AppleRequestQueue getIOLoadingService() {
        return FileIOService.get();
    }

    @Override
    public RequestSettingsBuilderVoid getSavingSettings() {
        return RequestSettingsBuilderVoid.VOID;
    }

    @Override
    public RequestSettingsBuilder<ServerManager> getLoadingSettings() {
        return RequestSettingsBuilder.empty();
    }

    @Override
    public Gson getGson() {
        return gson;
    }

    public void save(ServerManager serverManager) {
        save(serverManager.getId(), serverManager);
    }

    public void save(WatchGuild watchGuild) {
        save(getOrMake(watchGuild.getServerId()));
    }
}
