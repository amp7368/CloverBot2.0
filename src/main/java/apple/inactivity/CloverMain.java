package apple.inactivity;

import apple.inactivity.cache.VerifyDiscordCache;
import apple.inactivity.discord.DiscordBot;
import apple.inactivity.discord.changelog.ChangelogDatabase;
import apple.inactivity.manage.Servers;
import apple.inactivity.wynncraft.GuildListThread;
import apple.inactivity.wynncraft.guild.WynnGuildDatabase;

import javax.security.auth.login.LoginException;
import java.sql.SQLException;

public class CloverMain {
    public static void main(String[] args) throws LoginException, SQLException, ClassNotFoundException {
        VerifyDiscordCache.connect();
        WynnGuildDatabase.loadDatabase();
        ChangelogDatabase.loadNow();
        Servers.loadNow();
        DiscordBot bot = new DiscordBot();
        new Thread(new GuildListThread()).start();
        System.out.println("CloverBot started");
    }
}
