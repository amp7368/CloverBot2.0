package apple.inactivity.discord.commands;

import apple.inactivity.discord.commands.general.CommandInactivity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static apple.inactivity.discord.DiscordBot.PREFIX;

public enum Commands {
    INACTIVITY(Arrays.asList("inactivity", "activity"), "Gives a message of inactivity for a guild", "[guild]", CommandInactivity::new);

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

    public String getHelpMessage() {
        return String.format("**%s%s %s** - %s", PREFIX, commandNames.get(0), usageMessage, helpMessage);
    }

    public String getUsageMessage() {
        return String.format("Usage - %s%s %s", PREFIX, commandNames.get(0), usageMessage);
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
