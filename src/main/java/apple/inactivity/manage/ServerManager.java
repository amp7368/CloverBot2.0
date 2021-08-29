package apple.inactivity.manage;

import apple.inactivity.CloverMain;
import apple.inactivity.listeners.InactivityListener;
import apple.inactivity.wynncraft.FileIOService;
import apple.utilities.database.AppleJsonDatabase;
import apple.utilities.request.AppleRequestQueue;
import apple.utilities.request.settings.RequestSettingsBuilder;
import apple.utilities.request.settings.RequestSettingsBuilderVoid;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ServerManager implements AppleJsonDatabase<ServerManager> {
    private final transient Gson gson = new GsonBuilder()
            .registerTypeAdapter(InactivityListener.class, new InactivityListener.ListenerDeserializer())
            .registerTypeAdapter(InactivityListener.class, new InactivityListener.ListenerSerializer())
            .create();
    private WatchGuildManager watchGuild;
    private long discordServerId;

    // for gson
    public ServerManager() {
    }

    public ServerManager(long discordServerId) {
        this.discordServerId = discordServerId;
        this.watchGuild = new WatchGuildManager(discordServerId);
    }

    @NotNull
    public static File getDBFolder() {
        return new File(AppleJsonDatabase.getDBFolder(CloverMain.class), "clover");
    }

    @Override
    public File getDBFile() {
        return new File(getDBFolder(), discordServerId + ".json");
    }

    @Override
    public AppleRequestQueue getSavingService() {
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

    public long getId() {
        return discordServerId;
    }

    public WatchGuildManager getWatchGuildManager() {
        return watchGuild;
    }
}
