package apple.inactivity.discord.watcher;

import apple.discord.acd.ACD;
import apple.discord.acd.command.ACDCommand;
import apple.discord.acd.command.DiscordCommandAlias;
import apple.discord.acd.parameters.ParameterVargs;
import apple.inactivity.discord.ArgumentUtils;
import apple.inactivity.wynncraft.guild.WynnGuildHeader;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class WatchGuildCommand extends ACDCommand {
    public static final String LIST_COMMAND = "list watch";

    public WatchGuildCommand(ACD acd) {
        super(acd);
    }

    @DiscordCommandAlias(alias = "watch", channelType = ChannelType.TEXT)
    public void watchGuildBuilder(MessageReceivedEvent event, @ParameterVargs(usage = "guild", nonEmpty = true) String guildName) {
        Guild discordGuild = event.getGuild();
        User author = event.getAuthor();
        Member selfMember = discordGuild.getMember(acd.getSelfUser());
        WynnGuildHeader wynnGuild = ArgumentUtils.getWynnGuild(event, guildName, author, selfMember);
        if (wynnGuild == null) return;
        new WatchGuildBuilderMessage(acd, (TextChannel) event.getChannel(), event.getAuthor(), wynnGuild).makeFirstMessage();
    }
}
