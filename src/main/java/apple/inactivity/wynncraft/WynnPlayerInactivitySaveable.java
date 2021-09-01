package apple.inactivity.wynncraft;

import apple.inactivity.wynncraft.player.WynnInactivePlayer;
import apple.utilities.database.SaveFileable;

import java.util.HashMap;
import java.util.UUID;

public class WynnPlayerInactivitySaveable implements SaveFileable {
    private final HashMap<UUID, WynnInactivePlayer> players = new HashMap<>();
    private String fileName;

    public WynnPlayerInactivitySaveable() {
    }

    public WynnPlayerInactivitySaveable(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String getSaveFileName() {
        return fileName + ".json";
    }

    public HashMap<UUID, WynnInactivePlayer> getPlayers() {
        synchronized (players) {
            return players;
        }
    }

    public void put(WynnInactivePlayer player) {
        synchronized (players) {
            this.players.put(player.getUUID(), player);
        }
    }

    public String getId() {
        return fileName;
    }
}
