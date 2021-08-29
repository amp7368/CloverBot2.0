package apple.inactivity.discord.help;

import apple.discord.acd.ACD;
import apple.discord.acd.command.ACDCommand;
import apple.discord.acd.command.DiscordCommandAlias;
import apple.discord.acd.parameters.ParameterFlag;
import apple.inactivity.wynncraft.WynnDatabase;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandHelp extends ACDCommand {
    public CommandHelp(ACD acd) {
        super(acd);
    }

    @DiscordCommandAlias(alias = "help")
    public void help(MessageReceivedEvent event, @ParameterFlag(usage = "-legacy", flags = "-legacy") boolean showLegacy) {
        new MessageHelp(acd, event.getChannel(), showLegacy).makeFirstMessage();
        event.getChannel().sendMessage(String.valueOf(WynnDatabase.getSize())).queue();
    }
}
