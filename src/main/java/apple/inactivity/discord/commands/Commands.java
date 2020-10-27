package apple.inactivity.discord.commands;

import apple.inactivity.discord.commands.general.CommandHelp;
import apple.inactivity.discord.commands.general.CommandInactivity;
import apple.inactivity.discord.commands.general.CommandStats;
import apple.inactivity.discord.commands.general.CommandSuggest;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static apple.inactivity.discord.DiscordBot.PREFIX;

public enum Commands {
    INACTIVITY(Arrays.asList("inactivity", "activity"), "Gives a message of inactivity for a guild", "[guild]", CommandInactivity::new),
    SUGGEST(Collections.singletonList("suggest"), "Suggests an idea to appleptr16", "[message]", CommandSuggest::dealWithCommand),
    BUG(Collections.singletonList("bug"), "Reports a bug to appleptr16", "[message]", CommandSuggest::dealWithCommand),
    HELP(Collections.singletonList("help"), "Gives a help message", "", CommandHelp::dealWithCommand),
    STATS(Collections.singletonList("stats"), "Gives some stats about the player", "[player_name or uuid]", CommandStats::dealWithCommand);
    private final List<String> commandNames;
    private final String helpMessage;
    private final String usageMessage;
    private final Consumer<MessageReceivedEvent> command;

    Commands(List<String> commandNames, String helpMessage, String usageMessage, Consumer<MessageReceivedEvent> command) {
        this.commandNames = commandNames;
        this.helpMessage = helpMessage;
        this.usageMessage = usageMessage;
        this.command = command;
    }

    public String getUsageMessage() {
        return String.format("**%s%s %s** - %s", PREFIX, commandNames.get(0), usageMessage, helpMessage);
    }

    public String getBareUsageMessage() {
        return String.format("%s%s %s", PREFIX, commandNames.get(0), usageMessage);
    }


    public boolean isCommand(String command) {
        for (String myCommand : commandNames)
            if (command.startsWith(PREFIX + myCommand))
                return true;
        return false;
    }

    public void run(MessageReceivedEvent event) {
        command.accept(event);
    }
}
