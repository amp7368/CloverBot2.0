package apple.inactivity;

import apple.inactivity.cache.VerifyDiscordCache;
import apple.inactivity.discord.DiscordBot;
import apple.inactivity.wynncraft.GuildListThread;

import javax.security.auth.login.LoginException;
import java.sql.SQLException;

public class CloverMain {
    public static void main(String[] args) throws LoginException, SQLException, ClassNotFoundException {
        VerifyDiscordCache.connect();
        DiscordBot bot = new DiscordBot();
        new Thread(new GuildListThread()).start();
        System.out.println("CloverBot started");
    }
}
