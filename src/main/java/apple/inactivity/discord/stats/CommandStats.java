package apple.inactivity.discord.stats;

import apple.discord.acd.ACD;
import apple.discord.acd.command.ACDCommand;
import apple.discord.acd.command.DiscordCommandAlias;
import apple.discord.acd.parameters.ParameterVargs;
import apple.inactivity.mojang.MojangService;
import apple.inactivity.utils.Links;
import apple.inactivity.wynncraft.WynnDatabase;
import apple.inactivity.wynncraft.WynncraftService;
import apple.inactivity.wynncraft.player.WynnPlayer;
import apple.utilities.request.settings.RequestPrioritySettingsBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandStats extends ACDCommand {
    public CommandStats(ACD acd) {
        super(acd);
    }

    @DiscordCommandAlias(alias = "stats")
    public void stats(MessageReceivedEvent event, @ParameterVargs(usage = "[player]", nonEmpty = true) String playerName) {
        WynnPlayer player = WynnDatabase.getPlayer(playerName);
        MessageChannel channel = event.getChannel();
        if (player == null) {
            MojangService.getUUID(playerName, (uuid, name) -> {
                WynnPlayer playerNew;
                if ((playerNew = WynnDatabase.getPlayer(Links.splitUUID(uuid))) == null) {
                    WynncraftService.queuePriority(WynncraftService.WynnRequestPriority.IMMEDIATE, Links.splitUUID(uuid), (wynnPlayer) -> {
                        if (wynnPlayer == null) {
                            channel.sendMessage(String.format("Either the api is down, or the player '%s' doesn't exist.", playerName)).queue();
                        } else {
                            new MessageStats(acd, channel, wynnPlayer).makeFirstMessage();
                        }
                    });
                } else {
                    new MessageStats(acd, channel, playerNew).makeFirstMessage();
                }
            }, RequestPrioritySettingsBuilder.emptyPriority());

        }
    }
}
