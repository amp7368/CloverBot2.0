package apple.inactivity.discord.activity;

import apple.discord.acd.ACD;
import apple.discord.acd.command.ACDCommand;
import apple.discord.acd.command.DiscordCommandAlias;
import apple.discord.acd.parameters.ParameterVargs;
import apple.inactivity.discord.SendLogs;
import apple.inactivity.wynncraft.guild.WynnGuildDatabase;
import apple.inactivity.wynncraft.guild.WynnGuildHeader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommandInactivity extends ACDCommand {
    private final ACD acd;

    public CommandInactivity(ACD acd) {
        super(acd);
        this.acd = acd;
    }

    @DiscordCommandAlias(alias = {"inactivity v2", "activity v2"}, overlappingCommands = "activity", order = 2, channelType = ChannelType.TEXT)
    public void inactivityV2(MessageReceivedEvent event, @ParameterVargs(usage = "guild") String guildName) {
        Guild discordGuild = event.getGuild();
        User author = event.getAuthor();
        Member selfMember = discordGuild.getMember(acd.getSelfUser());
        WynnGuildHeader wynnGuild = getWynnGuild(event, guildName, author, selfMember);
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
        WynnGuildHeader wynnGuild = getWynnGuild(event, guildName, author, selfMember);
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

    @Nullable
    private WynnGuildHeader getWynnGuild(MessageReceivedEvent event, String guildName, User author, Member selfMember) {
        if (selfMember == null) {
            event.getChannel().sendMessage("Somehow I'm not a member of your server. Report this to appleptr16#5054").queue();
            SendLogs.log("Inactivity", String.format("*%s* has requested '%s', but I'm not a member.", author.getAsTag(), event.getMessage().getContentDisplay()));
            return null;
        }
        List<WynnGuildHeader> guildMatches = WynnGuildDatabase.getFromGuildName(guildName);
        WynnGuildHeader wynnGuild;
        if (guildMatches.size() == 1) {
            wynnGuild = guildMatches.get(0);
        } else if (guildMatches.isEmpty()) {
            event.getChannel().sendMessage(String.format("The guild '%s' was not found", guildName)).queue();
            return null;
        } else {
            // else try it with guildName, but tell the user if nothing happens
            event.getChannel().sendMessage(String.format("Pick the following guild that matches:\n%s",
                    guildMatches.stream().map(WynnGuildHeader::getName).collect(Collectors.joining("\n")))).queue();
            return null;
        }
        TextChannel channel = (TextChannel) event.getChannel();
        List<Permission> permissionsMissing = new ArrayList<>();
        if (!selfMember.hasPermission(channel, Permission.MESSAGE_ADD_REACTION))
            permissionsMissing.add(Permission.MESSAGE_ADD_REACTION);
        if (!selfMember.hasPermission(channel, Permission.VIEW_CHANNEL))
            permissionsMissing.add(Permission.VIEW_CHANNEL);
        if (!selfMember.hasPermission(channel, Permission.MESSAGE_HISTORY))
            permissionsMissing.add(Permission.MESSAGE_HISTORY);
        if (!permissionsMissing.isEmpty()) {
            channel.sendMessage("I don't have permission to add reactions. This will prevent my pageable messages from working correctly. Can I have these permissions: " +
                    permissionsMissing.stream().map(Permission::getName).collect(Collectors.joining(", and ")) + "?").queue();
            SendLogs.log("Inactivity", String.format("*%s* has requested '%s', but I can't add reactions.", event.getAuthor().getAsTag(), event.getMessage().getContentDisplay()));
        }
        return wynnGuild;
    }
}
