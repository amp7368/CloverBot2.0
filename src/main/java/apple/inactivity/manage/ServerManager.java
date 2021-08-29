package apple.inactivity.manage;

import apple.utilities.database.SaveFileable;

public class ServerManager implements SaveFileable {
    private WatchGuildManager watchGuild;
    private long discordServerId;

    // for gson
    public ServerManager() {
    }

    public ServerManager(long discordServerId) {
        this.discordServerId = discordServerId;
        this.watchGuild = new WatchGuildManager(discordServerId);
    }

    public static String getFileName(long discordServerId) {
        return discordServerId + ".json";
    }

    @Override
    public String getSaveFileName() {
        return getFileName(discordServerId);
    }


    public long getId() {
        return discordServerId;
    }

    public WatchGuildManager getWatchGuildManager() {
        return watchGuild;
    }

    public void register() {
        watchGuild.register();
    }

    public void save() {
        Servers.get().save(this);
    }
}
