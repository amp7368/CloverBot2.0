package apple.inactivity.discord.commands;

import apple.discord.acd.ACD;
import apple.discord.acd.command.ACDCommand;
import apple.discord.acd.command.DiscordCommandAlias;
import apple.discord.acd.parameters.ParameterVargs;
import apple.inactivity.discord.messages.MessageStats;
import apple.inactivity.mojang.MojangService;
import apple.inactivity.utils.Links;
import apple.inactivity.wynncraft.WynncraftService;
import apple.inactivity.wynncraft.guild.WynnGuildDatabase;
import apple.inactivity.wynncraft.player.WynnPlayer;
import apple.utilities.request.settings.RequestPrioritySettingsBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandStats extends ACDCommand {
    public CommandStats(ACD acd) {
        super(acd);
    }

    @DiscordCommandAlias(alias = "stats")
    public void stats(MessageReceivedEvent event, @ParameterVargs(usage = "[player]") String playerName) {
        WynnPlayer player = WynnGuildDatabase.getPlayer(playerName);
        MessageChannel channel = event.getChannel();
        if (player == null) {
            MojangService.getUUID(playerName, (uuid) -> {
                WynnPlayer playerNew;
                if ((playerNew = WynnGuildDatabase.getPlayer(Links.splitUUID(uuid))) == null) {
                    WynncraftService.queuePriority(WynncraftService.WynnRequestPriority.NOW, Links.splitUUID(uuid), (wynnPlayer) -> {
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
