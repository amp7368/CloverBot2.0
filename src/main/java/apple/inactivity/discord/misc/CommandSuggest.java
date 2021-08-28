package apple.inactivity.discord.misc;

import apple.discord.acd.ACD;
import apple.discord.acd.command.ACDCommand;
import apple.discord.acd.command.DiscordCommandAlias;
import apple.discord.acd.parameters.ParameterSingle;
import apple.inactivity.discord.DiscordBot;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandSuggest extends ACDCommand {
    public CommandSuggest(ACD acd) {
        super(acd);
    }

    @DiscordCommandAlias(alias = {"suggest", "bug"})
    public void suggest(MessageReceivedEvent event, @ParameterSingle(usage = "[message]") String message) {
        PrivateChannel dms = DiscordBot.client.getUserById(DiscordBot.APPLEPTR16).openPrivateChannel().complete();
        dms.sendMessage("**" + event.getAuthor().getAsTag() + "** __sent you the following:__").queue();
        dms.sendMessage(message).queue();
        event.getChannel().sendMessage("Thanks for the idea. I sent that to appleptr16").queue();
    }
}
