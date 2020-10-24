package apple.inactivity.discord.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public enum Commands {
    ;

    public boolean isCommand(String messageContent) {
        return false;
    }

    public void run(MessageReceivedEvent event) {

    }
}
