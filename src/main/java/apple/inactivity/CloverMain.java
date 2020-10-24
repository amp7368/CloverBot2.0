package apple.inactivity;

import apple.inactivity.discord.DiscordBot;

import javax.security.auth.login.LoginException;

public class CloverMain {
    public static void main(String[] args) throws LoginException {
        DiscordBot bot = new DiscordBot();
        bot.enableDiscord();
//        new GuildListThread().start();
    }
}
