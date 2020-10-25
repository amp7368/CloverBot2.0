package apple.inactivity.discord.commands.general;

import apple.inactivity.discord.DiscordBot;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandSuggest {
    public static void dealWithCommand(MessageReceivedEvent event) {
        PrivateChannel dms = DiscordBot.client.getUserById(DiscordBot.APPLEPTR16).openPrivateChannel().complete();
        dms.sendMessage("**" + event.getAuthor().getAsTag() + "** __sent you the following:__").queue();
        dms.sendMessage(event.getMessage()).queue();
        event.getChannel().sendMessage("Thanks for the idea. I sent that to appleptr16").queue();
    }
}
