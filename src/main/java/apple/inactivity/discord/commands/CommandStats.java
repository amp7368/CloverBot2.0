package apple.inactivity.discord.commands;

import apple.discord.acd.ACD;
import apple.discord.acd.command.ACDCommand;
import apple.discord.acd.command.DiscordCommandAlias;
import apple.discord.acd.command.ParameterVargs;
import apple.inactivity.data.PlayerData;
import apple.inactivity.discord.reactions.MessageStats;
import apple.inactivity.wynncraft.GetPlayer;
import apple.inactivity.wynncraft.GetUUID;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandStats extends ACDCommand {
    private ACD acd;

    public CommandStats(ACD acd) {
        super(acd);
        this.acd = acd;
    }

    @DiscordCommandAlias(alias = "stats")
    public void stats(MessageReceivedEvent event, @ParameterVargs(usage = "[player]") String playerName) {
        PlayerData player = GetPlayer.getPlayer(playerName);
        if (player == null) {
            playerName = GetUUID.getUuid(playerName);
            if ((player = GetPlayer.getPlayer(playerName)) == null) {
                event.getChannel().sendMessage(String.format("Either the api is down, or the player '%s' doesn't exist.", playerName)).queue();
                return;
            }
        }
        new MessageStats(acd,player, event.getChannel()).makeFirstMessage();
    }
}
