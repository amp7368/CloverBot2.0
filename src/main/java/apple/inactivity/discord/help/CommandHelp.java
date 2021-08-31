package apple.inactivity.discord.help;

import apple.discord.acd.ACD;
import apple.discord.acd.command.ACDCommand;
import apple.discord.acd.command.DiscordCommandAlias;
import apple.discord.acd.parameters.ParameterFlag;
import apple.inactivity.discord.CloverPermissions;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandHelp extends ACDCommand {
    public CommandHelp(ACD acd) {
        super(acd);
    }

    @DiscordCommandAlias(alias = "help", overlappingCommands = "help", order = 1, permission = CloverPermissions.ADMIN)
    public void helpAdmin(MessageReceivedEvent event, @ParameterFlag(usage = "-legacy", flags = "-legacy") boolean showLegacy) {
        new MessageHelp(acd, event.getChannel(), showLegacy, true).makeFirstMessage();
    }

    @DiscordCommandAlias(alias = "help", overlappingCommands = "help", order = 2, permission = CloverPermissions.NOT_ADMIN)
    public void help(MessageReceivedEvent event, @ParameterFlag(usage = "-legacy", flags = "-legacy") boolean showLegacy) {
        new MessageHelp(acd, event.getChannel(), showLegacy, false).makeFirstMessage();
    }
}
