package apple.inactivity.wynncraft;

import apple.inactivity.CloverMain;
import apple.inactivity.utils.FileIOLazyService;
import apple.inactivity.utils.FileIOService;
import apple.inactivity.wynncraft.player.WynnInactivePlayer;
import apple.utilities.database.keyed.AppleJsonDatabaseManagerKey;
import apple.utilities.request.AppleRequestQueue;
import apple.utilities.request.keyed.AppleRequestKeyQueue;
import apple.utilities.request.settings.RequestSettingsBuilder;
import apple.utilities.request.settings.RequestSettingsBuilderVoid;
import apple.utilities.util.FileFormatting;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

public class WynnPlayerInactivity implements AppleJsonDatabaseManagerKey<WynnPlayerInactivitySaveable> {
    private static WynnPlayerInactivity instance;
    private final HashMap<UUID, WynnInactivePlayer> players = new HashMap<>();
    private final HashMap<String, WynnPlayerInactivitySaveable> dbs = new HashMap<>();

    public WynnPlayerInactivity() {
        instance = this;
    }

    public static WynnPlayerInactivity get() {
        return instance;
    }

    @Override
    public AppleRequestQueue getIOLoadingService() {
        return FileIOService.get();
    }

    @Override
    public RequestSettingsBuilder<WynnPlayerInactivitySaveable> getLoadingSettings() {
        return RequestSettingsBuilder.empty();
    }

    @Override
    public File getDBFolder() {
        return FileFormatting.fileWithChildren(AppleJsonDatabaseManagerKey.getDBFolder(CloverMain.class), "wynncraft", "inactive","player");
    }

    @Override
    public AppleRequestKeyQueue<Boolean> getSavingService() {
        return FileIOLazyService.getWynnPlayerInactivityService();
    }

    @Override
    public RequestSettingsBuilderVoid getSavingSettings() {
        return RequestSettingsBuilderVoid.VOID;
    }

    public void loadAllNow() {
        for (WynnPlayerInactivitySaveable db : loadAllNow(WynnPlayerInactivitySaveable.class)) {
            loadDB(db);
        }
    }

    private void loadDB(WynnPlayerInactivitySaveable db) {
        synchronized (players) {
            this.players.putAll(db.getPlayers());
        }
    }

    public void addPlayer(WynnInactivePlayer player) {
        synchronized (players) {
            String key = player.getUUID().toString().substring(0, 2);
            WynnPlayerInactivitySaveable saveable = this.dbs.computeIfAbsent(key, (k) -> new WynnPlayerInactivitySaveable(key));
            this.players.put(player.getUUID(), player);
            saveable.put(player);
            this.save(saveable.getId(), saveable);
        }
    }

    public void getPlayer(UUID uuid, Consumer<WynnInactivePlayer> callback) {
        synchronized (players) {
            WynnInactivePlayer player = players.get(uuid);
            if (player == null) {
                WynncraftService.queuePriority(WynncraftService.WynnRequestPriority.LAZY, uuid.toString(), p -> {
                    callback.accept(p == null ? null : p.toWynnInactivePlayer());
                });
            } else {
                callback.accept(player);
            }
        }
    }

    public void updatePlayer(UUID uuid, Consumer<WynnInactivePlayer> callback) {
        WynncraftService.queuePriority(WynncraftService.WynnRequestPriority.LAZY, uuid.toString(), p -> {
            callback.accept(p == null ? null : p.toWynnInactivePlayer());
        });
    }
}
