package apple.inactivity.discord.commands;

import apple.discord.acd.ACD;
import apple.discord.acd.command.ACDCommand;
import apple.discord.acd.command.DiscordCommandAlias;
import apple.discord.acd.command.ParameterVargs;
import apple.inactivity.discord.SendLogs;
import apple.inactivity.discord.reactions.MessageInactivityProgress;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommandInactivity extends ACDCommand {
    private final ACD acd;

    public CommandInactivity(ACD acd) {
        super(acd);
        this.acd = acd;
    }

    @DiscordCommandAlias(alias = {"inactivity", "activity"})
    public void inactivity(MessageReceivedEvent event, @ParameterVargs(usage = "guild") String guildName) {
        Guild guild = event.getGuild();
        MessageChannel channel = event.getChannel();
        User author = event.getAuthor();
        Member selfMember = guild.getMember(acd.getSelfUser());
        if (selfMember == null) {
            channel.sendMessage("Somehow I'm not a member of your server. Report this to appleptr16#5054").queue();
            SendLogs.log("Inactivity", String.format("*%s* has requested '%s', but I'm not a member.", author.getAsTag(), event.getMessage().getContentDisplay()));
            return;
        }
        try {
            GuildChannel guildChannel = (GuildChannel) event.getChannel();
            List<Permission> permissionsMissing = new ArrayList<>();
            if (!selfMember.hasPermission(guildChannel, Permission.MESSAGE_ADD_REACTION))
                permissionsMissing.add(Permission.MESSAGE_ADD_REACTION);
            if (!selfMember.hasPermission(guildChannel, Permission.VIEW_CHANNEL))
                permissionsMissing.add(Permission.VIEW_CHANNEL);
            if (!selfMember.hasPermission(guildChannel, Permission.MESSAGE_HISTORY))
                permissionsMissing.add(Permission.MESSAGE_HISTORY);
            if (!permissionsMissing.isEmpty()) {
                channel.sendMessage("I don't have permission to add reactions. This will prevent my pageable messages from working correctly. Can I have these permissions: " +
                        permissionsMissing.stream().map(Permission::getName).collect(Collectors.joining(", and ")) + "?").queue();
                SendLogs.log("Inactivity", String.format("*%s* has requested '%s', but I can't add reactions.", event.getAuthor().getAsTag(), event.getMessage().getContentDisplay()));
            }
        } catch (ClassCastException ignored) {
            SendLogs.log("Inactivity", String.format("*%s* has requested '%s', but idk if I can add reactions.", event.getAuthor().getAsTag(), event.getMessage().getContentDisplay()));
        }
        try {
            new MessageInactivityProgress(acd, event.getChannel(), guildName).makeFirstMessage();
        } catch (PermissionException e) {
            SendLogs.log("Inactivity", String.format("*%s* has requested '%s' in the %s server, but there was a permission exception", event.getAuthor().getAsTag(), event.getMessage().getContentDisplay(), event.getGuild().getName()));
            return;
        }
        SendLogs.log("Inactivity", String.format("*%s* has requested '%s' in the %s server", event.getAuthor().getAsTag(), event.getMessage().getContentDisplay(), event.getGuild().getName()));

    }
}
