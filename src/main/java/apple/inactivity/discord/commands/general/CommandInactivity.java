package apple.inactivity.discord.commands.general;

import apple.inactivity.GuildListThread;
import apple.inactivity.Pretty;
import apple.inactivity.data.PlayerWithInactivity;
import apple.inactivity.discord.DiscordBot;
import apple.inactivity.discord.SendLogs;
import apple.inactivity.discord.commands.Commands;
import apple.inactivity.discord.reactions.InactivityMessage;
import apple.inactivity.wynncraft.GetGuildPlayers;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandInactivity extends Thread {
    private final MessageReceivedEvent event;

    public CommandInactivity(MessageReceivedEvent event) {
        this.event = event;
        this.start();
    }

    @Override
    public void run() {
        List<String> content = new ArrayList<>(Arrays.asList(event.getMessage().getContentDisplay().split(" ")));
        if (content.size() < 2) {
            event.getChannel().sendMessage(Commands.INACTIVITY.getUsageMessage()).queue();
            return;
        }
        content.remove(0); // remove c!inactivity
        String guildName = String.join(" ", content);
        List<String> guildMatches = GuildListThread.getGuildName(guildName); // get the correct guild name
        if (guildMatches.size() == 1) {
            guildName = guildMatches.get(0);
        } // else try it with guildName, but tell the user if nothing happens
        Message message = event.getChannel().sendMessage(Pretty.getProgress(0)).complete();
        @Nullable List<PlayerWithInactivity> playersInGuild = GetGuildPlayers.getGuildPlayers(guildName, message);
        Member selfMember = event.getGuild().getMember(DiscordBot.client.getSelfUser());
        if (selfMember == null) {
            event.getChannel().sendMessage("Somehow I'm not a member of your server. Report this to appleptr16#5054").queue();
            SendLogs.log("Inactivity", String.format("*%s* has requested '%s', but I'm not a member.", event.getAuthor().getAsTag(), event.getMessage().getContentDisplay()));
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
                event.getChannel().sendMessage("I don't have permission to add reactions. This will prevent my pageable messages from working correctly. Can I have these permissions: " +
                        permissionsMissing.stream().map(Permission::getName).collect(Collectors.joining(", and ")) + "?").queue();
                SendLogs.log("Inactivity", String.format("*%s* has requested '%s', but I can't add reactions.", event.getAuthor().getAsTag(), event.getMessage().getContentDisplay()));
            }
        } catch (ClassCastException ignored) {
            SendLogs.log("Inactivity", String.format("*%s* has requested '%s', but idk if I can add reactions.", event.getAuthor().getAsTag(), event.getMessage().getContentDisplay()));
        }
        try {
            new InactivityMessage(guildName, playersInGuild, message);
        } catch (PermissionException e) {
            SendLogs.log("Inactivity", String.format("*%s* has requested '%s' in the %s server, but there was a permission exception", event.getAuthor().getAsTag(), event.getMessage().getContentDisplay(), event.getGuild().getName()));
            return;
        }
        SendLogs.log("Inactivity", String.format("*%s* has requested '%s' in the %s server", event.getAuthor().getAsTag(), event.getMessage().getContentDisplay(), event.getGuild().getName()));
    }
}
