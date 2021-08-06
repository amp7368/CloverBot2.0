package apple.inactivity.discord.reactions;

import apple.discord.acd.ACD;
import apple.discord.acd.MillisTimeUnits;
import apple.discord.acd.reaction.gui.ACDGui;
import apple.inactivity.data.PlayerWithInactivity;
import apple.inactivity.discord.commands.CommandInactivity;
import apple.inactivity.utils.GuildListThread;
import apple.inactivity.utils.Pretty;
import apple.inactivity.wynncraft.GetGuildPlayers;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MessageInactivityProgress extends ACDGui implements Runnable {
    private final String guildName;
    private ACD acd;
    private MessageChannel channel;

    public MessageInactivityProgress(ACD acd, MessageChannel channel, String guildName) {
        super(acd, channel);
        this.acd = acd;
        this.channel = channel;
        List<String> guildMatches = GuildListThread.getGuildName(guildName);
        // get the correct guild name String guildName = String.join(" ", content);
        if (guildMatches.size() == 1) {
            this.guildName = guildMatches.get(0);
        } else if (guildMatches.isEmpty()) {
            this.guildName = null;
        } else {
            // else try it with guildName, but tell the user if nothing happens
            this.guildName = null;
        }
    }


    @Override
    public void run() {
        @Nullable List<PlayerWithInactivity> playersInGuild = GetGuildPlayers.getGuildPlayers(guildName, message);
        remove();
        new MessageInactivity(acd, guildName, playersInGuild, message).makeFirstMessage();
    }

    @Override
    protected void initButtons() {
        new Thread(this).start();
    }

    @Override
    protected long getMillisToOld() {
        return MillisTimeUnits.MINUTE_15;
    }

    @Override
    protected Message makeMessage() {
        return new MessageBuilder(Pretty.getProgress(0)).build();
    }
}
