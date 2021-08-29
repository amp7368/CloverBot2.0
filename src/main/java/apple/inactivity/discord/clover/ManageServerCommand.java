package apple.inactivity.discord.clover;

import apple.discord.acd.ACD;
import apple.discord.acd.command.ACDCommand;
import apple.discord.acd.command.ACDCommandResponse;
import apple.discord.acd.command.DefaultCommandLoggerLevel;
import apple.discord.acd.command.DiscordCommandAlias;
import apple.inactivity.discord.CloverPermissions;
import apple.inactivity.manage.ServerManager;
import apple.inactivity.manage.Servers;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ManageServerCommand extends ACDCommand {
    public static final String CLOVER_MANAGE_SERVER_COMMAND = "clover";

    public ManageServerCommand(ACD acd) {
        super(acd);
    }

    @DiscordCommandAlias(alias = CLOVER_MANAGE_SERVER_COMMAND, overlappingCommands = CLOVER_MANAGE_SERVER_COMMAND, order = 1, permission = CloverPermissions.ADMIN, channelType = ChannelType.TEXT)
    public void listWatch(MessageReceivedEvent event) {
        Guild discordGuild = event.getGuild();
        ServerManager manager = Servers.getOrMake(discordGuild.getIdLong());
        new ManageServerMessage(acd, (TextChannel) event.getChannel(), event.getAuthor(), manager, discordGuild).makeFirstMessage();
    }

    @DiscordCommandAlias(alias = CLOVER_MANAGE_SERVER_COMMAND, overlappingCommands = CLOVER_MANAGE_SERVER_COMMAND, order = 2, channelType = ChannelType.TEXT)
    public ACDCommandResponse listWatchFail(MessageReceivedEvent event) {
        event.getChannel().sendMessage("You need to have the permission 'Manage Server' to use this command here").queue();
        ACDCommandResponse acdCommandResponse = new ACDCommandResponse();
        acdCommandResponse.setLevel(DefaultCommandLoggerLevel.IGNORE);
        return acdCommandResponse;
    }
}
