package apple.inactivity.discord.activity;

import apple.discord.acd.ACD;
import apple.discord.acd.command.ACDCommand;
import apple.discord.acd.command.DiscordCommandAlias;
import apple.discord.acd.parameters.ParameterVargs;
import apple.inactivity.discord.ArgumentUtils;
import apple.inactivity.discord.SendLogs;
import apple.inactivity.wynncraft.guild.WynnGuildHeader;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommandInactivity extends ACDCommand {
    private final ACD acd;

    public CommandInactivity(ACD acd) {
        super(acd);
        this.acd = acd;
    }

    @DiscordCommandAlias(alias = {"inactivity", "activity"}, overlappingCommands = "activity", order = 2, channelType = ChannelType.TEXT)
    public void inactivityV2(MessageReceivedEvent event, @ParameterVargs(usage = "guild") String guildName) {
        Guild discordGuild = event.getGuild();
        User author = event.getAuthor();
        Member selfMember = discordGuild.getMember(acd.getSelfUser());
        WynnGuildHeader wynnGuild = ArgumentUtils.getWynnGuild(event, guildName, author, selfMember);
        if (wynnGuild == null) return;
        String contentRaw = event.getMessage().getContentRaw();
        String authorAsTag = author.getAsTag();
        String discordServer = event.getGuild().getName();
        new MessageInactivityProgress(acd, event.getChannel(), wynnGuild, event.getMember()) {
            @Override
            public void onFinishedProgress() {
                new MessageInactivityV2(acd, message, guildHeader, members).makeFirstMessage();
            }
        }.withPermissionExceptionHandler((p, s) -> {
            SendLogs.log("Inactivity", String.format("*%s* has requested '%s' in the %s server, but there was a permission exception",
                    authorAsTag,
                    contentRaw,
                    discordServer));
        }).makeFirstMessage();
    }

    @DiscordCommandAlias(alias = {"inactivity v1", "activity v1"}, overlappingCommands = "activity", order = 1, channelType = ChannelType.TEXT)
    public void inactivityV1(MessageReceivedEvent event, @ParameterVargs(usage = "guild") String guildName) {
        Guild discordGuild = event.getGuild();
        User author = event.getAuthor();
        Member selfMember = discordGuild.getMember(acd.getSelfUser());
        WynnGuildHeader wynnGuild = ArgumentUtils.getWynnGuild(event, guildName, author, selfMember);
        if (wynnGuild == null) return;
        String contentRaw = event.getMessage().getContentRaw();
        String authorAsTag = author.getAsTag();
        String discordServer = event.getGuild().getName();
        new MessageInactivityProgress(acd, event.getChannel(), wynnGuild, event.getMember()) {
            @Override
            public void onFinishedProgress() {
                message.delete().queue();
                List<InactivityWynnPlayer> wynnPlayers = new ArrayList<>(members.stream().map(InactivityWynnPlayer::new).collect(Collectors.toList()));
                List<String> messages = MessageInactivityV2.inactivityV1(wynnPlayers, guildHeader);
                for (String message : messages) {
                    event.getChannel().sendMessage(message).queue();
                }
            }
        }.withPermissionExceptionHandler((p, s) -> {
            SendLogs.log("Inactivity", String.format("*%s* has requested '%s' in the %s server, but there was a permission exception",
                    authorAsTag,
                    contentRaw,
                    discordServer));
        }).makeFirstMessage();
        SendLogs.log("Inactivity", String.format("*%s* has requested '%s' in the %s server", event.getAuthor().getAsTag(), event.getMessage().getContentDisplay(), event.getGuild().getName()));
    }
}
