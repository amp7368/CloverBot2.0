package apple.inactivity.discord.commands.general;

import apple.inactivity.discord.commands.Commands;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandHelp {
    public static void dealWithCommand(MessageReceivedEvent event) {
        event.getChannel().sendMessage("**" + Commands.INACTIVITY.getBareUsageMessage() + "** - gives an inactivity report for a guild. The guild name is case insensitive and a full guild name is not required\n" +
                "**" + Commands.HELP.getBareUsageMessage() + "** - gives this help message\n" +
                "**" + Commands.SUGGEST.getBareUsageMessage() + "** - sends an optional attachment as well as your message to appleptr16\n" +
                "**" + Commands.STATS.getBareUsageMessage() + "** - gives some stats about a player\n" +
                "https://discord.gg/XEyUWu9 is appleptr16#5054's discord server if you need to come over for some reason.\n" +
                "<https://discord.com/api/oauth2/authorize?client_id=616398849803681889&permissions=321600&scope=bot> is the link to invite CloverBot somewhere").queue();
    }
}
