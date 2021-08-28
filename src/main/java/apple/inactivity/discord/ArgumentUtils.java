package apple.inactivity.discord;

import apple.inactivity.wynncraft.guild.WynnGuildDatabase;
import apple.inactivity.wynncraft.guild.WynnGuildHeader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ArgumentUtils {
    @Nullable
    public static WynnGuildHeader getWynnGuild(MessageReceivedEvent event, String guildName, User author, Member selfMember) {
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
