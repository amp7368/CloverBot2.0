package apple.inactivity.discord;

import net.dv8tion.jda.api.entities.TextChannel;

public class SendLogs {
    private static TextChannel channel = DiscordBot.client.getTextChannelById(DiscordBot.LOGGING_CHANNEL);

    public static void log(String module, String message) {
        channel.sendMessage(String.format("**[%s]** %s", module, message)).queue();
    }
}
