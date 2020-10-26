package apple.inactivity.discord.commands.general;

import apple.inactivity.data.PlayerData;
import apple.inactivity.discord.commands.Commands;
import apple.inactivity.discord.reactions.StatsMessage;
import apple.inactivity.wynncraft.GetPlayer;
import apple.inactivity.wynncraft.GetUUID;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandStats {
    public static void dealWithCommand(MessageReceivedEvent event) {
        List<String> content = new ArrayList<>(Arrays.asList(event.getMessage().getContentDisplay().split(" ")));
        if (content.isEmpty()) {
            event.getChannel().sendMessage(Commands.STATS.getUsageMessage()).queue();
            return;
        }
        content.remove(0);
        String playerName = String.join(" ", content);
        PlayerData player = GetPlayer.getPlayer(playerName);
        if (player == null) {
            playerName = GetUUID.getUuid(playerName);
            if (playerName == null || (player = GetPlayer.getPlayer(playerName)) == null) {
                event.getChannel().sendMessage("Either the api is down, or that player doesn't exist.").queue();
                return;
            }
        }
        new StatsMessage(player, event.getChannel());
    }
}
