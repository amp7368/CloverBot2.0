package apple.inactivity;

import apple.inactivity.cache.VerifyDiscordCache;
import apple.inactivity.discord.DiscordBot;
import apple.inactivity.discord.changelog.ChangelogDatabase;
import apple.inactivity.manage.Servers;
import apple.inactivity.wynncraft.WynnDatabase;
import apple.inactivity.wynncraft.WynnPlayerInactivity;

import javax.security.auth.login.LoginException;
import java.sql.SQLException;

public class CloverMain {
    public static void main(String[] args) throws LoginException, SQLException, ClassNotFoundException {
        VerifyDiscordCache.connect();
        new WynnPlayerInactivity().loadAllNow();
        Servers.loadNow();
        WynnDatabase.loadDatabase();
        ChangelogDatabase.loadNow();
        WatchGuildDaemon.get().start();
        DiscordBot bot = new DiscordBot();
        new Thread(new GuildListDaemon()).start();
        System.out.println("CloverBot started");
    }
}
