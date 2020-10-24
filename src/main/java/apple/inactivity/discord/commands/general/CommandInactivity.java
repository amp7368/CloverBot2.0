package apple.inactivity.discord.commands.general;

import apple.inactivity.GuildListThread;
import apple.inactivity.Pretty;
import apple.inactivity.data.PlayerWithInactivity;
import apple.inactivity.discord.commands.Commands;
import apple.inactivity.discord.commands.DoCommand;
import apple.inactivity.wynncraft.GetGuildPlayers;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandInactivity implements DoCommand {
    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        List<String> content = new ArrayList<>(Arrays.asList(event.getMessage().getContentDisplay().split(" ")));
        if (content.size() < 2) {
            event.getChannel().sendMessage(Commands.INACTIVITY.getUsageMessage()).queue();
            return;
        }
        content.remove(0); // remove c!inactivity
        String guildName = String.join(" ", content);
        List<String> guildMatches = GuildListThread.getGuildName(guildName); // get the correct guild name
        if (guildMatches.size() == 1) {
            // we have 1 match. good. try to get the inactivity list
            guildName = guildMatches.get(0);
        } else if (!guildMatches.isEmpty()) {
            // we have more than one match. give a reactable message to ask the user which one they want
            return;
        } // else try it with guildName, but tell the user if nothing happens
        Message message = event.getChannel().sendMessage(Pretty.getProgress(0)).complete();
        List<PlayerWithInactivity> playersInGuild = GetGuildPlayers.getGuildPlayers(guildName, message);


    }
}
