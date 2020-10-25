package apple.inactivity.discord.commands.general;

import apple.inactivity.discord.commands.Commands;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandHelp {
    public static void dealWithCommand(MessageReceivedEvent event) {
        event.getChannel().sendMessage("**" + Commands.INACTIVITY.getBareUsageMessage() + "** - gives an inactivity report for a guild. The guild name is case insensitive and a full guild name is not required\n" +
                "**" + Commands.HELP.getBareUsageMessage() + "** - gives this help message\n" +
                "**" + Commands.SUGGEST.getBareUsageMessage() + "** - sends an optional attachment as well as your message to appleptr16").queue();
    }
}
